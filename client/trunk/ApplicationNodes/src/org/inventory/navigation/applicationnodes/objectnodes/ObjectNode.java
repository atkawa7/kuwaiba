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
import java.util.Formatter;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalAttributeMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.actions.Create;
import org.inventory.navigation.applicationnodes.objectnodes.actions.Delete;
import org.inventory.navigation.applicationnodes.objectnodes.actions.Edit;
import org.inventory.navigation.applicationnodes.objectnodes.actions.Refresh;
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

    protected Create createAction;
    protected Delete deleteAction;
    protected Refresh refreshAction;
    protected Edit editAction;

    protected Sheet sheet;
    protected Image icon;

    public ObjectNode(LocalObjectLight _lol, boolean isLeaf){
        super(Children.LEAF, Lookups.singleton(_lol));
        this.object = _lol;
        this.object.addPropertyChangeListener(this);

        com = CommunicationsStub.getInstance();

        icon = (com.getMetaForClass(_lol.getClassName(),false)).getSmallIcon();

        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EXPLORE"));
        editAction = new Edit(this);
        deleteAction = new Delete(this);
        refreshAction = new Refresh(this);
    }
    
    public ObjectNode(LocalObjectLight _lol){
        super(new ObjectChildren(), Lookups.singleton(_lol));
        this.object = _lol;
        this.object.addPropertyChangeListener(this);
        
        com = CommunicationsStub.getInstance();

        icon = (com.getMetaForClass(_lol.getClassName(),false)).getSmallIcon();
        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EXPLORE"));

        createAction = new Create(this);
        deleteAction = new Delete(this);
        editAction = new Edit(this);
        refreshAction = new Refresh(this);
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
        LocalObject lo;
        if (object instanceof LocalObject)
            lo = (LocalObject)object;
        else
            lo = com.getObjectInfo(object.getClassName(), object.getOid());

        for(LocalAttributeMetadata lam:meta.getAttributes()){
            if(lam.getIsVisible()){

                ObjectNodeProperty property = null;

                if (lam.getIsMultiple()){
                    //If so, this can be a reference to an object list item or a 1:1 to any other RootObject subclass
                    LocalObjectListItem[] list = com.getList(lam.getListAttributeClassName(),false);
                    LocalObjectListItem val = null;

                    for (LocalObjectListItem loli : list)
                        if(loli.getId().equals(lo.getAttribute(lam.getName()))){
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
                    //Those attributes that are not multiple, but references another object
                    //like nodeA or endpointB in physicalConnections should be ignored, at least by now
                    if (!lam.getType().equals(LocalObjectLight.class))
                        property = new ObjectNodeProperty(
                                                            lam.getName(),
                                                            lam.getType(),
                                                            lo.getAttribute(lam.getName()),
                                                            lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                                            lam.getDescription(),this);
                }
                if (property != null){
                    if(lam.getIsAdministrative())
                        administrativePropertySet.put(property);
                    else
                        generalPropertySet.put(property);
                }
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

    public void refresh(){
        
        //Force to retrieve the object info again
        if (object instanceof LocalObjectLight)
            object = com.getObjectInfoLight(object.getClassName(), object.getOid());
        else
            object = com.getObjectInfo(object.getClassName(), object.getOid());
        //Force to get the attributes again, but only if there's a property sheet already asigned
        if (this.sheet != null)
            setSheet(createSheet());

        icon = (com.getMetaForClass(object.getClassName(),false)).getSmallIcon();
        fireIconChange();

        //Don't refresh anything if the node is a leaf (used only in views)
        if (!(getChildren() instanceof ObjectChildren))
            return;

        if (!((ObjectChildren)getChildren()).getKeys().isEmpty())
            for (Node child : getChildren().getNodes())
                ((ObjectNode)child).refresh();
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
                NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
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
            if (com.saveObject(update)){
                object.setDisplayName(newName);
                fireDisplayNameChange(object.getDisplayname(), newName);
            }

            //So the PropertySheet reflects the changes too
            refresh();
        }catch(Exception e){
            e.printStackTrace();
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