/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.worker.core.handler;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gc.iotools.stream.is.InputStreamFromOutputStream;

import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.error.VitamCode;
import fr.gouv.vitam.common.error.VitamCodeHelper;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.format.identification.FormatIdentifier;
import fr.gouv.vitam.common.format.identification.FormatIdentifierFactory;
import fr.gouv.vitam.common.format.identification.exception.FileFormatNotFoundException;
import fr.gouv.vitam.common.format.identification.exception.FormatIdentifierBadRequestException;
import fr.gouv.vitam.common.format.identification.exception.FormatIdentifierFactoryException;
import fr.gouv.vitam.common.format.identification.exception.FormatIdentifierNotFoundException;
import fr.gouv.vitam.common.format.identification.exception.FormatIdentifierTechnicalException;
import fr.gouv.vitam.common.format.identification.model.FormatIdentifierResponse;
import fr.gouv.vitam.common.format.identification.siegfried.FormatIdentifierSiegfried;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.common.model.VitamAutoCloseable;
import fr.gouv.vitam.functional.administration.client.AdminManagementClient;
import fr.gouv.vitam.functional.administration.client.AdminManagementClientFactory;
import fr.gouv.vitam.functional.administration.common.FileFormat;
import fr.gouv.vitam.functional.administration.common.exception.ReferentialException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientBadRequestException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientNotFoundException;
import fr.gouv.vitam.logbook.common.exception.LogbookClientServerException;
import fr.gouv.vitam.logbook.common.parameters.LogbookLifeCycleObjectGroupParameters;
import fr.gouv.vitam.logbook.common.parameters.LogbookParameterName;
import fr.gouv.vitam.logbook.common.parameters.LogbookParametersFactory;
import fr.gouv.vitam.logbook.common.parameters.LogbookTypeProcess;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.parameter.WorkerParameters;
import fr.gouv.vitam.worker.common.HandlerIO;
import fr.gouv.vitam.worker.common.utils.IngestWorkflowConstants;
import fr.gouv.vitam.worker.common.utils.LogbookLifecycleWorkerHelper;
import fr.gouv.vitam.worker.common.utils.SedaConstants;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageNotFoundException;
import fr.gouv.vitam.workspace.api.exception.ContentAddressableStorageServerException;

/**
 * FormatIdentification Handler.<br>
 *
 */

// TODO P1: refactor me
// TODO P0: review Logbook messages (operation / lifecycle)
// TODO P0: fully use VitamCode

public class FormatIdentificationActionHandler extends ActionHandler implements VitamAutoCloseable {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(FormatIdentificationActionHandler.class);

    /**
     * Handler name
     */
    private static final String HANDLER_ID = "OG_OBJECTS_FORMAT_CHECK";

    /**
     * File format treatment
     */
    private static final String FILE_FORMAT = "FILE_FORMAT";
    private static final String EVT_TYPE_FILE_FORMAT = HANDLER_ID + "." + FILE_FORMAT;

    /**
     * Error list for file format treatment
     */
    private static final String FILE_FORMAT_TOOL_DOES_NOT_ANSWER = "TOOL_DOES_NOT_ANSWER";
    private static final String FILE_FORMAT_OBJECT_NOT_FOUND = "OBJECT_NOT_FOUND";
    private static final String FILE_FORMAT_NOT_FOUND = "NOT_FOUND";
    private static final String FILE_FORMAT_UPDATED_FORMAT = "UPDATED_FORMAT";
    private static final String FILE_FORMAT_PUID_NOT_FOUND = "PUID_NOT_FOUND";
    private static final String FILE_FORMAT_NOT_FOUND_REFERENTIAL_ERROR = "NOT_FOUND_REFERENTIAL";
    private static final String LOGBOOK_COMMIT_KO = "LOGBOOK_COMMIT_KO";

    private static final String FORMAT_IDENTIFIER_ID = "siegfried-local";

    private static final String RESULTS = "$results";


    private LogbookLifeCycleObjectGroupParameters logbookLifecycleObjectGroupParameters;
    private HandlerIO handlerIO;
    private FormatIdentifier formatIdentifier;

    private boolean metadatasUpdated = false;

    /**
     * Empty constructor
     */
    public FormatIdentificationActionHandler() {

    }

    /**
     * @return HANDLER_ID
     */
    public static final String getId() {
        return HANDLER_ID;
    }


    @Override
    public ItemStatus execute(WorkerParameters params, HandlerIO handler) {
        checkMandatoryParameters(params);
        logbookLifecycleObjectGroupParameters = LogbookParametersFactory.newLogbookLifeCycleObjectGroupParameters();
        handlerIO = handler;
        LOGGER.debug("FormatIdentificationActionHandler running ...");

        final ItemStatus itemStatus = new ItemStatus(HANDLER_ID);
        final String objectID = LogbookLifecycleWorkerHelper.getObjectID(params);
        try {
            try {
                LogbookLifecycleWorkerHelper.updateLifeCycleStartStep(handlerIO.getHelper(),
                    logbookLifecycleObjectGroupParameters,
                    params, HANDLER_ID, LogbookTypeProcess.INGEST);

            } catch (final ProcessingException e) {
                LOGGER.error(e);
                itemStatus.increment(StatusCode.FATAL);
                return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
            }

            try {
                formatIdentifier = FormatIdentifierFactory.getInstance().getFormatIdentifierFor(FORMAT_IDENTIFIER_ID);
            } catch (final FormatIdentifierNotFoundException e) {
                LOGGER.error(VitamCodeHelper.getLogMessage(VitamCode.WORKER_FORMAT_IDENTIFIER_NOT_FOUND,
                    FORMAT_IDENTIFIER_ID), e);
                itemStatus.increment(StatusCode.FATAL);
                return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
            } catch (final FormatIdentifierFactoryException e) {
                LOGGER.error(VitamCodeHelper.getLogMessage(VitamCode.WORKER_FORMAT_IDENTIFIER_IMPLEMENTATION_NOT_FOUND,
                    FORMAT_IDENTIFIER_ID), e);
                itemStatus.increment(StatusCode.FATAL);
                return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
            } catch (final FormatIdentifierTechnicalException e) {
                LOGGER.error(VitamCodeHelper.getLogMessage(VitamCode.WORKER_FORMAT_IDENTIFIER_TECHNICAL_INTERNAL_ERROR),
                    e);
                itemStatus.increment(StatusCode.FATAL);
                return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
            }
            File file = null;
            try {
                // Get objectGroup metadatas
                final JsonNode jsonOG = handlerIO.getJsonFromWorkspace(
                    IngestWorkflowConstants.OBJECT_GROUP_FOLDER + "/" + params.getObjectName());

                final Map<String, String> objectIdToUri = getMapOfObjectsIdsAndUris(jsonOG);

                final JsonNode qualifiers = jsonOG.get(SedaConstants.PREFIX_QUALIFIERS);
                if (qualifiers != null) {
                    final List<JsonNode> versions = qualifiers.findValues(SedaConstants.TAG_VERSIONS);
                    if (versions != null && !versions.isEmpty()) {
                        for (final JsonNode versionsArray : versions) {
                            for (final JsonNode version : versionsArray) {
                                try {
                                    final JsonNode jsonFormatIdentifier =
                                        version.get(SedaConstants.TAG_FORMAT_IDENTIFICATION);
                                    final String objectId = version.get(SedaConstants.PREFIX_ID).asText();
                                    // Retrieve the file
                                    file = loadFileFromWorkspace(objectIdToUri.get(objectId));

                                    final ObjectCheckFormatResult result =
                                        executeOneObjectFromOG(objectId, jsonFormatIdentifier, file,
                                            version);

                                    itemStatus.increment(result.getStatus());

                                    if (StatusCode.FATAL.equals(itemStatus.getGlobalStatus())) {
                                        return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
                                    }
                                } finally {
                                    if (file != null) {
                                        file.delete();
                                    }
                                    file = null;
                                }
                            }
                        }
                    }
                }

                if (metadatasUpdated) {
                    try (final InputStreamFromOutputStream<String> isos = new InputStreamFromOutputStream<String>() {

                        @Override
                        protected String produce(OutputStream sink) throws Exception {
                            JsonHandler.writeAsOutputStream(jsonOG, sink);
                            return params.getObjectName();
                        }
                    }) {
                        handlerIO.transferInputStreamToWorkspace(
                            IngestWorkflowConstants.OBJECT_GROUP_FOLDER + "/" + params.getObjectName(),
                            isos);
                    } catch (final IOException e) {
                        throw new ProcessingException("Issue while reading/writing the ObjectGroup", e);
                    }
                }

            } catch (final ProcessingException e) {
                LOGGER.error(e);
                itemStatus.increment(StatusCode.FATAL);
            } finally {
                // delete the file
                if (file != null) {
                    file.delete();
                }
            }

            try {
                commitLifecycleLogbook(itemStatus);
            } catch (final ProcessingException e) {
                LOGGER.error(e);
                // FIXME P0 WORKFLOW is it warning of something else ? is it really KO logbook message ?
                if (!StatusCode.FATAL.equals(itemStatus.getGlobalStatus()) &&
                    !StatusCode.KO.equals(itemStatus.getGlobalStatus())) {
                    itemStatus.setItemId(LOGBOOK_COMMIT_KO);
                    itemStatus.increment(StatusCode.WARNING);
                } else {
                    itemStatus.setItemId(LOGBOOK_COMMIT_KO);
                    itemStatus.increment(StatusCode.KO);
                }
            }

            if (itemStatus.getGlobalStatus().getStatusLevel() == StatusCode.UNKNOWN.getStatusLevel()) {
                itemStatus.increment(StatusCode.OK);
            }

            LOGGER.debug("FormatIdentificationActionHandler response: " + itemStatus.getGlobalStatus());
            return new ItemStatus(HANDLER_ID).setItemsStatus(HANDLER_ID, itemStatus);
        } finally {
            try {
                handlerIO.getLifecyclesClient().bulkUpdateObjectGroup(params.getContainerName(),
                    handlerIO.getHelper().removeUpdateDelegate(objectID));
            } catch (LogbookClientNotFoundException | LogbookClientBadRequestException |
                LogbookClientServerException e) {
                // Logbook error
                LOGGER.error(e);
                itemStatus.increment(StatusCode.FATAL);
            }
        }
    }

    /**
     * Update lifecycle logbook at the end of process
     *
     * @param itemStatus
     * @throws ProcessingException thrown if one error occurred
     */
    private void commitLifecycleLogbook(ItemStatus itemStatus)
        throws ProcessingException {
        logbookLifecycleObjectGroupParameters.putParameterValue(LogbookParameterName.eventIdentifier,
            GUIDFactory.newEventGUID(0).toString());
        // Reset the eventDetailData
        logbookLifecycleObjectGroupParameters.putParameterValue(LogbookParameterName.eventDetailData, "");
        LogbookLifecycleWorkerHelper.setLifeCycleFinalEventStatusByStep(handlerIO.getHelper(),
            logbookLifecycleObjectGroupParameters, itemStatus);
    }

    @Override
    public void checkMandatoryIOParameter(HandlerIO handler) throws ProcessingException {
        // Do not know...
    }

    private ObjectCheckFormatResult executeOneObjectFromOG(String objectId,
        JsonNode formatIdentification,
        File file, JsonNode version) {
        final ObjectCheckFormatResult objectCheckFormatResult = new ObjectCheckFormatResult(objectId);
        objectCheckFormatResult.setStatus(StatusCode.OK);
        try {

            // check the file
            final List<FormatIdentifierResponse> formats = formatIdentifier.analysePath(file.toPath());

            final FormatIdentifierResponse format = getFirstPronomFormat(formats);
            if (format == null) {
                throw new FileFormatNotFoundException("File format not found in " + FORMAT_IDENTIFIER_ID);
            }

            final String formatId = format.getPuid();

            final Select select = new Select();
            select.setQuery(eq(FileFormat.PUID, formatId));
            final JsonNode result;
            try (AdminManagementClient adminClient = AdminManagementClientFactory.getInstance().getClient()) {
                result = adminClient.getFormats(select.getFinalSelect());
            }

            // TODO P1 : what should we do if more than 1 result (for the moment, we take into account the first one)
            if (result.size() == 0) {
                // format not found in vitam referential
                objectCheckFormatResult.setStatus(StatusCode.KO);
                objectCheckFormatResult.setSubStatus(FILE_FORMAT_PUID_NOT_FOUND);
                logbookLifecycleObjectGroupParameters.putParameterValue(LogbookParameterName.eventDetailData,
                    "{\"ObjectId\": \"" + objectId + "\"}");
                logbookLifecycleObjectGroupParameters.setFinalStatus(EVT_TYPE_FILE_FORMAT, FILE_FORMAT_PUID_NOT_FOUND,
                    StatusCode.KO, null);
            } else {
                // check formatIdentification
                final JsonNode newFormatIdentification =
                    checkAndUpdateFormatIdentification(objectId, formatIdentification,
                        objectCheckFormatResult, result,
                        version);
                // Reassign new format
                ((ObjectNode) version).set(SedaConstants.TAG_FORMAT_IDENTIFICATION, newFormatIdentification);
            }
        } catch (InvalidParseOperationException | InvalidCreateOperationException | FormatIdentifierTechnicalException |
            IOException e) {
            LOGGER.error(e);
            objectCheckFormatResult.setStatus(StatusCode.FATAL);
            objectCheckFormatResult.setSubStatus(null);
        } catch (final ReferentialException e) {
            LOGGER.error(e);
            objectCheckFormatResult.setStatus(StatusCode.KO);
            objectCheckFormatResult.setSubStatus(FILE_FORMAT_NOT_FOUND_REFERENTIAL_ERROR);
        } catch (final FormatIdentifierBadRequestException e) {
            // path does not match a file
            LOGGER.error(e);
            objectCheckFormatResult.setStatus(StatusCode.FATAL);
            objectCheckFormatResult.setSubStatus(FILE_FORMAT_OBJECT_NOT_FOUND);
        } catch (final FileFormatNotFoundException e) {
            // format no found case
            LOGGER.error(e);
            objectCheckFormatResult.setStatus(StatusCode.KO);
            objectCheckFormatResult.setSubStatus(FILE_FORMAT_NOT_FOUND);

        } catch (final FormatIdentifierNotFoundException e) {
            // identifier does not respond
            LOGGER.error(e);
            objectCheckFormatResult.setStatus(StatusCode.FATAL);
            objectCheckFormatResult.setSubStatus(FILE_FORMAT_TOOL_DOES_NOT_ANSWER);

        }

        logbookLifecycleObjectGroupParameters.setFinalStatus(EVT_TYPE_FILE_FORMAT,
            objectCheckFormatResult.getSubStatus(),
            objectCheckFormatResult.getStatus(), null);

        try {
            handlerIO.getHelper().updateDelegate(logbookLifecycleObjectGroupParameters);
        } catch (final LogbookClientException exc) {
            LOGGER.error(exc);
            objectCheckFormatResult.setStatus(StatusCode.FATAL);
        }
        return objectCheckFormatResult;
    }

    private JsonNode checkAndUpdateFormatIdentification(String objectId,
        JsonNode formatIdentification,
        ObjectCheckFormatResult objectCheckFormatResult, JsonNode result, JsonNode version) {
        final JsonNode refFormat = result.get(RESULTS).get(0);
        final JsonNode puid = refFormat.get(FileFormat.PUID);
        final StringBuilder diff = new StringBuilder();
        JsonNode newFormatIdentification = formatIdentification;
        if ((newFormatIdentification == null || !newFormatIdentification.isObject()) && puid != null) {
            newFormatIdentification = JsonHandler.createObjectNode();
            ((ObjectNode) version).set(SedaConstants.TAG_FORMAT_IDENTIFICATION, newFormatIdentification);
        }
        if (newFormatIdentification != null) {
            final JsonNode fiPuid = newFormatIdentification.get(SedaConstants.TAG_FORMAT_ID);
            if (!puid.equals(fiPuid)) {
                objectCheckFormatResult.setStatus(StatusCode.WARNING);
                if (fiPuid != null && fiPuid.size() != 0) {
                    diff.append("- PUID : ");
                    diff.append(fiPuid);
                    diff.append('\n');
                }
                ((ObjectNode) newFormatIdentification).set(SedaConstants.TAG_FORMAT_ID, puid);
                diff.append("+ PUID : ");
                diff.append(puid);
                metadatasUpdated = true;
            }
            final JsonNode name = refFormat.get(FileFormat.NAME);
            final JsonNode fiFormatLitteral = newFormatIdentification.get(SedaConstants.TAG_FORMAT_LITTERAL);
            if (!name.equals(fiFormatLitteral)) {
                if (diff.length() != 0) {
                    diff.append('\n');
                }
                if (fiFormatLitteral != null && fiFormatLitteral.size() != 0) {
                    diff.append("- " + SedaConstants.TAG_FORMAT_LITTERAL + " : ");
                    diff.append(fiFormatLitteral);
                    diff.append('\n');
                }
                objectCheckFormatResult.setStatus(StatusCode.WARNING);
                ((ObjectNode) newFormatIdentification).set(SedaConstants.TAG_FORMAT_LITTERAL, name);
                diff.append("+ " + SedaConstants.TAG_FORMAT_LITTERAL + " : ");
                diff.append(name);
                metadatasUpdated = true;
            }
            final JsonNode mimeType = refFormat.get(FileFormat.MIME_TYPE);
            final JsonNode fiMimeType = newFormatIdentification.get(SedaConstants.TAG_MIME_TYPE);
            if (!mimeType.equals(fiMimeType)) {
                if (diff.length() != 0) {
                    diff.append('\n');
                }
                if (fiMimeType != null && fiMimeType.size() != 0) {
                    diff.append("- " + SedaConstants.TAG_MIME_TYPE + " : ");
                    diff.append(fiMimeType);
                    diff.append('\n');
                }
                objectCheckFormatResult.setStatus(StatusCode.WARNING);
                ((ObjectNode) newFormatIdentification).set(SedaConstants.TAG_MIME_TYPE, mimeType);
                diff.append("+ " + SedaConstants.TAG_MIME_TYPE + " : ");
                diff.append(mimeType);
                metadatasUpdated = true;
            }
        }

        if (StatusCode.WARNING.equals(objectCheckFormatResult.getStatus())) {
            objectCheckFormatResult.setSubStatus(FILE_FORMAT_UPDATED_FORMAT);
            logbookLifecycleObjectGroupParameters.putParameterValue(LogbookParameterName.eventIdentifier,
                GUIDFactory.newEventGUID(0).getId());
            // TODO P1 : create a real json object
            logbookLifecycleObjectGroupParameters.putParameterValue(LogbookParameterName.eventDetailData,
                "{\"diff\": \"" + diff.toString().replaceAll("\"", "'") + "\", \"ObjectId\": \"" + objectId + "\"}");
        }
        return newFormatIdentification;
    }

    /**
     * Retrieve the first corresponding file format from pronom referentiel
     *
     * @param formats formats list to analyse
     * @return the first pronom file format or null if not found
     */
    private FormatIdentifierResponse getFirstPronomFormat(List<FormatIdentifierResponse> formats) {
        for (final FormatIdentifierResponse format : formats) {
            if (FormatIdentifierSiegfried.PRONOM_NAMESPACE.equals(format.getMatchedNamespace())) {
                return format;
            }
        }
        return null;
    }

    private File loadFileFromWorkspace(String filePath)
        throws ProcessingException {
        try {
            return handlerIO.getFileFromWorkspace(IngestWorkflowConstants.SEDA_FOLDER + "/" + filePath);
        } catch (final IOException e) {
            LOGGER.debug("Error while saving the file", e);
            throw new ProcessingException(e);
        } catch (ContentAddressableStorageNotFoundException | ContentAddressableStorageServerException e) {
            LOGGER.debug("Workspace Server Error", e);
            throw new ProcessingException(e);
        }
    }


    private Map<String, String> getMapOfObjectsIdsAndUris(JsonNode jsonOG) throws ProcessingException {
        final Map<String, String> binaryObjectsToStore = new HashMap<>();

        // Filter on objectGroup objects ids to retrieve only binary objects
        // informations linked to the ObjectGroup
        final JsonNode work = jsonOG.get(SedaConstants.PREFIX_WORK);
        final JsonNode qualifiers = work.get(SedaConstants.PREFIX_QUALIFIERS);
        if (qualifiers == null) {
            return binaryObjectsToStore;
        }

        final List<JsonNode> versions = qualifiers.findValues(SedaConstants.TAG_VERSIONS);
        if (versions == null || versions.isEmpty()) {
            return binaryObjectsToStore;
        }
        for (final JsonNode version : versions) {
            for (final JsonNode binaryObject : version) {
                binaryObjectsToStore.put(binaryObject.get(SedaConstants.PREFIX_ID).asText(),
                    binaryObject.get(SedaConstants.TAG_URI).asText());
            }
        }
        return binaryObjectsToStore;
    }

    /**
     * Object used to keep all file format result for all objects. Not really actually used, but can be usefull
     */
    private class ObjectCheckFormatResult {
        private final String objectId;
        private StatusCode status;
        private String subStatus;

        ObjectCheckFormatResult(String objectId) {
            this.objectId = objectId;
        }

        public void setStatus(StatusCode status) {
            this.status = status;
        }

        public void setSubStatus(String subStatus) {
            this.subStatus = subStatus;
        }

        @SuppressWarnings("unused")
        public String getObjectId() {
            return objectId;
        }

        public StatusCode getStatus() {
            return status;
        }

        public String getSubStatus() {
            return subStatus;
        }
    }

    @Override
    public void close() {
        if (formatIdentifier != null) {
            formatIdentifier.close();
        }
    }
}
