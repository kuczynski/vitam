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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;

import org.jhades.JHades;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jayway.restassured.RestAssured;

import fr.gouv.vitam.common.BaseXx;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.VitamConfiguration;
import fr.gouv.vitam.common.exception.VitamApplicationServerException;
import fr.gouv.vitam.common.guid.GUIDFactory;
import fr.gouv.vitam.common.junit.JunitHelper;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.storage.driver.Connection;
import fr.gouv.vitam.storage.driver.exception.StorageDriverException;
import fr.gouv.vitam.storage.driver.model.GetObjectRequest;
import fr.gouv.vitam.storage.driver.model.PutObjectRequest;
import fr.gouv.vitam.storage.driver.model.PutObjectResult;
import fr.gouv.vitam.storage.engine.common.model.DataCategory;
import fr.gouv.vitam.storage.offers.workspace.rest.DefaultOfferApplication;
import fr.gouv.vitam.storage.offers.workspace.rest.DefaultOfferConfiguration;
import fr.gouv.vitam.workspace.core.WorkspaceConfiguration;

/**
 * Integration driver offer tests
 */
public class DriverToOfferTest {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(DriverToOfferTest.class);

    private static final String WORKSPACE_OFFER_CONF = "default-offer.conf";
    private static final String DEFAULT_STORAGE_CONF = "default-storage.conf";
    private static final String ARCHIVE_FILE_TXT = "archivefile.txt";

    private static DriverImpl driver;

    private static final String REST_URI = "/offer/v1";

    private static int serverPort;
    private static JunitHelper junitHelper;

    private static Connection connection;
    private static String guid;
    private static DefaultOfferApplication application;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // Identify overlapping in particular jsr311
        new JHades().overlappingJarsReport();

        junitHelper = JunitHelper.getInstance();
        serverPort = junitHelper.findAvailablePort();

        RestAssured.port = serverPort;
        RestAssured.basePath = REST_URI;

        final File workspaceOffer = PropertiesUtils.findFile(WORKSPACE_OFFER_CONF);
        final DefaultOfferConfiguration realWorkspaceOffer =
            PropertiesUtils.readYaml(workspaceOffer, DefaultOfferConfiguration.class);
        // newWorkspaceOfferConf = File.createTempFile("test", WORKSPACE_OFFER_CONF, workspaceOffer.getParentFile());
        // PropertiesUtils.writeYaml(newWorkspaceOfferConf, realWorkspaceOffer);

        try {
            application = new DefaultOfferApplication(realWorkspaceOffer);
            application.start();
        } catch (final VitamApplicationServerException e) {
            LOGGER.error(e);
            throw new IllegalStateException(
                "Cannot start the Wokspace Offer Application Server", e);
        }

        driver = new DriverImpl();
    }

    @AfterClass
    public static void shutdownAfterClass() throws Exception {
        if (connection != null) {
            connection.close();
        }
        junitHelper.releasePort(serverPort);

        application.stop();

        // delete files
        final WorkspaceConfiguration conf = PropertiesUtils.readYaml(PropertiesUtils.findFile(DEFAULT_STORAGE_CONF),
            WorkspaceConfiguration.class);
        final File container = new File(conf.getStoragePath() + "/1");
        final File folder = new File(container.getAbsolutePath(), DataCategory.UNIT.getFolder());
        final File object = new File(folder.getAbsolutePath(), guid);
        Files.deleteIfExists(object.toPath());
        Files.deleteIfExists(folder.toPath());
        Files.deleteIfExists(container.toPath());
    }

    @Test
    public void integrationTest() throws Exception {
        connection = driver.connect("http://localhost:" + serverPort, null);
        assertNotNull(connection);

        PutObjectRequest request = null;
        guid = GUIDFactory.newObjectGUID(1).toString();
        try (FileInputStream fin = new FileInputStream(PropertiesUtils.findFile(ARCHIVE_FILE_TXT))) {
            final MessageDigest messageDigest =
                MessageDigest.getInstance(VitamConfiguration.getDefaultDigestType().getName());
            try (DigestInputStream digestInputStream = new DigestInputStream(fin, messageDigest)) {
                request = new PutObjectRequest("1", VitamConfiguration.getDefaultDigestType().getName(), guid,
                    digestInputStream,
                    DataCategory.UNIT.name());
                final PutObjectResult result = connection.putObject(request);
                assertNotNull(result);

                final WorkspaceConfiguration conf =
                    PropertiesUtils.readYaml(PropertiesUtils.findFile(DEFAULT_STORAGE_CONF),
                        WorkspaceConfiguration.class);
                final File container = new File(conf.getStoragePath() + "/1");
                assertNotNull(container);
                final File object = new File(container.getAbsolutePath(), DataCategory.UNIT.getFolder() + "/" + guid);
                assertNotNull(object);
                assertTrue(com.google.common.io.Files.equal(PropertiesUtils.findFile(ARCHIVE_FILE_TXT), object));

                final String digestToCheck = result.getDigestHashBase16();
                assertNotNull(digestToCheck);
                assertEquals(digestToCheck, BaseXx.getBase16(messageDigest.digest()));
            }
        }

        try (FileInputStream fin = new FileInputStream(PropertiesUtils.findFile(ARCHIVE_FILE_TXT))) {
            request = new PutObjectRequest(null, VitamConfiguration.getDefaultDigestType().getName(), guid, fin,
                DataCategory.UNIT.name());
            connection.putObject(request);
            fail("Should have an exception !");
        } catch (final StorageDriverException exc) {
            // Nothing, missing tenant parameter
        }

        final GetObjectRequest getRequest = new GetObjectRequest("1", guid, DataCategory.UNIT.getFolder());
        connection.getObject(getRequest);
    }
}
