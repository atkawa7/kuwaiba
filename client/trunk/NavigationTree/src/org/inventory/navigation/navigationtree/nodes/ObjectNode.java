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
 *  under the License.
 */
package org.inventory.navigation.navigationtree.nodes;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
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
import org.inventory.navigation.navigationtree.actions.Create;
import org.inventory.navigation.navigationtree.actions.Delete;
import org.inventory.navigation.navigationtree.actions.Edit;
import org.inventory.navigation.navigationtree.nodes.properties.ObjectNodeProperty;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.OpenLocalExplorerAction;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Represents a node within the navigation tree
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNode extends AbstractNode{
    
    private LocalObjectLight object;
    //There can be only one instance for OpenLocalExplorerAction, this attribute is a kind of singleton
    private static OpenLocalExplorerAction explorerAction = new OpenLocalExplorerAction();

    private CommunicationsStub com;

    private Create createAction;
    private Delete deleteAction;
    private Edit editAction;

    private Sheet sheet;

    public ObjectNode(LocalObjectLight _lol){
        super(new ObjectChildren(), Lookups.singleton(_lol));
        this.object = _lol;

        com= CommunicationsStub.getInstance();

        explorerAction.putValue(OpenLocalExplorerAction.NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_EXPLORE"));

        createAction = new Create(this);
        deleteAction = new Delete(this);
        editAction = new Edit(this);
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
        String displayName = (object.getDisplayname().equals("") ||
                                    object.getDisplayname().equals(null))?java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_NONAME"):object.getDisplayname();
        return displayName + " ["+object.getClassName()+"]";
    }

    @Override
    protected Sheet createSheet(){
        sheet = Sheet.createDefault();
        
        Set generalPropertySet = Sheet.createPropertiesSet(); //General attributes category
        Set administrativePropertySet = Sheet.createPropertiesSet(); //Administrative attributes category

        LocalClassMetadata meta = com.getMetaForClass(object.getClassName());

        LocalObject lo = com.getObjectInfo(object.getClassName(), object.getOid(), meta);

        int i = 0;
        for(LocalAttributeMetadata lam:meta.getAttributes()){
            if(lam.getIsVisible()){

                ObjectNodeProperty property = null;

                if (meta.isMultiple(lam.getName())){
                    //TODO take this from the local cache
                    LocalObjectListItem[] list = com.getList(lam.getType());
                    LocalObjectListItem val = null;

                    for (LocalObjectListItem loli : list)
                        if(loli.getId().equals(lo.getAttribute(lam.getName()))){
                            val = loli;
                            break;
                        }

                    /****************************/

                    property = new ObjectNodeProperty(
                                           lam.getName(),
                                           LocalObjectListItem.class,
                                           val,
                                           lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                           lam.getDescription(),
                                           list,
                                           lo);
                }
                else{
                    property = new ObjectNodeProperty(
                                                        lam.getName(),
                                                        (lo.getAttribute(lam.getName())== null)?null:lo.getAttribute(lam.getName()).getClass(),
                                                        lo.getAttribute(lam.getName()),
                                                        lam.getDisplayName().equals("")?lam.getName():lam.getDisplayName(),
                                                        lam.getDescription(),lo);
                }

                if(lam.getIsAdministrative())
                    administrativePropertySet.put(property);
                else
                    generalPropertySet.put(property);
            }
            i++;
        }

        generalPropertySet.setName("1");
        administrativePropertySet.setName("2");

        generalPropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_GENERAL_ATTRIBUTES"));
        administrativePropertySet.setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_ADMINISTRATIVE_ATTRIBUTES"));

        
        sheet.put(generalPropertySet);
        sheet.put(administrativePropertySet);

        return sheet;
    }

    //This method is called for the very first time when the first context menu is created, and
    //called everytime
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{createAction, 
                            editAction,
                            deleteAction,
                            null,
                            SystemAction.get(CopyAction.class),
                            SystemAction.get(CutAction.class),
                            SystemAction.get(PasteAction.class),
                            null,
                            explorerAction};

    }

    @Override
    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        PasteType paste = getDropType( t, DnDConstants.ACTION_COPY, -1 );
        if( null != paste )
            s.add( paste );
    }

    @Override
    public PasteType getDropType(Transferable _obj, final int action, int index){
        final ObjectNode dropNode = (ObjectNode)NodeTransfer.node( _obj,
                DnDConstants.ACTION_COPY_OR_MOVE+NodeTransfer.CLIPBOARD_CUT );

        if (dropNode == null || this.equals(dropNode.getParentNode()))
            return null;

        return new PasteType() {

            @Override
            public Transferable paste() throws IOException {
                boolean canMove = false;
                try{
                    NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
                    LocalObjectLight obj = dropNode.getObject();
                    for(LocalClassMetadataLight lcml : com.getPossibleChildren(object.getPackageName()+"."+object.getClassName())){
                        if(lcml.getClassName().equals(obj.getClassName()))
                            canMove = true;
                    }
                    if (canMove){
                          if ((action & DnDConstants.ACTION_MOVE) != 0 ){
                              LocalObjectLight[] copiedNodes = com.copyObjects(getObject().getOid(),
                                                                new LocalObjectLight[] {obj});
                                if (copiedNodes!= null){
                                    for (LocalObjectLight lol : copiedNodes)
                                        getChildren().add(new Node[]{new ObjectNode(lol)});

                                }
                                else
                                    nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").
                                        getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR, com.getError());
                          }
                          else{
                                if (com.moveObjects(getObject().getOid(),new LocalObjectLight[] {obj})){
                                    dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
                                    getChildren().add(new Node[]{new ObjectNode(obj)});

                                }
                                else
                                    nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").
                                        getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR, com.getError());
                          }


                        

                    }else
                        nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").
                                    getString("LBL_MOVEOPERATION_TITLE"), NotificationUtil.ERROR,
                                    //NbBundle.getMessage(ObjectNode.class, "LBL_MOVEOPERATION_TEXT",new Object[]{obj.getClassName(),object.getClassName()})
                                    java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_MOVEOPERATION_TEXT")
                        );
                }catch(Exception ex){
                   ex.printStackTrace();
                }
                return object;
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
}