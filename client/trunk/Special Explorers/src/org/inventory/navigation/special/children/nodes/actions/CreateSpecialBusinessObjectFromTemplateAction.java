/**
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
 */
package org.inventory.navigation.special.children.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter.Popup;

/**
 * Creates an special object from a template
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CreateSpecialBusinessObjectFromTemplateAction extends GenericObjectNodeAction 
    implements Popup, ComposedAction {
    
    private static CreateSpecialBusinessObjectFromTemplateAction instance;
    
    private CreateSpecialBusinessObjectFromTemplateAction() {
    }
    
    public static CreateSpecialBusinessObjectFromTemplateAction getInstance() {
        return instance == null ? instance = new CreateSpecialBusinessObjectFromTemplateAction() : instance;
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SPECIAL_EXPLORERS, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String className = ((JMenuItem) e.getSource()).getName();
        List<LocalObjectLight> templates = CommunicationsStub.getInstance().getTemplatesForClass(className, false);
        
        if (templates == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else {
            if (templates.isEmpty())
                JOptionPane.showMessageDialog(null, "No templates were defined for this class", I18N.gm("error"), JOptionPane.INFORMATION_MESSAGE);
            else {
                Collections.sort(templates);
                SelectValueFrame templatesFrame = new SelectValueFrame(String.format("Available Templates for %s", className), I18N.gm("search"), "Create Special Object", templates);
                templatesFrame.addListener(this);
                templatesFrame.setVisible(true);
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleSpecialChildren = new JMenu("New Special from Template");

        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
        
        List<LocalClassMetadataLight> items;
        if (selectedObject == null)  //The root node
            items = CommunicationsStub.getInstance().getPossibleSpecialChildren(Constants.DUMMYROOT, false);
        else 
            items = CommunicationsStub.getInstance().getPossibleSpecialChildren(selectedObject.getClassName(), false);
        
        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.INFO_MESSAGE,
                CommunicationsStub.getInstance().getError());
            mnuPossibleSpecialChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty())
                mnuPossibleSpecialChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                    JMenuItem smiChildren = new JMenuItem(item.getClassName());
                    smiChildren.setName(item.getClassName());
                    smiChildren.addActionListener(this);
                    mnuPossibleSpecialChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleSpecialChildren, 20, 100);
        }
        return mnuPossibleSpecialChildren;
    }

    @Override
    public LocalValidator[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        HashMap<String, Object> attributes = new HashMap<>();
        
        if (e.getSource() instanceof SelectValueFrame) {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedTemplate = frame.getSelectedValue();
            
            if (selectedTemplate == null)
                JOptionPane.showMessageDialog(null, "Select a template", "Create Special Object", JOptionPane.INFORMATION_MESSAGE);
            else {
                LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);

                LocalObjectLight newObject = CommunicationsStub.getInstance().createSpecialObject(((LocalObjectLight) selectedTemplate).getClassName(), 
                    selectedObject.getClassName(), selectedObject.getId(), attributes, ((LocalObjectLight) selectedTemplate).getId());

                if (newObject == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                } else {
                    ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
                    if (selectedNode.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and does not have children. For those cases, let's avoid refreshing their children lists
                        ((AbstractChildren)selectedNode.getChildren()).addNotify();
                    
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("success"), NotificationUtil.INFO_MESSAGE, 
                        "Special Element created successfully");
                    frame.dispose();
                }
            }
        }
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
