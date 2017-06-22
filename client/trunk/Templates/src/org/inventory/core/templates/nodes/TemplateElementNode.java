/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.nodes;

import java.awt.Color;
import org.inventory.core.templates.nodes.properties.DateTypeProperty;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.templates.nodes.actions.TemplateActionsFactory;
import org.inventory.core.templates.nodes.properties.ListTypeProperty;
import org.inventory.core.templates.nodes.properties.PrimitiveTypeProperty;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.actions.CopyAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a template element.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TemplateElementNode extends AbstractNode implements PropertyChangeListener {

    private static final Image defaultIcon = Utils.createRectangleIcon(Utils.DEFAULT_ICON_COLOR, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);
    
    protected Image icon = defaultIcon;
    
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    public TemplateElementNode(LocalObjectLight object) {
        super(new TemplateElementChildren(), Lookups.singleton(object));
        setDisplayName(object.toString());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {TemplateActionsFactory.getCreateTemplateElementAction(), 
                             TemplateActionsFactory.getCreateTemplateElementSpecialAction(),
                             null, 
                             TemplateActionsFactory.getDeleteTemplateElementAction(),
                             null,
                             CopyAction.get(CopyAction.class),
                             PasteAction.get(PasteAction.class)};
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return icon;
    }

    @Override
    public Image getIcon(int type) {
        return icon;
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        LocalObjectLight currentObject = getLookup().lookup(LocalObjectLight.class);
        
        LocalClassMetadata classmetadata = com.getMetaForClass(currentObject.getClassName(), false);
        if (classmetadata == null) 
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            LocalObject templateElement = com.getTemplateElement(currentObject.getClassName(), currentObject.getOid());            
            if (templateElement == null) 
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else {
                Sheet.Set generalSet = Sheet.createPropertiesSet();
                templateElement.addPropertyChangeListener(WeakListeners.propertyChange(this, templateElement));
                for (LocalAttributeMetadata attributeMetadata : classmetadata.getAttributes()) {
                    PropertySupport property = null;
                    if (!attributeMetadata.isUnique()) { //Unique attributes are not shown
                                                        //as they're not intended to be copied    
                        switch (attributeMetadata.getMapping()) {
                            case Constants.MAPPING_DATE:
                            case Constants.MAPPING_TIMESTAMP:
                                property = new DateTypeProperty(attributeMetadata.getName(),
                                        attributeMetadata.getDisplayName(), attributeMetadata.getDescription(), templateElement);
                                break;
                            case Constants.MAPPING_PRIMITIVE:
                                property = new PrimitiveTypeProperty(attributeMetadata.getName(), attributeMetadata.getType(),
                                        attributeMetadata.getDisplayName(), attributeMetadata.getDescription(), templateElement);
                                break;
                            case Constants.MAPPING_MANYTOONE: //List type
                                List<LocalObjectListItem> list = com.getList(attributeMetadata.getListAttributeClassName(), true, false);
                                if (list == null)
                                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                                else
                                    property = new ListTypeProperty(attributeMetadata.getName(), attributeMetadata.getDisplayName(), 
                                            attributeMetadata.getDescription(), list, templateElement);
                                break;
                            default:
                                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.WARNING_MESSAGE, "Unique and binary attributes are ignored to avoid redundancies");
                        } //Do note that binary
                        if (property != null)
                            generalSet.put(property);
                    }
                }
                sheet.put(generalSet);
            }
        }
        return sheet;
    }

    @Override
    public boolean canRename() {
        return true;
    }
    
    @Override
    public boolean canCopy() {
        return true;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public void setName(String s) {
        getLookup().lookup(LocalObjectLight.class).setName(s);
        propertyChange(new PropertyChangeEvent(getLookup().lookup(LocalObjectLight.class), Constants.PROPERTY_NAME, s, s));

        if (getSheet() != null)
            setSheet(createSheet());
    }
    
    @Override
    public Transferable clipboardCopy() throws IOException {
        Transferable deflt = super.clipboardCopy();
        ExTransferable added = ExTransferable.create(deflt);
        added.put(new ExTransferable.Single(LocalObjectLight.DATA_FLAVOR) {
            @Override
            protected LocalObjectLight getData() {
                return getLookup().lookup(LocalObjectLight.class);
            }
        });
        return added;
    }
    
    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        //From the transferable we figure out if it comes from a copy or a cut operation
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY, -1);
        //It's also possible to define many paste types (like "normal paste" and "special paste")
        //by adding more entries to the list. Those will appear as options in the context menu
        if (paste != null) {
            s.add(paste);
        }
    }
    
    @Override
    public Transferable drag() throws IOException {
        return getLookup().lookup(LocalObjectLight.class);
    }

    @Override
    public PasteType getDropType(Transferable obj, final int action, int index) {
        Node dropNode = NodeTransfer.node(obj, NodeTransfer.DND_COPY);
        final LocalObjectLight currentObject = getLookup().lookup(LocalObjectLight.class);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain an ObjectNode
        if (!TemplateElementNode.class.isInstance(dropNode))
            return null;
        
        final LocalObjectLight incomingObject = dropNode.getLookup().lookup(LocalObjectLight.class);
        final TemplateElementNode incomingNode = (TemplateElementNode) dropNode;
        
        //Ignore those noisy attempts to move it to itself
        if (incomingObject.equals(currentObject))
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                try {
                    Object currentNode = Utilities.actionsGlobalContext().lookup(TemplateElementNode.class);
                                        
                    if (currentNode == null)
                        currentNode = Utilities.actionsGlobalContext().lookup(TemplateSpecialElementNode.class);
                    
                    if (incomingNode instanceof TemplateSpecialElementNode) {
                        boolean canMoveSpecialElement = false;
                        //Check if the current object can contain the drop node
                        List<LocalClassMetadataLight> possibleSpecialChildren = CommunicationsStub.getInstance().getPossibleSpecialChildren(currentObject.getClassName(), false);

                        for (LocalClassMetadataLight lcml : possibleSpecialChildren) {
                            if (lcml.getClassName().equals(incomingObject.getClassName())) {
                                canMoveSpecialElement = true;
                                break;
                            }
                        }
                    
                        if (canMoveSpecialElement) {
                            List<String> classNames = new ArrayList<>();
                            List<Long> ids = new ArrayList<>();

                            classNames.add(incomingObject.getClassName());
                            ids.add(incomingObject.getOid());

                            List<LocalObjectLight> copiedNodes = CommunicationsStub.getInstance().
                                    copyTemplateSpecialElements(classNames, ids, currentObject.getClassName(), currentObject.getOid());

                            if (copiedNodes != null) {
                                if (getChildren() instanceof AbstractChildren)
                                    ((AbstractChildren)getChildren()).addNotify();
                            } else 
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());

                        } else 
                            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE,
                                String.format("An special child instance of %s can't be moved into a %s %sinstance", 
                                    incomingObject.getClassName(), currentObject.getClassName(), 
                                    currentNode instanceof TemplateSpecialElementNode ? " special child " : ""));
                    } else {
                        boolean canMoveElement = false;
                        //Check if the current object can contain the drop node
                        List<LocalClassMetadataLight> possibleChildren = CommunicationsStub.getInstance().getPossibleChildren(currentObject.getClassName(), false);

                        for (LocalClassMetadataLight lcml : possibleChildren) {
                            if (lcml.getClassName().equals(incomingObject.getClassName())) {
                                canMoveElement = true;
                                break;
                            }
                        }
                    
                        if (canMoveElement) {
                            List<String> classNames = new ArrayList<>();
                            List<Long> ids = new ArrayList<>();

                            classNames.add(incomingObject.getClassName());
                            ids.add(incomingObject.getOid());

                            List<LocalObjectLight> copiedNodes = CommunicationsStub.getInstance().
                                    copyTemplateElements(classNames, ids, currentObject.getClassName(), currentObject.getOid());

                            if (copiedNodes != null) {
                                if (getChildren() instanceof AbstractChildren)
                                    ((AbstractChildren)getChildren()).addNotify();
                            } else 
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());

                        } else 
                            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE,
                                    String.format("An instance of %s can't be moved into a %s %sinstance",incomingObject.getClassName(), currentObject.getClassName(), 
                                        currentNode instanceof TemplateSpecialElementNode ? " special child " : ""));
                    }
                } catch (Exception ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                return null;
            }
        };
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LocalObjectLight affectedObject = (LocalObjectLight)evt.getSource();
        if (!com.updateTemplateElement(affectedObject.getClassName(), affectedObject.getOid(),
            new String[] {evt.getPropertyName()}, 
            new String[] {evt.getNewValue() == null ? null : (evt.getNewValue() instanceof LocalObjectListItem ? String.valueOf(((LocalObjectListItem)evt.getNewValue()).getId()) : String.valueOf(evt.getNewValue())) }))
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
                setDisplayName(affectedObject.toString());
        }
    }
    
    public static class TemplateElementChildren extends AbstractChildren {
        private List<LocalObjectLight> templateElementSpecialChildren;
        
        @Override
        public void addNotify() {
            LocalObjectLight templateElement = getNode().getLookup().lookup(LocalObjectLight.class);
            List<LocalObjectLight> templateElementChildren = CommunicationsStub.getInstance().
                    getTemplateElementChildren(templateElement.getClassName(), templateElement.getOid());
            
            templateElementSpecialChildren = CommunicationsStub.getInstance().
                    getTemplateSpecialElementChildren(templateElement.getClassName(), templateElement.getOid());
            
            if (templateElementChildren == null && templateElementSpecialChildren == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else {                
                List<LocalObjectLight> children = new ArrayList();
                children.addAll(templateElementChildren);
                children.addAll(templateElementSpecialChildren);
                Collections.sort(children);
                setKeys(children);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight t) {
            if (templateElementSpecialChildren == null)
                return new Node[0];
            
            if (templateElementSpecialChildren.contains(t))
                return new Node[] {new TemplateSpecialElementNode(t)};
            else
                return new Node[] {new TemplateElementNode(t)};
        }
    }
}