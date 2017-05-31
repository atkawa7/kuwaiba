/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.bookmarks.nodes;

import java.util.Collections;
import org.inventory.communications.core.LocalBookmarkFolder;
import org.inventory.navigation.bookmarks.actions.BookmarksActionFactory;
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
import org.inventory.navigation.bookmarks.nodes.properties.BookmarkFolderNativeTypeProperty;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
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
 * Represents a Bookmark folder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BookmarkFolderNode extends AbstractNode implements PropertyChangeListener {
    public static final String ICON_PATH = "org/inventory/navigation/bookmarks/res/icon.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalBookmarkFolder localBookmark;
    protected Sheet sheet;
    
    public BookmarkFolderNode(LocalBookmarkFolder localBookmark) {
        super(new BookmarkChildren(), Lookups.singleton(localBookmark));
        this.localBookmark = localBookmark;
        if (localBookmark.getName() != null)
            localBookmark.addPropertyChangeListener(WeakListeners.propertyChange(this, localBookmark));
    }
    
    @Override
    public void setName(String newName) {
        if (newName != null) {
            if (CommunicationsStub.getInstance().updateBookmarkFolder(localBookmark.getId(), newName)) {
                localBookmark.setName(newName);
                if (getSheet() != null)
                    setSheet(createSheet());
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());                
            }
        }
    }
    
    @Override
    public String getName(){
        return getEditableText();
    }
    
    public String getEditableText() {
        return localBookmark.getName() == null ? "" : localBookmark.getName();
    }
    
    @Override
    public String getDisplayName() {
        return localBookmark.toString();
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {SystemAction.get(PasteAction.class), BookmarksActionFactory.getDeleteBookmarkFolderAction() };
    }
    
    public LocalBookmarkFolder getLocalBookmark() {
        return localBookmark;
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
        
        //The clipboard does not contain an Bookmark Item Node
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
                        ObjectNode bookmarkItem = (ObjectNode) dropNode;
                        
                        if (bookmarkItem.getParentNode() instanceof BookmarkFolderNode) {
                            List<String> objClass = new ArrayList();
                            objClass.add(bookmarkItem.getObject().getClassName());
                            
                            List<Long> objId = new ArrayList();
                            objId.add(bookmarkItem.getObject().getOid());
                                
                            if (CommunicationsStub.getInstance().addObjectsToBookmarkFolder(objClass, objId, localBookmark.getId())) {
                                
                                ((BookmarkChildren) getChildren()).addNotify();
                            } else {
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
                                CommunicationsStub.getInstance().getError());
                            }
                        }
                    }
                }
                if (action == DnDConstants.ACTION_MOVE) {
                    if (dropNode instanceof ObjectNode) {
                        ObjectNode bookmarkItem = (ObjectNode) dropNode;
                        
                        if (bookmarkItem.getParentNode() instanceof BookmarkFolderNode) {
                            BookmarkFolderNode bookmark = (BookmarkFolderNode) bookmarkItem.getParentNode();
                            
                            List<String> objClass = new ArrayList();
                            objClass.add(bookmarkItem.getObject().getClassName());
                                
                            List<Long> objId = new ArrayList();
                            objId.add(bookmarkItem.getObject().getOid());
                            if (CommunicationsStub.getInstance().removeObjectsFromBookmarkFolder(
                                objClass, 
                                objId, 
                                bookmark.getLocalBookmark().getId())) {
                                
                                ((BookmarkChildren) bookmark.getChildren()).addNotify();
                                    
                                if (CommunicationsStub.getInstance().addObjectsToBookmarkFolder(objClass, objId, localBookmark.getId())) {
                                    
                                    ((BookmarkChildren) getChildren()).addNotify();
                                } else {
                                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
                                        CommunicationsStub.getInstance().getError());
                                }
                            } else {
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
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
        LocalBookmarkFolder lb = CommunicationsStub.getInstance().getBookmarkFolder(localBookmark.getId());
        if (lb == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return sheet;
        }
        localBookmark.setName(lb.getName());
                
        PropertySupport.ReadWrite propertyName = new BookmarkFolderNativeTypeProperty(
                Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, 
                Constants.PROPERTY_NAME, this, lb.getName());
        generalPropertySet.put(propertyName);
        
        generalPropertySet.setName("General Info");
        generalPropertySet.setDisplayName("General Attributes");
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookmarkFolderNode) {
            return ((BookmarkFolderNode) obj).getLocalBookmark().equals(getLocalBookmark());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.localBookmark);
        return hash;
    }
    
    @Override
    public boolean canRename() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource().equals(localBookmark)) {
            localBookmark = (LocalBookmarkFolder) evt.getSource();
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, localBookmark.getName());
            }
        }
    }
    
    public static class BookmarkChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            BookmarkFolderNode selectedNode = (BookmarkFolderNode) getNode();
            
            List<LocalObjectLight> bookmarkItems = CommunicationsStub.getInstance().getObjectsInBookmarkFolder(selectedNode.getLocalBookmark().getId(), -1);
            
            if (bookmarkItems == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            } else {
                Collections.sort(bookmarkItems);
                setKeys(bookmarkItems);
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
