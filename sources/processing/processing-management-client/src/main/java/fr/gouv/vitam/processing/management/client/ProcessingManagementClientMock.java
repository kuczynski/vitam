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
package fr.gouv.vitam.processing.management.client;

import java.util.ArrayList;
import java.util.List;

import fr.gouv.vitam.common.SingletonUtils;
import fr.gouv.vitam.common.client.AbstractMockClient;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.StatusCode;
import fr.gouv.vitam.processing.common.exception.ProcessingBadRequestException;
import fr.gouv.vitam.processing.common.exception.ProcessingException;
import fr.gouv.vitam.processing.common.exception.ProcessingUnauthorizeException;
import fr.gouv.vitam.processing.common.exception.WorkerAlreadyExistsException;
import fr.gouv.vitam.processing.common.exception.WorkflowNotFoundException;
import fr.gouv.vitam.processing.common.model.WorkerBean;

/**
 *
 */
public class ProcessingManagementClientMock extends AbstractMockClient implements ProcessingManagementClient {

    ProcessingManagementClientMock() {
        // Empty
    }

    @Override
    public ItemStatus executeVitamProcess(String container, String workflow)
        throws ProcessingUnauthorizeException, ProcessingBadRequestException, WorkflowNotFoundException,
        ProcessingException {
        final List<Integer> status = new ArrayList<>();
        status.add(0);
        status.add(0);
        status.add(1);
        status.add(0);
        status.add(0);
        status.add(0);
        return new ItemStatus("FakeId", "FakeMessage", StatusCode.OK, status, SingletonUtils.singletonMap(), null);
    }

    @Override
    public void registerWorker(String familyId, String workerId, WorkerBean workerDescription)
        throws ProcessingBadRequestException, WorkerAlreadyExistsException {
        // Nothing to do
    }

    @Override
    public void unregisterWorker(String familyId, String workerId) throws ProcessingBadRequestException {
        // Nothing to do
    }

}
