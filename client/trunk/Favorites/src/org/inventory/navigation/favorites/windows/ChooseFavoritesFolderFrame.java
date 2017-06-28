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
package org.inventory.navigation.favorites.windows;

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
import org.inventory.communications.core.LocalFavoritesFolder;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Frame for choose a Favorites folder
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ChooseFavoritesFolderFrame extends JFrame {
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAviableFavoritesFolders;
    private final List<LocalObjectLight> selectedObjects;
    private final List<LocalFavoritesFolder> favoritesFolders;
    
    public ChooseFavoritesFolderFrame(List<LocalObjectLight> selectedObjects, List<LocalFavoritesFolder> favoritesFolders) {
        this.selectedObjects = selectedObjects;
        this.favoritesFolders = favoritesFolders;       
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/navigation/favorites/Bundle").getString("LBL_TITLE_AVAILABLE_FAVORITES"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/navigation/favorites/Bundle").getString("LBL_INSTRUCTIONS_SELECT_FAVORITE"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAviableFavoritesFolders = new JList<>(favoritesFolders.toArray(new LocalFavoritesFolder[0]));
        lstAviableFavoritesFolders.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlScrollMain = new JScrollPane();
        txtField = new JTextField();
        txtField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        
        txtField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                favoritesFoldersFilter(txtField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                favoritesFoldersFilter(txtField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                favoritesFoldersFilter(txtField.getText());
            }
        });
        
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtField);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlScrollMain.setViewportView(lstAviableFavoritesFolders);
        add(lstAviableFavoritesFolders, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create Relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BtnAddToFavoritesFolderActionListener());
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
    
    private class BtnAddToFavoritesFolderActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAviableFavoritesFolders.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a Favorites folder from the list");
            else {
                List<String> objectsClassName = new ArrayList();
                List<Long> objectsId = new ArrayList();
                
                for (LocalObjectLight selectedObject : selectedObjects) {
                    objectsClassName.add(selectedObject.getClassName());
                    objectsId.add(selectedObject.getOid());
                    
                    if (CommunicationsStub.getInstance()
                        .addObjectsToFavoritesFolder(objectsClassName, objectsId, ((LocalFavoritesFolder) lstAviableFavoritesFolders.getSelectedValue()).getId())) {
                        
                        
                        JOptionPane.showMessageDialog(null, String.format("%s added to Favorites folder %s", selectedObject, lstAviableFavoritesFolders.getSelectedValue()));
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
    
    public void favoritesFoldersFilter(String text) {
        List<LocalFavoritesFolder> filteredFavoritesFolders = new ArrayList();
        for (LocalFavoritesFolder bookmarkFolder : favoritesFolders) {
            if (bookmarkFolder.getName().toLowerCase().contains(text.toLowerCase()))
                filteredFavoritesFolders.add(bookmarkFolder);
        }
        lstAviableFavoritesFolders.setListData(filteredFavoritesFolders.toArray(new LocalFavoritesFolder[0]));
    }
}
