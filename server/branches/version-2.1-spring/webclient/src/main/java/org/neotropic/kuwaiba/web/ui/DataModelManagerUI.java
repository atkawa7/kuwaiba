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
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.kuwaiba.web.modules.core.datamodelman.nodes.DataModelNode;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.DeleteListTypeItemVisualAction;
import org.neotropic.kuwaiba.modules.core.listtypeman.actions.NewListTypeItemVisualAction;
import org.neotropic.util.visual.icons.BasicIconGenerator;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.properties.PropertySheet.IPropertyValueChangedListener;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.tree.BasicTree;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Data Model manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = "datamodelman", layout = MainLayout.class)
public class DataModelManagerUI extends VerticalLayout implements ActionCompletedListener, IPropertyValueChangedListener {

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

    public DataModelManagerUI() {
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
                                            
            } catch (Exception ex) {
                Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {
        
        HorizontalLayout lytMainContent = new HorizontalLayout();
        lytMainContent.setSizeFull();
         
        SplitLayout splitLayout = new SplitLayout();
        
        HierarchicalDataProvider dataProvider = new AbstractBackEndHierarchicalDataProvider<DataModelNode, Void>() {
            @Override
            protected Stream<DataModelNode> fetchChildrenFromBackEnd(HierarchicalQuery<DataModelNode, Void> query) {
                DataModelNode parent = query.getParent();
                if (parent != null) {
                    ClassMetadataLight object = parent.getObject();
                    try {
                        List<ClassMetadataLight> children = mem.getSubClassesLightNoRecursive(object.getName(), false, false);
                        List<DataModelNode> theChildren = new ArrayList();
                        for (ClassMetadataLight child : children)
                            theChildren.add(new DataModelNode(child, child.getName()));
                        return theChildren.stream();
                    } catch (MetadataObjectNotFoundException ex) {
                        Notification.show(ex.getMessage());
                        return new ArrayList().stream();
                    }
                } else {
                    return Arrays.asList(new DataModelNode(new ClassMetadata(-1, Constants.NODE_DUMMYROOT, "Root"), "")).stream();
                }
            }

            @Override
            public int getChildCount(HierarchicalQuery<DataModelNode, Void> query) {
                DataModelNode parent = query.getParent();
                if (parent != null) {
                    ClassMetadataLight object = parent.getObject();
                    try {
                        return (int) mem.getSubClassesCount(object.getName());
                    } catch (MetadataObjectNotFoundException ex) {
                        Notification.show(ex.getMessage());
                        return 0;
                    }
                    
                } else
                    return 1;
            }

            @Override
            public boolean hasChildren(DataModelNode node) {
                return true;
            }
        };
        
        BasicTree<DataModelNode> basicTree = new BasicTree(dataProvider, new BasicIconGenerator());
        basicTree.setSizeFull();
        
        splitLayout.addToPrimary(basicTree);
         
        lytMainContent.add(splitLayout);
         
        add(lytMainContent);
    }

   

    @Override
    public void updatePropertyChanged(AbstractProperty property) {
        try {
            if (currentListTypeItem != null) {
                
                HashMap<String, String> attributes = new HashMap<>();
                attributes.put(property.getName(), property.getAsStringToPersist());

                bem.updateObject(currentListTypeItem.getClassName(), currentListTypeItem.getId(), attributes);

                        
                tblListTypeItems.select(currentListTypeItem);

                

                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.general.messages.property-update")).open();
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException
                | OperationNotPermittedException | InvalidArgumentException ex) {
            Logger.getLogger(DataModelManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
