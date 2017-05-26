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
package com.neotropic.inventory.modules.projects.windows;

import com.neotropic.inventory.modules.projects.ProjectsModuleService;
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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Frame to choose a Project type
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectsFrame extends JFrame {    
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAviableProjects;
    private final List<LocalObjectLight> selectedObjects;
    private final List<LocalObjectLight> projects;
    
    public ProjectsFrame(List<LocalObjectLight> selectedObjects, List<LocalObjectLight> projects) {
        this.selectedObjects = selectedObjects;
        this.projects = projects;
        
        setLayout(new BorderLayout());
        setTitle(ProjectsModuleService.bundle.getString("LBL_TITLE_AVAILABLE_PROJECTS"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(ProjectsModuleService.bundle.getString("LBL_INSTRUCTIONS_SELECT_PROJECTS"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        
        lstAviableProjects = new JList<>(projects.toArray(new LocalObjectLight[0]));
        lstAviableProjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlScrollMain = new JScrollPane();
        txtField = new JTextField();
        txtField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        
        txtField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                projectsFilter(txtField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                projectsFilter(txtField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                projectsFilter(txtField.getText());
            }
        });
        
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtField);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlScrollMain.setViewportView(lstAviableProjects);
        add(lstAviableProjects, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create Relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BtnAddToProjectActionListener());
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
    
    private class BtnAddToProjectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAviableProjects.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a project from the list");
            else {
                
                for (LocalObjectLight selectedObject : selectedObjects) {
                    
                    long objId = selectedObject.getOid();
                    String objClassName = selectedObject.getClassName();
                    long projectId = ((LocalObjectLight) lstAviableProjects.getSelectedValue()).getOid();
                    String projectClass = ((LocalObjectLight) lstAviableProjects.getSelectedValue()).getClassName();
                    
                    if (CommunicationsStub.getInstance().associateObjectToProject(projectClass, projectId, objClassName, objId)) {
                        
                        JOptionPane.showMessageDialog(null, String.format("%s added to project %s", selectedObject, lstAviableProjects.getSelectedValue()));
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    
    
    public void projectsFilter(String text) {
        List<LocalObjectLight> filteredProjects = new ArrayList();
        for (LocalObjectLight project : projects) {
            if (project.getName().toLowerCase().contains(text.toLowerCase()))
                filteredProjects.add(project);
        }
        lstAviableProjects.setListData(filteredProjects.toArray(new LocalObjectLight[0]));
    }
}
