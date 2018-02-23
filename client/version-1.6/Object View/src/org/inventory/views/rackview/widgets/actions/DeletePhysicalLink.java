/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.rackview.widgets.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.views.rackview.RackViewTopComponent;
import org.inventory.views.rackview.scene.RackViewScene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.windows.WindowManager;

/**
 * This Action is used to delete physical links shown in a rack view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeletePhysicalLink extends GenericInventoryAction implements Presenter.Popup {
    private static DeletePhysicalLink instance;
    private final JMenuItem popupPresenter;
    
    private Widget selectedWidget;
    
    private DeletePhysicalLink() {
        putValue(NAME, "Delete Physical Link");
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
        
    public static DeletePhysicalLink getInstance() {
        return instance == null ? instance = new DeletePhysicalLink() : instance;
    }
    
    public Widget getSelectedWidget() {
        return selectedWidget;
    }
    
    public void setSelectedWidget(Widget selectedWidget) {
        this.selectedWidget = selectedWidget;
    }
    

    @Override
    public LocalPrivilege getPrivilege() {
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
        if (selectedNode == null)
            JOptionPane.showMessageDialog(null, "You must select a node first");
        else {
            
            if (JOptionPane.showConfirmDialog(null, "This will delete the connection. Are you sure you want to do it?", 
                    "Delete Link", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
                if (CommunicationsStub.getInstance().deletePhysicalConnection(selectedNode.getObject().getClassName(), 
                        selectedNode.getObject().getOid())) {
                    
                    //If the node is on a tree, update the list
                    if (selectedNode.getParentNode() != null && AbstractChildren.class.isInstance(selectedNode.getParentNode().getChildren()))
                        ((AbstractChildren)selectedNode.getParentNode().getChildren()).addNotify();
                    
                    Object obj = ((RackViewScene) selectedWidget.getScene()).findObject(selectedWidget);
                    if (obj instanceof LocalObjectLight) {
                        LocalObjectLight lol = (LocalObjectLight) obj;
                        ((RackViewScene) selectedWidget.getScene()).removeEdge(lol);
                        
                        RackViewTopComponent rackView = ((RackViewTopComponent) WindowManager.
                            getDefault().findTopComponent("RackViewTopComponent_" + 
                            ((RackViewScene) selectedWidget.getScene()).getRack().getOid()));

                        if (rackView != null)
                            rackView.refreshScene();
                    }
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Link deleted successfully");
                }
                else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }        
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
}
