/*
 * *** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2013
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * *** END LICENSE BLOCK *****
 */

package org.dcm4chee.arc.audit;

import org.dcm4che3.audit.AuditMessages;
import org.dcm4che3.audit.EventTypeCode;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4chee.arc.ArchiveServiceEvent;
import org.dcm4chee.arc.ConnectionEvent;
import org.dcm4chee.arc.query.QueryContext;
import org.dcm4chee.arc.retrieve.RetrieveContext;
import org.dcm4chee.arc.retrieve.RetrieveWADO;
import org.dcm4chee.arc.store.StoreContext;
import org.dcm4chee.arc.retrieve.RetrieveEnd;
import org.dcm4chee.arc.retrieve.RetrieveStart;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.Socket;


/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since Feb 2016
 */
@ApplicationScoped
public class AuditTriggerObserver {
    @Inject
    private AuditService auditService;

    public void onArchiveServiceEvent(@Observes ArchiveServiceEvent event) {
        EventTypeCode eventTypeCode = null;
        switch (event.getType()) {
            case STARTED:
                eventTypeCode = AuditMessages.EventTypeCode.ApplicationStart;
                break;
            case STOPPED:
                eventTypeCode = AuditMessages.EventTypeCode.ApplicationStop;
                break;
            case RELOADED:
                return;
        }
        HttpServletRequest request = event.getRequest();
        auditService.auditApplicationActivity(eventTypeCode, request);
    }

    public void onStore(@Observes StoreContext ctx) {
        if ((null != ctx.getRejectionNote())) {
            auditService.auditInstancesDeleted(ctx);
            return;
        }
        if (ctx.getLocation() == null)
            return;

        auditService.auditInstanceStored(ctx);
    }

    public void onQuery(@Observes QueryContext ctx) {
        Association as = ctx.getAssociation();
        HttpServletRequest request = ctx.getHttpRequest();
        Attributes queryKeys = ctx.getQueryKeys();
        String calledAET = ctx.getCalledAET();
        String callingAET = ctx.getCallingAET();
        String localDevice = ctx.getLocalApplicationEntity().getDevice().getDeviceName();
        String remoteHostName = ctx.getRemoteHostName();
        String sopClassUID = ctx.getSOPClassUID();
        auditService.auditQuery(as, request, queryKeys, callingAET, calledAET, remoteHostName, localDevice, sopClassUID);
    }

    public void onRetrieveStart(@Observes @RetrieveStart RetrieveContext ctx) {
        AuditMessages.EventID eid = AuditMessages.EventID.BeginTransferringDICOMInstances;
        auditService.auditDICOMInstancesTransfer(ctx, eid);
    }

    public void onRetrieveEnd(@Observes @RetrieveEnd RetrieveContext ctx) {
        AuditMessages.EventID eid = AuditMessages.EventID.DICOMInstancesTransferred;
        auditService.auditDICOMInstancesTransfer(ctx, eid);
    }

    public void onRetrieveWADO(@Observes @RetrieveWADO RetrieveContext ctx) {
        auditService.auditWADORetrieve(ctx);
    }

    public void onConnection(@Observes ConnectionEvent event) {
        switch (event.getType()) {
            case ESTABLISHED:
                onConnectionEstablished(event.getConnection(), event.getRemoteConnection(), event.getSocket());
                break;
            case FAILED:
                onConnectionFailed(event.getConnection(), event.getRemoteConnection(), event.getSocket(),
                        event.getException());
                break;
            case REJECTED:
                onConnectionRejected(event.getConnection(), event.getSocket(), event.getException());
                break;
            case REJECTED_BLACKLISTED:
                onConnectionRejectedBlacklisted(event.getConnection(), event.getSocket());
                break;
            case ACCEPTED:
                onConnectionAccepted(event.getConnection(), event.getSocket());
                break;
        }
    }

    private void onConnectionEstablished(Connection conn, Connection remoteConn, Socket s) {
    }

    private void onConnectionFailed(Connection conn, Connection remoteConn, Socket s, Throwable e) {
    }

    private void onConnectionRejectedBlacklisted(Connection conn, Socket s) {
    }

    private void onConnectionRejected(Connection conn, Socket s, Throwable e) {
        auditService.auditConnectionRejected(s, e);
    }

    private void onConnectionAccepted(Connection conn, Socket s) {
    }
}
