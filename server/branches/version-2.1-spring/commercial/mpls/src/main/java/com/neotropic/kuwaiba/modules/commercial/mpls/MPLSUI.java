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

package com.neotropic.kuwaiba.modules.commercial.mpls;

import static com.neotropic.kuwaiba.modules.commercial.mpls.MPLSModule.CLASS_VIEW;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.DeleteMPLSViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.NewMPLSViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MPLSService;
import com.neotropic.kuwaiba.modules.commercial.mpls.widgets.MPLSDashboard;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ActionResponse;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.NotAuthorizedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the MPLS module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "mpls", layout = MPLSLayout.class)
public class MPLSUI extends VerticalLayout {

    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Autowired
    private MPLSService mplsService;
    
    @Autowired
    private DeleteMPLSViewVisualAction deleteMPLSViewVisualAction;
    
    @Autowired
    private NewMPLSViewVisualAction newMPLSViewVisualAction;
    
    @Autowired
    private ResourceFactory resourceFactory;
    
    /**
     * listener to attribute actions
     */
    private ActionCompletedListener listenerDeleteAction;
    /**
     * listener to attribute actions
     */
    private ActionCompletedListener listenerNewViewAction;
    
    private MPLSDashboard dashboard;
    
    private List<ViewObjectLight> mplsViews;
    
    private Grid<ViewObjectLight> tblViews;
     
    
    public MPLSUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.listtypeman.title")));      

        try {
            createContent();
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            
        }
        
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.deleteMPLSViewVisualAction.unregisterListener(listenerDeleteAction);
        this.newMPLSViewVisualAction.unregisterListener(listenerNewViewAction);
    }
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();                                          
            } catch (Exception ex) {
                Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
         
        listenerDeleteAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            dashboard.getMplsTools().setToolsEnabled(false);
            dashboard.setCurrentView(null);
        };
        this.deleteMPLSViewVisualAction.registerActionCompletedLister(listenerDeleteAction);
        
        listenerNewViewAction = (ActionCompletedListener.ActionCompletedEvent ev) -> {
            loadViews();
            tblViews.setItems(mplsViews);
            tblViews.getDataProvider().refreshAll();
            showActionCompledMessages(ev);
            dashboard.getMplsTools().setToolsEnabled(true);
            ActionResponse response = ev.getActionResponse();
            try {
                ViewObject newView = aem.getGeneralView((long) response.get("viewId"));
                dashboard.setCurrentView(newView);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
        this.newMPLSViewVisualAction.registerActionCompletedLister(listenerNewViewAction);
        
        Button btnNewView = new Button("New Mpls View", new Icon(VaadinIcon.PLUS), ev -> {
             this.newMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });
        
        initializeTblViews();
        
        dashboard = new MPLSDashboard(ts, mem, aem, bem, resourceFactory, mplsService);
        dashboard.setWidth("60%");
        H4 headerListTypes = new H4(ts.getTranslatedString("module.mpsl.mpls-views"));
        VerticalLayout lytViews = new VerticalLayout(headerListTypes, tblViews, btnNewView);
        lytViews.setSpacing(false);
        lytViews.setPadding(false);
        lytViews.setWidth("40%");
        
        HorizontalLayout lytMain = new HorizontalLayout(lytViews, dashboard);
        lytMain.setSizeFull();
        add(lytMain);
    }
    
      public void loadViews() {
        try {
            mplsViews = aem.getGeneralViews(CLASS_VIEW,-1);             
        } catch (InvalidArgumentException | NotAuthorizedException ex) {
            Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initializeTblViews() {
        
        loadViews();
        tblViews = new Grid<>();
        ListDataProvider<ViewObjectLight> dataProvider = new ListDataProvider<>(mplsViews);
        tblViews.setDataProvider(dataProvider);
        tblViews.setHeightFull(); //setHeader("View Name").
        tblViews.addColumn(ViewObjectLight::getName).setFlexGrow(3).setKey(ts.getTranslatedString("module.general.labels.name"));
        tblViews.addComponentColumn(item -> createActionsColumn(item)).setKey("component-column");
        
        HeaderRow filterRow = tblViews.appendHeaderRow();
        
        TextField txtViewNameFilter = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filterplaceholder"));
        txtViewNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
        txtViewNameFilter.setWidthFull();
        txtViewNameFilter.addValueChangeListener(event -> dataProvider.addFilter(
        project -> StringUtils.containsIgnoreCase(project.getName(),
                txtViewNameFilter.getValue())));
        
        filterRow.getCell(tblViews.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtViewNameFilter);
        
    }

    private HorizontalLayout createActionsColumn(ViewObjectLight item) {
        HorizontalLayout lytActions = new HorizontalLayout();
        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), evt -> {
             this.deleteMPLSViewVisualAction.getVisualComponent(new ModuleActionParameterSet(                       new ModuleActionParameter("viewId", item.getId()))).open();
        });
        btnDelete.setClassName("icon-button");
        Button btnEdit = new Button(new Icon(VaadinIcon.EDIT), evt -> {
            try {
                ViewObject view = aem.getGeneralView(item.getId());
                dashboard.setCurrentView(view);
                dashboard.getMplsTools().setToolsEnabled(true);
            } catch (ApplicationObjectNotFoundException ex) {
                Logger.getLogger(MPLSUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        btnEdit.setClassName("icon-button");
        
        lytActions.add(btnDelete, btnEdit);
        
        return lytActions;       
    }
}