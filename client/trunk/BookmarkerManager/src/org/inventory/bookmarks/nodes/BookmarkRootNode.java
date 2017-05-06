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

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBookmark;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.bookmarks.actions.NewBookmarkAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * The root node of the tree of bookmarks.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class BookmarkRootNode extends AbstractNode {
    public static final String ICON_PATH = "org/inventory/bookmarks/res/root.png";
    private static Image icon = ImageUtilities.loadImage(ICON_PATH);
    
    public BookmarkRootNode() {
        super(new BookmarkRootChildren());
        setDisplayName("Bookmarks");
    }
    
    @Override
    public Action[] getActions(boolean context){
        return new Action[]{ new NewBookmarkAction() };
    }
    
    @Override
    public Image getIcon(int i) {
        return icon;
    }
    
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
    
    public static class BookmarkRootChildren extends Children.Keys<LocalBookmark> {
        
        @Override
        public void addNotify() {
            List<LocalBookmark> bookmarks = CommunicationsStub.getInstance().getBookmarksForUser();
            
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
        protected Node[] createNodes(LocalBookmark key) {
            return new Node [] {new BookmarkNode(key)};
        }
    }
}
