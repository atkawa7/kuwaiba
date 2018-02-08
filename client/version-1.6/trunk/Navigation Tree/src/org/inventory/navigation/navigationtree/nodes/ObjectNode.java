/*
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.navigationtree.nodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionGroupActionsFactory;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.CreateBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.CreateBusinessObjectFromTemplateAction;
import org.inventory.navigation.navigationtree.nodes.actions.CreateMultipleBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.DeleteBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.EditObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.ExecuteClassLevelReportAction;
import org.inventory.navigation.navigationtree.nodes.actions.UpdateNodeAction;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.navigationtree.nodes.properties.DateTypeProperty;
import org.inventory.navigation.navigationtree.nodes.properties.ListTypeProperty;
import org.inventory.navigation.navigationtree.nodes.properties.NativeTypeProperty;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Represents a node within the navigation tree and perhaps other trees displaying inventory objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNode extends AbstractNode implements PropertyChangeListener {

    //There can be only one instance for OpenLocalExplorerAction, this attribute is a kind of singleton
    protected static OpenLocalExplorerAction explorerAction = new OpenLocalExplorerAction();
    protected CommunicationsStub com = CommunicationsStub.getInstance();
    protected Image icon;

    public ObjectNode(Children children, Lookup lookup) {
        super(children, lookup);
    }
    
    public ObjectNode(LocalObjectLight lol) {
        super(new ObjectChildren(), Lookups.singleton(lol));
        if (lol.getClassName() != null) {
            lol.addPropertyChangeListener(WeakListeners.propertyChange(this, lol));
            icon = com.getMetaForClass(lol.getClassName(), false).getSmallIcon();
            explorerAction.putValue(OpenLocalExplorerAction.NAME, "Open an Explorer from Here");
        }
    }

    public ObjectNode(LocalObjectLight lol, boolean isLeaf) {
        super(Children.LEAF, Lookups.singleton(lol));
        lol.addPropertyChangeListener(WeakListeners.propertyChange(this, lol));
        icon = com.getMetaForClass(lol.getClassName(), false).getSmallIcon();
        explorerAction.putValue(OpenLocalExplorerAction.NAME, "Open an Explorer from Here");
    }

    /**
     * Returns the wrapped object
     *
     * @return returns the related business object
     */
    public LocalObjectLight getObject() {
        return getLookup().lookup(LocalObjectLight.class);
    }

    @Override
    public String getDisplayName() {
        return getObject().toString();
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Set mandatoryPropertySet = Sheet.createPropertiesSet(); //Set with the mandatory attributes
        
        LocalObjectLight object = getObject();
        
        LocalClassMetadata meta = com.getMetaForClass(object.getClassName(), false);
        if (meta == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }
        
        LocalObject lo = com.getObjectInfo(object.getClassName(), object.getOid());
        
        if (lo == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            return sheet;
        }
        
        object.setName(lo.getName());
        
        for (LocalAttributeMetadata lam : meta.getAttributes()) {
            if (lam.isVisible()) {
                PropertySupport.ReadWrite property = null;
                int mapping = lam.getMapping();
                switch (mapping) {
                    case Constants.MAPPING_BINARY: //TODO: to do the mapping for binaries
                        continue;
                    case Constants.MAPPING_TIMESTAMP:
                    case Constants.MAPPING_DATE:
                        property = new DateTypeProperty((Date)lo.getAttribute(lam.getName()) , 
                                lam.getName(), Date.class, lam.getDisplayName(),
                                lam.getDescription(), this);
                        break;
                    case Constants.MAPPING_PRIMITIVE:
                        if (!lam.getType().equals(LocalObjectLight.class)) {
                            property = new NativeTypeProperty(
                                    lam.getName(),
                                    lam.getType(),
                                    lam.getDisplayName().isEmpty() ? lam.getName() : lam.getDisplayName(),
                                    lam.getDescription(), this, lo.getAttribute(lam.getName()));
                        }
                        break;
                    case Constants.MAPPING_MANYTOONE:
                        //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
                        List<LocalObjectListItem> list = com.getList(lam.getListAttributeClassName(), true, false);
                        if (list == null) {
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            return sheet;
                        }
                        LocalObjectListItem val = null;
                        if (lo.getAttribute(lam.getName()) == null) 
                            val = list.get(0); //None
                        else {
                            for (LocalObjectListItem loli : list) {
                                if (lo.getAttribute(lam.getName()).equals(loli)) {
                                    val = loli;
                                    break;
                                }
                            }
                        }
                        property = new ListTypeProperty(
                                lam.getName(),
                                lam.getDisplayName(),
                                lam.getDescription(),
                                list,
                                this,
                                val);
                        break;
                    default:
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, "Mapping not supported");
                        return sheet;
                }
                
                if (lam.isMandatory())
                    mandatoryPropertySet.put(property);
                else
                    generalPropertySet.put(property);
            }
        }
        
        mandatoryPropertySet.setDisplayName("Mandatory Attributes");
        mandatoryPropertySet.setName("mandatory");  //NOI18N
        generalPropertySet.setDisplayName("General Attributes");
        generalPropertySet.setName("general"); //NOI18N
        
        if (mandatoryPropertySet.getProperties().length != 0)
            sheet.put(mandatoryPropertySet);
        
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public void destroy() {
        getObject().removePropertyChangeListener(this);
    }

    public boolean refresh() {
         LocalObjectLight object = getObject();
        //Force to get the attributes again, but only if there's a property sheet already asigned
        if (getSheet() != null) 
            setSheet(createSheet());
        else
            //*****************
            //TODO: When fixing the listeners, change this as well, or you will lose the references to those listeners
            //*********
            object = com.getObjectInfoLight(object.getClassName(), object.getOid());
        
        if (object == null)
            return false;
        
        icon = (com.getMetaForClass(object.getClassName(), false)).getSmallIcon();
        fireIconChange();

        //Don't try to refresh the anything if the node is a leaf (used only in views)
        if (isLeaf())
            return true;
        
        //Update the children list
        if (getChildren() instanceof AbstractChildren)
            ((AbstractChildren) getChildren()).addNotify();
                
        return true;
    }
    
    //This method is called for the very first time when the first context menu is created, and
    //then called everytime
    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<>();
        LocalObjectLight object = getObject();
        
        actions.add(CreateBusinessObjectAction.getInstance(this));
        actions.add(CreateMultipleBusinessObjectAction.getInstance());
        actions.add(CreateBusinessObjectFromTemplateAction.getInstance());
        if (getParentNode() != null) {
            Action copyAction = SystemAction.get(CopyAction.class);
            copyAction.putValue(Action.NAME, I18N.gm("lbl_copy_action"));
                        
            Action cutAction = SystemAction.get(CutAction.class);
            cutAction.putValue(Action.NAME, I18N.gm("lbl_cut_action"));
                                    
            Action pasteAction = SystemAction.get(PasteAction.class);
            pasteAction.putValue(Action.NAME, I18N.gm("lbl_paste_action"));
            
            actions.add(copyAction);
            actions.add(cutAction);
            actions.add(pasteAction);
        }        
        actions.add(UpdateNodeAction.getInstance(this));
        actions.add(EditObjectAction.getInstance(this));
        
        actions.add(null); //Separator
        
        if (!com.hasCustomDeleteAction(getLookup().lookup(LocalObjectLight.class).getClassName())) {
            actions.add(SystemAction.get(DeleteBusinessObjectAction.class));
            actions.add(null); //Separator
        }
        
        actions.add(ExecuteClassLevelReportAction.getInstance());
                        
        for (GenericObjectNodeAction action : Lookup.getDefault().lookupAll(GenericObjectNodeAction.class)) {
            if (action.getClass().getAnnotation(ActionsGroupType.class) != null)
                continue;
                        
            if (action.appliesTo() != null) {
                for (String className : action.appliesTo()) {
                    if (CommunicationsStub.getInstance().isSubclassOf(object.getClassName(), className)) {
                        System.out.println("Nom is" + action.getClass().getName());
                        actions.add(action);
                        break;
                    }
                }
            } else
                actions.add(action);                
// Not used for now
//                else {
//                if (action.getValidators() != null) {
//                    for (String validator : action.getValidators()) {
//                        if (CommunicationsStub.getInstance().getMetaForClass(object.getClassName(), false).getValidator(validator) == 1) {
//                            actions.add(action);
//                            break;
//                        }
//                    }                                                
//                } else {
//                    actions.add(action);
//                }                
//            }
        }
        actions.add(ActionGroupActionsFactory.getInstanceOfOpenViewGroupActions());
        actions.add(ActionGroupActionsFactory.getInstanceOfRelateToGroupActions());
        actions.add(ActionGroupActionsFactory.getInstanceOfReleaseFromGroupActions());
        actions.add(ActionGroupActionsFactory.getInstanceMirrorPortActions());
        actions.add(ActionGroupActionsFactory.getInstanceDiagnosticActions());
        
        actions.add(null); //Separator
        actions.add(explorerAction);
        actions.add(ShowMoreInformationAction.getInstance(getObject().getOid(), getObject().getClassName()));
        return actions.toArray(new Action[]{});
    }

    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        //From the transferable we figure out if it comes from a copy or a cut operation
        PasteType paste = getDropType(t, NodeTransfer.node(t, NodeTransfer.CLIPBOARD_COPY) != null
                ? DnDConstants.ACTION_COPY : DnDConstants.ACTION_MOVE, -1);
        //It's also possible to define many paste types (like "normal paste" and "special paste")
        //by adding more entries to the list. Those will appear as options in the context menu
        if (paste != null) {
            s.add(paste);
        }
    }

    @Override
    public Transferable drag() throws IOException {
        return getObject();
    }

    //This method is called when the node is copied or cut
    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain an ObjectNode
        if (!ObjectNode.class.isInstance(dropNode))
            return null;
        
        LocalObjectLight object = getObject();
            
        //Ignore those noisy attempts to move it to itself
        if (dropNode.getLookup().lookup(LocalObjectLight.class).equals(object))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                boolean canMove = false;
                try {
                    LocalObjectLight obj = dropNode.getLookup().lookup(LocalObjectLight.class);
                    //Check if the current object can contain the drop node
                    List<LocalClassMetadataLight> possibleChildren = com.getPossibleChildren(getObject().getClassName(), false);
                    for (LocalClassMetadataLight lcml : possibleChildren) {
                        if (lcml.getClassName().equals(obj.getClassName()))
                            canMove = true;
                    }
                    if (canMove) {
                        if (action == DnDConstants.ACTION_COPY) {
                            LocalObjectLight[] copiedNodes = com.copyObjects(getObject().getClassName(), getObject().getOid(),
                                    new LocalObjectLight[]{obj});
                            if (copiedNodes != null) {
                                if (getChildren() instanceof AbstractChildren)
                                    ((AbstractChildren)getChildren()).addNotify();
                            } else 
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                        } else {
                            if (action == DnDConstants.ACTION_MOVE) {
                                if (com.moveObjects(getObject().getClassName(), getObject().getOid(), Arrays.asList(obj))) {
                                    //Refreshes the old parent node
                                    if (dropNode.getParentNode().getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)dropNode.getParentNode().getChildren()).addNotify();
                                    
                                    //Refreshes the new parent node
                                    if (getChildren() instanceof AbstractChildren)
                                        ((AbstractChildren)getChildren()).addNotify();
                                } else
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                            }
                        }
                    } else 
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE,
                                String.format("An instance of %s can't be moved into an instance of %s", obj.getClassName(), getObject().getClassName()));
                } catch (Exception ex) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                return null;
            }
        };
    }

    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public boolean canCut() {
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
    public Image getIcon(int i) {
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }

    @Override
    public void setName(String newName) {
        LocalObject update = new LocalObject(getObject().getClassName(), getObject().getOid(), new String[]{Constants.PROPERTY_NAME}, new Object[]{ newName });
        if (com.saveObject(update)) {
            getObject().setName(newName);
            if (getSheet() != null)
                setSheet(createSheet());
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());      
    }

    @Override
    public String getName() {
        return getObject().getName() == null ? "" : getObject().getName();
    }

    /**
     * The node listens for changes in the wrapped business object
     *
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(getObject())) {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) 
                fireNameChange(null, ""); //Weird, this should be fireDisplayNameChange, but it isn't
            
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectNode) {
                return ((ObjectNode) obj).getObject().equals(this.getObject());
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
