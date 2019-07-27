/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.navigation.favorites.nodes;

import java.util.Collections;
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.navigation.favorites.actions.FavoritesActionFactory;
import org.openide.util.ImageUtilities;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.Action;
import org.inventory.navigation.favorites.nodes.properties.FavoritesFolderNativeTypeProperty;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.actions.PasteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.nodes.Sheet.Set;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 * Represents a Favorites folder
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FavoritesFolderNode extends AbstractNode implements PropertyChangeListener {
    public static final String ICON_PATH = "org/inventory/navigation/favorites/res/icon.png";
    private static final Image NODE_ICON = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalFavoritesFolder localFavoritesFolder;
    protected Sheet sheet;
    
    public FavoritesFolderNode(LocalFavoritesFolder localFavoritesFolder) {
        super(new FavoritesFolderChildren(), Lookups.singleton(localFavoritesFolder));
        this.localFavoritesFolder = localFavoritesFolder;
        if (localFavoritesFolder.getName() != null)
            localFavoritesFolder.addPropertyChangeListener(WeakListeners.propertyChange(this, localFavoritesFolder));
    }
    
    @Override
    public void setName(String newName) {
        if (newName != null) {
            if (CommunicationsStub.getInstance().updateFavoritesFolder(localFavoritesFolder.getId(), newName)) {
                localFavoritesFolder.setName(newName);
                if (getSheet() != null)
                    setSheet(createSheet());
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                
            }
        }
    }
    
    @Override
    public String getName(){
        return localFavoritesFolder.getName();
    }
    
    @Override
    public String getDisplayName() {
        return localFavoritesFolder.toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(PasteAction.class), FavoritesActionFactory.getDeleteFavoritesFolderAction() };
    }
    
    public LocalFavoritesFolder getFavoritesFolder() {
        return localFavoritesFolder;
    }
        
    @Override
    public Image getIcon(int i) {
        return NODE_ICON;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
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
    public PasteType getDropType(Transferable _obj, final int action, int index) {
        final Node dropNode = NodeTransfer.node(_obj,
                NodeTransfer.DND_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        
        //When there's no an actual drag/drop operation, but a simple node selection
        if (dropNode == null) 
            return null;
        
        //The clipboard does not contain an Favorites Item Node
        if (!ObjectNode.class.isInstance(dropNode))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                if (action == DnDConstants.ACTION_COPY) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode favoritesItem = (ObjectNode) dropNode;
                        
                        if (favoritesItem.getParentNode() instanceof FavoritesFolderNode) {
                            List<String> objClass = new ArrayList<>();
                            objClass.add(favoritesItem.getObject().getClassName());
                            
                            List<String> objId = new ArrayList<>();
                            objId.add(favoritesItem.getObject().getId());
                                
                            if (CommunicationsStub.getInstance().addObjectsToFavoritesFolder(objClass, objId, localFavoritesFolder.getId())) {
                                
                                ((FavoritesFolderChildren) getChildren()).addNotify();
                            } else {
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, 
                                CommunicationsStub.getInstance().getError());
                            }
                        }
                    }
                }
                if (action == DnDConstants.ACTION_MOVE) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode favoritesItem = (ObjectNode) dropNode;
                        
                        if (favoritesItem.getParentNode() instanceof FavoritesFolderNode) {
                            FavoritesFolderNode bookmark = (FavoritesFolderNode) favoritesItem.getParentNode();
                            
                            List<String> objClass = new ArrayList<>();
                            objClass.add(favoritesItem.getObject().getClassName());
                                
                            List<String> objId = new ArrayList<>();
                            objId.add(favoritesItem.getObject().getId());
                            if (CommunicationsStub.getInstance().removeObjectsFromFavoritesFolder(
                                objClass, 
                                objId, 
                                bookmark.getFavoritesFolder().getId())) {
                                
                                ((FavoritesFolderChildren) bookmark.getChildren()).addNotify();
                                    
                                if (CommunicationsStub.getInstance().addObjectsToFavoritesFolder(objClass, objId, localFavoritesFolder.getId())) {
                                    
                                    ((FavoritesFolderChildren) getChildren()).addNotify();
                                } else {
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, 
                                        CommunicationsStub.getInstance().getError());
                                }
                            } else {
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE, 
                                    CommunicationsStub.getInstance().getError());
                            }
                        }
                    }
                }
                return null;
            }
        };
    }
    
    @Override
    protected Sheet createSheet () {
        sheet = Sheet.createDefault();
        Set generalPropertySet = Sheet.createPropertiesSet(); // General attributes category
        LocalFavoritesFolder lb = CommunicationsStub.getInstance().getFavoritesFolder(localFavoritesFolder.getId());
        if (lb == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return sheet;
        }
        localFavoritesFolder.setName(lb.getName());
                
        PropertySupport.ReadWrite propertyName = new FavoritesFolderNativeTypeProperty(
                Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, 
                Constants.PROPERTY_NAME, this, lb.getName());
        generalPropertySet.put(propertyName);
        
        generalPropertySet.setName(I18N.gm("general_properties"));
        generalPropertySet.setDisplayName(I18N.gm("general_properties"));
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FavoritesFolderNode) {
            return ((FavoritesFolderNode) obj).getFavoritesFolder().equals(getFavoritesFolder());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.localFavoritesFolder);
        return hash;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(localFavoritesFolder)) {
            localFavoritesFolder = (LocalFavoritesFolder) evt.getSource();
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, localFavoritesFolder.getName());
            }
        }
    }
    
    public static class FavoritesFolderChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            FavoritesFolderNode selectedNode = (FavoritesFolderNode) getNode();
            
            List<LocalObjectLight> favoritesItems = CommunicationsStub.getInstance().getObjectsInFavoritesFolder(selectedNode.getFavoritesFolder().getId(), -1);
            
            if (favoritesItems == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(favoritesItems);
                setKeys(favoritesItems);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node [] { new ObjectNode(key) };
        }
    }
}
