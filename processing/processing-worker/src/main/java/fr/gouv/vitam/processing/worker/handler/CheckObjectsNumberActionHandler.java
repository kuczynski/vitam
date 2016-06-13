package fr.gouv.vitam.processing.worker.handler;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.processing.common.CheckObjectsNumberMessage;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.model.EngineResponse;
import fr.gouv.vitam.processing.common.model.ProcessResponse;
import fr.gouv.vitam.processing.common.model.StatusCode;
import fr.gouv.vitam.processing.common.model.WorkParams;
import fr.gouv.vitam.processing.common.utils.ContainerExtractionUtils;
import fr.gouv.vitam.processing.common.utils.ContainerExtractionUtilsFactory;
import fr.gouv.vitam.processing.common.utils.ExtractUriResponse;
import fr.gouv.vitam.processing.common.utils.SedaUtils;
import fr.gouv.vitam.processing.common.utils.SedaUtilsFactory;

/**
 * Handler that checks that the number of digital objects stored in the workspace equals the number of digital objects
 * referenced in the manifest.xml file
 */
public class CheckObjectsNumberActionHandler extends ActionHandler {

    /**
     * Use to log vitam
     */
    public static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(CheckObjectsNumberActionHandler.class);

    /**
     * Handler's ID
     */
    public static final String HANDLER_ID = "CheckObjectsNumber";

    private final SedaUtilsFactory sedaUtilsFactory;
    private final ContainerExtractionUtilsFactory containerExtractionUtilsFactory;



    /**
     * @param sedaUtilsFactory
     * @param containerExtractionUtilsFactory
     */
    public CheckObjectsNumberActionHandler(SedaUtilsFactory sedaUtilsFactory,
        ContainerExtractionUtilsFactory containerExtractionUtilsFactory) {
        this.sedaUtilsFactory = sedaUtilsFactory;
        this.containerExtractionUtilsFactory = containerExtractionUtilsFactory;
    }


    /**
     * @return HANDLER_ID
     */
    public static final String getId() {
        return HANDLER_ID;
    }

    @Override
    public EngineResponse execute(WorkParams params) {
        ParametersChecker.checkParameter("params is a mandatory parameter", params);
        ParametersChecker.checkParameter("ServerConfiguration is a mandatory parameter",
            params.getServerConfiguration());
        LOGGER.info("CheckObjectsNumberActionHandler running ...");


        final List<String> messages = new ArrayList<>();
        final EngineResponse response = new ProcessResponse();
        response.setStatus(StatusCode.OK).setMessages(messages);

        try {

            final ExtractUriResponse extractUriResponse = getUriListFromManifest(params);

            if (extractUriResponse != null && !extractUriResponse.isErrorDuplicateUri()) {

                final List<URI> uriListFromManifest = extractUriResponse.getUriListManifest();
                final List<URI> uriListFromWorkspace = getUriListFromWorkspace(params);
                checkCountDigitalObjectConformity(uriListFromManifest, uriListFromWorkspace, response);

            } else if (extractUriResponse != null) {
                response.setStatus(StatusCode.KO).setMessages(extractUriResponse.getMessages());
            }

        } catch (XMLStreamException | ProcessingException | NullPointerException e) {
            response.setStatus(StatusCode.FATAL);
        }

        return response;

    }


    /**
     * gets URI list of Digital object from the workspace, checks if there are duplicated URIs
     *
     * @param params
     * @return
     * @throws XMLStreamException
     * @throws ProcessingException
     */
    private ExtractUriResponse getUriListFromManifest(WorkParams params)
        throws ProcessingException, XMLStreamException {
        // get uri list from manifest
        final SedaUtils sedaUtils = sedaUtilsFactory.create();
        return sedaUtils.getAllDigitalObjectUriFromManifest(params);
    }


    /**
     * gets URI list of Digital object from the workspace, checks if there are duplicated URIs
     *
     * @param params
     * @return List<URI>
     * @throws ProcessingException if params is null, or some params' attributes are null
     */
    private List<URI> getUriListFromWorkspace(WorkParams params) throws ProcessingException {
        final ContainerExtractionUtils containerExtractionUtils = containerExtractionUtilsFactory.create();
        return containerExtractionUtils.getDigitalObjectUriListFromWorkspace(params);
    }

    /**
     * Count the number of digital objects consistent between the manifest.xm file and the sip
     *
     * @param uriListManifest
     * @param uriListWorkspace
     * @param response
     * @throws ProcessingException will be throwed when one or all arguments is null
     */
    private void checkCountDigitalObjectConformity(List<URI> uriListManifest, List<URI> uriListWorkspace,
        EngineResponse response) throws ProcessingException {
        ParametersChecker.checkParameter("Manifest uri list is a mandatory parameter", uriListManifest);
        ParametersChecker.checkParameter("Workspace uri list is a mandatory parameter", uriListWorkspace);
        ParametersChecker.checkParameter("EngineResponse is a mandatory parameter", response);
        // TODO
        // Use Java 8, Methods Reference, lambda expressions and streams

        /**
         * compare the size between list uri from manifest and list uri from workspace.
         */
        final int countCompare = Integer.compare(uriListManifest.size(), uriListWorkspace.size());
        if (countCompare != 0) {
            response.setStatus(StatusCode.KO);
            response.getMessages().add(CheckObjectsNumberMessage.COUNT_DIGITAL_OBJECT_SIP.getMessage()
                .concat(Integer.toString(uriListWorkspace.size())));
            response.getMessages().add(CheckObjectsNumberMessage.COUNT_DIGITAL_OBJECT_MANIFEST.getMessage()
                .concat(Integer.toString(uriListManifest.size())));

            try {
                // found not declared digital object in the manifest
                foundUnreferencedDigitalObject(uriListManifest, uriListWorkspace, response,
                    CheckObjectsNumberMessage.NOT_FOUND_DIGITAL_OBJECT_WORKSPACE.getMessage());
                // found not declared digital object in the sip
                foundUnreferencedDigitalObject(uriListWorkspace, uriListManifest, response,
                    CheckObjectsNumberMessage.NOT_FOUND_DIGITAL_OBJECT_MANIFEST.getMessage());
            } catch (final IllegalAccessException e) {
                throw new ProcessingException("Some arguments were null", e);
            }
        } else {

            /**
             * count the number of object in the manifest found in the sip
             */
            long countConsistentDigitalObjectFromManifest = 0L;
            /**
             * count the number of digital object in the sip found in the manifest
             */
            long countConsistentDigitalObjectFromWorkspace = 0L;

            for (final URI uriManifest : uriListManifest) {
                if (uriListWorkspace.contains(uriManifest)) {
                    countConsistentDigitalObjectFromManifest++;
                } else {
                    response.setStatus(StatusCode.KO);
                    response.getMessages().add(CheckObjectsNumberMessage.NOT_FOUND_DIGITAL_OBJECT_WORKSPACE.getMessage()
                        .concat(uriManifest.toString()));
                }
            }

            for (final URI uriWorkspace : uriListWorkspace) {
                if (uriListManifest.contains(uriWorkspace)) {
                    countConsistentDigitalObjectFromWorkspace++;
                } else {
                    response.setStatus(StatusCode.KO);
                    response.getMessages().add(CheckObjectsNumberMessage.NOT_FOUND_DIGITAL_OBJECT_MANIFEST.getMessage()
                        .concat(uriWorkspace.toString()));
                }

            }

            final boolean countOK =
                countConsistentDigitalObjectFromManifest == countConsistentDigitalObjectFromWorkspace;
            final boolean countConsistent = countConsistentDigitalObjectFromManifest == uriListManifest.size();

            if (countOK && countConsistent) {
                response.setStatus(StatusCode.OK).getMessages()
                    .add(CheckObjectsNumberMessage.COUNT_DIGITAL_OBJECT_CONSISTENT.getMessage());

            } else {
                response.setStatus(StatusCode.KO);
            }
        }

    }

    /**
     * Found the undeclared digital object either in the manifest or in the sip
     *
     * @param uriListManifest
     * @param uriListWorkspace
     * @param response
     * @param element
     */
    private void foundUnreferencedDigitalObject(List<URI> uriListToCompared, List<URI> uriListReference,
        EngineResponse response, String element) throws IllegalAccessException {

        final Set<String> uriNotFoundSet = new HashSet<>();

        if (uriListToCompared == null || uriListReference == null) {
            throw new IllegalAccessException("uriListToCompared or uriListReference must not be null");
        }

        for (final URI uriManifest : uriListToCompared) {
            if (!uriListReference.contains(uriManifest)) {
                uriNotFoundSet.add(element
                    .concat(uriManifest.toString()));
            }

            if (!uriNotFoundSet.isEmpty()) {
                for (final String s : uriNotFoundSet) {
                    if (response != null && response.getMessages() != null) {
                        response.getMessages().add(s);
                    }
                }
            }
        }
    }
}