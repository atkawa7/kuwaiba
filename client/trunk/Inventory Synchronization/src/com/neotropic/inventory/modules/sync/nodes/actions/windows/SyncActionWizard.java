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
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalSyncFinding;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.core.LocalSyncResult;

/**
 * This frame will be used to display the findings in the synchronization process and 
 * launch the respective action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncActionWizard extends JFrame {
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
    private List<LocalSyncFinding> allFindings;
    private List<LocalSyncFinding> findingsToBeProcessed;
    
    /**
     * Default constructor
     * @param syncGroup The sync group associated to the current sync process
     * @param findings The list of findings to be displayed
     * @param listener The callback object that will listen for 
     */
    public SyncActionWizard(LocalSyncGroup syncGroup, final List<LocalSyncFinding> findings) throws IllegalArgumentException {
        this.allFindings = findings;
        this.syncGroup = syncGroup;
        this.findingsToBeProcessed = new ArrayList<>();
        
        if (findings.isEmpty())
            throw new IllegalArgumentException("The list of findings can not empty");
        
        setSize(800, 400);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout(5, 5));
        //setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        
        
        txtFindingDescription = new JTextArea(5, 10);
        txtFindingDescription.setLineWrap(true);
        add(new JScrollPane(txtFindingDescription), BorderLayout.NORTH);
        
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
                if (JOptionPane.showConfirmDialog(SyncActionWizard.this, 
                        "Are you sure you want to stop reviewing the findings? No changes will be commited", 
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
        
        btnExecute.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findingsToBeProcessed.add(allFindings.get(currentFinding));
                
                if (currentFinding == allFindings.size() - 1) {
                    JOptionPane.showMessageDialog(SyncActionWizard.this, "You have reviewed all the synchronization findings. The selected actions will be performed now", "Information", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    List<LocalSyncResult> executSyncActions = CommunicationsStub.getInstance().executeSyncActions(findingsToBeProcessed);
                    SyncResultsFrame syncResultFrame = new SyncResultsFrame(SyncActionWizard.this.syncGroup, executSyncActions);
                    syncResultFrame.setVisible(true);
                } else
                    renderNextFinding();
            }
        });
        
        renderCurrentFinding();
    }
    
    public final void renderCurrentFinding () {
        LocalSyncFinding finding = allFindings.get(currentFinding);
        setTitle(String.format("Findings in %s [%s] - %s/%s", syncGroup.getName(), syncGroup.getProvider(), currentFinding + 1, allFindings.size()));
        txtFindingDescription.setText(finding.getDescription());
        pnlScrollMain.setViewportView(buildExtraInformationComponentFromJSON(finding.getExtraInformation()));
        
        if (currentFinding == allFindings.size() - 1)
            btnExecute.setText("Finish");
        
        if (finding.getType() == LocalSyncFinding.EVENT_ERROR) {
            btnExecute.setEnabled(false);
        } else {
            btnExecute.setEnabled(true);
        }
    }
    
    public void renderNextFinding() {
        currentFinding++;
        renderCurrentFinding();
    }
    
    /**
     * Builds a JTree based on a JSON string that defines a containment hierarchy, normally used 
     * to depict a branch that will be modified in the associated device 
     * @param jsonString The tree definition as a JSON document
     * @return A tree with the structured defined in the JSON document
     */
    public JComponent buildExtraInformationComponentFromJSON(String jsonString) {
        
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject root = jsonReader.readObject();
        String type = root.getString("type");
        if(type.equals("branch")){
            DefaultMutableTreeNode rootNode =
                new DefaultMutableTreeNode("Root Device");

            JTree tree = new JTree(rootNode);
            JsonArray children = root.getJsonArray("children");

            int row = 0;
            DefaultMutableTreeNode currentNode = rootNode;
            for (JsonValue item : children) {
                jsonReader = Json.createReader(new StringReader(item.toString()));
                JsonObject obj = jsonReader.readObject().getJsonObject("child");
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(obj.getJsonObject("attributes").getString("name") + "[" + obj.getString("className")+"]");
                currentNode.add(newNode);
                tree.expandRow(row);
                currentNode = newNode;
                row++;
            }

            return tree;
        }
        else if(type.equals("object_port_move")){
                JLabel lblMsg = new JLabel();
                Long childId = Long.valueOf(root.getString("childId"));
                String className = root.getString("className");
                Long tempParentId = Long.valueOf(root.getString("parentId"));
                String parentClassName = root.getString("parentClassName");
                JsonObject jsonPortAttributes = root.getJsonObject("attributes");
                lblMsg.setText("The port: " + jsonPortAttributes.getString("name") + "[" + className + "] "
                        + "will be updated with this attributes " + 
                        jsonPortAttributes.toString());
                return lblMsg;
        }
        
        else if(type.equals("device")){
             JLabel lblMsg = new JLabel();
            JsonObject jsonAttributes = root.getJsonObject("attributes");
            jsonAttributes.getString("name");
            jsonAttributes.getString("description");
            lblMsg.setText("The device you are tryng to sync will be updated with this new attributes: \n" + jsonAttributes.toString());
            return lblMsg;
        }
        
        else if(type.equals("object_port_no_match")){
            JLabel lblMsg = new JLabel();
            String className = root.getString("className");
            JsonObject jsonPortAttributes = root.getJsonObject("attributes");
            String id;
            if(root.get("id") != null){
                id = root.getString("id");
                lblMsg.setText("The port with id: "+id+ " " + jsonPortAttributes.getString("name") + "["+className+"]");
            }
            else
                lblMsg.setText("The new port found with the sync " + jsonPortAttributes.getString("name") + "["+className+"]");
        } 
        
        return new JLabel("There is no extra information");
    }
    
}
