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
package fr.gouv.vitam.common.client;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;

/**
 * Results for client mock
 */
public class ClientMockResultHelper {

    private static final String RESULT =
        "{\"$query\":{}," +
            "\"$hits\":{\"total\":100,\"offset\":0,\"limit\":100}," +
            "\"$results\":";

    private static final String UNIT =
        "{\"DescriptionLevel\":\"Item\"," +
            "\"Title\":[\"METADATA ENCODING AND TRANSMISSION STANDARD: PRIMER AND REFERENCE MANUAL\",\"Manuel METS revu et corrigé\"]," +
            "\"Description\":[\"METSPrimerRevised.pdf\",\"Pseudo Archive METSPrimerRevised.pdf\"]," +
            "\"Tag\":[\"METS\",\"norme internationale\"],\"TransactedDate\":\"2012-09-16T10:22:02\"," +
            "\"Event\":[{\"EventType\":\"Création\",\"EventDateTime\":\"2010-01-01T10:22:02\"},{\"EventType\":\"Validation\",\"EventDateTime\":\"2010-02-01T10:22:02\"}]," +
            "\"_uds\":[{\"aeaaaaaaaaaam7mxaa7hcakyq4z6soyaaaaq\":1}],\"#id\":\"aeaaaaaaaaaam7mxaa7hcakyq4z6spqaaaaq\",\"#nbunits\":0,\"#tenant\":0," +
            "\"#object\":\"aeaaaaaaaaaam7mxaa7hcakyq4z6sjqaaaaq\",\"#unitups\":[\"aeaaaaaaaaaam7mxaa7hcakyq4z6soyaaaaq\"],\"#min\":1,\"#max\":2," +
            "\"#allunitups\":[\"aeaaaaaaaaaam7mxaa7hcakyq4z6soyaaaaq\"],\"#operations\":[\"aedqaaaaacaam7mxabhniakyq4z4ewaaaaaq\"]}";

    private static final String LOGBOOK_OPERATION =
        "\"evId\": \"aedqaaaaacaam7mxaaaamakvhiv4rsqaaaaq\"," +
            "\"evType\": \"Process_SIP_unitary\"," +
            "\"evDateTime\": \"2016-06-10T11:56:35.914\"," +
            "\"evIdProc\": \"aedqaaaaacaam7mxaaaamakvhiv4rsiaaaaq\"," +
            "\"evTypeProc\": \"INGEST\"," +
            "\"outcome\": \"STARTED\"," +
            "\"outDetail\": null," +
            "\"outMessg\": \"SIP entry : SIP.zip\"," +
            "\"agId\": {\"name\":\"ingest_1\",\"role\":\"ingest\",\"pid\":425367}," +
            "\"agIdApp\": null," +
            "\"agIdAppSession\": null," +
            "\"evIdReq\": \"aedqaaaaacaam7mxaaaamakvhiv4rsiaaaaq\"," +
            "\"agIdSubm\": null," +
            "\"agIdOrig\": null," +
            "\"obId\": null," +
            "\"obIdReq\": null," +
            "\"obIdIn\": null," +
            "\"events\": []}";
    private static final String RULE = "{\"_id\":\"aeaaaaaaaaaaaaabaa4ikakyetch6mqaaacq\", " +
        "\"_tenant\":\"0\", " +
        "\"RuleId\":\"APP-00005\", " +
        "\"RuleType\":\"AppraisalRule\", " +
        "\"RuleValue\":\"Pièces comptables (comptable)\", " +
        "\"RuleDescription\":\"Durée de conservation des pièces comptables pour le comptable l’échéance est calculée à partir de la date de solde comptable\", " +
        "\"RuleDuration\":\"6\", " +
        "\"RuleMeasurement\":\"Année\", " +
        "\"CreationDate\":\"2016-11-02\", " +
        "\"UpdateDate\":\"2016-11-02\"}";

    private static final String FORMAT = "{\"_id\":\"aeaaaaaaaaaaaaabaa44qakyetenaeyaaawq\", " +
        "\"CreatedDate\":\"2016-01-21T10:36:46\", " +
        "\"VersionPronom\":\"84\", " +
        "\"Version\":\"1.12\", " +
        "\"HasPriorityOverFileFormatID\":[], " +
        "\"MIMEType\":[], " +
        "\"Name\":\"Microsoft Works Word Processor for DOS\", " +
        "\"Alert\":\"false\", " +
        "\"Extension\":[\"wps\"], " +
        "\"PUID\":\"fmt/164\", " +
        "\"_tenant\":\"0\"}";

    private static final String ACCESSION_SUMMARY = "{\"_id\": \"aefaaaaaaaaam7mxaa2gyakygejizayaaaaq\"," +
        "\"_tenant\": 0," +
        "\"OriginatingAgency\": \"FRAN_NP_005568\"," +
        "    \"TotalObjects\": {" +
        "    \"Total\": 12," +
        "    \"Deleted\": 0," +
        "    \"Remained\": 12" +
        "}," +
        "\"TotalObjectGroups\": {" +
        "    \"Total\": 3," +
        "    \"Deleted\": 0," +
        "    \"Remained\": 3" +
        "}," +
        "\"TotalUnits\": {" +
        "    \"Total\": 3," +
        "    \"Deleted\": 0," +
        "    \"Remained\": 3" +
        "}," +
        "\"ObjectSize\": {" +
        "    \"Total\": 1035126," +
        "    \"Deleted\": 0," +
        "    \"Remained\": 1035126" +
        "}," +
        "\"creationDate\": \"2016-11-04T20:40:49.030\"}";

    private static final String ACCESSION_DETAIL = "{" +
        "\"_id\": \"aedqaaaaacaam7mxabsakakygeje2uyaaaaq\"," +
        "\"_tenant\": 0," +
        "\"OriginatingAgency\": \"FRAN_NP_005568\"," +
        "\"SubmissionAgency\": \"FRAN_NP_005061\"," +
        "\"EndDate\": \"2016-11-04T21:40:47.912+01:00\"," +
        "\"StartDate\": \"2016-11-04T21:40:47.912+01:00\"," +
        "\"Status\": \"STORED_AND_COMPLETED\"," +
        "\"TotalObjectGroups\": {" +
        "    \"total\": 1," +
        "    \"deleted\": 0," +
        "    \"remained\": 1" +
        "}," +
        "\"TotalUnits\": {" +
        "    \"total\": 1," +
        "    \"deleted\": 0," +
        "    \"remained\": 1" +
        "}," +
        "\"TotalObjects\": {" +
        "    \"total\": 4," +
        "    \"deleted\": 0," +
        "    \"remained\": 4" +
        "}," +
        "\"ObjectSize\": {" +
        "    \"total\": 345042," +
        "    \"deleted\": 0," +
        "    \"remained\": 345042" +
        "}}";

    private ClientMockResultHelper() {}

    /**
     * @return a default Logbook Result
     * @throws InvalidParseOperationException
     */
    public static JsonNode getLogbookResults() throws InvalidParseOperationException {
        final StringBuilder result = new StringBuilder(RESULT).append("[");
        for (int i = 0; i < 100; i++) {
            result.append("{\"_id\": \"aedqaaaaacaam7mxaaaamakvhiv4rsiaaa").append(i).append("\",")
                .append(LOGBOOK_OPERATION);
            if (i < 99) {
                result.append(",");
            }
        }
        result.append("]}");
        return JsonHandler.getFromString(result.toString());
    }

    /**
     * @return a default Logbook response Result
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getLogbooksRequestResponse() throws InvalidParseOperationException {
        return RequestResponseOK.getFromJsonNode(getLogbookResults());
    }

    /**
     * @return one default Logbook response
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getLogbookRequestResponse() throws InvalidParseOperationException {
        return RequestResponseOK.getFromJsonNode(getLogbookOperation());
    }

    /**
     * @return a default Logbook Operation
     * @throws InvalidParseOperationException
     */
    public static JsonNode getLogbookOperation() throws InvalidParseOperationException {
        return JsonHandler
            .getFromString(RESULT + "[{\"_id\": \"aedqaaaaacaam7mxaaaamakvhiv4rsiaaa0\"," + LOGBOOK_OPERATION + "]}");
    }

    /**
     * @param s the original object to be included in response
     * @return a default response
     * @throws InvalidParseOperationException
     */
    public static RequestResponse createReponse(String s) throws InvalidParseOperationException {
        return new RequestResponseOK()
            .setHits(1, 0, 1)
            .setQuery(null)
            .addResult(JsonHandler.getFromString(s));
    }

    /**
     *
     * @return a default Access Register Summary
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getAccessionRegisterSummary() throws InvalidParseOperationException {
        return createReponse(ACCESSION_SUMMARY);
    }

    /**
     *
     * @return a default Access Register Detail
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getAccessionRegisterDetail() throws InvalidParseOperationException {
        return createReponse(ACCESSION_DETAIL);
    }

    /**
     *
     * @return a default Format
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getFormat() throws InvalidParseOperationException {
        return createReponse(FORMAT);
    }

    /**
     *
     * @return a default Rule
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getRule() throws InvalidParseOperationException {
        return createReponse(RULE);
    }

    /**
     *
     * @return a default list of Formats
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getFormatList() throws InvalidParseOperationException {
        return createReponse(FORMAT);
    }

    /**
     *
     * @return a default list of Rules
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getRuleList() throws InvalidParseOperationException {
        return createReponse(RULE);
    }

    /**
     *
     * @return a default list of Rules
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getEmptyResult() throws InvalidParseOperationException {
        return createReponse(RESULT + "{}}");
    }

    /**
     * @return a default ArchiveUnit result
     * @throws InvalidParseOperationException
     */
    public static RequestResponse getArchiveUnitResult() throws InvalidParseOperationException {
        return createReponse(UNIT);
    }
}
