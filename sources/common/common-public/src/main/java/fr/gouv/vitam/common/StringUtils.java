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
package fr.gouv.vitam.common;

import java.util.concurrent.ThreadLocalRandom;

/**
 * String utils
 */
public final class StringUtils {
    /**
     * Random Generator
     */
    private static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    private StringUtils() {
        // empty
    }

    /**
     * @param length
     * @return a byte array with random values
     */
    public static final byte[] getRandom(final int length) {
        if (length <= 0) {
            return SingletonUtils.getSingletonByteArray();
        }
        final byte[] result = new byte[length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) (RANDOM.nextInt(95) + 32);
        }
        return result;
    }

    /**
     * Revert Arrays.toString for bytes
     *
     * @param bytesString
     * @return the array of bytes
     * @throws IllegalArgumentException if bytesString is null or empty
     */
    public static final byte[] getBytesFromArraysToString(final String bytesString) {
        ParametersChecker.checkParameter("Should not be null or empty", bytesString);
        final String[] strings = bytesString.replace("[", "").replace("]", "").split(", ");
        final byte[] result = new byte[strings.length];
        try {
            for (int i = 0; i < result.length; i++) {
                result[i] = (byte) (Integer.parseInt(strings[i]) & 0xFF);
            }
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
        return result;
    }

    /**
     *
     * @param object
     * @return the short name of the Class of this object
     */
    public static final String getClassName(Object object) {
        final Class<?> clasz = object.getClass();
        String name = clasz.getSimpleName();
        if (name != null && !name.isEmpty()) {
            return name;
        } else {
            name = clasz.getName();
            final int pos = name.lastIndexOf('.');
            if (pos < 0) {
                return name;
            }
            return name.substring(pos + 1);
        }
    }
}
