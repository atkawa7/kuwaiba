/*
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
 * 
 */
package org.inventory.navigation.bookmarks.windows;

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
import org.inventory.communications.core.LocalBookmarkFolder;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.util.Lookup;

/**
 * Frame for choose a Bookmark folder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ChooseBookmarkFolderFrame extends JFrame {
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAviableBookmarkFolders;
    private final List<LocalObjectLight> selectedObjects;
    private final List<LocalBookmarkFolder> bookmarkFolders;
    
    public ChooseBookmarkFolderFrame(List<LocalObjectLight> selectedObjects, List<LocalBookmarkFolder> bookmarkFolders) {
        this.selectedObjects = selectedObjects;
        this.bookmarkFolders = bookmarkFolders;       
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/navigation/bookmarks/Bundle").getString("LBL_TITLE_AVAILABLE_BOOKMARKS"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/navigation/bookmarks/Bundle").getString("LBL_INSTRUCTIONS_SELECT_BOOKMARK"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAviableBookmarkFolders = new JList<>(bookmarkFolders.toArray(new LocalBookmarkFolder[0]));
        lstAviableBookmarkFolders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlScrollMain = new JScrollPane();
        txtField = new JTextField();
        txtField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        
        txtField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                bookmarkFoldersFilter(txtField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                bookmarkFoldersFilter(txtField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                bookmarkFoldersFilter(txtField.getText());
            }
        });
        
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtField);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlScrollMain.setViewportView(lstAviableBookmarkFolders);
        add(lstAviableBookmarkFolders, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create Relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BtnAddToBookmarkFolderActionListener());
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
    
    private class BtnAddToBookmarkFolderActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAviableBookmarkFolders.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a Bookmark folder from the list");
            else {
                List<String> objectsClassName = new ArrayList();
                List<Long> objectsId = new ArrayList();
                
                for (LocalObjectLight selectedObject : selectedObjects) {
                    objectsClassName.add(selectedObject.getClassName());
                    objectsId.add(selectedObject.getOid());
                    
                    if (CommunicationsStub.getInstance()
                        .addObjectsToBookmarkFolder(objectsClassName, objectsId, ((LocalBookmarkFolder) lstAviableBookmarkFolders.getSelectedValue()).getId())) {
                        
                        
                        JOptionPane.showMessageDialog(null, String.format("%s added to Bookmark folder %s", selectedObject, lstAviableBookmarkFolders.getSelectedValue()));
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    
                    objectsClassName.clear();
                    objectsId.clear();
                }
            }
        }
    }
    
    public void bookmarkFoldersFilter(String text) {
        List<LocalBookmarkFolder> filteredBookmarkFolders = new ArrayList();
        for (LocalBookmarkFolder bookmarkFolder : bookmarkFolders) {
            if (bookmarkFolder.getName().toLowerCase().contains(text.toLowerCase()))
                filteredBookmarkFolders.add(bookmarkFolder);
        }
        lstAviableBookmarkFolders.setListData(filteredBookmarkFolders.toArray(new LocalBookmarkFolder[0]));
    }
}
