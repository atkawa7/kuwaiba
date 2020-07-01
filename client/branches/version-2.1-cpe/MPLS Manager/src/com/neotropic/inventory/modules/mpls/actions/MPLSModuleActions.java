/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.mpls.actions;

import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * All the actions used by the nodes of an MPLSModuleScene
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSModuleActions {
    private PopupMenuProvider nodeMenu;
    private PopupMenuProvider connectionMenu;
    private final RemoveObjectFromViewAction removeObjectFromViewAction;
    private final DeleteMPLSConnectionAction deleteMPLSConnectionAction;
    private final MPLSModuleScene scene;

    public MPLSModuleActions(MPLSModuleScene scene) {
        this.scene = scene;
        
        removeObjectFromViewAction = new RemoveObjectFromViewAction(scene);
        deleteMPLSConnectionAction = Lookup.getDefault().lookup(DeleteMPLSConnectionAction.class);
    }
    
    public PopupMenuProvider createMenuForConnection() {
        if (connectionMenu == null) 
            connectionMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {                   
                    List<Action> actions = new ArrayList<>();
                    actions.add(removeObjectFromViewAction);
                    actions.add(deleteMPLSConnectionAction);
                    actions.add(null);
                    
                    actions.addAll(Arrays.asList(widget.getLookup().lookup(ObjectNode.class).getActions(true)));
                    
                    return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView());                    
                }
            };
        return connectionMenu;
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) 
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    List<Action> actions = new ArrayList<>();
                    actions.add(removeObjectFromViewAction);
                    actions.add(SystemAction.get(DeleteBusinessObjectAction.class));
                    actions.add(null);

                    actions.addAll(Arrays.asList(widget.getLookup().lookup(ObjectNode.class).getActions(true)));

                    return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView()); 
                }
            };
        return nodeMenu;
    }
}
