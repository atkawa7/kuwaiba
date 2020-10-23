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

package org.neotropic.kuwaiba.modules.core.ltman;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.ltman.actions.DeleteListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.ltman.actions.NewListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyValueConverter;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.PropertySheet.IPropertyValueChangedListener;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the List type manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "ltman", layout = ListTypeManagerLayout.class)
public class ListTypeManagerUI extends VerticalLayout implements ActionCompletedListener, IPropertyValueChangedListener, HasDynamicTitle {

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
    
    private Label lblCurrentListType;
    /**
     * button used to create a new item with the list type preselected
     */
    Button btnAddListTypeItemSec;
     /**
     * the visual action to delete a list type item
     */ 
    VerticalLayout lytPropertySheet;
            
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
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
                
                if (currentListType != null)
                    loadListTypeItems(currentListType);
                
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
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
        
        Button btnAddListTypeItem = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                 (event) -> {
            this.newListTypeItemVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
        });            
        
        btnAddListTypeItemSec = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
            this.newListTypeItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                                                        new ModuleActionParameter("listType", currentListType))).open();
        }); 
        btnAddListTypeItemSec.setEnabled(false);
        btnAddListTypeItemSec.setClassName("align-self-end");
                    
        H4 headerListTypes = new H4(ts.getTranslatedString("module.listtypeman.listtypes"));
        VerticalLayout lytListTypes= new VerticalLayout(headerListTypes, tblListTypes, btnAddListTypeItem);
//        lytListTypes.setClassName("width30p");
        lytListTypes.setWidth("25%");    
        buildListTypeGrid();  
        
        lblCurrentListType = new BoldLabel();
        FlexLayout lytSubtitle = new FlexLayout();
        lytSubtitle.setWidthFull();
        lytSubtitle.setJustifyContentMode(JustifyContentMode.BETWEEN);
        H4 headerListTypeItems = new H4(ts.getTranslatedString("module.listtypeman.listtypeitems"));
        lytSubtitle.add(headerListTypeItems, lblCurrentListType);
        VerticalLayout lytListTypeItems = new VerticalLayout(lytSubtitle, btnAddListTypeItemSec, tblListTypeItems); 
//        lytListTypeItems.setClassName("width30p");
        lytListTypeItems.setWidth("30%");
        buildListTypeItemsGrid();
         
        propertysheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertysheet.addPropertyValueChangedListener(this);
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        lytPropertySheet = new VerticalLayout(headerPropertySheet, propertysheet);
        lytPropertySheet.setVisible(false);
//        lytPropertySheet.setClassName("width40p");
        lytPropertySheet.setWidth("45%");
         
        lytMainContent.add(lytListTypes, lytListTypeItems, lytPropertySheet);
         
        add(lytMainContent);
    }

    private void buildListTypeItemsGrid() {
        // build the items grid     
        
        tblListTypeItems.setHeightFull();
        
        tblListTypeItems.addColumn(BusinessObjectLight::getName)
                .setHeader(String.format("%s %s", ts.getTranslatedString("module.listtypeman.listtypeitem"), ts.getTranslatedString("module.general.labels.name")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
       
        tblListTypeItems.addComponentColumn(item -> createListTypeItemActionGrid(item));
        
        tblListTypeItems.addItemClickListener(ev -> {
            currentListTypeItem = ev.getItem();
            updatePropertySheet(); 
            lytPropertySheet.setVisible(true);
        });
    }

    private void updatePropertySheet() {
        try {
            BusinessObject aWholeListTypeItem = aem.getListTypeItem(currentListTypeItem.getClassName(), currentListTypeItem.getId());
            propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeListTypeItem, ts, aem, mem));
        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildListTypeGrid() throws InvalidArgumentException, MetadataObjectNotFoundException {
        // Build list type grid
        List<ClassMetadataLight> listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
        ListDataProvider<ClassMetadataLight> dataProvider = new ListDataProvider<>(listTypes);
        tblListTypes.setDataProvider(dataProvider);
        tblListTypes.setHeightFull();
        tblListTypes.addColumn(ClassMetadataLight::getName)
                .setHeader(String.format("%s %s", ts.getTranslatedString("module.listtypeman.listtype"), ts.getTranslatedString("module.general.labels.name")))
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblListTypes.addItemClickListener(ev -> {
            try {
                propertysheet.clear();
                btnAddListTypeItemSec.setEnabled(true);
                lytPropertySheet.setVisible(false);
                currentListType = ev.getItem();
                lblCurrentListType.setText(currentListType.getName());
                loadListTypeItems(ev.getItem());
            } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
                
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
     * Create a new input field to filter list types in the header row.
     * @param dataProvider Data provider to filter.
     * @return The new input field filter.
     */
    private TextField createTxtFieldListTypeName(ListDataProvider<ClassMetadataLight> dataProvider) {
        TextField txtListTypeName = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtListTypeName.setValueChangeMode(ValueChangeMode.EAGER);
        txtListTypeName.setWidthFull();
        txtListTypeName.addValueChangeListener(event -> dataProvider.addFilter(
        project -> StringUtils.containsIgnoreCase(project.getName(),
                txtListTypeName.getValue())));
        return txtListTypeName;
    }

    private HorizontalLayout createListTypeItemActionGrid(BusinessObjectLight listTypeItem) {
        HorizontalLayout lyt; 
        
        Button btnUsages = new Button(ts.getTranslatedString("module.listtypeman.uses"),
                new Icon(VaadinIcon.SPLIT), ev -> {
            try {
                List<BusinessObjectLight> listTypeItemUses = aem.getListTypeItemUses(listTypeItem.getClassName(), listTypeItem.getId(), -1);
                
                if (listTypeItemUses.isEmpty()) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.information"), ts.getTranslatedString("module.listtypeman.listtypeitem-not-used")).open();
                } else {
                    Dialog dlgListTypeItemUses = new Dialog();
                    Grid<BusinessObjectLight> tblListTypeItemUses = new Grid<>();
                    tblListTypeItemUses.setClassName("width500px");
                    tblListTypeItemUses.setItems(listTypeItemUses);
                    tblListTypeItemUses.addColumn(BusinessObjectLight::getName).setHeader(ts.getTranslatedString("module.general.labels.name"));
                    tblListTypeItemUses.addColumn(BusinessObjectLight::getClassName).setHeader(ts.getTranslatedString("module.general.labels.class-name"));
                    
                    VerticalLayout lytContent = new VerticalLayout(new H4(ts.getTranslatedString("module.listtypeman.listtypeitem-references")),
                            tblListTypeItemUses, new Button(ts.getTranslatedString("module.general.messages.ok"), event -> {
                                dlgListTypeItemUses.close();
                            }));                  
                    lytContent.setAlignItems(Alignment.CENTER);
                    dlgListTypeItemUses.add(lytContent);
                    dlgListTypeItemUses.open();
                }
            } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
                });
                        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH), ev -> {
            this.deleteListTypeItemVisualAction.getVisualComponent(new ModuleActionParameterSet(
                                                                        new ModuleActionParameter("listTypeItem", listTypeItem))).open();
        });
        lyt = new HorizontalLayout(btnUsages, btnDelete);
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
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));

                bem.updateObject(currentListTypeItem.getClassName(), currentListTypeItem.getId(), attributes);

                loadListTypeItems(currentListType);               
                tblListTypeItems.select(currentListTypeItem);

                updatePropertySheet();

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                | OperationNotPermittedException | InvalidArgumentException ex) {
            Logger.getLogger(ListTypeManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.listtypeman.title");
    }
}
