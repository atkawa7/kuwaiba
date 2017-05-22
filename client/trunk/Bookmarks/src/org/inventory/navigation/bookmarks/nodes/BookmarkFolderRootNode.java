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

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBookmarkFolder;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.bookmarks.actions.NewBookmarkFolderAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * The root node of the tree of bookmarks.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BookmarkFolderRootNode extends AbstractNode {
    public static final String ICON_PATH = "org/inventory/navigation/bookmarks/res/root.png";
    private static final Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    public BookmarkFolderRootNode() {
        super(new BookmarkFolderRootChildren());
        setDisplayName("Bookmark Folders");
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{ NewBookmarkFolderAction.getInstance() };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    public static class BookmarkFolderRootChildren extends Children.Keys<LocalBookmarkFolder> {
        
        @Override
        public void addNotify() {
            List<LocalBookmarkFolder> bookmarks = CommunicationsStub.getInstance().getBookmarkFoldersForUser();
            
            if (bookmarks == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());                                
            } else {
                Collections.sort(bookmarks);
                setKeys(bookmarks);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }

        @Override
        protected Node[] createNodes(LocalBookmarkFolder key) {
            return new Node [] {new BookmarkFolderNode(key)};
        }
    }
}
