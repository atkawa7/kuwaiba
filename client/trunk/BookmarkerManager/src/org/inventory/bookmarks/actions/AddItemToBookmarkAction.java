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
package org.inventory.bookmarks.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.bookmarks.windows.BookmarksFrame;
import org.inventory.communications.core.LocalBookmark;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class AddItemToBookmarkAction extends GenericObjectNodeAction {
    
    public AddItemToBookmarkAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/bookmarks/Bundle")
            .getString("LBL_ADD_BOOKMARK"));
    }
    
    @Override
    public String getValidator() {
        return null; //Enable this action for any object
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalBookmark> bookmarks = CommunicationsStub.getInstance().getBookmarksForUser();
                
        if (bookmarks == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            Lookup.Result<LocalObjectLight> selectedNodes = Utilities
                .actionsGlobalContext().lookupResult(LocalObjectLight.class);
            
            Collection lookupResult = selectedNodes.allInstances();
            LocalObjectLight[] selectedObjects = new LocalObjectLight[lookupResult.size()];
            int i = 0;
            for (Iterator it = lookupResult.iterator(); it.hasNext();) {
                selectedObjects[i] = (LocalObjectLight) it.next();
                i += 1;
            }
            BookmarksFrame frame = new BookmarksFrame(selectedObjects, bookmarks);
            frame.setVisible(true);
        }
    }
}
