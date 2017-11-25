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
 * Class used to manage the popup menu in grouped actions
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionsGroupActionsFactory {
    private static ActionsGroupActions openViewGroupActions;
    private static ActionsGroupActions relateToGroupActions;
    private static ActionsGroupActions releaseFromGroupActions;
    
    public static ActionsGroupActions getInstanceOfOpenViewGroupActions() {
        if (openViewGroupActions == null)
            openViewGroupActions = new ActionsGroupActions("Show", 
                "/org/inventory/navigation/navigationtree/res/show_view_actions_group.png", 
                GenericOpenViewAction.class);
        return openViewGroupActions;                    
    }
    
    public static ActionsGroupActions getInstanceOfRelateToGroupActions() {
        if (relateToGroupActions == null)
            relateToGroupActions = new ActionsGroupActions("Relate to", 
                "/org/inventory/navigation/navigationtree/res/relate_to_actions_group.png",
                GenericRelateToAction.class);
        return relateToGroupActions;                    
    }

    public static ActionsGroupActions getInstanceOfReleaseFromGroupActions() {
        if (releaseFromGroupActions == null)
            releaseFromGroupActions = new ActionsGroupActions("Release from", 
                "/org/inventory/navigation/navigationtree/res/release_from_actions_group.png",
                GenericReleaseFromAction.class);
        return releaseFromGroupActions;                    
    }
}
