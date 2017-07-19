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
package org.inventory.core.services.utils;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * A subMenu dialog for composed actions which list a set of items to be selected
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SubMenuDialog extends JDialog {
    private ComposedAction action;
    private JList lstSubMenuItems;
    private JButton btnOk;
    private JButton btnCancel;    
    private static SubMenuDialog instance;
    
    private SubMenuDialog() {
        setModal(true);
        setLocationByPlatform(true);
        setUndecorated(true);
        
        initComponents();
        String escKey = "escKey";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), escKey);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(escKey, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnCancelActionPerformed(e);
            }
        });
    }
    
    public static SubMenuDialog getInstance(String title, ComposedAction action) {
        if (action != null) {
            if (instance == null)
                instance = new SubMenuDialog();
            
            instance.setAction(action);
            instance.setTitle(title);
            
            return instance;
        } else
            return null;
            
    }
    
    public void setAction(ComposedAction action) {
        this.action = action;
    }
    
    private void initComponents() {
        btnOk = new JButton("OK");
        btnOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnOkActionPerformed(e);
            }
        });
        
        btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                btnCancelActionPerformed(e);
            }
        });
        
        JPanel panel = new JPanel();
        
        GridBagLayout layout = new GridBagLayout();
        panel.setLayout(layout);
        
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lstSubMenuItems = new JList(), gbc);
        lstSubMenuItems.setFixedCellWidth(300);
        lstSubMenuItems.setBorder(new EmptyBorder(10, 10, 10, 10));
        lstSubMenuItems.addMouseListener(new MouseAdapter() {
        
            @Override
            public void mouseClicked(MouseEvent e) {
                submenuItemMouseClicked(e);
            }
        });
        lstSubMenuItems.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                submenuItemMouseMoved(e);
            }
        });
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.add(btnOk);
        pnlButtons.add(btnCancel);
        
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(pnlButtons, gbc);
        
        add(panel);
        getRootPane().setDefaultButton(btnOk);
        
        pack();
    }
    
    public SubMenuItem getSelectedSubMenuItem() {
        return (SubMenuItem) lstSubMenuItems.getSelectedValue();
    }
    
    private void submenuItemMouseClicked(MouseEvent e) {
        if (e.getSource() instanceof JList) {
            JList list = (JList) e.getSource();
            if (e.getClickCount() == 2) {
                int index = list.locationToIndex(e.getPoint());
                list.getModel().getElementAt(index);
                setVisible(false);
                action.finalActionPerformed(new ActionEvent(this, -1, ""));
            }
        }
    }
    
    private void submenuItemMouseMoved(MouseEvent e) {
        if (e.getSource() instanceof JList) {
            JList list = (JList) e.getSource();
            int index = list.locationToIndex(e.getPoint());
            if (index > -1) {
                SubMenuItem listItem = (SubMenuItem) list.getModel().getElementAt(index);
                
                String toolTipText = listItem.getToolTipText();
                if (toolTipText != null)
                    list.setToolTipText(toolTipText);
            }
        }
    }
    
    private void btnOkActionPerformed(ActionEvent evt) {
        if (getSelectedSubMenuItem() == null) {
            NotificationUtil.getInstance().showSimplePopup("Warning", 
                NotificationUtil.WARNING_MESSAGE, "Select a item from the list");
        } else {            
            setVisible(false);
            action.finalActionPerformed(new ActionEvent(this, -1, ""));
        }
    }
    
    private void btnCancelActionPerformed(ActionEvent evt) {
        setVisible(false);
    }

    /**
     * Shows a set of items in a subMenu dialog 
     * @param subMenuItems The list of items for the subMenu
     */
    public void showSubmenu(final List<SubMenuItem> subMenuItems) {
        if (subMenuItems == null)
            return;
        
        if (subMenuItems.isEmpty())
            return;
                       
        ListModel<SubMenuItem> listModel = new AbstractListModel() {

            @Override
            public int getSize() {
                return subMenuItems.size();
            }

            @Override
            public Object getElementAt(int index) {
                return subMenuItems.get(index);
            }
        };
        lstSubMenuItems.setModel(listModel);
        pack();
        setVisible(true);
    }
}