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

package org.neotropic.kuwaiba.web.ui;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.DeleteListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.NewListTypeItemVisualAction;
import org.neotropic.util.visual.properties.PropertyFactory;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.PropertySheet.IPropertyValueChangedListener;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the service manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "listtypeman", layout = MainLayout.class)
public class ListTypeManagerUI extends VerticalLayout implements ActionCompletedListener, IPropertyValueChangedListener {

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
     * object to save the selected list type item
     */
    private BusinessObjectLight currentListTypeItem;
    
    /**
     * button used to create a new item with the list type preselected
     */
    Button btnAddListTypeItemSec;
    
     /**
     * the visual action to delete a list type item
     */  
    @Autowired
    private DeleteListTypeItemVisualAction deleteListTypeItemVisualAction;
    
    PropertySheet propertysheet;

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
        
        VerticalLayout lytListTypeItems = new VerticalLayout(btnAddListTypeItemSec, tblListTypeItems);
        lytListTypeItems.setAlignItems(Alignment.END);
        lytListTypeItems.setClassName("width30p");
        
        VerticalLayout lytListTypes= new VerticalLayout(tblListTypes, btnAddListTypeItem);
        lytListTypes.setClassName("width30p");
                
        buildListTypeItemsGrid();  
         
        propertysheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertysheet.addPropertyValueChangedListener(this);
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        VerticalLayout lytPropertySheet = new VerticalLayout(headerPropertySheet, propertysheet);
        lytPropertySheet.setClassName("width40p");
         
        lytMainContent.add(lytListTypes, lytListTypeItems, lytPropertySheet);
         
        add(lytMainContent);
    }

    private void buildListTypeItemsGrid() {
        // build the items grid     
        
        tblListTypeItems.setHeightFull();
        
        tblListTypeItems.addColumn(BusinessObjectLight::getName).setHeader(ts.getTranslatedString("module.listtypeman.listtypeitems"))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
       
        tblListTypeItems.addComponentColumn(item -> createListTypeItemActionGrid(item));
        
        tblListTypeItems.addItemClickListener(ev -> {
            currentListTypeItem = ev.getItem();
            updatePropertySheet(); 
        });
    }

    private void updatePropertySheet() {
        try {
            propertysheet.setItems(PropertyFactory.propertiesFromRemoteObject(currentListTypeItem, aem, bem, mem));
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                propertysheet.clear();
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
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), ev -> {

            ModuleActionParameter listTypeParameter = new ModuleActionParameter("listTypeItem", listTypeItem);
            this.deleteListTypeItemVisualAction.getVisualComponent(listTypeParameter).open();

        });
        lyt = new HorizontalLayout(btnDelete);
        lyt.setWidthFull();
        lyt.setSpacing(true);
        
        return lyt;
    }

    private HorizontalLayout createCenteredHeader(String translatedString) {
        Label lblHeader = new Label(translatedString);
        lblHeader.setClassName("bold-text");
        HorizontalLayout lyt = new HorizontalLayout(lblHeader);
        lyt.setAlignItems(FlexComponent.Alignment.CENTER);
        lyt.setJustifyContentMode(JustifyContentMode.CENTER);
        
        return lyt;
    }

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (currentListTypeItem != null) {
                
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), property.getAsStringToPersist());

                bem.updateObject(currentListTypeItem.getClassName(), currentListTypeItem.getId(), attributes);

                loadListTypeItems(currentListType);               
                tblListTypeItems.select(currentListTypeItem);               
//                updatePropertySheet();

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                | OperationNotPermittedException | InvalidArgumentException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
