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
package fr.gouv.vitam.common.guid;

import fr.gouv.vitam.common.ServerIdentity;

/**
 * GUID Factory <br>
 * <br>
 * Usage:<br>
 * One should use the appropriate helper according to the type of the object for the GUID.<br>
 * For instance: for a Unit newUnitGUID(tenantId);<br>
 * <br>
 * <b>No one should not in general use directly newGUID helpers.</b><br>
 */
public final class GUIDFactory {
    private static final ServerIdentity serverIdentity = ServerIdentity.getInstance();

    /**
     * Default empty constructor
     */
    private GUIDFactory() {
        // Empty constructor
    }

    /**
     * Usable for internal GUID with default tenantId (0) and objectType (0)
     *
     * @return a new GUID
     */
    public static final GUID newGUID() {
        return new GUIDImplPrivate(0, 0, serverIdentity.getPlatformId(), false);
    }

    /**
     * Usable for GUID with default tenantId (0)
     *
     * @param objectType object type id between 0 and 255
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newGUID(final int objectType) {
        return new GUIDImplPrivate(objectType, 0, serverIdentity.getPlatformId(), false);
    }

    /**
     * Usable for GUID
     *
     * @param objectType object type id between 0 and 255
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newGUID(final int objectType, final int tenantId) {
        return new GUIDImplPrivate(objectType, tenantId, serverIdentity.getPlatformId(), false);
    }

    /**
     * Usable for internal GUID with default tenantId (0) and objectType (0)
     *
     * @param worm True if Worm GUID
     * @return a new GUID
     */
    public static final GUID newGUID(final boolean worm) {
        return new GUIDImplPrivate(0, 0, serverIdentity.getPlatformId(), worm);
    }

    /**
     * Usable for GUID with default tenantId (0)
     *
     * @param objectType object type id between 0 and 255
     * @param worm True if Worm GUID
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newGUID(final int objectType, final boolean worm) {
        return new GUIDImplPrivate(objectType, 0, serverIdentity.getPlatformId(), worm);
    }

    /**
     * Usable for GUID
     *
     * @param objectType object type id between 0 and 255
     * @param tenantId tenant id between 0 and 2^30-1
     * @param worm True if Worm GUID
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newGUID(final int objectType, final int tenantId, final boolean worm) {
        return new GUIDImplPrivate(objectType, tenantId, serverIdentity.getPlatformId(), worm);
    }

    /**
     * Usable when a strict children GUID is to be created, therefore inherits information from parent GUID <br>
     * Keep in case in the future it could be useful.
     *
     * @param existingGUID used to get the objectType parent ({@link GUIDObjectType}), tenantId,
     *        serverIdentity.getPlatformId() and Worm
     * @return a new GUID
     */
    static final GUID newChildrenGUID(final GUID existingGUID) {
        return new GUIDImplPrivate(GUIDObjectType.getChildrenType(existingGUID.getObjectId()),
            existingGUID.getTenantId(), existingGUID.getPlatformId(),
            existingGUID.isWorm());
    }

    /**
     * Create a Unit GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newUnitGUID(final int tenantId) {
        final int type = GUIDObjectType.UNIT_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a ObjectGroup GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newObjectGroupGUID(final int tenantId) {
        final int type = GUIDObjectType.OBJECTGROUP_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a ObjectGroup GUID
     *
     * @param unitParentGUID GUID of parent Unit
     * @return a new GUID
     */
    public static final GUID newObjectGroupGUID(final GUID unitParentGUID) {
        final int type = GUIDObjectType.OBJECTGROUP_TYPE;
        return new GUIDImplPrivate(type, unitParentGUID.getTenantId(),
            serverIdentity.getPlatformId(), GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Object GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newObjectGUID(final int tenantId) {
        final int type = GUIDObjectType.OBJECT_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Object GUID
     *
     * @param objectGroupParentGUID GUID of parent ObjectGroup
     * @return a new GUID
     */
    public static final GUID newObjectGUID(final GUID objectGroupParentGUID) {
        final int type = GUIDObjectType.OBJECT_TYPE;
        return new GUIDImplPrivate(type, objectGroupParentGUID.getTenantId(),
            serverIdentity.getPlatformId(), GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Operation Logbook GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newOperationLogbookGUID(final int tenantId) {
        final int type = GUIDObjectType.OPERATION_LOGBOOK_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Write Logbook GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newWriteLogbookGUID(final int tenantId) {
        final int type = GUIDObjectType.WRITE_LOGBOOK_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Storage Operation GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @param worm
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newStorageOperationGUID(final int tenantId, final boolean worm) {
        return new GUIDImplPrivate(GUIDObjectType.STORAGE_OPERATION_TYPE, tenantId, serverIdentity.getPlatformId(),
            worm);
    }

    /**
     * Create a Operation Id GUID
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newOperationIdGUID(final int tenantId) {
        final int type = GUIDObjectType.OPERATIONID_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Request Id GUID (X-CID)
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newRequestIdGUID(final int tenantId) {
        final int type = GUIDObjectType.REQUESTID_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     * Create a Manifest GUID (SEDA)
     *
     * @param tenantId tenant id between 0 and 2^30-1
     * @return a new GUID
     * @throws IllegalArgumentException if any of the argument are out of range
     */
    public static final GUID newManifestGUID(final int tenantId) {
        final int type = GUIDObjectType.MANIFEST_TYPE;
        return new GUIDImplPrivate(type, tenantId, serverIdentity.getPlatformId(),
            GUIDObjectType.getDefaultWorm(type));
    }

    /**
     *
     * @param uuid
     * @return True if the given GUID is using a WORM media
     */
    public static final boolean isWorm(final GUID uuid) {
        return uuid.isWorm();
    }

    /**
     * @return the size of the key in bytes
     */
    public static final int getKeysize() {
        return GUIDImpl.KEYSIZE;
    }

    /**
     * @return the size of the key using Base32 format in bytes
     */
    public static final int getKeysizeBase32() {
        return GUIDImpl.KEYB32SIZE;
    }

}