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

package org.dcm4chee.arc.hl7;

import org.dcm4che3.conf.api.ConfigurationException;
import org.dcm4che3.data.*;
import org.dcm4che3.hl7.HL7Segment;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.hl7.HL7Application;
import org.dcm4che3.net.hl7.UnparsedHL7Message;
import org.dcm4che3.net.hl7.service.HL7Service;
import org.dcm4che3.util.UIDUtils;
import org.dcm4chee.arc.conf.ArchiveHL7ApplicationExtension;
import org.dcm4chee.arc.patient.PatientService;
import org.dcm4chee.arc.store.StoreContext;
import org.dcm4chee.arc.store.StoreService;
import org.dcm4chee.arc.store.StoreSession;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @author Vrinda Nayak <vrinda.nayak@j4care.com>
 * @since May 2016
 */
@ApplicationScoped
@Typed(HL7Service.class)
class ImportReportService extends AbstractHL7Service {

    @Inject
    private PatientService patientService;

    @Inject
    private StoreService storeService;

    public ImportReportService() {
        super("ORU^R01");
    }

    @Override
    protected void process(HL7Application hl7App, Socket s, UnparsedHL7Message msg) throws Exception {
        PatientUpdateService.updatePatient(hl7App, s, msg, patientService);
        importReport(hl7App, s, msg);
    }

    private void importReport(HL7Application hl7App, Socket s, UnparsedHL7Message msg) throws Exception {
        ArchiveHL7ApplicationExtension arcHL7App =
                hl7App.getHL7ApplicationExtension(ArchiveHL7ApplicationExtension.class);
        String aet = arcHL7App.getAETitle();
        if (aet == null) {
            throw new ConfigurationException("No AE Title associated with HL7 Application: "
                    + hl7App.getApplicationName());
        }
        ApplicationEntity ae = hl7App.getDevice().getApplicationEntity(aet);
        if (ae == null) {
            throw new ConfigurationException("No local AE with AE Title " + aet
                    + " associated with HL7 Application: " + hl7App.getApplicationName());
        }
        HL7Segment msh = msg.msh();
        String hl7cs = msh.getField(17, hl7App.getHL7DefaultCharacterSet());
        Attributes attrs = SAXTransformer.transform(
                msg.data(), hl7cs, arcHL7App.importReportTemplateURI(), null);

        if (!attrs.containsValue(Tag.StudyInstanceUID)) {
            List<String> suids = storeService.studyIUIDsByAccessionNo(attrs.getString(Tag.AccessionNumber));
            switch (suids.size()) {
                case 0:
                    attrs.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
                    break;
                case 1:
                    attrs.setString(Tag.StudyInstanceUID, VR.UI, suids.get(0));
                    break;
                default:
                    mstore(s, ae, msh, attrs, suids);
                    return;
            }
        }
        if (!attrs.containsValue(Tag.SOPInstanceUID))
            attrs.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());
        if (!attrs.containsValue(Tag.SeriesInstanceUID))
            attrs.setString(Tag.SeriesInstanceUID, VR.UI,
                    UIDUtils.createNameBasedUID(attrs.getBytes(Tag.SOPInstanceUID)));
        store(s, ae, msh, attrs);
    }

    private void store(Socket s, ApplicationEntity ae, HL7Segment msh, Attributes attrs) throws IOException {
        try (StoreSession session = storeService.newStoreSession(s, msh, ae)) {
            StoreContext ctx = storeService.newStoreContext(session);
            ctx.setSopClassUID(attrs.getString(Tag.SOPClassUID));
            ctx.setSopInstanceUID(attrs.getString(Tag.SOPInstanceUID));
            ctx.setReceiveTransferSyntax(UID.ExplicitVRLittleEndian);
            storeService.store(ctx, attrs);
        }
    }

    private void mstore(Socket s, ApplicationEntity ae, HL7Segment msh, Attributes attrs, List<String> suids)
            throws IOException {
        int n = suids.size();
        Sequence seq = attrs.newSequence(Tag.IdenticalDocumentsSequence, n);
        for (String suid : suids)
            seq.add(refStudy(suid));
        for (int i = 0; i < n; i++) {
            Attributes refStudy = seq.remove(i);
            Attributes refSeries = refStudy.getNestedDataset(Tag.ReferencedSeriesSequence);
            Attributes refSOP = refSeries.getNestedDataset(Tag.ReferencedSOPSequence);
            attrs.setString(Tag.StudyInstanceUID, VR.UI, refStudy.getString(Tag.StudyInstanceUID));
            attrs.setString(Tag.SeriesInstanceUID, VR.UI, refSeries.getString(Tag.SeriesInstanceUID));
            attrs.setString(Tag.SOPInstanceUID, VR.UI, refSOP.getString(Tag.ReferencedSOPInstanceUID));
            store(s, ae, msh, attrs);
            seq.add(i, refStudy);
        }
    }

    private Attributes refStudy(String studyUID) {
        Attributes refStudyAttrs = new Attributes(2);
        refStudyAttrs.setString(Tag.StudyInstanceUID, VR.UI, studyUID);
        refStudyAttrs.newSequence(Tag.ReferencedSeriesSequence, 2).add(refSeries());
        return refStudyAttrs;
    }

    private Attributes refSeries() {
        Attributes refSeriesAttrs = new Attributes(2);
        refSeriesAttrs.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        refSeriesAttrs.newSequence(Tag.ReferencedSOPSequence, 1).add(refSOP());
        return refSeriesAttrs;
    }

    private Attributes refSOP() {
        Attributes attrs = new Attributes(2);
        attrs.setString(Tag.ReferencedSOPInstanceUID, VR.UI, UIDUtils.createUID());
        attrs.setString(Tag.ReferencedSOPClassUID, VR.UI, UID.BasicTextSRStorage);
        return attrs;
    }

}
