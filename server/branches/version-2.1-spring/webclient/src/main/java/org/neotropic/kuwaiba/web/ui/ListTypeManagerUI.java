/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.DeleteListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.NewListTypeItemVisualAction;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the service manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "listtypeman", layout = MainLayout.class)
public class ListTypeManagerUI extends VerticalLayout implements ActionCompletedListener {

    /**
     * the visual action to create a new list type item
     */
    @Autowired
    private NewListTypeItemVisualAction newListTypeItemVisualAction;

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
    /**
     * The grid with the list Types
     */
    private Grid<ClassMetadataLight> tblListTypes;
    
    /**
     * The grid with the list Type items
     */
    private Grid<BusinessObjectLight> tblListTypeItems;
    /**
     * object to save the selected list type
     */
    private ClassMetadataLight currentListType;
    /**
     * button used to create a new item with the list type preselected
     */
    Button btnAddListTypeItemSec;
    
     /**
     * the visual action to delete a list type item
     */
    @Autowired
    private DeleteListTypeItemVisualAction deleteListTypeItemVisualAction;

    public ListTypeManagerUI() {
        super();
        tblListTypes = new Grid<>();
        tblListTypeItems = new Grid<>();
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
        this.newListTypeItemVisualAction.unregisterListener(this);
        this.deleteListTypeItemVisualAction.unregisterListener(this);
    }
    
    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
                
                if (currentListType != null)
                    loadListTypeItems(currentListType);
                
            } catch (Exception ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
        
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
         
        this.newListTypeItemVisualAction.registerActionCompletedLister(this);
        this.deleteListTypeItemVisualAction.registerActionCompletedLister(this);
        
        Button btnAddListTypeItem = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName(), (event) -> {
            this.newListTypeItemVisualAction.getVisualComponent().open();
        });            
        
        btnAddListTypeItemSec = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName(), (event) -> {
            ModuleActionParameter listTypeParameter = new ModuleActionParameter("listType", currentListType);
            this.newListTypeItemVisualAction.getVisualComponent(listTypeParameter).open();
        }); 
        btnAddListTypeItemSec.setEnabled(false);
        
        buildListTypeGrid();
        
        VerticalLayout rightContent = new VerticalLayout(btnAddListTypeItemSec, tblListTypeItems);
        rightContent.setAlignItems(Alignment.END);
        
        VerticalLayout leftContent = new VerticalLayout(tblListTypes, btnAddListTypeItem);
         leftContent.setWidth("400px");
                
         buildListTypeItemsGrid();      
         
         lytMainContent.add(leftContent, rightContent);
         
         add(lytMainContent);
    }

    private void buildListTypeItemsGrid() {
        // build the items grid     
        
        tblListTypeItems.setHeightFull();
        
        tblListTypeItems.addColumn(BusinessObjectLight::getName).setHeader(ts.getTranslatedString("module.listtypeman.listtypeitems"))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
       
        tblListTypeItems.addComponentColumn(item -> createListTypeItemActionGrid(item));
    }

    private void buildListTypeGrid() throws InvalidArgumentException, MetadataObjectNotFoundException {
        //build list type grid
        List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
             
        ListDataProvider<ClassMetadataLight> dataProvider = new ListDataProvider<>(listTypes);
        
        tblListTypes.setDataProvider(dataProvider);
        
        tblListTypes.setHeightFull();
        
        tblListTypes.addColumn(ClassMetadataLight::getName).setHeader(createCenteredHeader(ts.getTranslatedString("module.listtypeman.listtypes")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblListTypes.addItemClickListener(ev -> {
            try {
                btnAddListTypeItemSec.setEnabled(true);
                currentListType = ev.getItem();
                loadListTypeItems(ev.getItem());
            } catch (Exception ex) {
                
            }
        });
        
        HeaderRow filterRow = tblListTypes.appendHeaderRow();
        
        TextField txtFilterListTypeName = createTxtFieldListTypeName(dataProvider);
        filterRow.getCell(tblListTypes.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtFilterListTypeName);
    }

    private void loadListTypeItems(ClassMetadataLight item) throws MetadataObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(item.getName());
        tblListTypeItems.setItems(listTypeItems);
        tblListTypeItems.getDataProvider().refreshAll();
    }
    
    /**
     * create a new input field to filter list types in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldListTypeName(ListDataProvider<ClassMetadataLight> dataProvider) {
        TextField txtListTypeName = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filterplaceholder"));
        txtListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtListTypeName.setWidthFull();
        txtListTypeName.addValueChangeListener(event -> dataProvider.addFilter(
        project -> StringUtils.containsIgnoreCase(project.getName(),
                txtListTypeName.getValue())));
        return txtListTypeName;
    }

    private HorizontalLayout createListTypeItemActionGrid(BusinessObjectLight listTypeItem) {
        HorizontalLayout lyt;      
        Button btnEdit = new Button(new Icon(VaadinIcon.PENCIL));
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), ev -> {

            ModuleActionParameter listTypeParameter = new ModuleActionParameter("listTypeItem", listTypeItem);
            this.deleteListTypeItemVisualAction.getVisualComponent(listTypeParameter).open();

        });
        lyt = new HorizontalLayout(btnEdit, btnDelete);
        lyt.setWidthFull();
        lyt.setSpacing(true);
        
        return lyt;
    }

    private HorizontalLayout createCenteredHeader(String translatedString) {
        Label lblHeader = new Label(translatedString);
        lblHeader.setClassName("boldText");
        HorizontalLayout lyt = new HorizontalLayout(lblHeader);
        lyt.setAlignItems(FlexComponent.Alignment.CENTER);
        lyt.setJustifyContentMode(JustifyContentMode.CENTER);
        
        return lyt;
    }
}
