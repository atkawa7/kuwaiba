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

import com.neotropic.flow.component.paper.toggle.PaperToggleButton;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualActionToo;
import org.neotropic.kuwaiba.modules.core.navigation.icons.BasicBusinessObjectIconGenerator;
import org.neotropic.kuwaiba.modules.core.navigation.navtree.nodes.InventoryObjectNode;
import org.neotropic.kuwaiba.visualization.api.resources.ComplexGrid;
import org.neotropic.kuwaiba.visualization.api.resources.GridFilter;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.modules.core.search.BusinessObjectSearchResultRenderer;
import org.neotropic.kuwaiba.modules.core.search.NavDashboardFactory;
import org.neotropic.kuwaiba.modules.core.search.SearchResultCallback;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Route(value = "navman", layout = NavigationLayout.class)
public class NavUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle {
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualActionToo actNewObj;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DeleteBusinessObjectVisualAction actDeleteObj;
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
     * 
     */
    @Autowired
    private NavDashboardFactory navDashboardFactory;
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
    //Gui
    /**
     * Holds both the Locations parents and the device parent
     */
    private HorizontalLayout lytParents;
    /**
     * The bread scrums for location
     */
    private HorizontalLayout lytLocationParents;
    /**
     * The bread scrums in device
     */
    private HorizontalLayout lytDeviceParents;
    /**
     * Contains the three columns
     */
    private HorizontalLayout lytNav;
    /**
     * RigthColumn
     */
    private VerticalLayout lytLocation;
    /**
     * Center column
     */
    private VerticalLayout lytNetwork;
    /**
     * Holds the name of the selected object and the option: show only ports 
     */
    private HorizontalLayout lytHeaderNetwork;
    /**
     * Left Column
     */
    private VerticalLayout lytPropertys;
    /**
     * The main search text input in the header
     */
    private TextField txtSearch;
    /**
     * The search bar in the header
     */
    private VerticalLayout lytSearch;
    /**
     * The last searched text, it is used to update
     */
    private String lastSearchText;
    /**
     * To keep track of the grids that are been shown after a search
     */
    private HashMap<String, ComplexGrid> currentGridResults;
    /**
     * Last set of results after a search, result grouped by class name
     */
    private HashMap<String, List<BusinessObjectLight>> currentSearchedResults;

    private int currentSkip;
    private int currentLimit;
    private int currentCount;
    private boolean isOnlyPorts;
    private Button btnNext;
    private Button btnBack;
    private Span lblPage;
    private HorizontalLayout lytPagination;
    
    @Override
    public void onAttach(AttachEvent ev) {
        setPadding(false);
        setMargin(false);
        setSizeFull();

        getUI().ifPresent(ui -> ui.getPage().setTitle(ts.getTranslatedString("module.navigation.title")));
       
        setupLayouts();
        setupSearchBar();
        
        this.currentSkip = 0;
        this.currentLimit = 10;
        this.currentCount = 0;
        this.isOnlyPorts = false;
        this.currentSearchedResults = new HashMap<>();
        this.currentGridResults = new HashMap<>();
        this.actNewObj.registerActionCompletedLister(this);
        this.actDeleteObj.registerActionCompletedLister(this);
    }

    @Override
    public void onDetach(DetachEvent ev) {
        this.actNewObj.unregisterListener(this);
        this.actDeleteObj.unregisterListener(this);
    }

    @Override
    public void actionCompleted(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            //We update the UI if we are deleting we must remove the object(s) from the current list of results
            if(ev.getActionResponse().containsKey(ActionResponse.Action.REMOVE_OBJECT)){
                BusinessObjectLight obj = (BusinessObjectLight)ev.getActionResponse().get(ActionResponse.Action.REMOVE_OBJECT);
                if(currentGridResults.get(obj.getClassName()) != null && currentSearchedResults.get(obj.getClassName()).contains(obj))
                    currentGridResults.get(obj.getClassName()).getDataProvider().refreshAll();
            }// if we are creating we must add he object(s) to the current list of results
            else if(ev.getActionResponse().containsKey(NewBusinessObjectAction.PARAM_PARENT_OID) && 
                    ev.getActionResponse().containsKey(NewBusinessObjectAction.PARAM_PARENT_CLASS_NAME))
            {
                String className = (String)ev.getActionResponse().get(NewBusinessObjectAction.PARAM_CLASS_NAME);
                if(currentGridResults.get(className) != null)
                        currentGridResults.get(className).getDataProvider().refreshAll();
            }
            //TODO if we are updating attributes we must refresh of results
            new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        } else 
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage(), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
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
            lytSearch.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
            
            btnNext = new Button(">");
            btnBack = new Button("<");
            lblPage = new Span("1");
            btnNext.addClickListener(e -> {
                int nextSkip = currentSkip + currentLimit;
                btnBack.setEnabled(true);
                processResults(lastSearchText, nextSkip, currentLimit);
            });

            btnBack.addClickListener(e -> {
                int backSkip = currentSkip - currentLimit;
                if(backSkip < 0)
                    btnBack.setEnabled(false);
                else{
                    processResults(lastSearchText, backSkip, currentLimit);
                    btnNext.setEnabled(currentCount == currentLimit);
                }
            });
            
            btnBack.setEnabled(false);
            lytPagination = new HorizontalLayout(btnBack, lblPage, btnNext);
            lytPagination.setDefaultVerticalComponentAlignment(Alignment.CENTER);
            lytPagination.setVisible(false);
        }
        //Main column that contains the three columns
        if(lytNav == null){
            
            lytNav = new HorizontalLayout();
            lytNav.setSpacing(false);
            lytNav.setMargin(false);
            lytNav.setPadding(false);
            lytNav.setSizeFull();
        }
        //The rigth colum
        if(lytLocation == null){
            lytLocation = new VerticalLayout();
            lytLocation.setMaxWidth("30%");
        }
        //the first row of the rigth column
        if(lytParents == null){
            lytLocationParents = new HorizontalLayout();
            lytLocationParents.setSpacing(false);
            lytLocationParents.setMargin(false);
            lytLocationParents.setPadding(false);
            
            lytDeviceParents= new HorizontalLayout();
            lytDeviceParents.setSpacing(false);
            lytDeviceParents.setMargin(false);
            lytDeviceParents.setPadding(false);
            
            lytParents = new HorizontalLayout(lytLocationParents, lytDeviceParents);
            lytParents.setSpacing(true);
            lytParents.setMargin(false);
            lytParents.setPadding(false);
            lytParents.setMinHeight("15px");
            lytParents.setWidth("100%");
        }
        //Center column
        if(lytNetwork == null){
            this.lytNetwork = new VerticalLayout();
            this.lytHeaderNetwork = new HorizontalLayout();
            this.lytHeaderNetwork.setSpacing(true);
            this.lytHeaderNetwork.setMargin(false);
            this.lytHeaderNetwork.setPadding(false);

            this.lytNetwork.setMaxWidth("40%");
            this.lytNetwork.add(new Span("..."));
        }
        //the main content 
        this.lytContent = new VerticalLayout();
        this.lytContent.setSizeFull();
        this.lytContent.setSpacing(false);
        this.lytContent.setMargin(false);
        this.lytContent.setPadding(false);

        lytNav.add(lytLocation);
        lytNav.add(lytNetwork);
        //lytNav.add(lytPropertys);
        lytContent.add(lytSearch, lytNav);
        
        add(lytContent);
    }
    
    /**
     * Setups the search bar
     */
    private void setupSearchBar(){
        Icon searchIcon = new Icon(VaadinIcon.SEARCH);
        searchIcon.setSize("16px");
        txtSearch = new TextField();
        txtSearch.setClassName("search-box-large");
        txtSearch.setPlaceholder(ts.getTranslatedString("module.general.messages.search"));
        txtSearch.setSuffixComponent(searchIcon);
        txtSearch.setClearButtonVisible(true);
        txtSearch.setTabIndex(0);
        txtSearch.setWidth("520px");
        txtSearch.setValueChangeMode(ValueChangeMode.EAGER);

        lytSearch.add(txtSearch);
        lytSearch.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER,
                txtSearch);
        lytSearch.add(lytParents);
        lytPagination.setVisible(false);
        lytSearch.add(lytPagination);

        txtSearch.addKeyPressListener(event -> {
            if (event.getKey().getKeys().get(0).equals(Key.ENTER.getKeys().get(0))) //Weirdly enough, event.getKey().equals(Key.Enter) ALWAYS returns fals
                processResults(txtSearch.getValue(), 0, 10);
        });
    }
     
    /**
     * Creates/updates the localization path, that shows the whole list 
     * of the parents  of the selected object in the tree
     * @param selectedItemParents the selected object in the location tree
     * @param kind the kind of bread crumbs if is location or device
     */
    private Div createBreadCrumbs(List<BusinessObjectLight> selectedItemParents, int kind){
        Div divPowerline = new Div();
        divPowerline.setWidthFull();
        divPowerline.setClassName("powerline");
        
        Collections.reverse(selectedItemParents);
        selectedItemParents.forEach(parent -> {
            Span span = new Span(new Label(parent.getClassName().equals(Constants.DUMMY_ROOT) ? "/" : parent.getName()));
            span.setSizeUndefined();
            span.setTitle(String.format("[%s]", parent.getClassName()));
            span.addClassNames("powerline-component", kind == 1 ? "dracula-cyan" : "dracula-orange");
            divPowerline.add(span);
        });
        
        return divPowerline;
    }
    
    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.navigation.title");
    }
    
    public class NavNodeSearchResultCallback implements SearchResultCallback<BusinessObjectLight> {
        @Override
        public Component buildSearchResultDetailsPage(BusinessObjectLight searchResult) {
            return navDashboardFactory.build(searchResult);
        }
    }
     
    /**
     * After a search the searched text is process to create a result of 
     * business objects grouped by class name in grids
     * @param searchedText the searched text
     * @param skip the skip for pagination 
     * @param limit the limit for pagination
     */
    private void processResults(String searchedText, int skip, int limit) {
        try {
            currentGridResults = new HashMap<>(); //we must clean what its been shown
            HashMap<String, List<BusinessObjectLight>> searchResults = new HashMap<>();
            //if the search has changed we must execute the query again
            if(!searchedText.equals(lastSearchText)){
                currentSkip = 0;
                searchResults = bem.getSuggestedObjectsWithFilterGroupedByClassName(searchedText, skip, limit);
            }
            else if(skip != currentSkip || currentLimit != limit){
                searchResults = bem.getSuggestedObjectsWithFilterGroupedByClassName(lastSearchText, skip, limit);
                currentCount = searchResults.keySet().size();
            }
            if (searchResults.isEmpty())
                lytLocation.add(new Label(ts.getTranslatedString("module.general.messages.no-search-results")));
            else {
                if(!currentSearchedResults.equals(searchResults) || currentSearchedResults != null && currentGridResults.isEmpty()){
                    lytPagination.setVisible(true);
                    currentSearchedResults = searchResults;
                    if(currentCount < 10 && skip > 0)
                        btnNext.setEnabled(false);
                    else if(skip == 0)
                            btnNext.setEnabled(true);
                }
                lastSearchText = searchedText;
                currentSkip = skip;
                Component results = createGridsResults();
                lytLocation.removeAll();
                lytLocation.add(results);
                int page = (currentSkip + currentLimit)/currentLimit;
                lblPage.setText(Integer.toString(page));
            }
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getLocalizedMessage(),
                    AbstractNotification.NotificationType.ERROR, ts).open();
        }
    }
   
    /**
     * Creates the grids with the results
     * @return an accordion with a grid for every set of classes
     */
    private Component createGridsResults(){
        Accordion acrClasses = new Accordion();
        acrClasses.setWidth("100%");
        
        currentSearchedResults.entrySet().forEach(entry -> {
            String className = entry.getKey();
        
            ComplexGrid<BusinessObjectLight> grid = new ComplexGrid<>(bem, className, lastSearchText);
            grid.addItemClickListener(t -> {
                try{
                    lytLocationParents.removeAll();
                    List<BusinessObjectLight> selectedItemParents = bem.getParents(t.getItem().getClassName(), t.getItem().getId());
                    lytLocationParents.add(createBreadCrumbs(selectedItemParents, 1));
                    
                    lytNetwork.removeAll();
                    TreeGrid<InventoryObjectNode> navTree = createNavigationTree(t.getItem());
                    lytNetwork.add(navTree);
                } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                }
            });
            //uncommnet to avoid actions 
            grid.setFirstColumn(grid.addColumn(new BusinessObjectSearchResultRenderer(
                    actionRegistry.getActionsForModule(NavigationModule.MODULE_ID),
                    new NavNodeSearchResultCallback(),
                    new BasicBusinessObjectIconGenerator(resourceFactory)
            )));
            
            grid.setPageSize(10);// Sets the max number of items to be rendered on the grid for each page, only works for the after 50 elements
            currentGridResults.put(className, grid);
            grid.createDataProviderPaginateGrid();
            grid.createPaginateGridFilter();
            grid.setMaxHeight("570px");
            acrClasses.add(className + " (" + currentSearchedResults.get(className).size() + ") ", grid);
        });
        return acrClasses;
    }
    
    /**
     * Creates the navigation tree when a item is selected in the grid classes
     * @param obj the selected business object 
     * @return the navigation tree a vaadin tree grid 
     * @throws InvalidArgumentException if something is wrong 
     * @throws MetadataObjectNotFoundException if the parent class is not found when the parent's bread crumbs is been created for the selected item in the navigation tree
     * @throws BusinessObjectNotFoundException if the object is not found when the parent's bread crumbs is been created for the selected item in the navigation tree
     */
    private TreeGrid createNavigationTree(BusinessObjectLight obj) throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException{
        TreeGrid<InventoryObjectNode> navTree = new TreeGrid<>();
        Grid.Column<InventoryObjectNode> column = navTree.addComponentHierarchyColumn(item -> createNode(item));
        navTree.addThemeVariants(GridVariant.LUMO_NO_BORDER, 
                GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_COMPACT);

        navTree.setSelectionMode(Grid.SelectionMode.SINGLE);
        navTree.addItemClickListener(e -> {
            try {
                lytDeviceParents.removeAll();
                List<BusinessObjectLight> parents = bem.getParentsUntilFirstOfClass(e.getItem().getObject().getClassName(),
                        e.getItem().getObject().getId(), obj.getClassName());
                lytDeviceParents.add(createBreadCrumbs(parents, 2)); 
            } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                Logger.getLogger(NavUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        HeaderRow headerRow = navTree.prependHeaderRow();
        PaperToggleButton btn = new PaperToggleButton("Only Show Ports");
        btn.addValueChangeListener(e ->{
            isOnlyPorts = e.getValue();
            try {
                navTree.setDataProvider(getDataProviderFiltered(obj.getId(), obj.getClassName()));
            } catch (InvalidArgumentException | MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(NavUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        headerRow.getCell(column).setComponent(new HorizontalLayout(new Label(obj.getName()), btn));
        navTree.setDataProvider(getDataProviderFiltered(obj.getId(), obj.getClassName()));
        
        return navTree;
    }
    
    /**
     * Creates a node in the navigation tree
     * @param obj the object that will be the node
     * @return the business object as node
     */
    private Component createNode(InventoryObjectNode obj){
        BasicBusinessObjectIconGenerator iconGenerator = new BasicBusinessObjectIconGenerator(resourceFactory);
        Div divTitle = new Div(new Label(obj.getObject().toString()));
        divTitle.setClassName("search-result-title");
        divTitle.setWidthFull();
        
        Image objIcon = new Image(StreamResourceRegistry.getURI(iconGenerator.apply(obj.getObject())).toString(), "-");
        objIcon.setHeight(Integer.toString(ResourceFactory.DEFAULT_SMALL_ICON_HEIGHT) + "px");
        objIcon.setWidth(Integer.toString(ResourceFactory.DEFAULT_SMALL_ICON_WIDTH) + "px");
        
        HorizontalLayout lytTitle = new HorizontalLayout(objIcon, divTitle);

        lytTitle.setBoxSizing(BoxSizing.BORDER_BOX);  
        lytTitle.setSpacing(true);
        lytTitle.setMargin(false);
        lytTitle.setPadding(false);
        lytTitle.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);
        
        return lytTitle;
    }
    
    /**
     * Creates the data provider from a given class to filter
     * @param parentId the relative root id of the navigation tree
     * @param parentClassName the relative root className of the navigation tree
     * @return a filtered data provider
     */
    private HierarchicalDataProvider getDataProviderFiltered(String parentId, String parentClassName)
            throws InvalidArgumentException, MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        List<BusinessObjectLight> children;
        if(!isOnlyPorts)
            children = bem.getObjectChildren(parentClassName, parentId, 0, 0);
        else
            children = bem.getChildrenOfClassLightRecursive(parentId, parentClassName, Constants.CLASS_GENERICPORT, -1);

        List<InventoryObjectNode> inventoryNodes = new ArrayList<>();
        children.forEach(object -> { inventoryNodes.add(new InventoryObjectNode(object)); });
       
        return new AbstractBackEndHierarchicalDataProvider<InventoryObjectNode, Void>() {

            @Override
            public int getChildCount(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        
                        return (int) bem.getObjectChildrenCount(object.getClassName(), object.getId());
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        return 0;
                    }
                } else
                    return children.size();
            }
            
            @Override
            protected Stream<InventoryObjectNode> fetchChildrenFromBackEnd(HierarchicalQuery<InventoryObjectNode, Void> query) {
                InventoryObjectNode parent = query.getParent();
                if (parent != null) {
                    BusinessObjectLight object = parent.getObject();
                    try {
                        List<BusinessObjectLight> children = bem.getObjectChildren(
                                object.getClassName(), object.getId(), query.getOffset(), query.getLimit());
                        List<InventoryObjectNode> nodes = new ArrayList();
                        children.forEach(child -> {
                            nodes.add(new InventoryObjectNode(child));
                        });
                        return nodes.stream();
                    } catch (InvalidArgumentException ex) {
                        new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"),
                                ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                        return new ArrayList().stream();
                    }
                } else
                    return inventoryNodes.stream();
            }

            @Override
            public boolean hasChildren(InventoryObjectNode node) {
                try {
                    List<BusinessObjectLight> children = bem.getObjectChildren(node.getClassName(), node.getObject().getId(), 0, 0);
                    return children.size() > 0;
                } catch (InvalidArgumentException ex) {
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.error"),
                            ex.getMessage(), AbstractNotification.NotificationType.ERROR, ts).open();
                    return false;
                }
            }
        };
    }
}
