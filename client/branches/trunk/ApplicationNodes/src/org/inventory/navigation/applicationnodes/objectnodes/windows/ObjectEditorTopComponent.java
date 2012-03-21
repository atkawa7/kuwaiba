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
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.explorer.propertysheet.PropertySheetView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectEditorTopComponent extends TopComponent{

    static final String ICON_PATH = "org/inventory/navigation/applicationnodes/res/edit.png";

    private PropertySheetView editor;
    private Node node;

    public ObjectEditorTopComponent(){}

    public ObjectEditorTopComponent(Node _node) {

        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        editor = new PropertySheetView();
        this.node = _node;

        this.setDisplayName(node.getDisplayName());

        //This requires that CoreUI to be enable in the project
        Mode myMode = WindowManager.getDefault().findMode("properties");
        if (myMode != null)
            myMode.dockInto(this);
        else{
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Display Warning", NotificationUtil.WARNING, "\"Properties\" Window Mode not available");
        }

        setLayout(new BorderLayout());

        add(editor,BorderLayout.CENTER);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        //This is important. If setNodes is called in the constructor, it won't work!
        editor.setNodes(new Node[]{node});
    }
}
