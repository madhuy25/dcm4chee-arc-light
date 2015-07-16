package org.dcm4chee.archive.store.org.dcm4chee.archive.store.impl;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Device;
import org.dcm4chee.archive.conf.ArchiveAEExtension;
import org.dcm4chee.archive.conf.ArchiveDeviceExtension;
import org.dcm4chee.archive.storage.Storage;
import org.dcm4chee.archive.store.StoreSession;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @since Jul 2015
 */
class StoreSessionImpl implements StoreSession {
    private final Association as;
    private final ApplicationEntity ae;
    private Storage storage;

    public StoreSessionImpl(Association as) {
        this.as = as;
        this.ae = as.getApplicationEntity();
    }

    @Override
    public Association getAssociation() {
        return as;
    }

    @Override
    public ApplicationEntity getLocalApplicationEntity() {
        return ae;
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    @Override
    public void setStorage(Storage storage) {
        this.storage = storage;
    }

}