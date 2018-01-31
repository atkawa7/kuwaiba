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
 *
 */
package org.inventory.core.services.api.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.i18n.I18N;

/**
 * The Frame is use the select a value given a list of possible values and 
 * to execute an action.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SelectValueFrame extends JFrame {
    private List<ComposedAction> listeners;
    private JTextField txtSearch;
    private final JScrollPane pnlMain;
    private final JList lstFilteredObjects;
    
    private final List<Object> possibleValues;
    
    public SelectValueFrame(String title, String instructions, String lblAction, List objects) {
        this.possibleValues = objects;
        
        setLayout(new BorderLayout());
        setTitle(title);
        setSize(400, 650);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(instructions);
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        
        lstFilteredObjects = new JList<>(objects.toArray(new Object[0]));
        lstFilteredObjects.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstFilteredObjects.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    e.setSource(SelectValueFrame.this);
                    btnActionPerformed(new ActionEvent(SelectValueFrame.this, -1, ""));
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
        
        pnlMain = new JScrollPane();
        txtSearch = new JTextField();
        txtSearch.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filter(txtSearch.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filter(txtSearch.getText());                
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filter(txtSearch.getText());                
            }
        });
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtSearch);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlMain.setViewportView(lstFilteredObjects);
        add(lstFilteredObjects, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        
        JButton btnAction = new JButton(lblAction);
        btnAction.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                e.setSource(SelectValueFrame.this);
                btnActionPerformed(e);
            }
        });
        pnlButtons.add(btnAction);
        
        JButton btnClose = new JButton(I18N.gm("close"));
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);
    }
    
    public Object getSelectedValue() {
        return lstFilteredObjects.getSelectedValue();
    }
        
    private void btnActionPerformed(ActionEvent actionEvent) {
        fireChangeEvent(actionEvent);
    }
    
    public void addListener(ComposedAction listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        
        if (!listeners.contains(listener))
            listeners.add(listener);
    }        
    
    public void removeListener(ComposedAction listener) {
        listeners.remove(listener);
    }
    
    public void removeAllListener() {
        while (!listeners.isEmpty())
            listeners.remove(listeners.get(0));
    }
    
    public void fireChangeEvent(ActionEvent ev) {
        for (ComposedAction listener : listeners)
            listener.finalActionPerformed(ev);
    }
        
    public void filter(String text) {
        List<Object> filteredObjects = new ArrayList<>();
        for (Object possibleValue : possibleValues) {
            if (possibleValue.toString().toLowerCase().contains(text.toLowerCase()))
                filteredObjects.add(possibleValue);
        }
        lstFilteredObjects.setListData(filteredObjects.toArray(new Object[0]));
    }
}
