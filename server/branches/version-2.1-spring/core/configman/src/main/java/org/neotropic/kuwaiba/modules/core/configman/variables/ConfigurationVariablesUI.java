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
package org.neotropic.kuwaiba.modules.core.configman.variables;

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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.configman.ConfigurationManagerLayout;
import org.neotropic.kuwaiba.modules.core.configman.variables.actions.DeleteConfigurationVariableVisualAction;
import org.neotropic.kuwaiba.modules.core.configman.variables.actions.DeleteConfigurationVariablesPoolVisualAction;
import org.neotropic.kuwaiba.modules.core.configman.variables.actions.NewConfigurationVariableVisualAction;
import org.neotropic.kuwaiba.modules.core.configman.variables.actions.NewConfigurationVariablesPoolVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.properties.PropertyFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Configuration Variables Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "configman", layout = ConfigurationManagerLayout.class)
public class ConfigurationVariablesUI extends VerticalLayout implements ActionCompletedListener, PropertySheet.IPropertyValueChangedListener {  
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
     * The visual action to create a new configuration variables pool
     */
    @Autowired
    private NewConfigurationVariablesPoolVisualAction newConfigurationVariablesPoolVisualAction;
    /**
     * The visual action to delete a configuration variables pool
     */
    @Autowired
    private DeleteConfigurationVariablesPoolVisualAction deleteConfigurationVariablesPoolVisualAction;
    /**
     * The visual action to create a new configuration variable
     */
    @Autowired
    private NewConfigurationVariableVisualAction newConfigurationVariableVisualAction;
    /**
     * The visual action to delete a configuration variable
     */
    @Autowired
    private DeleteConfigurationVariableVisualAction deleteConfigurationVariableVisualAction;
    /**
     * The grid with the configuration variables Pool
     */
    private final Grid<Pool> tblConfigVariablesPool;
    /**
     * The grid with the configuration variables
     */
    private final Grid<ConfigurationVariable> tblConfigVariables;
    /**
     * Object to save the selected configuration variables pool
     */
    private Pool currentConfigVariablesPool;
    /**
     * Object to save the selected configuration variable
     */
    private ConfigurationVariable currentConfigVariable;
    /**
     * Header current configuration variables pool
     */
    private H4 headerCurrentConfigVariablesPool;
    /**
     * Button used to create a new configuration variables pool
     */
    Button btnAddConfigurationVariablePool;
    /**
     * Button used to delete a configuration variables pool
     */
    Button btnDeleteConfigurationVariablesPool; 
    /**
     * Button used to create a new configuration variable
     */
    Button btnAddConfigurationVariable;
    /**
     * Button used to create a new configuration variable with the pool preselected
     */
    Button btnAddConfigVariablesSec;
    /**
     * Layout of configuration variables pool
     */
    VerticalLayout lytConfigurationVariablesPool;
    /**
     * Layout of configuration variables
     */
    VerticalLayout lytConfigurationVariables;
    /**
     * Layout of property sheet
     */
    VerticalLayout lytPropertySheet;
    
    PropertySheet propertysheet;
    
    public ConfigurationVariablesUI() {
        super();
        setSizeFull();
        tblConfigVariablesPool = new Grid<>();
        tblConfigVariables = new Grid<>();
    }

    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);
        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.configvarman.title")));
        
        try {
            createContent();
        } catch (InvalidArgumentException  ex) {
            Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.newConfigurationVariablesPoolVisualAction.unregisterListener(this);
        this.deleteConfigurationVariablesPoolVisualAction.unregisterListener(this);
        this.newConfigurationVariableVisualAction.unregisterListener(this);
        this.deleteConfigurationVariableVisualAction.unregisterListener(this);
    }

    private void createContent() throws InvalidArgumentException { 
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
        lytMainContent.setMargin(false);
        lytMainContent.setPadding(false);
        
        this.newConfigurationVariablesPoolVisualAction.registerActionCompletedLister(this);
        this.deleteConfigurationVariablesPoolVisualAction.registerActionCompletedLister(this);
        this.newConfigurationVariableVisualAction.registerActionCompletedLister(this);
        this.deleteConfigurationVariableVisualAction.registerActionCompletedLister(this);
 
        btnAddConfigurationVariablePool = new Button(this.newConfigurationVariablesPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newConfigurationVariablesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        
        btnAddConfigurationVariablePool.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.actions.new-configuration-variable-pool.description"));

        btnDeleteConfigurationVariablesPool = new Button(this.deleteConfigurationVariablesPoolVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteConfigurationVariablesPoolVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("configurationVariablePool", currentConfigVariablesPool))).open();
                });
        
        btnDeleteConfigurationVariablesPool.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.actions.delete-configuration-variable-pool.description"));
        btnDeleteConfigurationVariablesPool.setEnabled(false);
        
        btnAddConfigurationVariable = new Button(this.newConfigurationVariableVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newConfigurationVariableVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        
        btnAddConfigurationVariable.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.actions.new-configuration-variable.description"));

        btnAddConfigVariablesSec = new Button(this.newConfigurationVariableVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newConfigurationVariableVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("configurationVariable", currentConfigVariablesPool))).open();
                });
        
        btnAddConfigVariablesSec.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.actions.new-configuration-variable-sec.description"));
        btnAddConfigVariablesSec.setEnabled(false);
        btnAddConfigVariablesSec.setClassName("align-self-end");
        
        lytConfigurationVariablesPool = new VerticalLayout(tblConfigVariablesPool, btnAddConfigurationVariable, btnAddConfigurationVariablePool, btnDeleteConfigurationVariablesPool);
        lytConfigurationVariablesPool.setWidth("25%");
        lytConfigurationVariablesPool.setPadding(false);
        lytConfigurationVariablesPool.setSpacing(false);
        lytConfigurationVariablesPool.setMargin(false);
        buildConfigurationVariablesPoolGrid();
        
        headerCurrentConfigVariablesPool = new H4(); 
        H4 headerConfigurationVariables = new H4(ts.getTranslatedString("module.configvarman.configurationvariables"));
        HorizontalLayout lytConfigHeaders = new HorizontalLayout(headerCurrentConfigVariablesPool, headerConfigurationVariables);
        lytConfigurationVariables = new VerticalLayout(lytConfigHeaders, btnAddConfigVariablesSec, tblConfigVariables);
        lytConfigurationVariables.setWidth("30%");
        lytConfigurationVariables.setSpacing(false);
        lytConfigurationVariables.setMargin(false);
        lytConfigurationVariables.setPadding(false);
        lytConfigurationVariables.setVisible(false);
        buildConfigurationVariablesGrid();
        
        propertysheet = new PropertySheet(ts, new ArrayList<>(), "");
        propertysheet.addPropertyValueChangedListener(this);
        H4 headerPropertySheet = new H4(ts.getTranslatedString("module.propertysheet.labels.header"));
        lytPropertySheet = new VerticalLayout(headerPropertySheet, propertysheet);
        lytPropertySheet.setWidth("45%");
        lytPropertySheet.setVisible(false);
        lytPropertySheet.setMargin(false);
        lytPropertySheet.setPadding(false);
        lytPropertySheet.setSpacing(false);

        lytMainContent.add(lytConfigurationVariablesPool, lytConfigurationVariables, lytPropertySheet);
        add(lytMainContent);
    }
    
    private void buildConfigurationVariablesPoolGrid() throws UnsupportedOperationException { 
        // Build configuration variables pool grid
        List<Pool> configurationVariablesPool = aem.getConfigurationVariablesPools();
        ListDataProvider<Pool> dataProvider = new ListDataProvider<>(configurationVariablesPool);
        tblConfigVariablesPool.setDataProvider(dataProvider);
        tblConfigVariablesPool.setHeightFull();
        tblConfigVariablesPool.addColumn(Pool::getName)
                .setKey(ts.getTranslatedString("module.general.labels.name"));

        tblConfigVariablesPool.addItemClickListener(listener -> {
            try {
                propertysheet.clear();
                btnDeleteConfigurationVariablesPool.setEnabled(true);
                lytConfigurationVariables.setVisible(true);
                btnAddConfigVariablesSec.setEnabled(true);
                lytPropertySheet.setVisible(false);
                currentConfigVariablesPool = listener.getItem();
                headerCurrentConfigVariablesPool.setText(currentConfigVariablesPool.getName());
                loadConfigurationVariables(listener.getItem());
            } catch (ApplicationObjectNotFoundException | UnsupportedOperationException ex) {
                Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        // Filter Configuration Variable Pool by Name
        HeaderRow filterRow = tblConfigVariablesPool.appendHeaderRow();
        TextField txtPoolName = createTxtFieldPoolName(dataProvider);
        filterRow.getCell(tblConfigVariablesPool.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtPoolName);
    }

    private void buildConfigurationVariablesGrid() throws UnsupportedOperationException {
        // Build configuration variables grid
        tblConfigVariables.setHeightFull();
        tblConfigVariables.addColumn(ConfigurationVariable::getName)
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        tblConfigVariables.addComponentColumn(this::createConfigurationVariableActionGrid).setWidth("1%");

        tblConfigVariables.addItemClickListener(ev -> {
            currentConfigVariable = ev.getItem();
            updatePropertySheet();
            lytPropertySheet.setVisible(true);
        });
    }

    /**
     * create a new input field to configuration variables pool in the header row
     *
     * @param dataProvider data provider to filter
     * @return the new input field filter
     */
    private TextField createTxtFieldPoolName(ListDataProvider<Pool> dataProvider) {
        Icon icon = VaadinIcon.SEARCH.create();
        icon.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.label.filter-configuration-variable-pool"));
        icon.setSize("16px");
        
        TextField txtPoolName = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filterplaceholder"));
        txtPoolName.setValueChangeMode(ValueChangeMode.EAGER);
        txtPoolName.setWidthFull();
        txtPoolName.setSuffixComponent(icon);
        txtPoolName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(),
                        txtPoolName.getValue())));
        return txtPoolName;
    }
    
    private void loadConfigurationVariablesPools() {
        List<Pool> configurationVariablesPool = aem.getConfigurationVariablesPools();
        tblConfigVariablesPool.setItems(configurationVariablesPool);
        tblConfigVariablesPool.getDataProvider().refreshAll();
    }

    private void loadConfigurationVariables(Pool pool) throws UnsupportedOperationException, ApplicationObjectNotFoundException {
        List<ConfigurationVariable> configurationVariables = aem.getConfigurationVariablesInPool(pool.getId());
        tblConfigVariables.setItems(configurationVariables);
        tblConfigVariables.getDataProvider().refreshAll();
    }

    private void updatePropertySheet() {  
        try { 
            ConfigurationVariable aWholeConfigurationVariable = aem.getConfigurationVariable(currentConfigVariable.getName());
            propertysheet.setItems(PropertyFactory.propertiesFromConfigurationVariable(aWholeConfigurationVariable));
        } catch (UnsupportedOperationException | ApplicationObjectNotFoundException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
            Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
                    
                if(currentConfigVariablesPool != null){
                    loadConfigurationVariablesPools();
                    loadConfigurationVariables(currentConfigVariablesPool);
                } else
                    loadConfigurationVariablesPools();
               
            } catch (UnsupportedOperationException | ApplicationObjectNotFoundException ex) {
                Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    @Override
    public void updatePropertyChanged(AbstractProperty<? extends Object> property) {
        try{ 
            if(currentConfigVariable != null){
                
               aem.updateConfigurationVariable(currentConfigVariable.getName(), property.getName(), String.valueOf(property.getValue()));
               
               loadConfigurationVariables(currentConfigVariablesPool);
               tblConfigVariables.select(currentConfigVariable);
               
               updatePropertySheet();
               
               new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (UnsupportedOperationException | InvalidArgumentException | ApplicationObjectNotFoundException  ex) {
            Logger.getLogger(ConfigurationVariablesUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private HorizontalLayout createConfigurationVariableActionGrid(ConfigurationVariable configurationVariable) {
        HorizontalLayout lyt;
        
        Button btnDelete = new Button(new Icon(VaadinIcon.TRASH),
                (event) -> {
            this.deleteConfigurationVariableVisualAction.getVisualComponent(new ModuleActionParameterSet(
                    new ModuleActionParameter("configurationVariable", configurationVariable))).open();
        });
        btnDelete.getElement().setProperty("title", ts.getTranslatedString("module.configvarman.actions.delete-configuration-variable.description"));
        
        lyt = new HorizontalLayout(btnDelete);
        lyt.setDefaultVerticalComponentAlignment(Alignment.END);
        lyt.setSizeFull();
        
        return lyt;
    }

}
