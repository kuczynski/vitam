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
package fr.gouv.vitam.common.auth.web.filter;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.gouv.vitam.common.shiro.junit.AbstractShiroTest;

public class X509AuthenticationFilterTest extends AbstractShiroTest {
    private X509Certificate cert;

    byte[] certBytes = new byte[] {'[', 'B', '@', 1, 4, 0, 'c', 9, 'f', 3, 9};
    BigInteger serial = new BigInteger("1000000000000000");

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    HttpServletRequest requestNull = mock(HttpServletRequest.class);

    @Before
    public void setUp() throws Exception {
        cert = mock(X509Certificate.class);
        when(cert.getEncoded()).thenReturn(certBytes);
        when(cert.getSerialNumber()).thenReturn(serial);
        final X509Certificate[] clientCertChain = new X509Certificate[] {cert};

        when(request.getRemoteHost()).thenReturn("127.0.0.1");
        when(request.getAttribute("javax.servlet.request.X509Certificate")).thenReturn(clientCertChain);

        when(requestNull.getRemoteHost()).thenReturn("127.0.0.1");
    }

    @After
    public void tearDownSubject() {
        clearSubject();
    }

    @Test
    public void givenFilterAccessDenied() throws Exception {
        // Needs mock subject for login call
        Subject subjectUnderTest = mock(Subject.class);
        Mockito.doNothing().when(subjectUnderTest).login(anyObject());
        setSubject(subjectUnderTest);

        final X509AuthenticationFilter filter = new X509AuthenticationFilter();
        filter.onAccessDenied(request, response);
    }

    @Test
    public void givenFilterCreateToken() throws Exception {
        final X509AuthenticationFilter filter = new X509AuthenticationFilter();
        filter.createToken(request, response);
    }

    @Test(expected = Exception.class)
    public void givenFilterCreateTokenWhenClientCertChainNullThenThrowException() throws Exception {
        final X509AuthenticationFilter filter = new X509AuthenticationFilter();
        filter.createToken(requestNull, response);
    }

}
