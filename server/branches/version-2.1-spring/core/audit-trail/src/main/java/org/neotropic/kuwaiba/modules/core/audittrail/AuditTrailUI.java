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
package org.neotropic.kuwaiba.modules.core.audittrail;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ActivityLogEntry;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.klaudeta.PaginatedGrid;

/**
 * Main for the Audit Trail module. This class manages how the pages corresponding
 * to different functionalities are present in a single place. 
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "audit-trail", layout = AuditTrailLayout.class)
public class AuditTrailUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The grid with the list audit trail
     */
    private final PaginatedGrid<ActivityLogEntry> tblAuditTrail; 
    /**
     * Save time stamp activity to display later in readable format
     */
    private Date timeStamp;
    /**
     * Save activity type to display later in readable format
     */
    private final HashMap<Integer, String> types;
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) { }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.audittrail.title");
    }
    
    public AuditTrailUI() {
        super();
        setSizeFull();
        tblAuditTrail = new PaginatedGrid<>();
        types = new HashMap<>();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        createContent();
    }
    
    @Override
    public void onDetach(DetachEvent ev) { }

    private void createContent() {
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        lytMainContent.setMargin(false);
        lytMainContent.setPadding(false);
        
        Button btnRefresh = new Button(ts.getTranslatedString("module.audittrail.actions.refresh"), new Icon(VaadinIcon.REFRESH),
                (event) -> {
                    refreshAuditTrail();
                }
        );
        btnRefresh.setClassName("icon-button");
        
        getActivityType();
        buildAuditTrailGrid();
        H4 header = new H4(ts.getTranslatedString("module.audittrail.header"));
        header.setClassName("header-position");
        HorizontalLayout headers = new HorizontalLayout(header, btnRefresh);
        headers.setClassName("header-layout-position");
        VerticalLayout lytAuditTrail = new VerticalLayout(headers, tblAuditTrail);
        lytAuditTrail.setMargin(false);
        lytAuditTrail.setPadding(false);
        
        lytMainContent.add(lytAuditTrail);
        add(lytMainContent);
    }

    private void buildAuditTrailGrid() {
        List<ActivityLogEntry> auditTrail = aem.getGeneralActivityAuditTrail(0, -1, null);
        ListDataProvider<ActivityLogEntry> dataprovider = new ListDataProvider<>(auditTrail);
        tblAuditTrail.setDataProvider(dataprovider);
        tblAuditTrail.setHeightFull();
        tblAuditTrail.addColumn(item -> timeStamp = new Date(item.getTimestamp()))
                .setHeader(ts.getTranslatedString("module.audittrail.activity-timestamp")).setResizable(true);
        tblAuditTrail.addColumn(item -> ts.getTranslatedString(types.get(item.getType())))
                .setHeader(ts.getTranslatedString("module.audittrail.activity-type")).setResizable(true);
        tblAuditTrail.addColumn(ActivityLogEntry::getUserName)
                .setHeader(ts.getTranslatedString("module.audittrail.activity-user")).setResizable(true);
        tblAuditTrail.addColumn(ActivityLogEntry::getAffectedProperty)
                .setHeader(ts.getTranslatedString("module.audittrail.activity-property")).setResizable(true);
        tblAuditTrail.addColumn(ActivityLogEntry::getOldValue)
                .setHeader(ts.getTranslatedString("module.audittrail.activity-oldValue")).setResizable(true);
        tblAuditTrail.addColumn(ActivityLogEntry::getNewValue)
                .setHeader(ts.getTranslatedString("module.audittrail.activity-newValue")).setResizable(true);
        tblAuditTrail.addColumn(ActivityLogEntry::getNotes)
                .setHeader(ts.getTranslatedString("module.audittrail.activity-notes")).setResizable(true);
        tblAuditTrail.setClassNameGenerator(item -> item.getNotes() != null && !item.getNotes().isEmpty() ? "text" : "");
        // Sets the max number of items to be rendered on the grid for each page
        tblAuditTrail.setPageSize(15);
        // Sets how many pages should be visible on the pagination before and/or after the current selected page
        tblAuditTrail.setPaginatorSize(1);
    }
    
    private void refreshAuditTrail() {
        List<ActivityLogEntry> auditTrail = aem.getGeneralActivityAuditTrail(0, -1, null);
        tblAuditTrail.setItems(auditTrail);
        tblAuditTrail.getDataProvider().refreshAll();
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
