/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.*;
import com.neotropic.inventory.modules.sync.nodes.SyncDataSourceConfigurationNode;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.windows.SelectValueFrame;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.navigationtree.windows.ObjectEditorTopComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates/edits the data source configuration of the object
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
@ActionsGroupType(group=ActionsGroupType.Group.HAS_CONFIGURATION)
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ConfigSyncDatasourceAction extends GenericObjectNodeAction implements ComposedAction{

    public ConfigSyncDatasourceAction() {
        putValue(NAME, "Config Datasource");
    }
    
    private JComponent setSize(JComponent component) {
        Dimension size = new Dimension(200, 20);
        
        component.setMinimumSize(size);
        component.setMaximumSize(size);
        component.setPreferredSize(size);
        component.setSize(size);
        return component;
    }
        
    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
         return new String[] {Constants.CLASS_GENERICNETWORKELEMENT};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        List<LocalSyncGroup> syncGroups = CommunicationsStub.getInstance().getSyncGroups();
        
        LocalSyncDataSourceConfiguration syncDataSourceConfiguration = CommunicationsStub.getInstance().getSyncDataSourceConfiguration(selectedObjects.get(0).getId());
        
        if (syncGroups ==  null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (syncGroups.isEmpty()) {
                JOptionPane.showMessageDialog(null, "There are no sync gropus created. Create at least one using the Sync Manager", 
                    I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            } else {
                if(syncDataSourceConfiguration == null){    
                    SelectValueFrame frame = new SelectValueFrame(
                        "Available Sync Groups",
                        "Search",
                        "Create Relationship", syncGroups);
                    frame.addListener(this);
                    frame.setVisible(true);
                }
                else
                       NotificationUtil.getInstance().showSimplePopup("Datasource is already created", 
                               NotificationUtil.INFO_MESSAGE, "Edit in property sheet");
            }
        }
        
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e.getSource() instanceof SelectValueFrame) 
        {
            SelectValueFrame frame = (SelectValueFrame) e.getSource();
            Object selectedSyncGroup =  frame.getSelectedValue();
            
            if (selectedSyncGroup  == null)
                JOptionPane.showMessageDialog(null, "Select a sync group from the list");


            HashMap<String, String> parameters = new HashMap();
                        parameters.put("deviceId", String.valueOf((selectedObjects.get(0).getId())));
                        parameters.put("deviceClass", (selectedObjects.get(0).getClassName()));
        
            LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
                                    createSyncDataSourceConfiguration(
                                            selectedObjects.get(0).getId(),
                                            ((LocalSyncGroup) selectedSyncGroup ).getId(), 
                                        selectedObjects.get(0).getName() + " DataSource configuration", parameters);
            
          
            SyncDataSourceConfigurationNode syncDataSourceConfigurationNode = new SyncDataSourceConfigurationNode(newSyncConfig);
            
            
            ObjectEditorTopComponent component = new ObjectEditorTopComponent(syncDataSourceConfigurationNode);
            component.open();
            component.requestActive();

        }
    

    
//        if (e.getSource() instanceof SelectValueFrame) {
//            SelectValueFrame frame = (SelectValueFrame) e.getSource();
//            Object selectedValue = frame.getSelectedValue();
//            
//            if (selectedValue == null)
//                JOptionPane.showMessageDialog(null, "Select a service from the list");
//            
//            else{
//                List<String> classNames = new ArrayList<>();
//                List<Long> objectIds = new ArrayList<>();
//                //TODO check here
//                for(LocalObjectLight selectedObject : selectedObjects){
//                    classNames.add(selectedObject.getClassName());
//                    objectIds.add(selectedObject.getId());
//                }
//                
//                //if (selectedGroup.getProvider().getId().equals("BridgeDomainSyncProvider")) {
//                
//                //Bridge Domains configuration
//                
//                    JTextField txtBDConfigName = new JTextField(25);
//                    JTextField txtBDConfigHost = new JTextField(25);
//                    JTextField txtBDConfigPort = new JTextField(25);
//                    JTextField txtBDConfigUser = new JTextField(25);
//                    JTextField txtBDConfigPassword = new JPasswordField(25);
//
////                    JComplexDialogPanel pnlNewSshDSConfiguration = new JComplexDialogPanel(new String[] { "Name", "Host", "Port", "User", "Password" }, 
////                            new JComponent[] { txtBDConfigName, txtBDConfigHost, txtBDConfigPort, txtBDConfigUser, txtBDConfigPassword });
////
////                    if (JOptionPane.showConfirmDialog(null, pnlNewSshDSConfiguration, I18N.gm("new_ds_config"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
////                        HashMap<String, String> parameters = new HashMap();
////                        parameters.put("deviceId", String.valueOf((selectedObjects.get(0).getId())));
////                        parameters.put("deviceClass", (selectedObjects.get(0).getClassName()));
////                        parameters.put("ipAddress", txtBDConfigHost.getText());
////                        parameters.put("port", txtBDConfigPort.getText());
////                        parameters.put("user", txtBDConfigUser.getText());
////                        parameters.put("password", txtBDConfigPassword.getText());
////
////                        LocalSyncDataSourceConfiguration newSyncDataSourceConfiguration = 
////                                CommunicationsStub.getInstance().createSyncDataSourceConfiguration(
////                                        ((LocalObjectLight) selectedValue).getId(), 
////                                        txtBDConfigName.getText(), parameters);
////                        
////                        if (newSyncDataSourceConfiguration == null) {
////                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
////                                CommunicationsStub.getInstance().getError());
////                        } 
////                    }
//                    
//                    
////                  else {
////                            ((SyncGroupNode.SyncGroupNodeChildren) selectedNode.getChildren()).addNotify();
////
////                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
////                                NotificationUtil.INFO_MESSAGE, I18N.gm("new_sync_config_created_successfully"));
////                        }
////                    }
//
//
////                        
//                    JPanel pnlName = new JPanel();
//                    JTextField txtSyncDataSourceConfigName = (JTextField) pnlName.add(setSize(new JTextField()));
//                    txtSyncDataSourceConfigName.setName("txtSyncDataSourceConfigName");
//                    pnlName.add(new JLabel("*")).setForeground(Color.RED);
//
////                    JPanel pnlDevices = new JPanel();
//                    //TODO erease this
//                    //JComboBox<LocalObjectLight> cmbDevices = (JComboBox) pnlDevices.add(setSize(new JComboBox(commDevices.toArray(new LocalObjectLight[0]))));
////                    cmbDevices.setName("cmbDevices");
////                    pnlDevices.add(new JLabel("*")).setForeground(Color.RED);
//
//                    JPanel pnlIpAddress = new JPanel();
//                    JTextField txtIPAddress = (JTextField) pnlIpAddress.add(setSize(new JTextField()));
//                    txtIPAddress.setName("txtIPAddress");
//                    ((JLabel) pnlIpAddress.add(new JLabel("*"))).setForeground(Color.RED);
//
//                    JPanel pnlSnmpPort = new JPanel();        
//                    JTextField txtSnmpPort = (JTextField) pnlSnmpPort.add(setSize(new JTextField("161")));
//                    txtSnmpPort.setName("txtSnmpPort");
//                    ((JLabel) pnlSnmpPort.add(new JLabel("*"))).setForeground(Color.RED);
//
//                    final String snmpVersion2c = "2c"; // NOI18N
//                    final String snmpVersion3 = "3"; // NOI18N
//                    final String none = "None"; // NOI18N
//
//                    JPanel pnlVersion = new JPanel();
//                    final JComboBox cboVersion = (JComboBox) pnlVersion.add(setSize(new JComboBox()));
//                    cboVersion.setName("cboVersion");
//                    cboVersion.addItem(I18N.gm("select_snmp_version"));
//                    cboVersion.addItem(snmpVersion2c);
//                    cboVersion.addItem(snmpVersion3);
//                    ((JLabel) pnlVersion.add(new JLabel("*"))).setForeground(Color.RED);
//
//                    final JComboBox cboAuthProtocol = (JComboBox) setSize(new JComboBox());
//                    cboAuthProtocol.setName("cboAuthenticationProtocol");
//                    cboAuthProtocol.addItem(none);
//                    cboAuthProtocol.addItem("MD5"); // NOI18N
//                    //cboAuthProtocol.addItem("SHA"); // NOI18N Not supported yet
//                    cboAuthProtocol.setSelectedItem(none);        
//                    cboAuthProtocol.setEnabled(false);
//
//                    final JPasswordField txtAuthPass = (JPasswordField) setSize(new JPasswordField());
//                    txtAuthPass.setName("txtAuthenticationProtocolPassPhrase");
//                    txtAuthPass.setEditable(false);
//
//                    cboAuthProtocol.addActionListener(new ActionListener() {
//
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            if (!none.equals((String) cboAuthProtocol.getSelectedItem()))
//                                txtAuthPass.setEditable(true);
//                            else {
//                                txtAuthPass.setText("");
//                                txtAuthPass.setEditable(false);
//                            }
//                        }
//                    });
//                    /*
//                    final JTextField txtSecurityEngineID = new JTextField();
//                    txtSecurityEngineID.setName("txtSecurityEngineID");
//                    txtSecurityEngineID.setEnabled(false);
//
//                    final JTextField txtContextEngineID = new JTextField();        
//                    txtContextEngineID.setName("txtContextEngineID");
//                    txtContextEngineID.setEnabled(false);
//                    */
//                    final JComboBox cboSecurityLevel = (JComboBox) setSize(new JComboBox());
//                    cboSecurityLevel.setName("cboSecurityLevel");
//                    cboSecurityLevel.addItem("noAuthNoPriv"); // NOI18N
//                    cboSecurityLevel.addItem("authNoPriv"); // NOI18N
//                    cboSecurityLevel.addItem("authPriv"); // NOI18N
//                    cboSecurityLevel.setSelectedItem("noAuthNoPriv"); // NOI18N
//                    cboSecurityLevel.setEnabled(false);
//
//                    final JTextField txtContextName = (JTextField) setSize(new JTextField());
//                    txtContextName.setName("txtContextName");
//                    txtContextName.setEditable(false);        
//
//                    final JTextField txtSecurityName = (JTextField) setSize(new JTextField());
//                    txtSecurityName.setName("txtSecurityName");
//                    txtSecurityName.setEditable(false);
//
//                    final JComboBox cboPrivacyProtocol = (JComboBox) setSize(new JComboBox());
//                    cboPrivacyProtocol.setName("cboPrivacyProtocol");
//                    cboPrivacyProtocol.addItem(none);
//                    cboPrivacyProtocol.addItem("DES"); // NOI18N
//                    //cboPrivacyProtocol.addItem("AES"); // NOI18N Not supported yet
//                    cboPrivacyProtocol.setSelectedItem(none);
//                    cboPrivacyProtocol.setEnabled(false);        
//
//                    final JPasswordField txtPrivacyPass = (JPasswordField) setSize(new JPasswordField());
//                    txtPrivacyPass.setName("txtPrivacyProtocolPassPhrase");
//                    txtPrivacyPass.setEditable(false);
//
//                    cboPrivacyProtocol.addActionListener(new ActionListener() {
//
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            if (!none.equals((String) cboPrivacyProtocol.getSelectedItem()))
//                                txtPrivacyPass.setEditable(true);
//                            else {
//                                txtPrivacyPass.setText("");
//                                txtPrivacyPass.setEditable(false);                
//                            }
//                        }
//                    });
//                    final List<JLabel> mandatoryAttrs = new ArrayList();
//
//                    JPanel pnlCommunity = new JPanel();
//                    pnlCommunity.setName(Constants.PROPERTY_COMMUNITY);
//                    final JTextField txtCommunity = (JTextField) pnlCommunity.add(setSize(new JTextField("public")));
//                    txtCommunity.setToolTipText("Default value \"public\"");
//                    txtCommunity.setName("txtcomunity");
//                    txtCommunity.setForeground(Color.GRAY);
//                    txtCommunity.setEditable(false);        
//                    JLabel lblCommunity = (JLabel) pnlCommunity.add(new JLabel(" "));
//                    lblCommunity.setName(Constants.PROPERTY_COMMUNITY);
//                    mandatoryAttrs.add(lblCommunity);
//
//                    JPanel pnlAuthProtocol = new JPanel();
//                    pnlAuthProtocol.setName(Constants.PROPERTY_AUTH_PROTOCOL);
//                    pnlAuthProtocol.add(cboAuthProtocol);
//                    JLabel lblAuthProtocol = (JLabel) pnlAuthProtocol.add(new JLabel(" "));
//                    lblAuthProtocol.setName(Constants.PROPERTY_AUTH_PROTOCOL);
//                    mandatoryAttrs.add(lblAuthProtocol);
//
//                    JPanel pnlAuthPass = new JPanel();
//                    pnlAuthPass.setName(Constants.PROPERTY_AUTH_PASS);
//                    pnlAuthPass.add(txtAuthPass);
//                    JLabel lblAuthPass = (JLabel) pnlAuthPass.add(new JLabel(" "));
//                    lblAuthPass.setName(Constants.PROPERTY_AUTH_PASS);
//                    mandatoryAttrs.add(lblAuthPass);
//
//                    JPanel pnlSecurityLevel = new JPanel();
//                    pnlSecurityLevel.setName(Constants.PROPERTY_SECURITY_LEVEL);
//                    pnlSecurityLevel.add(cboSecurityLevel);
//                    JLabel lblSecurityLevel = (JLabel) pnlSecurityLevel.add(new JLabel(" "));
//                    lblSecurityLevel.setName(Constants.PROPERTY_SECURITY_LEVEL);
//                    mandatoryAttrs.add(lblSecurityLevel);
//
//                    JPanel pnlContextName = new JPanel();
//                    pnlContextName.setName(Constants.PROPERTY_CONTEXT_NAME);
//                    pnlContextName.add(txtContextName);
//                    JLabel lblContextName = (JLabel) pnlContextName.add(new JLabel(" "));
//                    lblContextName.setName(Constants.PROPERTY_SECURITY_LEVEL);
//
//                    JPanel pnlSecurityName = new JPanel();
//                    pnlSecurityName.setName(Constants.PROPERTY_SECURITY_NAME);
//                    pnlSecurityName.add(txtSecurityName);
//                    JLabel lblSecurityName = (JLabel) pnlSecurityName.add(new JLabel(" "));
//                    lblSecurityName.setName(Constants.PROPERTY_SECURITY_NAME);
//                    mandatoryAttrs.add(lblSecurityName);        
//
//                    JPanel pnlPrivacyProtocol = new JPanel();
//                    pnlPrivacyProtocol.setName(Constants.PROPERTY_PRIVACY_PROTOCOL);
//                    pnlPrivacyProtocol.add(cboPrivacyProtocol);
//                    JLabel lblPrivacyProtocol = (JLabel) pnlPrivacyProtocol.add(new JLabel(" "));
//                    lblPrivacyProtocol.setName(Constants.PROPERTY_PRIVACY_PROTOCOL);
//                    mandatoryAttrs.add(lblPrivacyProtocol);
//
//                    JPanel pnlPrivacyPass = new JPanel();
//                    pnlPrivacyPass.setName(Constants.PROPERTY_PRIVACY_PASS);
//                    pnlPrivacyPass.add(txtPrivacyPass);        
//                    JLabel lblPrivacyPass = (JLabel) pnlPrivacyPass.add(new JLabel(" "));
//                    lblPrivacyPass.setName(Constants.PROPERTY_PRIVACY_PASS);
//                    lblPrivacyPass.setForeground(Color.GRAY);
//                    mandatoryAttrs.add(lblPrivacyPass);
//
//                    final JComplexDialogPanel pnlSyncDataSourceProperties = new JComplexDialogPanel(
//                        new String[] {I18N.gm("sync_datasource_config_name"),
//                            I18N.gm("ip_address"), 
//                            I18N.gm("port"), 
//                            I18N.gm("snmp_version"),
//                            I18N.gm("community"), 
//                            I18N.gm("snmp_version_3_security_level"), I18N.gm("snmp_version_3_security_name"), I18N.gm("snmp_version_3_auth_protocol"), I18N.gm("snmp_version_3_auth_pass"), I18N.gm("snmp_version_3_privacy_protocol"), I18N.gm("snmp_version_3_privacy_pass"), I18N.gm("snmp_version_3_context_name"),
//                            "Bridge Domain Configuration",
//                            "Name", "Host", "Port", "User", "Password"}, 
//                        new JComponent[] { pnlName, pnlIpAddress, pnlSnmpPort, 
//                            pnlVersion, 
//                            pnlCommunity,
//                            pnlSecurityLevel, pnlSecurityName, pnlAuthProtocol, pnlAuthPass, pnlPrivacyProtocol, pnlPrivacyPass, pnlContextName,
//                            new JLabel(" --- "),
//                            txtBDConfigName, txtBDConfigHost, txtBDConfigPort, txtBDConfigUser, txtBDConfigPassword}
//                    );
//
//                    cboVersion.addActionListener(new ActionListener() {
//
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            for (JLabel mandatoryAttr :mandatoryAttrs)
//                                mandatoryAttr.setText(" ");
//
//                            List<String> attributes = new ArrayList();
//
//                            String selectedVersion = (String) cboVersion.getSelectedItem();
//
//                            if (I18N.gm("select_snmp_version").equals(selectedVersion)) {
//                                txtCommunity.setText("");
//                                txtCommunity.setEditable(false);
//
//                                cboAuthProtocol.setSelectedItem(none);
//                                cboAuthProtocol.setEnabled(false);
//
//                                txtAuthPass.setText("");
//                                txtAuthPass.setEditable(false);
//                                /*
//                                txtSecurityEngineID.setEditable(false);
//                                txtContextEngineID.setEditable(false);
//                                */
//                                cboSecurityLevel.setEnabled(false);
//
//                                txtContextName.setText("");
//                                txtContextName.setEditable(false);
//
//                                txtSecurityName.setText("");
//                                txtSecurityName.setEditable(false);
//
//                                cboPrivacyProtocol.setSelectedItem(none);
//                                cboPrivacyProtocol.setEnabled(false);
//
//                                txtPrivacyPass.setText("");
//                                txtPrivacyPass.setEditable(false);
//                            }
//                            if (snmpVersion2c.equals(selectedVersion)) {
//                                attributes.add(Constants.PROPERTY_COMMUNITY);
//
//                                txtCommunity.setForeground(Color.BLACK);
//                                txtCommunity.setEditable(true);
//
//                                cboAuthProtocol.setSelectedItem(none);
//                                cboAuthProtocol.setEnabled(false);
//
//                                txtAuthPass.setText("");
//                                txtAuthPass.setEditable(false);
//                                /*
//                                txtSecurityEngineID.setEditable(false);
//                                txtContextEngineID.setEditable(false);
//                                */
//                                cboSecurityLevel.setEnabled(false);
//
//                                txtContextName.setText("");
//                                txtContextName.setEditable(false);
//
//                                txtSecurityName.setText("");
//                                txtSecurityName.setEditable(false);
//
//                                cboPrivacyProtocol.setSelectedItem(none);
//                                cboPrivacyProtocol.setEnabled(false);
//
//                                txtPrivacyPass.setText("");
//                                txtPrivacyPass.setEditable(false);
//                            }
//                            if (snmpVersion3.equals(selectedVersion)) {
//                                txtCommunity.setText("");
//                                txtCommunity.setEditable(false);
//
//                                cboAuthProtocol.setEnabled(true);
//                                /*
//                                txtSecurityEngineID.setEditable(true);
//                                txtContextEngineID.setEditable(true);
//                                */
//                                cboSecurityLevel.setEnabled(true);
//                                cboSecurityLevel.setSelectedItem("noAuthNoPriv");
//                                txtContextName.setEditable(true);
//                                txtSecurityName.setEditable(true);
//                                cboPrivacyProtocol.setEnabled(true);
//                            }
//                            for (JLabel mandatoryAttr :mandatoryAttrs) {
//                                for (String attribute : attributes) {
//                                    if (mandatoryAttr.getName().equals(attribute)) {
//                                        mandatoryAttr.setText("*");
//                                        mandatoryAttr.setForeground(Color.RED);
//                                    }
//                                }
//                            }
//                        }
//                    });
//
//                    cboSecurityLevel.addActionListener(new ActionListener() {
//
//                        @Override
//                        public void actionPerformed(ActionEvent e) {
//                            for (JLabel mandatoryAttr :mandatoryAttrs)
//                                mandatoryAttr.setText(" ");
//
//                            List<String> attributes = new ArrayList();
//                            attributes.add(Constants.PROPERTY_SECURITY_LEVEL);
//
//                            switch ((String) cboSecurityLevel.getSelectedItem()) {
//                                case "noAuthNoPriv":
//                                    attributes.add(Constants.PROPERTY_SECURITY_NAME);
//                                    break;
//                                case "authNoPriv":
//                                    attributes.add(Constants.PROPERTY_AUTH_PROTOCOL);
//                                    attributes.add(Constants.PROPERTY_AUTH_PASS);
//                                    attributes.add(Constants.PROPERTY_SECURITY_NAME);
//                                    break;
//                                case "authPriv":
//                                    attributes.add(Constants.PROPERTY_AUTH_PROTOCOL);
//                                    attributes.add(Constants.PROPERTY_AUTH_PASS);
//                                    attributes.add(Constants.PROPERTY_SECURITY_NAME);
//                                    attributes.add(Constants.PROPERTY_PRIVACY_PROTOCOL);
//                                    attributes.add(Constants.PROPERTY_PRIVACY_PASS);
//                                    break;
//                            }
//
//                            for (JLabel mandatoryAttr :mandatoryAttrs) {
//                                for (String attribute : attributes) {
//                                    if (mandatoryAttr.getName().equals(attribute)) {
//                                        mandatoryAttr.setText("*");
//                                        mandatoryAttr.setForeground(Color.RED);
//                                    }
//                                }
//                            }
//                        }
//                    });
//
//                    if (JOptionPane.showConfirmDialog(null, pnlSyncDataSourceProperties, I18N.gm("new_ds_config"), 
//                        JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
//
//                            HashMap<String, String> parameters = new HashMap<>();
//                            parameters.put("ipAddress", txtIPAddress.getText()); //NOI18N
//                            parameters.put("port", txtBDConfigPort.getText()); //NOI18N   
//                            //TODO check if this parameter is necesary
//                            //parameters.put("deviceId", Long.toString(selectedObjects.get(0).getId())); //NOI18N
//                            //parameters.put("deviceClass", selectedObjects.get(0).getClassName()); //NOI18N
//                            //Bridge Domain
//                            //TODO check if this parameter is necesary
//                            //parameters.put("deviceId", String.valueOf((selectedObjects.get(0).getId())));
//                            //parameters.put("deviceClass", (selectedObjects.get(0).getClassName()));
//                            parameters.put("bdipAddress", txtBDConfigHost.getText());
//                            parameters.put("bdport", txtBDConfigPort.getText());
//                            parameters.put("bduser", txtBDConfigUser.getText());
//                            parameters.put("bdpassword", txtBDConfigPassword.getText());
//           
//                            String version = (String) cboVersion.getSelectedItem();                
//                            parameters.put(Constants.PROPERTY_VERSION, version);
//
//                            if (snmpVersion2c.equals(version))
//                                parameters.put(Constants.PROPERTY_COMMUNITY, txtCommunity.getText()); //NOI18N
//
//                            if (snmpVersion3.equals(version)) {
//
//                                String authProtocol = (String) cboAuthProtocol.getSelectedItem();
//                                if (authProtocol != null)
//                                    parameters.put(Constants.PROPERTY_AUTH_PROTOCOL, authProtocol);
//
//                                String authrotocolPass = txtAuthPass.getText();
//                                if (authrotocolPass != null)
//                                    parameters.put(Constants.PROPERTY_AUTH_PASS, authrotocolPass);
//                                /*
//                                String securityEngineID =txtSecurityEngineID.getText();
//                                if (securityEngineID != null)
//                                    parameters.put("securityEngineID", securityEngineID);                    
//
//                                String contextEngineID = txtContextEngineID.getText();
//                                if (contextEngineID != null)
//                                    parameters.put("contextEngineID", contextEngineID);
//                                */                                        
//                                String securityLevel = (String) cboSecurityLevel.getSelectedItem();
//                                if (securityLevel != null)
//                                    parameters.put(Constants.PROPERTY_SECURITY_LEVEL, securityLevel);
//
//                                String contextName = txtContextName.getText();
//                                if (contextName != null)
//                                    parameters.put(Constants.PROPERTY_CONTEXT_NAME, contextName);
//
//                                String securityName = txtSecurityName.getText();
//                                if (securityName != null)
//                                    parameters.put(Constants.PROPERTY_SECURITY_NAME, securityName);
//
//                                String privacyProtocol = (String) cboPrivacyProtocol.getSelectedItem();
//                                if (privacyProtocol != null)
//                                    parameters.put(Constants.PROPERTY_PRIVACY_PROTOCOL, privacyProtocol);
//
//                                String privacyProtocolPassPhrase = txtPrivacyPass.getText();
//                                if (privacyProtocolPassPhrase != null)
//                                    parameters.put(Constants.PROPERTY_PRIVACY_PASS, privacyProtocolPassPhrase);
//                            }
//
//                            LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
//                                    createSyncDataSourceConfiguration(
//                                            selectedObjects.get(0).getId(),
//                                            ((LocalSyncGroup) selectedValue).getId(), 
//                                        txtSyncDataSourceConfigName.getText(), parameters);
//
//                            if (newSyncConfig == null) {
//                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
//                                    CommunicationsStub.getInstance().getError());
//                            } 
//                            //TODO update the sync node
////                            else {
////                                ((SyncGroupNode.SyncGroupNodeChildren) selectedNode.getChildren()).addNotify();
////
////                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
////                                    NotificationUtil.INFO_MESSAGE, I18N.gm("new_sync_config_created_successfully"));
////                            }
//                        //} //TODO erease this because the combo box for device is no loger need it
//                    }
//            }
//        }//end main if
    }
    
}
