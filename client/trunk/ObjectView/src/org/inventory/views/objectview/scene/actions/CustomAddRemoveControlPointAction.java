/**
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.objectview.scene.actions;

import java.awt.event.ActionEvent;
import org.inventory.core.visual.widgets.AbstractScene;
import org.netbeans.api.visual.widget.Widget;
//import org.netbeans.modules.visual.action.AddRemoveControlPointAction;

/**
 * This class is used to fire a scene change event whenever this is triggered so we can
 * track the change and notify the TopComponent to mark the scene as unsaved. 
 * org.netbeans.modules.visual.action.AddRemoveControlPointAction does not report 
 * when the action is performed successfully
  * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CustomAddRemoveControlPointAction { //extends AddRemoveControlPointAction {

//    private AbstractScene scene;
//    
//    public CustomAddRemoveControlPointAction (AbstractScene scene) {
//        super(5.0, 5.0, null);
//        this.scene = scene;
//    }
//
//    @Override
//    public State mouseClicked(Widget widget, WidgetMouseEvent event) {
//        State state = super.mouseClicked(widget, event);
//        if (state.equals(State.CONSUMED))
//            scene.fireChangeEvent(new ActionEvent(scene, AbstractScene.SCENE_CHANGE, "control-point-added")); //NOI18N
//        return state;
//    }
}
