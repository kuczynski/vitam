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
package fr.gouv.vitam.functional.administration.common;

import java.util.Calendar;

/**
 * Enum for Rule Measurement
 */
public enum RuleMeasurementEnum {

    /**
     * Month
     */
    MOIS("Mois", Calendar.MONTH),
    /**
     * Day
     */
    JOURS("Jours", Calendar.DAY_OF_MONTH),
    /**
     * Year
     */
    ANNEE("Année", Calendar.YEAR),
    /**
     * Second
     */
    SECONDES("Secondes", Calendar.SECOND);

    private final String type;
    private final int calendarUnitType;

    /**
     * Constructor
     */
    private RuleMeasurementEnum(String ruleMeasurement, int calendarUnitType) {
        type = ruleMeasurement;
        this.calendarUnitType = calendarUnitType;
    }

    /**
     *
     * @return the type of the measure
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @return the Calendar Unit Type
     */
    public int getCalendarUnitType() {
        return calendarUnitType;
    }

    /**
     *
     * @param month
     * @return the associated RuleMeasurementEnum according to parameter
     */
    public static RuleMeasurementEnum getEnumFromMonth(String month) {
        for (final RuleMeasurementEnum e : values()) {
            if (e.getType().equalsIgnoreCase(month)) {
                return e;
            }
        }
        return null;
    }
}
