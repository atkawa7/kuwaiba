/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sdh.actions;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.SDHModuleService;
import com.neotropic.inventory.modules.sdh.scene.SDHModuleScene;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * All the actions used by the nodes of an SDHModuleScene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHModuleActionsFactory {
    private PopupMenuProvider nodeMenu;
    private PopupMenuProvider connectionMenu;
    private RemoveSDHBusinessObjectFromViewAction removeSDHBusinessObjectFromViewAction;
    private DeleteSDHTransportLink deleteSDHTransportLink;
    private ShowSDHContainersInTransportLink showSDHContainersInTransportLinkAction;
    private SDHModuleScene scene;

    public SDHModuleActionsFactory(SDHModuleScene scene) {
        this.scene = scene;
        removeSDHBusinessObjectFromViewAction = new RemoveSDHBusinessObjectFromViewAction();
        deleteSDHTransportLink = new DeleteSDHTransportLink();
        showSDHContainersInTransportLinkAction = new ShowSDHContainersInTransportLink();
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) 
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    List<Action> actions = new ArrayList<>();
                    actions.add(removeSDHBusinessObjectFromViewAction);
                    actions.add(SystemAction.get(DeleteBusinessObjectAction.class));
                    actions.add(null);

                    ObjectNodeWidget nodeWidget = (ObjectNodeWidget)widget;
                    actions.addAll(Arrays.asList(nodeWidget.getLookup().lookup(ObjectNode.class).getActions(true)));

                    return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView()); 
                    
                }
            };
        return nodeMenu;
    }
    
    public PopupMenuProvider createMenuForConnection() {
        if (connectionMenu == null) 
            connectionMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu mnuActions = new JPopupMenu();
                    mnuActions.add(removeSDHBusinessObjectFromViewAction);
                    mnuActions.add(deleteSDHTransportLink);
                    mnuActions.add(showSDHContainersInTransportLinkAction);
                    
                    //AbstractConnectionWidget connectionWidget = (AbstractConnectionWidget)widget;
                    //actions.addAll(Arrays.asList(connectionWidget.getNode().getActions(true)));
                    
                    //return Utilities.actionsToPopup(actions.toArray(new Action[0]), scene.getView());
                    return mnuActions;
                }
            };
        return connectionMenu;
    }
    
    public class RemoveSDHBusinessObjectFromViewAction extends GenericInventoryAction {

        public RemoveSDHBusinessObjectFromViewAction() {
            this.putValue(NAME, "Remove from View"); 
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            for (Object selectedObject : selectedObjects) {
                LocalObjectLight castedObject = (LocalObjectLight)selectedObject;
                if (CommunicationsStub.getInstance().isSubclassOf(castedObject.getClassName(), SDHModuleService.CLASS_GENERICEQUIPMENT))
                    scene.removeNodeWithEdges(castedObject);
                    
                else
                    scene.removeEdge(castedObject);
                
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete")); //NOI18N
            }         
        }

        @Override
        public LocalPrivilege getPrivilege() {
            return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SDH_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
        }
    }
    
    public class DeleteSDHTransportLink extends GenericInventoryAction {
        
        public DeleteSDHTransportLink() {
            this.putValue(NAME, "Delete Transport Link"); 
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            
            if (JOptionPane.showConfirmDialog(null, 
                    "This will delete all the containers and tributary links \n Are you sure you want to do this?", 
                    "Delete Transport Link", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
                Set<?> selectedObjects = scene.getSelectedObjects();
                for (Object selectedObject : selectedObjects) {
                    LocalObjectLight castedObject = (LocalObjectLight)selectedObject;
                    if (CommunicationsStub.getInstance().deleteSDHTransportLink(castedObject.getClassName(), castedObject.getOid())) {
                        NotificationUtil.getInstance().showSimplePopup("Delete Operation", NotificationUtil.INFO_MESSAGE, "Transport link deleted successfully");
                        scene.removeEdge(castedObject);
                        scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
                    } else 
                        NotificationUtil.getInstance().showSimplePopup("Delete Operation", NotificationUtil.INFO_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }
        }

        @Override
        public LocalPrivilege getPrivilege() {
            return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SDH_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
        }
    }
    
    public class ShowSDHContainersInTransportLink extends GenericInventoryAction {

        public ShowSDHContainersInTransportLink() {
            this.putValue(NAME, "Show Virtual Circuits Inside");
        }        
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Set<?> selectedObjects = scene.getSelectedObjects();
            if (selectedObjects.size() != 1)
                JOptionPane.showMessageDialog(null, "Select only one node", "Error", JOptionPane.WARNING_MESSAGE);
            else {
                LocalObjectLight castedObject = (LocalObjectLight)selectedObjects.iterator().next();
                List<LocalSDHContainerLinkDefinition> structure = CommunicationsStub.getInstance().getSDHTransportLinkStructure(castedObject.getClassName(), castedObject.getOid());
                TopComponent sdhLinkStructure = new SDHLinkStructureTopComponent(castedObject, structure, 
                        SDHModuleService.calculateCapacity(castedObject.getClassName(), SDHModuleService.LinkType.TYPE_TRANSPORTLINK), 1);
                sdhLinkStructure.open();
                sdhLinkStructure.requestActive();
            }
        }

        @Override
        public LocalPrivilege getPrivilege() {
            return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SDH_MODULE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
        }
    }
}
