/*******************************************************************************
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
 *******************************************************************************/
package fr.gouv.vitam.worker.common.utils;

/**
 * Constants for the Ingest Workflow.
 */
public class IngestWorkflowConstants {

    /**
     * Prefix of file containing mapping between object group's seda ID and object group's vitam guid.
     */
    public static final String OBJECT_GROUP_ID_TO_GUID_MAP_FILE_NAME_PREFIX = "OBJECT_GROUP_ID_TO_GUID_MAP_";
    /**
     * Prefix of file containing mapping between archive unit's seda ID and archive unit's vitam guid.
     */
    public static final String ARCHIVE_ID_TO_GUID_MAP_FILE_NAME_PREFIX = "ARCHIVE_ID_TO_GUID_MAP_";
    /**
     * Prefix of file containing mapping between all BinaryDataObjects' seda ID and BinaryDataObjects vitam guid of an
     * object group.
     */
    public static final String BINARY_DATA_OBJECT_ID_TO_GUID_MAP_FILE_NAME_PREFIX =
        "BINARY_DATA_OBJECT_ID_TO_GUID_MAP_";
    /**
     * Prefix of file containing mapping between BinaryDataObjects' seda ID and object group's seda ID.
     */
    public static final String BDO_TO_OBJECT_GROUP_ID_MAP_FILE_NAME_PREFIX = "BDO_TO_OBJECT_GROUP_ID_MAP_";
    /**
     * Prefix of file containing mapping between all object GUID and its URI
     */
    public static final String OBJECT_GUID_TO_URI_MAP_FILE_NAME_PREFIX =
        "OBJECT_GUID_TO_URI_";
    /**
     * Prefix of file containing mapping between object group's seda ID and archive units' seda ID.
     */
    public static final String OBJECT_GROUP_ID_TO_ARCHIVE_UNIT_ID_MAP_FILE_NAME_PREFIX = "OG_TO_ARCHIVE_ID_MAP_";
    /**
     * Prefix of file containing mapping between BinaryDataObjects' seda ID and data object version.
     */
    public static final String BDO_TO_DO_VERSION_MAP_FILE_NAME_PREFIX = "BDO_TO_DO_VERSION_MAP_";
    /**
     * Prefix of file the ingest units tree.
     */
    public static final String ARCHIVE_TREE_TMP_FILE_NAME_PREFIX = "INGEST_TREE_";

    /**
     * Sub folder unzipped SIP in Worskpace folder of container : containerId/SIP
     */
    public static final String SEDA_FOLDER = "SIP";
    /**
     * Name of the seda manifest file in Worskpace folder of SIP : containerId/SIP/manifest.xml
     */
    public static final String SEDA_FILE = "manifest.xml";
    /**
     * Sub folder for binary data objects in Worskpace folder of SIP: containerId/SIP/Content
     */
    public static final String CONTENT_FOLDER = "Content";
    /**
     * Sub folder for object groups metadata in Worskpace folder of container : containerId/ObjectGroup
     */
    public static final String OBJECT_GROUP_FOLDER = "ObjectGroup";
    /**
     * Sub folder for archive units metadata in Worskpace folder of container : containerId/Units
     */
    public static final String ARCHIVE_UNIT_FOLDER = "Units";
    /**
     * Sub folder for archive units tree in Worskpace folder of container : containerId/Exec
     */
    public static final String EXEC_FOLDER = "Exec";
    /**
     * Sub folder for work file in Worskpace folder of container : containerId/tmp
     */
    public static final String TMP_FOLDER = "tmp";

    // TODO P1 : add doc
    public static final String ROOT_TAG = "ROOT";
    public static final String WORK_TAG = "WORK";
    public static final String UPS_SEPARATOR = "-";
    public static final String UP_FIELD = "_up";
    public static final String RULES = "RulesToApply";

    public static final String RULES_TAG = "Rules";

    /**
     * Sub folder for work file in Worskpace folder of container : containerId/ATR
     */
    public static final String ATR_FOLDER = "ATR";

    private IngestWorkflowConstants() {}

}
