/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.nodes;

import java.util.ArrayList;
import com.vaadin.data.util.BeanItem;
import com.vaadin.server.Page;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.web.gui.nodes.properties.NodeProperty;
import org.kuwaiba.apis.web.gui.nodes.properties.Sheet;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.apis.web.gui.wrappers.LocalObjectListItem;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 * A node that represents a business domain object from the model.
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 * @param <T> The type of the business object
 */
public abstract class AbstractNode<T> {
    /**
     * Business object behind this node (model)
     */
    protected T object;
    /**
     * Node's displayName. If null, the toString method of the business object will be used
     */
    protected String displayName;
    /**
     * Reference to the tree containing this node
     */
    protected DynamicTree tree;

    public AbstractNode() {}
    
    public AbstractNode(T object) {
        this.object = object;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Object getObject() {
        return object;
    }
    
    /**
     * This method relates and adds this object to a tree 
     * @param tree The tree.
     */
    public void setTree(DynamicTree tree) {
        this.tree = tree;
        this.tree.addItem(this);
    }

    public DynamicTree getTree() {
        return tree;
    }
    
    /**
     * What to do when expanding the node is requested. Always check if the tree has been set!
     */
    public abstract void expand();
    /**
     * What to do when collapsing the node is requested. Always check if the tree has been set!
     */
    public void collapse() {
        if (getTree() == null)
            return;
        
        List<AbstractNode> nodesToRemove = getChildren(this, new ArrayList<>());
        
        if (!nodesToRemove.isEmpty())
            for (AbstractNode node : nodesToRemove) 
                getTree().removeItem(node);
    }
    
    /**
     * Deletes the node and its children
     */
    public void delete() {
        List<AbstractNode> nodesToRemove = getChildren(this, new ArrayList<>());
        
        if(!nodesToRemove.isEmpty())
            for (AbstractNode node : nodesToRemove) 
                getTree().removeItem(node);
                
        tree.removeItem(this);
    }
    
    public List<AbstractNode> getChildren(AbstractNode node, List<AbstractNode> nodes){
        Collection<AbstractNode> children = (Collection<AbstractNode>) getTree().getChildren(node);
        if(children != null){
            for (AbstractNode child : children) {
                Collection<AbstractNode> subChildren = (Collection<AbstractNode>) getTree().getChildren(child);
                if(subChildren != null){
                    nodes.add(child);
                    nodes.addAll(getChildren(child, nodes));
                }
                else
                    nodes.add(child);
            }
        }
        return nodes;
    }
        
    /**
     * Actions associated to this node
     * @return An array of actions
     */
    public abstract AbstractAction[] getActions();
    
    /**
     * What to do when commanded to refresh the node.
     * @param recursive Refresh the children nodes.
     */
    public abstract void refresh(boolean recursive);
    
    /**
     * Adds a child node
     * @param node 
     */
    public void add(AbstractNode node) {
        tree.addItem(node);
        tree.setParent(node, this);
    }
    
    /**
     * Removes a node
     * @param node 
     */
    public void remove(AbstractNode node) {
        tree.removeItem(node);
    }
    
    @Override
    public String toString() {
        return displayName == null ? object.toString() : displayName;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj instanceof AbstractNode) 
            return object.equals(((AbstractNode)obj).getObject());
        else
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.object);
        return hash;
    }
    
    public Sheet createPropertySheet(){
        try {
            RemoteObject remoteObject = getTree().getTopComponent().getWsBean().getObject(
                    ((RemoteObjectLight)getObject()).getClassName(),
                    ((RemoteObjectLight)getObject()).getOid(),
                    Page.getCurrent().getWebBrowser().getAddress(), 
                     getTree().getTopComponent().getApplicationSession().getSessionId());
            
            BeanItem<RemoteObject> beanItem = new BeanItem<> (remoteObject);
            ObjectNodePropertyChangeValueListener valueListener = new ObjectNodePropertyChangeValueListener(getTree().getTopComponent(), beanItem, getTree().getTopComponent().getEventBus());
            
            Sheet sheet = new Sheet(beanItem, valueListener);
            
            ClassInfo meta = getTree().getTopComponent().getWsBean().getClass(remoteObject.getClassName(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    getTree().getTopComponent().getApplicationSession().getSessionId());
            
            String[] classAttributes = meta.getAttributeNames();
            int i = 0;
            //Arrays.sort(classAttributes);
            for (String classAttribute : classAttributes) {
                if(meta.getAttributesIsVisible()[i]){
                    int k = 0;
                    for (String attribute : remoteObject.getAttributes()) {
                        if (attribute.equals(classAttribute))
                            break;
                        k++;
                    }
                    String attributeValue = "";
                    if(k < remoteObject.getAttributes().length)
                        attributeValue = remoteObject.getValues()[k][0];
                   
                    AttributeMetadata a =  new AttributeMetadata();
                    String[] attributeTypes = meta.getAttributeTypes();
                    
                    switch (Util.getMappingFromType(attributeTypes[i])) {
                        case AttributeMetadata.MAPPING_TIMESTAMP:
                        case AttributeMetadata.MAPPING_DATE:
                            sheet.createDateProperty(classAttribute, meta.getAttributesDescription()[i], new Date(Long.valueOf(attributeValue)), i);
                            break;
                        case AttributeMetadata.MAPPING_PRIMITIVE:
                            sheet.createPrimitiveField(classAttribute,  meta.getAttributesDescription()[i], attributeValue, meta.getAttributeTypes()[i], i);
                            break;
                        case AttributeMetadata.MAPPING_MANYTOONE:
                            List<RemoteObjectLight> listTypeItems = getTree().getTopComponent().getWsBean().getListTypeItems(meta.getAttributeTypes()[i], 
                                Page.getCurrent().getWebBrowser().getAddress(),
                                getTree().getTopComponent().getApplicationSession().getSessionId()
                            );
                            
                            List<LocalObjectListItem> localListTypeItems = new ArrayList<>();
                            for (RemoteObjectLight listTypeItem : listTypeItems) {
                                localListTypeItems.add(new LocalObjectListItem(
                                        listTypeItem.getOid(),
                                        listTypeItem.getClassName(), 
                                        listTypeItem.getName()));
                            }
                            
                            LocalObjectListItem actualItem = null;
                            
                            for (LocalObjectListItem listTypeItem : localListTypeItems) {
                                if(!attributeValue.isEmpty()){
                                    if(listTypeItem.getOid() == Long.valueOf(attributeValue))
                                        actualItem = listTypeItem;
                                }
                            }
                            sheet.createListTypeField(classAttribute,  meta.getAttributesDescription()[i], localListTypeItems, actualItem, i);
                            break;
                        default:
                            NotificationsUtil.showError("Mapping not supported");
                    }        
                }
                i++;
            }//end for
            sheet.setPageLength(sheet.size());
            return sheet;
            
         } catch (ServerSideException ex) {
            Logger.getLogger(NodeProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
