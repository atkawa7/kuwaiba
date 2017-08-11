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
 */
package org.inventory.navigation.special.children.nodes.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter.Popup;

/**
 * Creates an special object from a template
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CreateSpecialBusinessObjectFromTemplateAction extends GenericObjectNodeAction 
    implements Popup {
    
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
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        
        else {
            if (templates.isEmpty())
                JOptionPane.showMessageDialog(null, "No templates were defined for this class", "Error", JOptionPane.INFORMATION_MESSAGE);
            else {
                Collections.sort(templates);
                TemplateListFrame templatesFrame = new TemplateListFrame(className, templates);
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
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE,
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
    public String getValidator() {
        return null;
    }
    
    private class TemplateListFrame extends JFrame {

        HashMap<String, Object> attributes = new HashMap<>();
        
        public TemplateListFrame(String className, List<LocalObjectLight> availableTemplates) {
        
            final JList<LocalObjectLight> lstAvailableTemplates = new JList<>(availableTemplates.toArray(new LocalObjectLight[0]));
            JScrollPane pnlScrollMain = new JScrollPane(lstAvailableTemplates);
            setTitle(String.format("Available Templates for %s", className));
            setLayout(new BorderLayout());
            setSize(400, 650);
            setLocationRelativeTo(null);
            add(pnlScrollMain);
            
            JPanel pnlButtons = new JPanel();
            pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
            JButton btnCreate = new JButton("Create Special Object");
            pnlButtons.add(btnCreate);
            btnCreate.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocalObjectLight selectedTemplate = lstAvailableTemplates.getSelectedValue();
                    if (selectedTemplate == null)
                        JOptionPane.showMessageDialog(null, "Select a template", "Create Special Object", JOptionPane.INFORMATION_MESSAGE);
                    else {
                        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
                        
                        LocalObjectLight newObject = CommunicationsStub.getInstance().createSpecialObject(selectedTemplate.getClassName(), 
                            selectedObject.getClassName(), selectedObject.getOid(), attributes, selectedTemplate.getOid());
                        
                        if (newObject == null) {
                            NotificationUtil.getInstance().showSimplePopup("Error", 
                                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        } else {
                            ObjectNode selectedNode = Utilities.actionsGlobalContext().lookup(ObjectNode.class);
                            if (selectedNode.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and does not have children. For those cases, let's avoid refreshing their children lists
                                ((AbstractChildren)selectedNode.getChildren()).addNotify();

                            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                            "Special Element created successfully");
                        } 
                    }
                }
            });
            JButton btnClose = new JButton("Close");
            btnClose.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
            pnlButtons.add(btnClose);
            add(pnlButtons, BorderLayout.SOUTH);
        }
    }
}
