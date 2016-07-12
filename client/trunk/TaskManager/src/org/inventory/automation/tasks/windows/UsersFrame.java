/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.automation.tasks.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.inventory.automation.tasks.nodes.TaskNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UsersFrame extends JFrame {
    
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList<LocalUserObject> lstAvailableUsers;
    private TaskNode selectedTaskNode;
    private List<LocalUserObject> users;
    
    
    public UsersFrame(TaskNode selectedTaskNode, List<LocalUserObject> users) {
        this.selectedTaskNode = selectedTaskNode;
        this.users = users;
        setLayout(new BorderLayout());
        setTitle("Available Users");
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel("Select a user from the list");
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableUsers = new JList<>(users.toArray(new LocalUserObject[0]));
        lstAvailableUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlScrollMain = new JScrollPane();
        txtField = new JTextField();
        txtField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        txtField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }
        });
        
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtField);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlScrollMain.setViewportView(lstAvailableUsers);
        add(lstAvailableUsers, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Subscribe");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BtnConnectActionListener());
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
    
    private class BtnConnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAvailableUsers.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a user from the list");
            else {
                CommunicationsStub com =CommunicationsStub.getInstance();
                if (com.subscribeUserToTask(lstAvailableUsers.getSelectedValue().getUserId(), 
                        selectedTaskNode.getLookup().lookup(LocalTask.class).getId())) {
                    
                    JOptionPane.showMessageDialog(null, "User subscribed successfully");
                    
                    LocalTask task = selectedTaskNode.getLookup().lookup(LocalTask.class);
                    
                    List<LocalUserObjectLight> subscribers = com.getSubscribersForTask(task.getId());
                
                    if (subscribers == null)
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        task.setUsers(subscribers);
                        ((TaskNode.TaskChildren)selectedTaskNode.getChildren()).addNotify();
                    }
                    
                    dispose();
                } else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void servicesFilter(String text){
        List<LocalUserObject> filteredUsers = new ArrayList<>();
        for(LocalUserObject service : users){
            if(service.getUserName().toLowerCase().contains(text.toLowerCase()) 
                    || service.getFirstName().toLowerCase().contains(text.toLowerCase()))
                filteredUsers.add(service);
        }
        lstAvailableUsers.setListData(filteredUsers.toArray(new LocalUserObject[0]));
    }
}