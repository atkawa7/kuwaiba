/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.sdh.actions;

import com.neotropic.inventory.modules.sdh.LocalSDHContainerLinkDefinition;
import com.neotropic.inventory.modules.sdh.SDHModuleService;
import com.neotropic.inventory.modules.sdh.scene.SDHModuleScene;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * All the actions used by the nodes of an SDHModuleScene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SDHModuleActions {
    private PopupMenuProvider nodeMenu;
    private PopupMenuProvider connectionMenu;
    private RemoveSDHBusinessObjectFromView removeSDHBusinessObjectFromViewAction;
    private DeleteSDHConnection deleteSDHConnectionAction;
    private ShowSDHContainersInTransportLink showSDHContainersInTransportLinkAction;
    private ShowSDHConnectionsInGenericCommunicationsElement showSDHConnectionsInGenericCommunicationsElementAction;
    private SDHModuleScene scene;

    public SDHModuleActions(SDHModuleScene scene) {
        this.scene = scene;
        removeSDHBusinessObjectFromViewAction = new RemoveSDHBusinessObjectFromView();
        deleteSDHConnectionAction = new DeleteSDHConnection();
        showSDHContainersInTransportLinkAction = new ShowSDHContainersInTransportLink();
        showSDHConnectionsInGenericCommunicationsElementAction = new ShowSDHConnectionsInGenericCommunicationsElement();
    }
    
    public PopupMenuProvider createMenuForNode() {
        if (nodeMenu == null) 
            nodeMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu theMenu = new JPopupMenu("Options");
                    theMenu.add(removeSDHBusinessObjectFromViewAction);
                    theMenu.add(SystemAction.get(DeleteBusinessObjectAction.class));
                    theMenu.add(new JSeparator());
                    Widget theWidget = scene.getFocusedWidget();
                    if (theWidget instanceof AbstractNodeWidget) { //For some reason, a right click selects automatically an edge, but not a node (!)
                        AbstractNodeWidget nodeWidget = (AbstractNodeWidget)theWidget;
                        for (Action action : nodeWidget.getNode().getActions(true)) {
                            if (action instanceof Presenter.Popup) //For some reason, these kind of actions are not properly display, so we ignore them
                                continue;
                            if(action == null)
                                theMenu.add(new JSeparator());
                            else
                                theMenu.add(action);
                            //theMenu.add(showSDHConnectionsInGenericCommunicationsElementAction);
                        }
                        return theMenu;
                    }
                    else
                        return null;
                }
            };
        return nodeMenu;
    }
    
    public PopupMenuProvider createMenuForConnection() {
        if (connectionMenu == null) 
            connectionMenu = new PopupMenuProvider() {

                @Override
                public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                    JPopupMenu theMenu = new JPopupMenu("Options");
                    theMenu.add(removeSDHBusinessObjectFromViewAction);
                    theMenu.add(deleteSDHConnectionAction);
                    theMenu.add(showSDHContainersInTransportLinkAction);
                    theMenu.add(new JSeparator());
                    
                    AbstractConnectionWidget nodeWidget = (AbstractConnectionWidget)widget;
                    for (Action action : nodeWidget.getNode().getActions(true)) {
                        if (action instanceof Presenter.Popup) //For some reason, these kind of actions are not properly display, so we ignore them
                            continue;
                        if (action == null)
                            theMenu.add(new JSeparator());
                        else
                            theMenu.add(action);
                        //theMenu.add(showSDHConnectionsInGenericCommunicationsElementAction);
                    }
                    return theMenu;
                }
            };
        return connectionMenu;
    }
    
    public class RemoveSDHBusinessObjectFromView extends AbstractAction {

        public RemoveSDHBusinessObjectFromView() {
            this.putValue(NAME, "Remove from view"); 
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
                
                scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
            }         
        }
    }
    
    public class DeleteSDHConnection extends AbstractAction {
        
        public DeleteSDHConnection() {
            this.putValue(NAME, "Delete"); 
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
    }
    
    public class ShowSDHContainersInTransportLink extends AbstractAction {

        public ShowSDHContainersInTransportLink() {
            this.putValue(NAME, "Show virtual circuits inside");
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
    }
    
    public class ShowSDHConnectionsInGenericCommunicationsElement extends AbstractAction {

        public ShowSDHConnectionsInGenericCommunicationsElement() {
            this.putValue(NAME, "Show transport links");
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            
        }
    }
}
