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
 */

package com.neotropic.inventory.modules.sync.nodes.actions.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.inventory.communications.core.LocalSyncFinding;
import org.inventory.communications.core.LocalSyncGroup;

/**
 * This frame will be used to display the findings in the synchronization process and 
 * launch the respective action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncActionWizard extends JFrame {
    /**
     * The findings to be displayed
     */
    private List<LocalSyncFinding> findingsToDisplay;
    /**
     * The current finding on display
     */
    private int currentFinding = 0;
    /**
     * Sync group associated to this sync process
     */
    private LocalSyncGroup syncGroup;
    /**
     * Label that displays the finding's textual description
     */
    private JTextArea txtFindingDescription;
    private JScrollPane pnlScrollMain;
    private JButton btnExecute;
    private JButton btnClose;
    private JButton btnSkip;
    
    /**
     * Default constructor
     * @param syncGroup The sync group associated to the current sync process
     * @param findings The list of findings to be displayed
     * @param listener The callback object that will listen for 
     */
    public SyncActionWizard(LocalSyncGroup syncGroup, List<LocalSyncFinding> findings, final ActionListener listener) throws IllegalArgumentException {
        
        if (findings.isEmpty())
            throw new IllegalArgumentException("The list of findings can not empty");
        
        this.syncGroup = syncGroup;
        
        setSize(400, 800);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(5, 5));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        this.findingsToDisplay = findings;
        
        txtFindingDescription = new JTextArea(3, 100);
        add(txtFindingDescription, BorderLayout.NORTH);
        
        pnlScrollMain = new JScrollPane();
        add(pnlScrollMain, BorderLayout.CENTER);
        
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        add(pnlBottom, BorderLayout.SOUTH);
        
        btnClose = new JButton("Close");
        btnSkip = new JButton("Skip");
        btnExecute = new JButton("Execute");
        
        pnlBottom.add(btnExecute);
        pnlBottom.add(btnSkip);
        pnlBottom.add(btnClose);
        
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(null, 
                        "Are you sure you want to stop reviewing the findings? The remaining ones will be ignored", 
                        "Information", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)
                    dispose();
            }
        });
        
        btnSkip.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                renderNextFinding();
            }
        });
        
        btnExecute.addActionListener(listener);
        
        renderCurrentFinding();
    }
    
    public final void renderCurrentFinding () {
        LocalSyncFinding finding = findingsToDisplay.get(currentFinding);
        txtFindingDescription.setText(finding.getDescription());
        setTitle(String.format("Findings in %s [%s] - %s/%s", syncGroup.getName(), syncGroup.getProvider(), currentFinding + 1, findingsToDisplay.size()));
        pnlScrollMain.setViewportView(createTreeFromJSON(finding.getExtraInformation()));
        
    }
    
    public void renderNextFinding() {
        if (currentFinding < findingsToDisplay.size() - 1) {
            currentFinding++;
            renderCurrentFinding();
        } else {
            JOptionPane.showMessageDialog(null, "You have reviewed all the synchronization findings", "Information", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        }
    }
    
    /**
     * Builds a JTree based on a JSON string that defines a containment hierarchy, normally used 
     * to depict a branch that will be modified in the associated device 
     * @param jsonString The tree definition as a JSON document
     * @return A tree with the structured defined in the JSON document
     */
    public static JTree createTreeFromJSON(String jsonString) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject root = jsonReader.readObject();
        DefaultMutableTreeNode rootNode =
            new DefaultMutableTreeNode("Root Device");

        JTree tree = new JTree(rootNode);

        JsonArray items = root.getJsonArray("branch");

        int row = 0;
        DefaultMutableTreeNode currentNode = rootNode;
        for (JsonValue item : items) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(item);
            currentNode.add(newNode);
            tree.expandRow(row);
            currentNode = newNode;
            row++;
        }
        
        return tree;
    }
    
}