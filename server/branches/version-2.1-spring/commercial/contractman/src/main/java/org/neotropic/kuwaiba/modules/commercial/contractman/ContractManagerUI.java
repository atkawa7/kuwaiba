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
package org.neotropic.kuwaiba.modules.commercial.contractman;

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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.DeleteContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.DeleteContractsPoolVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.NewContractVisualAction;
import org.neotropic.kuwaiba.modules.commercial.contractman.actions.NewContractsPoolVisualAction;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyFactory;
import org.neotropic.kuwaiba.visualization.api.properties.PropertyValueConverter;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.PropertySheet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Contract Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "contractman", layout = ContractManagerLayout.class) 
public class ContractManagerUI extends VerticalLayout implements ActionCompletedListener,
        PropertySheet.IPropertyValueChangedListener, HasDynamicTitle {
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
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager .
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The grid with the list pool
     */
    private final Grid<Pool> tblPools;
    /**
     * The grid with the list pool
     */
    private final Grid<BusinessObjectLight> tblContracts;
    /**
     * Layout of contracts pool
     */
    VerticalLayout lytPools;
    /**
     * Layout of contracts
     */
    VerticalLayout lytContracts;
    /**
     * Split the content
     */
    SplitLayout splitLayout;
    /**
     * Type of pool module root. These pools are used in models and are the root of such model
     */
    public static final int POOL_TYPE_MODULE_ROOT = 2;
    /**
     * Pool items limit. -1 To return all
     */
    public static final int LIMIT = -1;
    /**
     * Object to save the selected pool
     */
    private Pool currentPool;
    /**
     * Object to save the selected contract
     */
    private BusinessObjectLight currentContract;
    /**
     * Layout of property sheet
     */
    VerticalLayout lytPropertySheet;
    PropertySheet propertysheet;
    /**
     * Button used to create a new pool
     */
    Button btnAddPool;
    /**
     * The visual action to create a new contracts pool
     */
    @Autowired
    private NewContractsPoolVisualAction newContractsPoolVisualAction;
    /**
     * Button used to delete a pool
     */
    Button btnDeletePool;
    /**
     * The visual action to delete a contracts pool
     */
    @Autowired
    private DeleteContractsPoolVisualAction deleteContractsPoolVisualAction;    
    /**
     * Button used to delete a contract
     */
    Button btnDeleteContract;
    /**
     * The visual action to delete a contract
     */
    @Autowired
    private DeleteContractVisualAction deleteContractVisualAction;
    /**
     * Button used to create a new contract
     */
    Button btnAddContract;
    /**
     * Button used to create a new contract with the pool preselected
     */    
    Button btnAddContractSec;
    /**
     * The visual action to delete a contract
     */
    @Autowired
    private NewContractVisualAction newContractVisualAction;
    /**
     * Button used to show more information about a contract
     */    
    Button btnInfo;
    /**
     * Boolean used to update properties 
     */
    Boolean isPool = false;
    /**
     * Object to save pool name preselected
     */
    H4 headerPoolName;
    
    public ContractManagerUI() {
        super();
        setSizeFull();
        tblPools = new Grid<>();
        tblContracts = new Grid<>();
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.INFO, ts).open();
            if (currentPool != null) {
                loadPools();
                loadContracts(currentPool);
            } else
                loadPools();
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();            
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.contractman.title")));

        createContent();
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
        this.newContractsPoolVisualAction.unregisterListener(this);
        this.deleteContractsPoolVisualAction.unregisterListener(this);
        this.deleteContractVisualAction.unregisterListener(this);
        this.deleteContractVisualAction.unregisterListener(this);
    }

    private void createContent() {
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        lytMainContent.setMargin(false);
        lytMainContent.setPadding(false);
        
        this.newContractsPoolVisualAction.registerActionCompletedLister(this);
        this.deleteContractsPoolVisualAction.registerActionCompletedLister(this);
        this.newContractVisualAction.registerActionCompletedLister(this);
        this.deleteContractVisualAction.registerActionCompletedLister(this);
        
        btnAddPool = new Button(this.newContractsPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
            (event) -> {
                this.newContractsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
            });
        btnAddPool.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.pool.new-pool.description"));
        btnAddPool.setClassName("icon-button");
        
        Command deletePool = () -> {
          currentPool = null;  
          lytContracts.setVisible(false);
          lytPropertySheet.setVisible(false);
          btnDeletePool.setEnabled(false);
          tblPools.getDataProvider().refreshAll();
        };
        btnDeletePool = new Button(this.deleteContractsPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
           (event) -> {
                this.deleteContractsPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("pool", currentPool),
                        new ModuleActionParameter("commandClose", deletePool)
                )).open();
           });
        btnDeletePool.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.pool.delete-pool.description"));
        btnDeletePool.setEnabled(false);
        btnDeletePool.setClassName("icon-button");
        
        btnAddContract = new Button(this.newContractVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                   this.newContractVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        btnAddContract.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.contract.new-contract.description"));
        btnAddContract.setClassName("icon-button");
        
        btnAddContractSec = new Button(this.newContractVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                   this.newContractVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("pool", currentPool))).open();
                });
        btnAddContractSec.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.contract.new-contract.description"));
        btnAddContractSec.setEnabled(false);
        btnAddContractSec.setClassName("align-self-end");
        btnAddContractSec.addClassName("icon-button");
         
        buildPoolGrid();
        H4 headerPools = new H4(ts.getTranslatedString("module.contractman.pool.header"));
        headerPools.setClassName("header-position");
        lytPools = new VerticalLayout(headerPools, tblPools, btnAddContract, btnAddPool, btnDeletePool);
        lytPools.setWidth("25%");
        lytPools.setPadding(false);
        lytPools.setMargin(false);
        lytPools.setSpacing(false);
                       
        buildContractsGrid();
        headerPoolName = new H4();
        headerPoolName.setClassName("header-position");
        H4 headerContract = new H4(ts.getTranslatedString("module.contractman.contract.header"));
        headerContract.setClassName("header-position");
        HorizontalLayout lytHeaders = new HorizontalLayout(headerContract, headerPoolName);
        lytHeaders.setClassName("header-layout-position");
        lytHeaders.setMargin(false);
        lytHeaders.setPadding(false);
        lytContracts = new VerticalLayout(lytHeaders, btnAddContractSec, tblContracts);
        lytContracts.setPadding(false);
        lytContracts.setMargin(false);
        lytContracts.setSpacing(false);
        lytContracts.setWidth("30%");
        lytContracts.setHeightFull();
        lytContracts.setVisible(false);
        
        propertysheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertysheet.addPropertyValueChangedListener(this);
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        headerPropertySheet.setClassName("header");
        lytPropertySheet = new VerticalLayout(headerPropertySheet, propertysheet);
        lytPropertySheet.setWidth("45%");
        lytPropertySheet.setVisible(false);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setSpacing(false);
        
        lytMainContent.add(lytPools, lytContracts, lytPropertySheet);
        add(lytMainContent);
    }
    
    private void loadPools() {
        try {
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICCONTRACT, POOL_TYPE_MODULE_ROOT, true);
            tblPools.setItems(listPool);
            tblPools.getDataProvider().refreshAll();
            if (listPool.isEmpty())
                btnAddContract.setEnabled(false);
            else
                btnAddContract.setEnabled(true);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildPoolGrid() {
        try {
            List<Pool> listPool = bem.getRootPools(Constants.CLASS_GENERICCONTRACT, POOL_TYPE_MODULE_ROOT, true);
            ListDataProvider<Pool> dataProvider = new ListDataProvider<>(listPool);
            tblPools.setDataProvider(dataProvider);
            tblPools.setHeightFull();
            tblPools.addColumn(Pool::getName)
                    .setKey(ts.getTranslatedString("module.general.labels.name"));
            if (listPool.isEmpty())
                btnAddContract.setEnabled(false);
            else 
                btnAddContract.setEnabled(true);
            tblPools.addItemClickListener(event -> {
                btnDeletePool.setEnabled(true);
                lytContracts.setVisible(true);
                btnAddContractSec.setEnabled(true);
                currentPool = event.getItem();
                loadContracts(event.getItem());
                headerPoolName.setText(currentPool.getName());
                updatePropertySheet(currentPool);
                lytPropertySheet.setVisible(true);
            });

            // Filter Pool by Name
            HeaderRow filterRow = tblPools.appendHeaderRow();
            TextField txtPoolName = createTxtFieldPoolName(dataProvider);
            filterRow.getCell(tblPools.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtPoolName);
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * create a new input field to pool in the header row
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldPoolName(ListDataProvider<Pool> dataProvider) {
        Icon iconSearch = VaadinIcon.SEARCH.create();
        iconSearch.setSize("16px");
        
        TextField txtPoolName = new TextField();
        txtPoolName.setPlaceholder(ts.getTranslatedString("module.general.labels.filter-placeholder"));
        txtPoolName.setClassName("search");
        txtPoolName.setValueChangeMode(ValueChangeMode.EAGER);
        txtPoolName.setWidthFull();
        txtPoolName.setSuffixComponent(iconSearch);
        txtPoolName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtPoolName.getValue())));
        return txtPoolName;
    }

    private void loadContracts(Pool pool) {
        try {
            List<BusinessObjectLight> listContracts = bem.getPoolItems(pool.getId(), LIMIT);
            tblContracts.setItems(listContracts);
            tblContracts.getDataProvider().refreshAll();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildContractsGrid() {
        tblContracts.setHeightFull();
        tblContracts.addColumn(BusinessObjectLight::getName)
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        tblContracts.addComponentColumn(this::createContractActionGrid);
        tblContracts.addItemClickListener(event -> {
            currentContract = event.getItem();
            try {
                updatePropertySheet(currentContract);
            } catch (BusinessObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InventoryException ex) {
                Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private void updatePropertySheet(Pool pool) {
        try {
            Pool aWholePool = bem.getPool(pool.getId());
            propertysheet.setItems(PropertyFactory.propertiesFromPool(aWholePool, ts));
            isPool = true;
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updatePropertySheet(BusinessObjectLight contract) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, InventoryException {
        BusinessObject aWholeContract = bem.getObject(contract.getClassName(), contract.getId());
        propertysheet.setItems(PropertyFactory.propertiesFromBusinessObject(aWholeContract, ts, aem, mem)); 
        isPool = false;
    }

    /**
     * Update properties for pools and contracts.
     * @param property 
     */
    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object>  property) {
        if(isPool == true) {
            try {
                if (property.getName().equals(Constants.PROPERTY_NAME)) {
                    aem.setPoolProperties(currentPool.getId(), String.valueOf(property.getValue()), currentPool.getDescription());
                    currentPool.setName(String.valueOf(property.getValue()));
                    headerPoolName.setText(String.valueOf(property.getValue()));
                } else if (property.getDescription().equals(Constants.PROPERTY_DESCRIPTION)) {
                    aem.setPoolProperties(currentPool.getId(), currentPool.getName(), String.valueOf(property.getValue()));
                    currentPool.setDescription(String.valueOf(property.getValue()));
                }
                tblPools.select(currentPool);
                updatePropertySheet(currentPool);
                loadPools();
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (Exception ex) {
                Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), PropertyValueConverter.getAsStringToPersist(property));
                bem.updateObject(currentContract.getClassName(), currentContract.getId(), attributes);
                if (property.getName().equals(Constants.PROPERTY_NAME))
                    currentContract.setName(String.valueOf(property.getValue()));
                loadContracts(currentPool);
                tblContracts.select(currentContract);
                updatePropertySheet(currentContract);
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update"),
                        AbstractNotification.NotificationType.INFO, ts).open();
            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | OperationNotPermittedException | InvalidArgumentException ex) {
                Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InventoryException ex) {
                Logger.getLogger(ContractManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
     
    public HorizontalLayout createContractActionGrid(BusinessObjectLight contract) {
        HorizontalLayout lytActions;
        
        btnInfo = new Button(new Icon(VaadinIcon.INFO_CIRCLE));
        btnInfo.addClickListener(event -> {
           InfoDialog infoDialog = new InfoDialog(currentPool, contract); 
        });
        btnInfo.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.contract.show-info"));
        
        Command deleteContract = () -> {
          tblContracts.getDataProvider().refreshAll(); 
          updatePropertySheet(currentPool);
        };
        btnDeleteContract = new Button(new Icon(VaadinIcon.TRASH),
          (event) -> {
                this.deleteContractVisualAction.getVisualComponent(new ModuleActionParameterSet(
                        new ModuleActionParameter("contract", contract),
                        new ModuleActionParameter("commandClose", deleteContract)
                )).open();
          });
        btnDeleteContract.getElement().setProperty("title", ts.getTranslatedString("module.contractman.actions.contract.delete-contract.name"));
        
        lytActions = new HorizontalLayout(btnDeleteContract, btnInfo);
        lytActions.setAlignItems(Alignment.END);
        lytActions.setClassName("action");
        lytActions.setSpacing(false);
        return lytActions;
    }
    
    private class InfoDialog extends Dialog {
        private InfoDialog(Pool pool, BusinessObjectLight contract) {
            H4 headerInfo = new H4(String.format("%s %s", ts.getTranslatedString("module.contractman.contract.label.info-header"), contract.getName()));
            headerInfo.setClassName("header");
            Label lblId = new Label(String.format("%s: %s", ts.getTranslatedString("module.contractman.contract.label.info-id"), contract.getId()));
            Label lblClass = new Label(String.format("%s: %s", ts.getTranslatedString("module.contractman.contract.label.info-class"), pool.getClassName()));
            Label lblPath = new Label(String.format("%s: %s", ts.getTranslatedString("module.contractman.contract.label.info-path"), pool.getName()));
            
            Dialog wdwInfoDialog = new Dialog();
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), new Icon(VaadinIcon.CLOSE_SMALL));
            btnClose.setAutofocus(true);
            btnClose.setClassName("icon-button");
            btnClose.addClickListener(event -> {
                wdwInfoDialog.close();
            });
            
            VerticalLayout lytDialog = new VerticalLayout(headerInfo, lblId, lblClass, lblPath);
            VerticalLayout lytButton = new VerticalLayout(btnClose);
            lytButton.setAlignItems(Alignment.END);
            
            wdwInfoDialog.add(lytDialog, lytButton);
            wdwInfoDialog.setWidth("30%");
            wdwInfoDialog.setHeight("30%");
            wdwInfoDialog.open();
        }
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.contractman.title");
    }

}