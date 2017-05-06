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
package org.inventory.bookmarks.nodes;

import java.util.Collections;
import org.inventory.communications.core.LocalBookmark;
import org.inventory.bookmarks.actions.BookmarksActionFactory;
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
import org.inventory.bookmarks.nodes.BookmarkRootNode.BookmarkRootChildren;
import org.inventory.bookmarks.nodes.properties.BookmarkNativeTypeProperty;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
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
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BookmarkNode extends AbstractNode implements PropertyChangeListener {
    public static final String ICON_PATH = "org/inventory/bookmarks/res/icon.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    private LocalBookmark localBookmark;
    protected Sheet sheet;
    
    public BookmarkNode(LocalBookmark localBookmark) {
        super(new BookmarkNodeChildren(), Lookups.singleton(localBookmark));
        this.localBookmark = localBookmark;
        localBookmark.addPropertyChangeListener(WeakListeners.propertyChange(this, localBookmark));
    }
    
    @Override
    public void setName(String newName) {
        LocalBookmark lb = CommunicationsStub.getInstance().getBookmark(localBookmark.getId());
        if (lb == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        localBookmark.setName(lb.getName() == null ? Constants.LABEL_NONAME : lb.getName());
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
        return new Action[] {SystemAction.get(PasteAction.class), BookmarksActionFactory.getDeleteBookmarkAction() };
    }
    
    public LocalBookmark getLocalBookmark() {
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
        if (!BookmarkItemNode.class.isInstance(dropNode))
            return null;
        
        //Can't move to the same parent, only copy
        if (this.equals(dropNode.getParentNode()) && (action == DnDConstants.ACTION_MOVE)) 
            return null;
        
        return new PasteType() {
            @Override
            public Transferable paste() throws IOException {
                if (action == DnDConstants.ACTION_COPY) {
                    if (dropNode instanceof BookmarkItemNode) {
                        BookmarkItemNode bookmarkItem = (BookmarkItemNode) dropNode;
                        
                        if (bookmarkItem.getParentNode() instanceof BookmarkNode) {
                            List<String> objClass = new ArrayList();
                            objClass.add(bookmarkItem.getObject().getClassName());
                            
                            List<Long> objId = new ArrayList();
                            objId.add(bookmarkItem.getObject().getOid());
                                
                            if (CommunicationsStub.getInstance().associateObjectsToBookmark(objClass, objId, localBookmark.getId())) {
                                
                                ((BookmarkNodeChildren) getChildren()).addNotify();
                            } else {
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE, 
                                CommunicationsStub.getInstance().getError());
                            }
                        }
                    }
                }
                if (action == DnDConstants.ACTION_MOVE) {
                    if (dropNode instanceof BookmarkItemNode) {
                        BookmarkItemNode bookmarkItem = (BookmarkItemNode) dropNode;
                        
                        if (bookmarkItem.getParentNode() instanceof BookmarkNode) {
                            BookmarkNode bookmark = (BookmarkNode) bookmarkItem.getParentNode();
                            
                            List<String> objClass = new ArrayList();
                            objClass.add(bookmarkItem.getObject().getClassName());
                                
                            List<Long> objId = new ArrayList();
                            objId.add(bookmarkItem.getObject().getOid());
                            if (CommunicationsStub.getInstance().releaseObjectsFromBookmark(
                                objClass, 
                                objId, 
                                bookmark.getLocalBookmark().getId())) {
                                
                                ((BookmarkNodeChildren) bookmark.getChildren()).addNotify();
                                    
                                if (CommunicationsStub.getInstance().associateObjectsToBookmark(objClass, objId, localBookmark.getId())) {
                                    
                                    ((BookmarkNodeChildren) getChildren()).addNotify();
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
        LocalBookmark lb = CommunicationsStub.getInstance().getBookmark(localBookmark.getId());
        if (lb == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return sheet;
        }
        localBookmark.setName(lb.getName());
        
        PropertySupport.ReadWrite propertyName = new BookmarkNativeTypeProperty(
                Constants.PROPERTY_NAME, String.class, Constants.PROPERTY_NAME, 
                Constants.PROPERTY_NAME, this, lb.getName());
        generalPropertySet.put(propertyName);
        
        generalPropertySet.setDisplayName("General");
        sheet.put(generalPropertySet);
        return sheet;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BookmarkNode) {
            return ((BookmarkNode) obj).getLocalBookmark().equals(getLocalBookmark());
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
            localBookmark = (LocalBookmark) evt.getSource();
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME)) {                
                setDisplayName(getDisplayName());
                fireNameChange(null, localBookmark.getName());
            }
        }
    }
    
    public static class BookmarkNodeChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            BookmarkNode selectedNode = (BookmarkNode) getNode();
            
            List<LocalObjectLight> bookmarkItems = CommunicationsStub.getInstance().getBookmarkItems(selectedNode.getLocalBookmark().getId(), -1);
            
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
            return new Node [] { new BookmarkItemNode(key) };
        }
    }
}
