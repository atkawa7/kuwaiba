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
package org.neotropic.kuwaiba.modules.core.navigation;

import com.neotropic.flow.component.paperdialog.PaperToggleButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyPressEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.NavigationTree;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route(value = "navman", layout = NavLayout.class)
public class NavUI extends VerticalLayout implements ActionCompletedListener {
    /**
     * Reference to the action registry.
     */
    @Autowired
    private ActionRegistry actionRegistry;
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Factory to build resources from data source.
     */
    @Autowired
    private ResourceFactory resourceFactory;
    /**
     * Contains a list of default common filters
     */
    private HorizontalLayout lytFilters;
    /**
     * The actual content. Initially it is just a search box then it can become
     * a page displaying the service/customer details.
     */
    private VerticalLayout lytContent;
    /**
     * footer to dipslay detail information
     */
    private HorizontalLayout lytDetail;
    
    /**
     * Location filters
     */
    private List<Button> locationButtonsFilters;
    /**
     * Label to show parents path for location
     */
    private Label lblLocationParentsPath;
    //Gui
    private HorizontalLayout lytLocationButonsFilters;
    /**
     * Contains the three columns
     */
    private HorizontalLayout lytNav;
    /**
     * RigthColumn
     */
    private VerticalLayout lytLocation;
    private HorizontalLayout lytLocationPath;
    /**
     * Center column
     */
    private VerticalLayout lytNetwork;
    /**
     * Left Column
     */
    private VerticalLayout lytPropertys;
    /**
     * The search bar in the header
     */
    private VerticalLayout lytSearch;
    //
    private PaperToggleButton tgbOnlyPorts;
    
    private TextField txtSearch;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setPadding(false);
        setMargin(false);
        setSizeFull();

        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.navigation.title")));
       
        setupLayouts();
        setupSearchBar();
        setupLocationHeader();

    }

    @Override
    public void onDetach(DetachEvent ev) {

    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();
        } else {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
        }
    }

    
    /**
     * setUp the layouts 
     */
    private void setupLayouts(){
        //in the header after the nav layout
        if(lytSearch == null){
            lytSearch = new VerticalLayout();
            lytSearch.setSpacing(false);
            lytSearch.setMargin(false);
            lytSearch.setPadding(false);
            lytSearch.setBoxSizing(BoxSizing.BORDER_BOX);
            lytSearch.setId("nav-search-component");
            lytSearch.setWidth("100%");
        }
        //Main column that contains the three columns
        if(lytNav == null){
            lytNav = new HorizontalLayout();
            lytNav.setSpacing(false);
            lytNav.setMargin(false);
            lytNav.setPadding(false);
            lytNav.setSizeFull();
            lytNav.getStyle().set("background-color", "purple");
        }
        //The rigth colum
        if(lytLocation == null){
            lytLocation = new VerticalLayout();
            lytLocation.setMaxWidth("30%");
            lytLocation.getStyle().set("background-color", "green");
        }
        //the first row of the rigth column
        if(lytLocationButonsFilters == null){
            lytLocationButonsFilters = new HorizontalLayout();
            lytLocationButonsFilters.setHeight("25px");
            lytLocationButonsFilters.setWidth("100%");
        }
        //the second row in the rigth column
        if(lblLocationParentsPath == null){
            lytLocationPath = new HorizontalLayout();
            lytLocationPath.setSpacing(false);
            lytLocationPath.setMargin(false);
            lytLocationPath.setPadding(false);
            lytLocationPath.setHeight("15px");
            lytLocationPath.setWidth("100%");
        }
        
        //Center column
        if(lytNetwork == null){
            lytNetwork = new VerticalLayout();
            lytNetwork.getStyle().set("background-color", "yellow");
            lytNetwork.setMaxWidth("40%");
            lytNetwork.add(new Span("..."));
        }
        
        //Location filters and path 
        lblLocationParentsPath = new Label("Location path ...");
        locationButtonsFilters = new ArrayList<>();
        
        //the main content 
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        this.lytContent.setSpacing(false);
        this.lytContent.setMargin(false);
        this.lytContent.setPadding(false);

        lytNav.add(lytLocation);
        lytNav.add(lytNetwork);
        //lytNav.add(lytPropertys);
        this.lytContent.add(lytSearch, lytNav);
        add(this.lytContent);
    }
    
    /**
     * Setups the search bar
     */
    private void setupSearchBar(){
        if(txtSearch == null){
            Icon searchIcon = new Icon(VaadinIcon.SEARCH);
            searchIcon.setSize("20px");
            txtSearch = new TextField();
            txtSearch.setClassName("search-box-large");
            txtSearch.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
            txtSearch.setPrefixComponent(searchIcon);
            txtSearch.setClearButtonVisible(true);
            txtSearch.setTabIndex(0);
            txtSearch.setWidth("520px");
            
            lytSearch.add(txtSearch);

            txtSearch.addKeyPressListener(new ComponentEventListener<KeyPressEvent>() {
                @Override
                public void onComponentEvent(KeyPressEvent event) {
                    if (event.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) { //Weirdly enough, event.getKey().equals(Key.Enter) ALWAYS returns false
                        try {
                            List<BusinessObjectLight> searchResults = bem.getObjectsOfClassLight(txtSearch.getValue(), -1);

                            if (searchResults.isEmpty()) {
                                lytLocation.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
                            } else {
                                lytLocation.removeAll();
                                NavigationTree navTree = new NavigationTree(getDataProviderSeveral(searchResults), new BasicIconGenerator(resourceFactory));
                                lytLocation.add(navTree);
                            }
                        } catch (Exception ex) {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                        }
                    }
                }
            });
        }
    }
    
//    public void replaceContent(Component newContent) {
//        this.lytContent.removeAll();
//        this.lytContent.add(newContent);
//    }

    /**
     * Default data provider to load the whole navigation tree staring on root
     *
     * @return
     */
    private HierarchicalDataProvider getDataProvider() {
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(
                                object.getClassName(), object.getId(), query.getOffset(), query.getLimit());
                        List<InventoryObjectNode> nodes = new ArrayList();
                        for (BusinessObjectLight child : children) {
                            nodes.add(new InventoryObjectNode(child));
                        }
                        return nodes.stream();
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return new ArrayList().stream();
                    }
                } else {
                    return Arrays.asList(new InventoryObjectNode(
                            new BusinessObjectLight(Constants.DUMMY_ROOT, null, "Root")
                    )).stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
                    } catch (Exception ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return 0;
                    }
                } else {
                    return 1;
                }
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                return true;
            }
        };
    }

    /**
     * Creates the data provider from a given class to filter
     *
     * @param parentId the relative root id of the navigation tree
     * @param parentClassName the relative root className of the navigation tree
     * @return a filtered data provider
     */
    private HierarchicalDataProvider getDataProviderSeveral(List<BusinessObjectLight> roots)
            throws InvalidArgumentException {
        List<InventoryObjectNode> inventoryNodes = new ArrayList<>();
        roots.forEach(object -> {
            inventoryNodes.add(new InventoryObjectNode(object));
        });

        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {

            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(
                                object.getClassName(), object.getId(), query.getOffset(), query.getLimit());
                        List<InventoryObjectNode> nodes = new ArrayList();
                        for (BusinessObjectLight child : children) {
                            nodes.add(new InventoryObjectNode(child));
                        }
                        return nodes.stream();
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return new ArrayList().stream();
                    }
                } else {
                    return inventoryNodes.stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        //return (int) bem.getObjectsWithFilterLight(className, filterName, filterValue);
                        
                        
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
                    } catch (Exception ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return 0;
                    }
                } else {
                    return inventoryNodes.size();
                }
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                try {
                    List<BusinessObjectLight> children = bem.getObjectChildren(node.getClassName(), node.getObject().getId(), 0, 0);
                    return children.size() > 0;
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage()).open();
                    return false;
                }
            }
        };
    }

    /**
     * Creates the data provider from a given class to filter
     *
     * @param parentId the relative root id of the navigation tree
     * @param parentClassName the relative root className of the navigation tree
     * @return a filtered data provider
     */
    private HierarchicalDataProvider getDataProviderFiltered(String parentId, String parentClassName)
            throws InvalidArgumentException {
        List<BusinessObjectLight> children = bem.getObjectChildren(parentClassName, parentId, 0, 0);
        List<InventoryObjectNode> inventoryNodes = null;

        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {

            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(
                                object.getClassName(), object.getId(), query.getOffset(), query.getLimit());
                        List<InventoryObjectNode> nodes = new ArrayList();
                        for (BusinessObjectLight child : children) {
                            nodes.add(new InventoryObjectNode(child));
                        }
                        return nodes.stream();
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return new ArrayList().stream();
                    }
                } else {
                    inventoryNodes.stream();
                }
                inventoryNodes.stream();
                return null;
            }

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {

                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
                    } catch (Exception ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage()).open();
                        return 0;
                    }
                } else {
                    return 1;
                }
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                try {
                    List<BusinessObjectLight> children = bem.getObjectChildren(node.getClassName(), node.getObject().getId(), 0, 0);
                    return children.size() > 0;
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage()).open();
                    return false;
                }
            }
        };
    }

    /**
     * Create template data provider items (structure )and refresh items
     *
     * @param object; TemplateObjectLight; parent template
     */
    /*
    private void refreshStructure(TemplateObjectLight object) {
        //create fisrt call data
        List<TemplateObjectLight> allChildsTemplateLight = new ArrayList<>();
        allChildsTemplateLight.addAll(aem.getTemplateElementChildren(object.getClassName(), object.getId()));
        allChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(object.getClassName(), object.getId()));
        childsTemplateDataProvider = new AbstractBackEndHierarchicalDataProvider<TemplateObjectLight, Void>() {
            //count parent elements
            @Override
            public int getChildCount(HierarchicalQuery<TemplateObjectLight, Void> query) {
                if (query.getParent() != null) {
                    List<TemplateObjectLight> allInnerChildsTemplateLight = new ArrayList<>();
                    allInnerChildsTemplateLight.addAll(aem.getTemplateElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    allInnerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    return allInnerChildsTemplateLight.size();
                } else { //in case element set for fisrt time 
                    return allChildsTemplateLight.size();
                }
            }

            //load child elements    
            @Override
            public boolean hasChildren(TemplateObjectLight item) {
                List<TemplateObjectLight> innerChildsTemplateLight = new ArrayList<>();
                innerChildsTemplateLight.addAll(aem.getTemplateElementChildren(item.getClassName(), item.getId()));
                innerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(item.getClassName(), item.getId()));

                return innerChildsTemplateLight.size() > 0;
            }

            //return parent elements list
            @Override
            protected Stream<TemplateObjectLight> fetchChildrenFromBackEnd(HierarchicalQuery<TemplateObjectLight, Void> query) {
                //load normal and special children and gorus both to be display
                if (query.getParent() != null) {
                    List<TemplateObjectLight> allInnerChildsTemplateLight = new ArrayList<>();
                    allInnerChildsTemplateLight.addAll(aem.getTemplateElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    allInnerChildsTemplateLight.addAll(aem.getTemplateSpecialElementChildren(query.getParent().getClassName(), query.getParent().getId()));
                    return allInnerChildsTemplateLight.stream();
                } else {
                    return allChildsTemplateLight.stream();
                }
            }
        };
        tblChildsTemplate.setDataProvider(childsTemplateDataProvider);
        tblChildsTemplate.getDataProvider().refreshAll();
    }*/
//    private NavigationTree buildTree() {
//        NavigationTree navigationTree = 
//        navigationTree.setSizeFull();
//        return navigationTree;
//    }
    


    

    /**
     * Loads and creates the header of the tree for location
     * The levels to filter
     * ----
     * Location: Country, City, etc until the first father of GenericNetWorkElement
     * it could be Rack or Building
     * All children of ViewableObject
     * except: floor, room, slot - they will be show or hide filters
     * except: ConfigurationItem
     * ----
     * GenericNetworkElement
     * ----
     * Ports
     */
    private void setupLocationHeader(){
        //first we the list of classMetadata
        List<ClassMetadataLight> locationFilters = locationFilter(Constants.DUMMY_ROOT, new ArrayList<>());

        for(ClassMetadataLight classMetadata : locationFilters)
            locationButtonsFilters.add(new Button(classMetadata.getName()));
        
        for (Button button : locationButtonsFilters) {
            lytLocationButonsFilters.add(button);

            button.addClickListener(e -> {
                try {
                    String className = e.getSource().getText();
                    List<BusinessObjectLight> searchResults = bem.getObjectsOfClassLight(className, -1);
                    lytLocation.removeAll();
                    if (searchResults.isEmpty())
                        new SimpleNotification("", ts.getTranslatedString("module.general.messages.no-search-results")).open();
                    else {
                        NavigationTree localitationNavTree = new NavigationTree(getDataProviderSeveral(searchResults), new BasicIconGenerator(resourceFactory));
                        localitationNavTree.addSelectionListener(ei -> {
                            Set selectedItems = ei.getAllSelectedItems();
                            for (Object item : selectedItems) {
                                try {
                                    BusinessObjectLight obj = ((InventoryObjectNode)item).getObject();
                                    getLocalizationPath(obj, null);
                                    
                                    List<BusinessObjectLight> childrenOfClassLight = bem.getChildrenOfClassLightRecursive(obj.getId(), obj.getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 0);
                                    if (childrenOfClassLight.isEmpty()) {
                                        lytNetwork.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
                                    } else {
                                        lytNetwork.removeAll();
                                        NavigationTree navTree = new NavigationTree(getDataProviderSeveral(childrenOfClassLight), new BasicIconGenerator(resourceFactory));
                                        lytNetwork.add(navTree);
                                    }    
                                    break;
                                } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
                                    Logger.getLogger(NavUI.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });
                        lytLocation.add(localitationNavTree);
                    }
                } catch (Exception ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage()).open();
                }
            });
        }
            
        lytSearch.add(lytLocationButonsFilters);
    }
    
    private void setupDevices(){
        tgbOnlyPorts = new PaperToggleButton("Only Ports");
        tgbOnlyPorts.setClassName("green", true);
        //only port filter
        
        tgbOnlyPorts.addValueChangeListener(ef -> { if(ef.getValue()){ } });
    }
    
    /**
     * Creates/updates the localization path, that shows the whole list 
     * of the parents  of the selected object in the tree
     * @param selectedLocationObj the selected object in the location tree
     * @param selectedNetworkObj the selected object in the network tree
     */
    
private void getLocalizationPath(BusinessObjectLight selectedLocationObj,
            BusinessObjectLight selectedNetworkObj)
    {
        if(lblLocationParentsPath == null)
            lblLocationParentsPath = new Label();
        
//        List<BusinessObjectLight> selectedItemParents = bem.getParents(
//                selectedLocationObj.getClassName(), selectedLocationObj.getId());
//        Collections.reverse(parents);
//        lblLocationParentsPath.setText(parents.stream().map(obj -> obj.getName())
//				.collect(Collectors.joining(" >  ")));
    }
    
    //Helpers
    /**
     * Iterates over the containment hierarchy staring from the dummy root, 
     * it search for any class of geographical localization like Continent, 
     * Country, etc until city
     * @param parentClassName from where the search starts
     * @param locationPossibleChildren an empty
     * @return 
     */
    private List<ClassMetadataLight> locationFilter(String parentClassName, List<ClassMetadataLight> locationPossibleChildren) {
        try {
            List<ClassMetadataLight> possibleChildren;
            possibleChildren = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight child : possibleChildren) {
                //we stop searching for locations if we reach the GenericConfigurationItem class
                if (!mem.isSubclassOf(Constants.CLASS_VIEWABLEOBJECT, child.getName()) && mem.isSubclassOf(Constants.CLASS_CONFIGURATIONITEM, child.getName()))
                    return locationPossibleChildren;
                else if (!child.getName().equals(Constants.CLASS_SLOT) //we must avoid the slots
                        && !mem.isSubclassOf(Constants.CLASS_CONFIGURATIONITEM, child.getName()) //we also avoid the config item
                        && mem.isSubclassOf(Constants.CLASS_VIEWABLEOBJECT, child.getName()) //any other sub class of ViewableObject its ok
                        && !locationPossibleChildren.contains(child)) 
                {
                    locationPossibleChildren.add(child);
                    locationFilter(child.getName(), locationPossibleChildren);
                }//end else if
            }//end for
        } catch (MetadataObjectNotFoundException ex) {
            Logger.getLogger(NavUI.class.getName()).log(Level.SEVERE, "locationFilter", ex);
        }
        return locationPossibleChildren;
    }
}
