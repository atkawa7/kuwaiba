/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.navigation.applicationnodes.objectnodes;

import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListElementNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.CreateObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.EditObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.RefreshObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.RelateToServiceAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowRelatedServicesAction;
import org.inventory.navigation.applicationnodes.objectnodes.properties.ObjectNodeProperty;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Represents a node within the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNode extends AbstractNode implements PropertyChangeListener{

    public static final String GENERIC_ICON_PATH="org/inventory/navigation/applicationnodes/res/default.png";
    protected LocalObjectLight object;
    //There can be only one instance for OpenLocalExplorerAction, this attribute is a kind of singleton
    protected static OpenLocalExplorerAction explorerAction = new OpenLocalExplorerAction();

    protected CommunicationsStub com;

    protected CreateObjectAction createAction;
    protected DeleteObjectAction deleteAction;
    protected RefreshObjectAction refreshAction;
    protected EditObjectAction editAction;
    private RelateToServiceAction relateToServiceAction;
    private ShowRelatedServicesAction showRelatedServicesAction;

    protected Sheet sheet;
    protected Image icon;
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);

    public ObjectNode(LocalObjectLight _lol, boolean isLeaf){
        super(Children.LEAF, Lookups.singleton(_lol));
        this.object = _lol;
        this.object.addPropertyChangeListener(this);

        com = CommunicationsStub.getInstance();

        icon = (com.getMetaForClass(_lol.getClassName(),false)).getSmallIcon();

        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EXPLORE"));
        editAction = new EditObjectAction(this);
        deleteAction = new DeleteObjectAction(this);
        refreshAction = new RefreshObjectAction(this);
    }
    
    public ObjectNode(LocalObjectLight _lol){
        super(new ObjectChildren(), Lookups.singleton(_lol));
        this.object = _lol;
        this.object.addPropertyChangeListener(this);
        
        com = CommunicationsStub.getInstance();

        icon = (com.getMetaForClass(_lol.getClassName(),false)).getSmallIcon();
        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EXPLORE"));

        createAction = new CreateObjectAction(this);
        deleteAction = new DeleteObjectAction(this);
        editAction = new EditObjectAction(this);
        refreshAction = new RefreshObjectAction(this);
        if (object.getValidator("isRelatableToService")){
            relateToServiceAction = new RelateToServiceAction(object);
            showRelatedServicesAction = new ShowRelatedServicesAction(object);
        }
    }

    /*
     * Returns the wrapped object
     * @return returns the related business object
     */
    public LocalObjectLight getObject(){
        return this.object;
    }
    
    @Override
    public String getDisplayName(){
        String className = CommunicationsStub.getInstance().getMetaForClass(object.getClassName(),false).getDisplayName();
        return getEditableText() + " ["+(className==null?object.getClassName():className)+"]";
    }

    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        
        Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Set administrativePropertySet = Sheet.createPropertiesSet(); //Administrative attributes category

        LocalClassMetadata meta = com.getMetaForClass(object.getClassName(),false);
        if (meta == null){
            nu.showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return sheet;
        }

        LocalObject lo = null;
        if (object instanceof LocalObject)
            lo = (LocalObject)object;
        else
            lo = com.getObjectInfo(object.getClassName(), object.getOid());

        if (lo == null){
            nu.showSimplePopup("Error", NotificationUtil.ERROR, com.getError());
            return sheet;
        }

        for(LocalAttributeMetadata lam:meta.getAttributes()){
            if(lam.isVisible()){

                ObjectNodeProperty property = null;

                if (lam.isMultiple()){
                    //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
                    LocalObjectListItem[] list = com.getList(lam.getListAttributeClassName(),false);
                    LocalObjectListItem val = null;

                    for (LocalObjectListItem loli : list)
                        if(loli.getOid().equals(lo.getAttribute(lam.getName()))){
                            val = loli;
                            break;
                        }
                    property = new ObjectNodeProperty(
                                           lam.getName(),
                                           LocalObjectListItem.class,
                                           val,
                                           lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                           lam.getDescription(),
                                           list,
                                           this);
                }
                else{
                    //Those attributes that are not multiple, but reference another object
                    //like nodeA or endpointB in physicalConnections should be ignored, at least by now
                    if (!lam.getType().equals(LocalObjectLight.class))
                        property = new ObjectNodeProperty(
                                                            lam.getName(),
                                                            lam.getType(),
                                                            lo.getAttribute(lam.getName()),
                                                            lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                                            lam.getDescription(),this);
                }
                generalPropertySet.put(property);
            }         
        }

        generalPropertySet.setName("1");
        administrativePropertySet.setName("2");

        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        administrativePropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ADMINISTRATIVE_ATTRIBUTES"));

        
        sheet.put(generalPropertySet);
        sheet.put(administrativePropertySet);

        return sheet;
    }

    public boolean refresh(){
        LocalObjectLight refreshedObject;
        //Force to retrieve the object info again
        if (object instanceof LocalObjectLight)
            refreshedObject = com.getObjectInfoLight(object.getClassName(), object.getOid());
        else
            refreshedObject = com.getObjectInfo(object.getClassName(), object.getOid());

        if (refreshedObject == null) //The object has been deleted or couldn'be retrieved
            return false;
        else
            object = refreshedObject;
        
        //Force to get the attributes again, but only if there's a property sheet already asigned
        if (this.sheet != null)
            setSheet(createSheet());

        icon = (com.getMetaForClass(object.getClassName(),false)).getSmallIcon();
        fireIconChange();

        //Don't try to refresh the anything if the node is a leaf (used only in views)
        if (!(getChildren() instanceof ObjectChildren))
            return true;

        
        if (((ObjectChildren)getChildren()).getKeys() != null){ //Expanded node
            List<LocalObjectLight> children = com.getObjectChildren(object.getOid(), com.getMetaForClass(object.getClassName(), false).getOid());

            
            List<Node> toBeDeleted = new ArrayList<Node>(Arrays.asList(getChildren().getNodes()));
            List<LocalObjectLight> toBeAdded = new ArrayList<LocalObjectLight>(children);

            for (Node child : getChildren().getNodes()){
                for (LocalObjectLight myChild : children){
                    if (((ObjectNode)child).getObject().equals(myChild)){
                        ((ObjectNode)child).refresh();
                        toBeDeleted.remove(child);
                        toBeAdded.remove(myChild);
                    }
                }
            }
            children = toBeAdded;

            for (Node deadNode : toBeDeleted)
                ((ObjectChildren)getChildren()).remove(new Node[]{deadNode});

            for (LocalObjectLight newChild : toBeAdded)
                ((ObjectChildren)getChildren()).add(new Node[]{new ObjectNode(newChild)});
        }
        

        return true;
    }

    //This method is called for the very first time when the first context menu is created, and
    //then called everytime
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{createAction,
                            refreshAction,
                            editAction,
                            deleteAction,
                            null, //Separator
                            SystemAction.get(CopyAction.class),
                            SystemAction.get(CutAction.class),
                            SystemAction.get(PasteAction.class),
                            null, //Separator
                            relateToServiceAction,
                            showRelatedServicesAction,
                            null, //Separator
                            explorerAction};

    }

    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        //From the transferable we figure out if it comes from a copy or a cut operation
        PasteType paste = getDropType( t, NodeTransfer.node(t, NodeTransfer.CLIPBOARD_COPY) != null ?
                                        DnDConstants.ACTION_COPY:DnDConstants.ACTION_MOVE, -1 );
        //It's also possible to define many paste types (like "normal paste" and "special paste") 
        //by adding more entries to the list. Those will appear as options in the context menu
        if( paste != null )
            s.add( paste );

    }

    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index){
        final ObjectNode dropNode = (ObjectNode)NodeTransfer.node( _obj,
                NodeTransfer.DND_COPY_OR_MOVE+NodeTransfer.CLIPBOARD_CUT);

        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null)
            return null;

        //Ignore those noisy attempts to move it to itself
        if (dropNode.getObject().equals(object))
            return null;

        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE))
            return null;

        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                boolean canMove = false;
                try{
                    LocalObjectLight obj = dropNode.getObject();

                    //Check if the current object can contain the drop node
                    List<LocalClassMetadataLight> possibleChildren = com.getPossibleChildren(object.getClassName(),false);
                    for(LocalClassMetadataLight lcml : possibleChildren){
                        if(lcml.getClassName().equals(obj.getClassName()))
                            canMove = true;
                    }
                    if (canMove){
                          if (action == DnDConstants.ACTION_COPY){
                              LocalObjectLight[] copiedNodes = com.copyObjects(getObject().getOid(),
                                                                new LocalObjectLight[] {obj});
                                if (copiedNodes!= null){
                                    for (LocalObjectLight lol : copiedNodes)
                                        getChildren().add(new Node[]{new ObjectNode(lol)});

                                }
                                else
                                    nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").
                                        getString("LBL_COPYOPERATION_TITLE"), NotificationUtil.ERROR, com.getError());
                          }
                          else{
                              if (action == DnDConstants.ACTION_MOVE){
                                    if (com.moveObjects(getObject().getOid(),new LocalObjectLight[] {obj})){
                                        dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
                                        getChildren().add(new Node[]{new ObjectNode(obj)});
                                    }
                                    else
                                        nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").
                                            getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR, com.getError());
                              }
                          }                        
                    }else
                        nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").
                                    getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR,
                                    new Formatter().format(java.util.ResourceBundle.
                                        getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_MOVEOPERATION_TEXT"),obj.getClassName(),object.getClassName()).toString());
                }catch(Exception ex){
                    nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").
                                        getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR, ex.getClass().getSimpleName() +" "+ex.getMessage());
                }
                 return null;
            }
        };
    }

    //TODO Set this to false is the object is locked
    @Override
    public boolean canRename(){
        return true;
    }

    @Override
    public boolean canCut(){
        return true;
    }

    @Override
    public boolean canCopy(){
        return true;
    }

    @Override
    public boolean canDestroy(){
        return true;
    }

    @Override
    public Image getIcon(int i){
        if (icon==null)
            //TODO: Inefficient, create only one instance to save memory
            return ImageUtilities.loadImage(GENERIC_ICON_PATH);
        return icon;
    }

    @Override
    public Image getOpenedIcon(int i){
        return getIcon(i);
    }

    @Override
    public void setName(String newName){
        try{
            LocalObject update = Lookup.getDefault().lookup(LocalObject.class);
            update.setLocalObject(object.getClassName(),
                    new String[]{"name"}, new Object[]{newName}); //NOI18N
            update.setOid(object.getOid());
            if (com.saveObject(update) != null){
                object.setDisplayName(newName);
                fireDisplayNameChange(object.getDisplayname(), newName);
                
                if (this instanceof ListElementNode)
                    CommunicationsStub.getInstance().getList(object.getClassName(), true);
            }

            //So the PropertySheet reflects the changes too
            refresh();
        }catch(Exception e){
            nu.showSimplePopup("Error", NotificationUtil.ERROR, e.getMessage());
        }
    }

    @Override
    public String getName(){     
        return getEditableText();
    }

    public String getEditableText(){
        String displayName;
        if (object instanceof LocalObject)
            displayName = (((LocalObject)object).getAttribute("name").equals("")) ?
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NONAME"):((LocalObject)object).getAttribute("name").toString();
        else
            displayName= (object.getDisplayname().equals(""))?
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NONAME"):object.getDisplayname();
        return displayName;
    }

    /**
     * The node listen for changes in the wrapped business object
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(object)){
            object = (LocalObjectLight)evt.getSource();
            if (evt.getPropertyName().equals(PROP_NAME))
                setName(evt.getNewValue().toString());
        }
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof ObjectNode){
            return ((ObjectNode)obj).getObject().getOid().longValue() == this.getObject().getOid().longValue();
        }else return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.object != null ? this.object.hashCode() : 0);
        return hash;
    }
}