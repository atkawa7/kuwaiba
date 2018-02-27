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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.LocalSyncFinding;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.core.LocalSyncResult;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.RequestProcessor;

/**
 * This frame will be used to display the findings in the synchronization process and 
 * launch the respective action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncActionsFrame extends JFrame {
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
    private static final Border normalBorder = new EmptyBorder(2, 2, 2, 2);
    private static final Border alarmBorder = new LineBorder(Color.RED, 1);
    
    /**
     * Default constructor
     * @param syncGroup The sync group associated to the current sync process
     * @param findings The list of findings to be displayed
     * @param listener The callback object that will listen for 
     */
    public SyncActionsFrame(LocalSyncGroup syncGroup, final List<LocalSyncFinding> findings) throws IllegalArgumentException {
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
        btnExecute = new JButton("Add to Execution Queue");
        
        pnlBottom.add(btnExecute);
        pnlBottom.add(btnSkip);
        pnlBottom.add(btnClose);
        
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JOptionPane.showConfirmDialog(SyncActionsFrame.this, 
                        "Are you sure you want to stop reviewing the findings? No changes will be committed", 
                        I18N.gm("information"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)
                    dispose();
            }
        });
        
        btnSkip.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFinding == allFindings.size() - 1) {
                    JOptionPane.showMessageDialog(SyncActionsFrame.this, "You have reviewed all the synchronization findings. The selected actions will be performed now", "Information", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    
                    final ProgressHandle progr = ProgressHandleFactory.createHandle(String.format("Executing sync actions for %s", SyncActionsFrame.this.syncGroup.getName()));
                    Runnable executeSyncActions = new Runnable() {
                        
                        @Override
                        public void run() {
                            List<LocalSyncResult> executSyncActions = CommunicationsStub.getInstance().executeSyncActions(findingsToBeProcessed);
                            SyncResultsFrame syncResultFrame = new SyncResultsFrame(SyncActionsFrame.this.syncGroup, executSyncActions);
                            syncResultFrame.setVisible(true);
                            progr.finish();
                        }

                    };
                    RequestProcessor.getDefault().post(executeSyncActions);
                    progr.start();
                } else {
                    currentFinding++;
                    renderCurrentFinding();
                }
            }
        });
        
        btnExecute.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                findingsToBeProcessed.add(allFindings.get(currentFinding));
                
                if (currentFinding == allFindings.size() - 1) {
                    if (findingsToBeProcessed.isEmpty())
                        JOptionPane.showMessageDialog(SyncActionsFrame.this, 
                                "You have reviewed all the synchronization findings with no selected actions to perform", "Information", JOptionPane.INFORMATION_MESSAGE);
                    else {
                        JOptionPane.showMessageDialog(SyncActionsFrame.this, 
                                "You have reviewed all the synchronization findings. The selected actions will be performed now", "Information", JOptionPane.INFORMATION_MESSAGE);
                        List<LocalSyncResult> executSyncActions = CommunicationsStub.getInstance().executeSyncActions(findingsToBeProcessed);
                        SyncResultsFrame syncResultFrame = new SyncResultsFrame(SyncActionsFrame.this.syncGroup, executSyncActions);
                        syncResultFrame.setVisible(true);
                    }
                    dispose();
                } else {
                    currentFinding++;
                    renderCurrentFinding();
                }
            }
        });
        
        renderCurrentFinding();
    }
    
    public final void renderCurrentFinding () {
        LocalSyncFinding finding = allFindings.get(currentFinding);
        setTitle(String.format("Findings in %s [%s] - %s/%s", syncGroup.getName(), syncGroup.getProvider(), currentFinding + 1, allFindings.size()));
        txtFindingDescription.setText(finding.getDescription() != null ? finding.getDescription() : "Empty");
        pnlScrollMain.setViewportView(buildExtraInformationComponentFromJSON(finding.getExtraInformation()));
        
        if (currentFinding == allFindings.size() - 1) {
            btnExecute.setText("Add to Queue and Finish");
            btnSkip.setText("Skip and Finish");
        }
        
        if (finding.getType() == LocalSyncFinding.EVENT_ERROR) {
            btnExecute.setEnabled(false);
            pnlScrollMain.setBorder(alarmBorder);
            btnSkip.setText("Next");
        }
        else if (finding.getType() == LocalSyncFinding.EVENT_DELETE)
            pnlScrollMain.setBorder(alarmBorder);
        else {
            btnExecute.setEnabled(true);
            pnlScrollMain.setBorder(normalBorder);
            btnSkip.setText("Skip");
        }
    }
    
    /**
     * Builds a JTree based on a JSON string that defines a containment hierarchy, normally used 
     * to depict a branch that will be modified in the associated device
     * TODO: Rewrite this so it does not depend on the format returned by the reference implementation
     * @param jsonString The tree definition as a JSON document
     * @return A tree with the structured defined in the JSON document
     */
    public JComponent buildExtraInformationComponentFromJSON(String jsonString) {
        if (jsonString == null)
            return new JLabel("There is no extra information");
        
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject root = jsonReader.readObject();
        String type = root.getString("type");
        switch (type) {
            case "branch":
                DefaultMutableTreeNode rootNode =
                        new DefaultMutableTreeNode("Root Device");
                
                JTree tree = new JTree(rootNode);
                JsonArray children = root.getJsonArray("children");
                
                int row = 0;
                DefaultMutableTreeNode currentNode = rootNode;
                
                for (JsonValue item : children) {
                    jsonReader = Json.createReader(new StringReader(item.toString()));
                    JsonObject obj = jsonReader.readObject().getJsonObject("child");
                    if(row == 0)
                        rootNode.setUserObject(obj.getString("parentName") + "["+obj.getString("parentClassName")+"]");
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(obj.getJsonObject("attributes").getString("name") + "[" + obj.getString("className")+"]");
                    currentNode.add(newNode);
                    tree.expandRow(row);
                    currentNode = newNode;
                    row++;
                }
                
                return tree;
            case "object_port_move":
                String className = root.getString("className");
                JsonObject jsonPortAttributes = root.getJsonObject("attributes");
                
                return new JLabel("The port: " + jsonPortAttributes.getString("name") + "[" + className + "] "
                            + "will be updated with these attributes " + getAttributesWithNames(root,Json.createObjectBuilder().build() , jsonPortAttributes));
            case "device":
                return new JLabel("The atributes will be updated" + 
                        getAttributesWithNames(root, root.getJsonObject("oldAttributes"), root.getJsonObject("attributes")));
            
            case "error":
                className="";
                if(root.get("className") != null)
                    className = root.getString("className");
                String attributeName = "";
                type = "";
                String instanceId = "";
                if(root.get("attributeName") != null)
                    attributeName = root.getString("attributeName");
                if(root.get("InstanceId") != null)
                    instanceId = root.getString("InstanceId");
                if(root.get("attributeType") != null)
                    type = root.getString("attributeType");
                if(!instanceId.isEmpty())
                    return new JLabel((String.format(I18N.gm("class_name_undertemined"), 
                        instanceId, className)));
                else
                    return new JLabel((String.format(I18N.gm("create_an_attribute_in_class"), 
                        attributeName, type, className)));
                
            case "object_port_no_match":
                className = root.getString("className");
                jsonPortAttributes = root.getJsonObject("attributes");
                String id;
                if(root.get("id") != null){
                    id = root.getString("id");
                    return new JLabel("The port with id: " + id+ " " + jsonPortAttributes.getString("name") + "["+className+"]");
                }
            case "hierarchy":
                String msg = "";
                JsonObject jsonObject = root.getJsonObject("hierarchy");
                for (Map.Entry<String, JsonValue> entry : jsonObject.entrySet()) {
                    String theClass = "";
                    String theChildren = "";
                    JsonReader childReader = Json.createReader(new StringReader(entry.getValue().toString()));
                    children = childReader.readArray();
                    theClass = entry.getKey();
                    for (JsonValue child : children) {
                        JsonReader classReader = Json.createReader(new StringReader(child.toString()));
                        JsonObject childObj = classReader.readObject();
                        theChildren += childObj.getString("child") + ", ";
                    }
                    msg += theClass + " => " + theChildren + " - ";
                }
                return new JLabel(msg);
        }
        
        return new JLabel("There is no extra information");
    }
    
    /**
     * To translate the list type attributes form their id their names 
     * @param comparedAttributes the attributes with ids
     * @return a hash map of translated attributes
     * @throws MetadataObjectNotFoundException if some class name given to search a list type doesn't exists
     * @throws InvalidArgumentException if some class name given to search a list type is not a list type
     * @throws ObjectNotFoundException the given list type id doesn't exists
     */
    private String getAttributesWithNames(JsonObject obj, JsonObject oldAttributes, JsonObject attributes){
        String newAttrs = "[";
        String oldAttrs = "[";
        for (String key : attributes.keySet()) {
            if(LocalClassMetadata.getMappingFromType(key) == Constants.MAPPING_MANYTOONE &&
               isNumeric(attributes.getString(key)))
            {
                LocalClassMetadata objectMetadata = CommunicationsStub.getInstance().getObjectInfo(obj.getString("deviceClassName"), Long.valueOf(obj.getString("deviceId"))).getObjectMetadata();
                String[] attributesNames = objectMetadata.getAttributesNames();
                String listType = "";
                for (int i=0; i<attributesNames.length; i++) {
                    if(attributesNames[i].equals(key)){
                        listType = objectMetadata.getAttributesTypes()[i];
                        break;
                    }
                }
                if(listType != null){
                    LocalObjectListItem listTypeItem = CommunicationsStub.getInstance().getListTypeItem(listType, Long.valueOf(attributes.getString(key)));
                    if(listTypeItem != null)
                        newAttrs += key + ": " + listTypeItem.getName()+"; ";
                    if(oldAttributes != null && !oldAttributes.isEmpty()){
                        if(oldAttributes.get(key) != null){
                            long listtypeId = Long.valueOf(oldAttributes.getString(key));
                            listTypeItem = CommunicationsStub.getInstance().getListTypeItem(listType, listtypeId);
                            if(listTypeItem != null)
                                oldAttrs += key + ": " + listTypeItem.getName()+"; ";
                        }
                    }
                    else
                        oldAttrs += key + ":; ";
                }
            }
            else{
                newAttrs += (key + ": ") + (attributes.get(key)  != null ? attributes.getString(key) : "") + "; ";
                if(oldAttributes != null)
                    oldAttrs += key + ": " + (oldAttributes.get(key) != null ? oldAttributes.getString(key) : "") + "; ";
            }
        }
        newAttrs = newAttrs.substring(0, newAttrs.length()-2) + "]";
        oldAttrs = oldAttrs.length() > 1 ? (oldAttrs.substring(0, oldAttrs.length()-2) + "]") : "[]";
        
        return oldAttributes != null ? String.format(" - from: %s - to: %s -", oldAttrs, newAttrs) : newAttrs;
    }
    
    public static boolean isNumeric(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
