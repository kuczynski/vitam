/*******************************************************************************
 * This file is part of Vitam Project.
 *
 * Copyright Vitam (2012, 2016)
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL license as circulated
 * by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL license and that you
 * accept its terms.
 *******************************************************************************/
package fr.gouv.vitam.core;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoWriteException;

import fr.gouv.vitam.api.MetaData;
import fr.gouv.vitam.api.config.MetaDataConfiguration;
import fr.gouv.vitam.api.exception.MetaDataAlreadyExistException;
import fr.gouv.vitam.api.exception.MetaDataDocumentSizeException;
import fr.gouv.vitam.api.exception.MetaDataExecutionException;
import fr.gouv.vitam.api.exception.MetaDataNotFoundException;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.multiple.RequestMultiple;
import fr.gouv.vitam.common.database.parser.request.GlobalDatasParser;
import fr.gouv.vitam.common.database.parser.request.multiple.InsertParserMultiple;
import fr.gouv.vitam.common.database.parser.request.multiple.RequestParserMultiple;
import fr.gouv.vitam.common.database.parser.request.multiple.SelectParserMultiple;
import fr.gouv.vitam.common.database.parser.request.multiple.UpdateParserMultiple;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.core.database.collections.MongoDbVarNameAdapter;
import fr.gouv.vitam.core.database.collections.Result;
import fr.gouv.vitam.core.utils.MetadataJsonResponseUtils;

/**
 * MetaDataImpl implements a MetaData interface
 */
public final class MetaDataImpl implements MetaData {

    private final DbRequestFactory dbRequestFactory;

    private static final VitamLogger LOGGER =
        VitamLoggerFactory.getInstance(MetaDataImpl.class);

    private static final String REQUEST_IS_NULL = "Request select is null or is empty";

    /**
     * MetaDataImpl constructor
     *
     * @param configuration of mongoDB access
     * @param mongoDbAccessFactory
     * @param dbRequestFactory
     */
    // FIXME REVIEW should be private and adding public static final Metadata newMetadata(...) calling this private
    // constructor
    public MetaDataImpl(MetaDataConfiguration configuration, MongoDbAccessMetadataFactory mongoDbAccessFactory,
        DbRequestFactory dbRequestFactory) {
        mongoDbAccessFactory.create(configuration);
        // FIXME REVIEW should check null
        this.dbRequestFactory = dbRequestFactory;
    }

    @Override
    public void insertUnit(JsonNode insertRequest)
        throws InvalidParseOperationException, MetaDataDocumentSizeException, MetaDataExecutionException,
        MetaDataAlreadyExistException, MetaDataNotFoundException {
        Result result = null;
        try {
            GlobalDatasParser.sanityRequestCheck(insertRequest.toString());
        } catch (final InvalidParseOperationException e) {
            throw new MetaDataDocumentSizeException(e);
        }

        try {
            final InsertParserMultiple insertParser = new InsertParserMultiple(new MongoDbVarNameAdapter());
            insertParser.parse(insertRequest);
            result = dbRequestFactory.create().execRequest(insertParser, result);
        } catch (final InvalidParseOperationException e) {
            throw e;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MetaDataExecutionException(e);
        } catch (final MongoWriteException e) {
            throw new MetaDataAlreadyExistException(e);
        }

        if (result.isError()) {
            throw new MetaDataNotFoundException("Parents not found");
        }
    }

    @Override
    public void insertObjectGroup(JsonNode objectGroupRequest)
        throws InvalidParseOperationException, MetaDataDocumentSizeException, MetaDataExecutionException,
        MetaDataAlreadyExistException, MetaDataNotFoundException {
        Result result = null;

        try {
            GlobalDatasParser.sanityRequestCheck(objectGroupRequest.toString());
        } catch (final InvalidParseOperationException e) {
            throw new MetaDataDocumentSizeException(e);
        }

        try {
            InsertParserMultiple insertParser = new InsertParserMultiple(new MongoDbVarNameAdapter());
            insertParser.parse(objectGroupRequest);
            insertParser.getRequest().addHintFilter(BuilderToken.FILTERARGS.OBJECTGROUPS.exactToken());
            result = dbRequestFactory.create().execRequest(insertParser, result);
        } catch (final InvalidParseOperationException e) {
            throw e;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new MetaDataExecutionException(e);
        } catch (final MongoWriteException e) {
            throw new MetaDataAlreadyExistException(e);
        }

        if (result.isError()) {
            throw new MetaDataNotFoundException("Parents not found");
        }
    }

    @Override
    public JsonNode selectUnitsByQuery(String selectQuery)
        throws MetaDataExecutionException, InvalidParseOperationException,
        MetaDataDocumentSizeException {
        LOGGER.info("Begin selectUnitsByQuery ...");
        LOGGER.debug("SelectUnitsByQuery/ selectQuery: " + selectQuery);
        return selectMetadataObject(selectQuery, null, null);

    }

    @Override
    public JsonNode selectUnitsById(String selectQuery, String unitId)
        throws InvalidParseOperationException, MetaDataExecutionException,
        MetaDataDocumentSizeException {
        LOGGER.info("Begin selectUnitsById .../id:" + unitId);
        LOGGER.debug("SelectUnitsById/ selectQuery: " + selectQuery);
        return selectMetadataObject(selectQuery, unitId, null);
    }

    @Override
    public JsonNode selectObjectGroupById(String selectQuery, String objectGroupId)
        throws InvalidParseOperationException, MetaDataDocumentSizeException, MetaDataExecutionException {
        LOGGER.debug("SelectObjectGroupById - objectGroupId : " + objectGroupId);
        LOGGER.debug("SelectObjectGroupById - selectQuery : " + selectQuery);
        return selectMetadataObject(selectQuery, objectGroupId, Collections.singletonList(BuilderToken.FILTERARGS
            .OBJECTGROUPS));
    }

    // TODO : maybe do not encapsulate all exception in a MetaDataExecutionException. We may need to know if it is
    // NOT_FOUND for example
    private JsonNode selectMetadataObject(String selectQuery, String unitOrObjectGroupId, List<BuilderToken
            .FILTERARGS> filters)
        throws MetaDataExecutionException, InvalidParseOperationException,
        MetaDataDocumentSizeException {
        Result result = null;
        JsonNode jsonNodeResponse;
        if (StringUtils.isEmpty(selectQuery)) {
            throw new InvalidParseOperationException(REQUEST_IS_NULL);
        }
        try {
            // sanity check:InvalidParseOperationException will be thrown if request select invalid or size is too large
            GlobalDatasParser.sanityRequestCheck(selectQuery);
        } catch (InvalidParseOperationException eInvalidParseOperationException) {
            throw new MetaDataDocumentSizeException(eInvalidParseOperationException);
        }
        try {
            // parse Select request
            RequestParserMultiple selectRequest = new SelectParserMultiple();
            selectRequest.parse(JsonHandler.getFromString(selectQuery));
            // Reset $roots (add or override id on roots)
            if (unitOrObjectGroupId != null && !unitOrObjectGroupId.isEmpty()) {
                RequestMultiple request = selectRequest.getRequest();
                if (request != null) {
                    LOGGER.debug("Reset $roots id with :" + unitOrObjectGroupId);
                    request.resetRoots().addRoots(unitOrObjectGroupId);
                }
            }
            if (filters != null && !filters.isEmpty()) {
                RequestMultiple request = selectRequest.getRequest();
                if (request != null) {
                    String[] hints = filters.stream()
                        .map(BuilderToken.FILTERARGS::exactToken)
                        .toArray(String[]::new);
                    LOGGER.debug("Adding given $hint filters: " + Arrays.toString(hints));
                    request.addHintFilter(hints);
                }
            }
            // Execute DSL request
            result = dbRequestFactory.create().execRequest(selectRequest, result);
            jsonNodeResponse = MetadataJsonResponseUtils.populateJSONObjectResponse(result, selectRequest);

        } catch (final InstantiationException | IllegalAccessException | MetaDataAlreadyExistException | MetaDataNotFoundException e) {
            LOGGER.error(e);
            throw new MetaDataExecutionException(e);
        }
        return jsonNodeResponse;
    }

    // FIXME ne jamais supprimer une deprecation warning !
    @SuppressWarnings("deprecation")
    @Override
    public JsonNode updateUnitbyId(String updateQuery, String unitId)
        throws InvalidParseOperationException, MetaDataExecutionException, MetaDataDocumentSizeException {
        Result result = null;
        JsonNode jsonNodeResponse = null;
        if (StringUtils.isEmpty(updateQuery)) {
            throw new InvalidParseOperationException(REQUEST_IS_NULL);
        }
        try {
            // sanity check:InvalidParseOperationException will be thrown if request select invalid or size is too large
            GlobalDatasParser.sanityRequestCheck(updateQuery);
        } catch (InvalidParseOperationException eInvalidParseOperationException) {
            throw new MetaDataDocumentSizeException(eInvalidParseOperationException);
        }
        try {
            // parse Update request
            RequestParserMultiple updateRequest = new UpdateParserMultiple();
            updateRequest.parse(JsonHandler.getFromString(updateQuery));
            // Reset $roots (add or override unit_id on roots)
            if (unitId != null && !unitId.isEmpty()) {
                RequestMultiple request = updateRequest.getRequest();
                if (request != null) {
                    LOGGER.debug("Reset $roots unit_id by :" + unitId);
                    request.resetRoots().addRoots(unitId);
                }
            }
            // Execute DSL request
            result = dbRequestFactory.create().execRequest(updateRequest, result);
            jsonNodeResponse = MetadataJsonResponseUtils.populateJSONObjectResponse(result, updateRequest);
        } catch (final MetaDataExecutionException e) {
            LOGGER.error(e);
            throw e;
        } catch (final InvalidParseOperationException e) {
            LOGGER.error(e);
            throw e;
        } catch (final InstantiationException e) {
            LOGGER.error(e);
            throw new MetaDataExecutionException(e);
        } catch (final IllegalAccessException e) {
            LOGGER.error(e);
            throw new MetaDataExecutionException(e);
        } catch (MetaDataAlreadyExistException | MetaDataNotFoundException e) {
            // Should not happen there
            LOGGER.error(e);
            throw new MetaDataExecutionException(e);
        }
        return jsonNodeResponse;
    }
}
