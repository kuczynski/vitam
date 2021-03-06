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
package fr.gouv.vitam.metadata.rest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.with;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response.Status;

import org.jhades.JHades;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.fasterxml.jackson.databind.JsonNode;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import fr.gouv.vitam.common.GlobalDataRest;
import fr.gouv.vitam.common.database.parser.request.GlobalDatasParser;
import fr.gouv.vitam.common.database.server.elasticsearch.ElasticsearchNode;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.junit.JunitHelper.ElasticsearchTestConfiguration;
import fr.gouv.vitam.common.server.application.configuration.MongoDbNode;
import fr.gouv.vitam.metadata.api.config.MetaDataConfiguration;
import fr.gouv.vitam.metadata.core.database.collections.MetadataCollections;

/**
 *
 */
public class SelectObjectGroupResourceTest {


    private static final String DATA =
        "{ \"#id\": \"aeaqaaaaaeaaaaakaarp4akuuf2ldmyaaaaq\", " + "\"data\": \"data2\" }";
    private static final String DATA2 =
        "{ \"#id\": \"aeaqaaaaaeaaaaakaarp4akuuf2ldmyaaaab\"," + "\"data\": \"data2\" }";


    private static final String OBJECT_GROUP_ID = "aeaqaaaaaeaaaaakaarp4akuuf2ldmyaaaab";
    private static final String QUERY_PATH = "{ \"$path\" :  [\"aeaqaaaaaeaaaaakaarp4akuuf2ldmyaaaaq\"]  }";
    private static final String DATA_URI = "/metadata/v1";
    private static final String OBJECT_GROUPS_URI = "/objectgroups";
    private static final String DATABASE_NAME = "vitam-test";
    private static final String JETTY_CONFIG = "jetty-config-test.xml";
    private static MongodExecutable mongodExecutable;
    static MongodProcess mongod;

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    private final static String CLUSTER_NAME = "vitam-cluster";
    private final static String HOST_NAME = "127.0.0.1";

    private static MetaDataApplication application;

    private static final String BAD_QUERY_TEST =
        "{ \"$or\" : " + "[ " + "   {\"$exists\" : \"#id\"}, " + "   {\"$missing\" : \"mavar2\"}, " +
            "   {\"$badRquest\" : \"mavar3\"}, " +
            "   {\"$or\" : [ " + "          {\"$in\" : { \"mavar4\" : [1, 2, \"maval1\"] }}, " + "]}";

    private static final String SERVER_HOST = "localhost";

    private static final String BODY_TEST =
        "{\"$query\": {\"$eq\": {\"data\" : \"data2\" }}, \"$projection\": {}, \"$filter\": {}}";
    private static JunitHelper junitHelper;
    private static int serverPort;
    private static int dataBasePort;
    private static ElasticsearchTestConfiguration config = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Identify overlapping in particular jsr311
        new JHades().overlappingJarsReport();
        junitHelper = JunitHelper.getInstance();
        // ES
        try {
            config = JunitHelper.startElasticsearchForTest(tempFolder, CLUSTER_NAME);
        } catch (final VitamApplicationServerException e1) {
            assumeTrue(false);
        }

        final List<ElasticsearchNode> nodes = new ArrayList<>();
        nodes.add(new ElasticsearchNode(HOST_NAME, config.getTcpPort()));

        dataBasePort = junitHelper.findAvailablePort();

        final MongodStarter starter = MongodStarter.getDefaultInstance();
        mongodExecutable = starter.prepare(new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(dataBasePort, Network.localhostIsIPv6()))
            .build());
        mongod = mongodExecutable.start();

        final List<MongoDbNode> mongo_nodes = new ArrayList<>();
        mongo_nodes.add(new MongoDbNode(SERVER_HOST, dataBasePort));
        // TODO: using configuration file ? Why not ?
        final MetaDataConfiguration configuration =
            new MetaDataConfiguration(mongo_nodes, DATABASE_NAME, CLUSTER_NAME, nodes);
        configuration.setJettyConfig(JETTY_CONFIG);
        serverPort = junitHelper.findAvailablePort();

        application = new MetaDataApplication(configuration);
        application.start();
        JunitHelper.unsetJettyPortSystemProperty();

        RestAssured.port = serverPort;
        RestAssured.basePath = DATA_URI;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (config == null) {
            return;
        }
        JunitHelper.stopElasticsearchForTest(config);

        application.stop();
        mongod.stop();
        mongodExecutable.stop();
        junitHelper.releasePort(dataBasePort);
        junitHelper.releasePort(serverPort);
    }

    @Before
    public void before() {
        Assume.assumeTrue("Elasticsearch not started but should", config != null);
    }

    @After
    public void tearDown() {
        MetadataCollections.C_UNIT.getCollection().drop();
    }

    private static final JsonNode buildDSLWithOptions(String query, String data) throws InvalidParseOperationException {
        return JsonHandler
            .getFromString("{ \"$roots\" : [ \"\" ], \"$query\" : [ " + query + " ], \"$data\" : " + data + " }");
    }


    private static String createJsonStringWithDepth(int depth) {
        final StringBuilder obj = new StringBuilder();
        if (depth == 0) {
            return " \"b\" ";
        }
        obj.append("{ \"a\": ").append(createJsonStringWithDepth(depth - 1)).append("}");
        return obj.toString();
    }

    @Test
    public void getObjectGroupPostOK() throws Exception {
        with()
            .contentType(ContentType.JSON)
            .body(buildDSLWithOptions("", DATA)).when()
            .post("/units").then()
            .statusCode(Status.CREATED.getStatusCode());
        with()
            .contentType(ContentType.JSON)
            .body(buildDSLWithOptions(QUERY_PATH, DATA2)).when()
            .post(OBJECT_GROUPS_URI).then()
            .statusCode(Status.CREATED.getStatusCode());

        given().header(GlobalDataRest.X_HTTP_METHOD_OVERRIDE, HttpMethod.GET).contentType(ContentType.JSON)
            .body(JsonHandler.getFromString(BODY_TEST)).when().get(OBJECT_GROUPS_URI + "/" + OBJECT_GROUP_ID).then()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    public void getObjectGroupPRECONDITION_FAILED() {
        given()
            .contentType(ContentType.JSON)
            .when()
            .get(OBJECT_GROUPS_URI + "/" + OBJECT_GROUP_ID)
            .then()
            .statusCode(Status.PRECONDITION_FAILED.getStatusCode());

    }


    @Test
    public void getObjectGroupBadRequest() {

        given()
            .contentType(ContentType.JSON)
            .body(BAD_QUERY_TEST)
            .when()
            .post(OBJECT_GROUPS_URI)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getObjectGroupEmptyRequestBadRequest() {

        given()
            .contentType(ContentType.JSON)
            .body("")
            .when()
            .post(OBJECT_GROUPS_URI)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getObjectGroupRequestEntityTooLarge() throws Exception {
        final int limitRequest = GlobalDatasParser.limitRequest;
        GlobalDatasParser.limitRequest = 99;
        given()
            .contentType(ContentType.JSON)
            .body(buildDSLWithOptions("", createJsonStringWithDepth(101)).asText()).when()
            .post("/units/").then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
        GlobalDatasParser.limitRequest = limitRequest;
    }


    @Test
    public void getObjectGroupEmptyQueryPreconditionFailed() {

        given()
            .contentType(ContentType.JSON)
            .body("")
            .when()
            .get(OBJECT_GROUPS_URI + "/" + OBJECT_GROUP_ID)
            .then()
            .statusCode(Status.PRECONDITION_FAILED.getStatusCode());
    }

    @Test(expected = InvalidParseOperationException.class)
    public void shouldReturnErrorRequestBadRequest() throws Exception {
        given()
            .contentType(ContentType.JSON)
            .body(buildDSLWithOptions("", "lkvhvgvuyqvkvj")).when()
            .get(OBJECT_GROUPS_URI + "/" + OBJECT_GROUP_ID).then()
            .statusCode(Status.BAD_REQUEST.getStatusCode());
    }

}
