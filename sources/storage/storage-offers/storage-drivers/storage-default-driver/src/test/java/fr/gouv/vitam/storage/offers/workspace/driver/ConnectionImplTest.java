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
package fr.gouv.vitam.storage.offers.workspace.driver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.client.TestVitamClientFactory;
import fr.gouv.vitam.common.digest.DigestType;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.junit.FakeInputStream;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.server.application.AbstractVitamApplication;
import fr.gouv.vitam.common.server.application.configuration.DefaultVitamApplicationConfiguration;
import fr.gouv.vitam.common.server.application.junit.VitamJerseyTest;
import fr.gouv.vitam.storage.driver.exception.StorageDriverException;
import fr.gouv.vitam.storage.driver.model.GetObjectRequest;
import fr.gouv.vitam.storage.driver.model.GetObjectResult;
import fr.gouv.vitam.storage.driver.model.PutObjectRequest;
import fr.gouv.vitam.storage.driver.model.PutObjectResult;
import fr.gouv.vitam.storage.driver.model.StorageCapacityResult;
import fr.gouv.vitam.storage.engine.common.model.DataCategory;
import fr.gouv.vitam.storage.engine.common.model.ObjectInit;

public class ConnectionImplTest extends VitamJerseyTest {

    protected static final String HOSTNAME = "localhost";
    private static JunitHelper junitHelper;
    private static ConnectionImpl connection;

    public ConnectionImplTest() {
        super(new TestVitamClientFactory(8080, "/offer/v1", mock(Client.class)));
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        junitHelper = JunitHelper.getInstance();
    }

    @Override
    public void beforeTest() throws VitamApplicationServerException {
        try {
            connection = DriverImpl.getInstance().connect("http://" + HOSTNAME + ":" + getServerPort(), null);
        } catch (final StorageDriverException e) {
            throw new VitamApplicationServerException(e);
        }
    }

    // Define the getApplication to return your Application using the correct Configuration
    @Override
    public StartApplicationResponse<AbstractApplication> startVitamApplication(int reservedPort) {
        final TestVitamApplicationConfiguration configuration = new TestVitamApplicationConfiguration();
        configuration.setJettyConfig(DEFAULT_XML_CONFIGURATION_FILE);
        final AbstractApplication application = new AbstractApplication(configuration);
        try {
            application.start();
        } catch (final VitamApplicationServerException e) {
            throw new IllegalStateException("Cannot start the application", e);
        }
        return new StartApplicationResponse<AbstractApplication>()
            .setServerPort(application.getVitamServer().getPort())
            .setApplication(application);
    }

    // Define your Application class if necessary
    public final class AbstractApplication
        extends AbstractVitamApplication<AbstractApplication, TestVitamApplicationConfiguration> {
        protected AbstractApplication(TestVitamApplicationConfiguration configuration) {
            super(TestVitamApplicationConfiguration.class, configuration);
        }

        @Override
        protected void registerInResourceConfig(ResourceConfig resourceConfig) {
            resourceConfig.registerInstances(new MockResource(mock));
        }

        @Override
        protected void platformSecretConfiguration() {
            // None
        }

    }
    // Define your Configuration class if necessary
    public static class TestVitamApplicationConfiguration extends DefaultVitamApplicationConfiguration {
    }

    @Path("/offer/v1")
    public static class MockResource {
        private final ExpectedResults expectedResponse;

        public MockResource(ExpectedResults expectedResponse) {
            this.expectedResponse = expectedResponse;
        }

        @GET
        @Path("/status")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getStatus() {
            return expectedResponse.get();
        }

        @GET
        @Path("/objects")
        @Produces(MediaType.APPLICATION_JSON)
        public Response getContainerInformation() {
            return expectedResponse.get();
        }

        @POST
        @Path("/objects/{guid:.+}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Response postObject(@PathParam("guid") String objectGUID, ObjectInit objectInit) {
            return expectedResponse.post();
        }

        @PUT
        @Path("/objects/{guid:.+}")
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        @Produces(MediaType.APPLICATION_JSON)
        public Response putObject(@PathParam("id") String objectId, InputStream input) {
            return expectedResponse.put();
        }

        @GET
        @Path("/objects/{id:.+}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(value = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM})
        public Response getObject(@PathParam("id") String objectId) {
            return expectedResponse.get();
        }
    }

    @AfterClass
    public static void shutdownAfterClass() {
        try {
            connection.close();
        } catch (final Exception e) {

        }
    }

    @Test(expected = VitamApplicationServerException.class)
    public void getStatusKO() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        connection.checkStatus();
    }

    @Test
    public void getStatusNoContent() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.NO_CONTENT).build());
        assertNotNull(connection.getServiceUrl());
        connection.checkStatus();
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithoutRequestKO() throws Exception {
        connection.putObject(null);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithEmptyRequestKO() throws Exception {
        connection.putObject(new PutObjectRequest(null, null, null, null, null));
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectRequestWithOnlyMissingTenantIdKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, false, true);
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectRequestWithOnlyMissingDataStreamKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(false, true, true, true, true);
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectRequestWithOnlyMissingAlgortihmKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, false, true, true, true);
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectRequestWithOnlyMissingGuidKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, false, true, true);
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectRequestWithOnlyMissingTypeKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, false);
        connection.putObject(request);
    }

    @Test
    public void putObjectWithRequestOK() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(-1)).build());
        when(mock.put()).thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(0)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(1)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(2)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(3)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(4)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(5)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(6)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(7)).build());
        final PutObjectResult result = connection.putObject(request);
        assertNotNull(result);
        assertNotNull(result.getDistantObjectId());
        assertNotNull(result.getDigestHashBase16());
    }

    // chunk size (1024) factor size case
    @Test
    public void putBigObjectWithRequestOk() throws Exception {
        final PutObjectRequest request = new PutObjectRequest("0" + this, DigestType.MD5.getName(), "GUID",
            new FakeInputStream(2097152, true), DataCategory.OBJECT.name());
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(-1)).build());
        when(mock.put()).thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(0)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(1)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(2)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(3)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(4)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(5)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(6)).build());
        final PutObjectResult result = connection.putObject(request);
        assertNotNull(result);
        assertNotNull(result.getDistantObjectId());
        assertNotNull(result.getDigestHashBase16());
    }

    // No chunk size (1024) factor case
    @Test
    public void putBigObject2WithRequestOk() throws Exception {
        final PutObjectRequest request = new PutObjectRequest("0" + this, DigestType.MD5.getName(), "GUID",
            new FakeInputStream(2201507, true), DataCategory.OBJECT.name());
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(-1)).build());
        when(mock.put()).thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(0)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(1)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(2)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(3)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(4)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(5)).build())
            .thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(6)).build());
        final PutObjectResult result = connection.putObject(request);
        assertNotNull(result);
        assertNotNull(result.getDistantObjectId());
        assertNotNull(result.getDigestHashBase16());
    }

    // TODO activate when chunk mode is done in {@see DefaultOfferService} method createObject
    @Ignore // chunk management
    @Test(expected = StorageDriverException.class)
    public void putBigObjectWithRequestInternalError() throws Exception {
        final PutObjectRequest request = new PutObjectRequest("0" + this, DigestType.MD5.getName(), "GUID",
            new FakeInputStream(2097152, true), DataCategory.OBJECT.name());
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(-1)).build());
        when(mock.put()).thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(0)).build())
            .thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        connection.putObject(request);
    }

    // TODO activate when chunk mode is done in {@see DefaultOfferService} method createObject
    @Ignore // chunk management
    @Test(expected = StorageDriverException.class)
    public void putBigObjectWithBadRequestDuringTransfert() throws Exception {
        final PutObjectRequest request = new PutObjectRequest("0", DigestType.MD5.getName(), "GUID",
            new FakeInputStream(2095104, true), DataCategory.OBJECT.name());
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(-1)).build());
        when(mock.put()).thenReturn(Response.status(Status.CREATED).entity(getPutObjectResult(0)).build())
            .thenReturn(Response.status(Status.BAD_REQUEST).build());
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithRequestThrowsInternalServerErrorOnPostKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        when(mock.post()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithRequestThrowsNotFoundErrorOnPutKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(0)).build());
        when(mock.put()).thenReturn(Response.status(Status.NOT_FOUND).build());
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithRequestThrowsOtherErrorOnPutKO() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(0)).build());
        when(mock.put()).thenReturn(Response.status(Status.BAD_REQUEST).build());
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectWithRequestThrowsInternalServerErrorOnPutOK() throws Exception {
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        when(mock.post()).thenReturn(Response.status(Status.CREATED).entity(getPostObjectResult(0)).build());
        when(mock.put()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectThrowsInternalServerException() throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        connection.putObject(request);
    }

    @Test(expected = StorageDriverException.class)
    public void putObjectThrowsOtherException() throws Exception {
        when(mock.post()).thenReturn(Response.status(Status.SERVICE_UNAVAILABLE).build());
        final PutObjectRequest request = getPutObjectRequest(true, true, true, true, true);
        connection.putObject(request);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeObjectNotImplemented() throws Exception {
        connection.removeObject(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGetObjectRequestIllegalArgumentException() throws Exception {
        connection.getObject(null);
    }


    @Test
    public void getStorageCapacityOK() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.OK).entity(getStorageCapacityResult()).build());
        final StorageCapacityResult result = connection.getStorageCapacity("0");
        assertNotNull(result);
        assertEquals("0" + this, result.getTenantId());
        assertNotNull(result.getUsableSpace());
        assertNotNull(result.getUsedSpace());
    }

    @Test(expected = StorageDriverException.class)
    public void getStorageCapacityException() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        connection.getStorageCapacity("0");
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGetObjectGUIDIllegalArgumentException() throws Exception {
        final GetObjectRequest request = new GetObjectRequest("0" + this, null, DataCategory.OBJECT.getFolder());
        connection.getObject(request);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getGetObjectTypeIllegalArgumentException() throws Exception {
        final GetObjectRequest request = new GetObjectRequest(null, "guid", DataCategory.OBJECT.getFolder());
        connection.getObject(request);
    }

    @Test
    public void getObjectNotFound() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            connection.getObject(request);
            fail("Expected exception");
        } catch (final StorageDriverException exc) {
            assertEquals(exc.getErrorCode(), StorageDriverException.ErrorCode.NOT_FOUND);
        }
    }

    @Test
    public void getObjectInternalError() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            connection.getObject(request);
            fail("Expected exception");
        } catch (final StorageDriverException exc) {
            assertEquals(StorageDriverException.ErrorCode.INTERNAL_SERVER_ERROR, exc.getErrorCode());
        }
    }

    @Test
    public void getObjectPreconditionFailed() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.PRECONDITION_FAILED).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            connection.getObject(request);
            fail("Expected exception");
        } catch (final StorageDriverException exc) {
            assertEquals(StorageDriverException.ErrorCode.PRECONDITION_FAILED, exc.getErrorCode());
        }
    }

    @Test
    public void getObjectOK() throws Exception {
        final InputStream stream = new ByteArrayInputStream("Test".getBytes());
        when(mock.get()).thenReturn(Response.status(Status.OK).entity(stream).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        final GetObjectResult result = connection.getObject(request);
        assertNotNull(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void objectExistInOfferWithEmptyParameterThrowsException() throws Exception {
        connection.objectExistsInOffer(null);
    }

    @Test
    public void objectExistInOfferInternalServerError() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            connection.objectExistsInOffer(request);
            fail("Expected exception");
        } catch (final StorageDriverException exc) {
            assertEquals(StorageDriverException.ErrorCode.INTERNAL_SERVER_ERROR, exc.getErrorCode());
        }
    }

    @Test
    public void objectExistInOfferNotFound() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.NOT_FOUND).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            final boolean found = connection.objectExistsInOffer(request);
            assertEquals(false, found);
        } catch (final StorageDriverException exc) {
            fail("Ne exception expected");
        }
    }

    @Test
    public void objectExistInOfferFound() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.OK).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            final boolean found = connection.objectExistsInOffer(request);
            assertEquals(true, found);
        } catch (final StorageDriverException exc) {
            fail("Ne exception expected");
        }
    }


    @Test
    public void objectExistInOfferPreconditionFailed() throws Exception {
        when(mock.get()).thenReturn(Response.status(Status.BAD_REQUEST).build());
        final GetObjectRequest request = new GetObjectRequest("0" + this, "guid", DataCategory.OBJECT.getFolder());
        try {
            connection.objectExistsInOffer(request);
            fail("Expected exception");
        } catch (final StorageDriverException exc) {
            assertEquals(StorageDriverException.ErrorCode.PRECONDITION_FAILED, exc.getErrorCode());
        }
    }


    private PutObjectRequest getPutObjectRequest(boolean putDataS, boolean putDigestA, boolean putGuid,
        boolean putTenantId, boolean putType)
        throws Exception {
        FileInputStream stream = null;
        String digest = null;
        String guid = null;
        String tenantId = null;
        String type = null;

        if (putDataS) {
            stream = new FileInputStream(PropertiesUtils.findFile("digitalObject.pdf"));
        }
        if (putDigestA) {
            digest = DigestType.MD5.getName();
        }
        if (putGuid) {
            guid = "GUID";
        }
        if (putTenantId) {
            tenantId = "0";
        }
        if (putType) {
            type = DataCategory.OBJECT.name();
        }
        return new PutObjectRequest(tenantId, digest, guid, stream, type);
    }

    private ObjectInit getPostObjectResult(int uniqueId) {
        final ObjectInit object = new ObjectInit();
        object.setId("" + uniqueId);
        object.setDigestAlgorithm(VitamConfiguration.getDefaultDigestType());
        object.setSize(1024);
        object.setType(DataCategory.OBJECT);
        return object;
    }

    private JsonNode getPutObjectResult(int uniqueId) throws JsonProcessingException, IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualObj = mapper.readTree("{\"digest\":\"aaakkkk" + uniqueId + "\",\"size\":\"666\"}");
        return actualObj;
    }

    private JsonNode getStorageCapacityResult() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode result = mapper.readTree("{\"tenantId\":\"0" + this + "\",\"usableSpace\":\"100000\"," +
            "\"usedSpace\":\"100000\"}");
        return result;
    }

}
