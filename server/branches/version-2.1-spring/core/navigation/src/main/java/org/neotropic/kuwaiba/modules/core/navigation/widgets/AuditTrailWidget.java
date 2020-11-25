/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.widgets;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Shows audit trail associated to a given object.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
public class AuditTrailWidget extends AbstractDashboardWidget {
    /**
     * The inventory object we need the report list for.
     */
    private final BusinessObjectLight inventoryObject;
    /**
     * The grid with the audit trail of an object
     */
    private final Grid<ActivityLogEntry> tblObjectAuditTrail;
    /**
     * Save time stamp activity to display later in readable format
     */
    private Date timeStamp;
    /**
     * Save activity type to display later in readable format
     */
    private final HashMap<Integer, String> types;
    
    public AuditTrailWidget(BusinessObjectLight inventoryObject, MetadataEntityManager mem, ApplicationEntityManager aem,
            BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.inventoryObject = inventoryObject;
        types = new HashMap<>();
        tblObjectAuditTrail = new Grid<>();
        super.setTitle(ts.getTranslatedString("module.navigation.widgets.audit-trail.title"));
        super.createCover();
        coverComponent.addClassName("widgets-colors-good-green");
    }
    
    @Override
    public void createContent() {
        getActivityType();
        buildAuditTrailGrid();
        VerticalLayout lytContent = new VerticalLayout(tblObjectAuditTrail);
        lytContent.addClassName("widgets-layout-dialog-list");
        contentComponent = lytContent;
    }
    
    private void buildAuditTrailGrid() {
        try {
            List<ActivityLogEntry> objectEntries = aem.getBusinessObjectAuditTrail(inventoryObject.getClassName(), inventoryObject.getId(), -1);
            if (objectEntries.isEmpty()) {
                Label lblNoAuditTrail = new Label(ts.getTranslatedString("module.navigation.widgets.audit-trail.ui.no-audit-trail"));
                lblNoAuditTrail.addClassName("text-padded");
                contentComponent = lblNoAuditTrail;
                return;
            }
            tblObjectAuditTrail.setItems(objectEntries);
            tblObjectAuditTrail.setHeightFull();
            tblObjectAuditTrail.addColumn(item -> timeStamp = new Date(item.getTimestamp()))
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-timestamp"));
            tblObjectAuditTrail.addColumn(item -> ts.getTranslatedString(types.get(item.getType())))
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-type"));
            tblObjectAuditTrail.addColumn(ActivityLogEntry::getUserName)
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-user"));
            tblObjectAuditTrail.addColumn(ActivityLogEntry::getAffectedProperty)
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-property"));
            tblObjectAuditTrail.addColumn(ActivityLogEntry::getOldValue)
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-oldValue"));
            tblObjectAuditTrail.addColumn(ActivityLogEntry::getNewValue)
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-newValue"));
            tblObjectAuditTrail.addColumn(ActivityLogEntry::getNotes)
                    .setHeader(ts.getTranslatedString("module.audittrail.activity-notes"));
            tblObjectAuditTrail.setClassNameGenerator(item -> !item.getNotes().isEmpty() ? "text" : "");

        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(AuditTrailWidget.class.getName()).log(Level.SEVERE, null, ex);
            Label lblUnexpectedError = new Label(ex.getLocalizedMessage());
            lblUnexpectedError.addClassName("text-padded");
            contentComponent = lblUnexpectedError;
        }
    }

    private void getActivityType() {
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, "module.audittrail.activity-type.create-application-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, "module.audittrail.activity-type.delete-application-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, "module.audittrail.activity-type.update-aplication-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, "module.audittrail.activity-type.create-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, "module.audittrail.activity-type.delete-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, "module.audittrail.activity-type.update-inventory-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, "module.audittrail.activity-type.create-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, "module.audittrail.activity-type.delete-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, "module.audittrail.activity-type.update-metadata-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, "module.audittrail.activity-type.move-object");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_DELETE_APPLICATION_OBJECT, "module.audittrail.activity-type.massive-delete");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, "module.audittrail.activity-type.view-update");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, "module.audittrail.activity-type.session-created");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CLOSE_SESSION, "module.audittrail.activity-type.session-closed");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_USER, "module.audittrail.activity-type.new-user");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT, "module.audittrail.activity-type.massive-application-object-update");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, "module.audittrail.activity-type.create-inventory-object-relationship");
        types.put(ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, "module.audittrail.activity-type.release-inventory-object-relationship");
    }
}
