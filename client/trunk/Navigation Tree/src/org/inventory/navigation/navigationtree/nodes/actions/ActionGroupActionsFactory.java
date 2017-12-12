/**
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
package org.inventory.navigation.navigationtree.nodes.actions;

/**
 * Class as factory to the actions that belong to the same group
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionGroupActionsFactory {
    private static ActionsGroup openViewGroupActions;
    private static ActionsGroup relateToGroupActions;
    private static ActionsGroup releaseFromGroupActions;
    private static ActionsGroup mirrorPortActions;
    
    public static ActionsGroup getInstanceOfOpenViewGroupActions() {
        if (openViewGroupActions == null)
            openViewGroupActions = new ActionsGroup("Show", 
                "org/inventory/navigation/navigationtree/res/show_view_actions_group.png", 
                GenericOpenViewAction.class);
        return openViewGroupActions;                    
    }
    
    public static ActionsGroup getInstanceOfRelateToGroupActions() {
        if (relateToGroupActions == null)
            relateToGroupActions = new ActionsGroup("Relate To", 
                "org/inventory/navigation/navigationtree/res/relate_to_actions_group.png",
                GenericRelateToAction.class);
        return relateToGroupActions;                    
    }

    public static ActionsGroup getInstanceOfReleaseFromGroupActions() {
        if (releaseFromGroupActions == null)
            releaseFromGroupActions = new ActionsGroup("Release From", 
                "org/inventory/navigation/navigationtree/res/release_from_actions_group.png",
                GenericReleaseFromAction.class);
        return releaseFromGroupActions;                    
    }
    
    public static ActionsGroup getInstanceMirrorPortActions() {
        return mirrorPortActions == null ? mirrorPortActions = new ActionsGroup("Mirror Ports", 
            "org/inventory/navigation/navigationtree/res/mirror_ports_actions_group.png", 
            GenericMirrorPortAction.class) : mirrorPortActions;
    }
}
