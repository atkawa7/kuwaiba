/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.AbstractAction;
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
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter.Popup;

/**
 * Creates an inventory object from a template
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateBusinessObjectFromTemplateAction extends AbstractAction implements Popup {
    private CommunicationsStub com = CommunicationsStub.getInstance();

    @Override
    public void actionPerformed(ActionEvent ev) {
        String className = ((JMenuItem)ev.getSource()).getName();
        List<LocalObjectLight> templates = com.getTemplatesForClass(className);
        
        if (templates == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (templates.isEmpty())
                JOptionPane.showMessageDialog(null, "No templates were defined for this class", "Error", JOptionPane.INFORMATION_MESSAGE);
            else {
                TemplateListFrame templatesFrame = new TemplateListFrame(className, templates);
                templatesFrame.setVisible(true);
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_FROM_TEMPLATE"));

        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
        
        List<LocalClassMetadataLight> items;
        if (selectedObject == null)  //The root node
            items = com.getPossibleChildren(Constants.DUMMYROOT, false);
        else 
            items = com.getPossibleChildren(selectedObject.getClassName(), false);
        
        if (items == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.INFO_MESSAGE,
                com.getError());
            mnuPossibleChildren.setEnabled(false);
        }
        else {
            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

            MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
        }
        return mnuPossibleChildren;
    }
    
    private class TemplateListFrame extends JFrame {

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
            JButton btnCreate = new JButton("Create Object");
            pnlButtons.add(btnCreate);
            btnCreate.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent ae) {
                    LocalObjectLight selectedTemplate = lstAvailableTemplates.getSelectedValue();
                    if (selectedTemplate == null)
                        JOptionPane.showMessageDialog(null, "Select a template", "Create Object", JOptionPane.INFORMATION_MESSAGE);
                    else {
                        LocalObjectLight selectedObject = Utilities.actionsGlobalContext().lookup(LocalObjectLight.class);
                        LocalObjectLight newObject = CommunicationsStub.getInstance().createObject(selectedTemplate.getClassName(), 
                                selectedObject.getClassName(), selectedObject.getOid(), selectedTemplate.getOid());
                        if (newObject == null)
                            NotificationUtil.getInstance().showSimplePopup("Error", 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        else {
                            AbstractNode selectedNode = Utilities.actionsGlobalContext().lookup(AbstractNode.class);
                            if (selectedNode.getChildren() instanceof AbstractChildren) //Some nodes are created on the fly and does not have children. For those cases, let's avoid refreshing their children lists
                                ((AbstractChildren)selectedNode.getChildren()).addNotify();

                            NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE,
                                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
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