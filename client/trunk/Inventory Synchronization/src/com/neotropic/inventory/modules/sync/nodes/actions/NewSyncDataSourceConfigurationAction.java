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
package com.neotropic.inventory.modules.sync.nodes.actions;

import com.neotropic.inventory.modules.sync.nodes.SyncGroupNode;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalSyncDataSourceConfiguration;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.Utilities;

/**
 * Action to create a new Sync Group
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
class NewSyncDataSourceConfigurationAction extends GenericInventoryAction {
    
    public NewSyncDataSourceConfigurationAction() {
        putValue(NAME, I18N.gm("new_ds_config"));
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
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends SyncGroupNode> selectedNodes = Utilities.actionsGlobalContext()
            .lookupResult(SyncGroupNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext())
            return;
        
        SyncGroupNode selectedNode = selectedNodes.next();
        
        List<LocalObjectLight> commDevices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICNETWORKELEMENT);
        if (commDevices == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            return;
        }
        
        JPanel pnlName = new JPanel();
        JTextField txtSyncDataSourceConfigName = (JTextField) pnlName.add(setSize(new JTextField()));
        txtSyncDataSourceConfigName.setName("txtSyncDataSourceConfigName");
        pnlName.add(new JLabel("*")).setForeground(Color.RED);
        
        JPanel pnlDevices = new JPanel();
        JComboBox<LocalObjectLight> cmbDevices = (JComboBox) pnlDevices.add(setSize(new JComboBox(commDevices.toArray(new LocalObjectLight[0]))));
        cmbDevices.setName("cmbDevices");
        pnlDevices.add(new JLabel("*")).setForeground(Color.RED);
        
        JPanel pnlIpAddress = new JPanel();
        JTextField txtIPAddress = (JTextField) pnlIpAddress.add(setSize(new JTextField()));
        txtIPAddress.setName("txtIPAddress");
        ((JLabel) pnlIpAddress.add(new JLabel("*"))).setForeground(Color.RED);
        
        JPanel pnlPort = new JPanel();        
        JTextField txtPort = (JTextField) pnlPort.add(setSize(new JTextField("161")));
        txtPort.setName("txtPort");
        ((JLabel) pnlPort.add(new JLabel("*"))).setForeground(Color.RED);
        
        final String snmpVersion2c = "2c"; // NOI18N
        final String snmpVersion3 = "3"; // NOI18N
        final String none = "None"; // NOI18N
        
        JPanel pnlVersion = new JPanel();
        final JComboBox cboVersion = (JComboBox) pnlVersion.add(setSize(new JComboBox()));
        cboVersion.setName("cboVersion");
        cboVersion.addItem(I18N.gm("select_snmp_version"));
        cboVersion.addItem(snmpVersion2c);
        cboVersion.addItem(snmpVersion3);
        ((JLabel) pnlVersion.add(new JLabel("*"))).setForeground(Color.RED);
                
        final JComboBox cboAuthProtocol = (JComboBox) setSize(new JComboBox());
        cboAuthProtocol.setName("cboAuthenticationProtocol");
        cboAuthProtocol.addItem(none);
        cboAuthProtocol.addItem("MD5"); // NOI18N
        //cboAuthProtocol.addItem("SHA"); // NOI18N Not supported yet
        cboAuthProtocol.setSelectedItem(none);        
        cboAuthProtocol.setEnabled(false);
        
        final JPasswordField txtAuthPass = (JPasswordField) setSize(new JPasswordField());
        txtAuthPass.setName("txtAuthenticationProtocolPassPhrase");
        txtAuthPass.setEditable(false);
        
        cboAuthProtocol.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!none.equals((String) cboAuthProtocol.getSelectedItem()))
                    txtAuthPass.setEditable(true);
                else {
                    txtAuthPass.setText("");
                    txtAuthPass.setEditable(false);
                }
            }
        });
        /*
        final JTextField txtSecurityEngineID = new JTextField();
        txtSecurityEngineID.setName("txtSecurityEngineID");
        txtSecurityEngineID.setEnabled(false);
        
        final JTextField txtContextEngineID = new JTextField();        
        txtContextEngineID.setName("txtContextEngineID");
        txtContextEngineID.setEnabled(false);
        */
        final JComboBox cboSecurityLevel = (JComboBox) setSize(new JComboBox());
        cboSecurityLevel.setName("cboSecurityLevel");
        cboSecurityLevel.addItem("noAuthNoPriv"); // NOI18N
        cboSecurityLevel.addItem("authNoPriv"); // NOI18N
        cboSecurityLevel.addItem("authPriv"); // NOI18N
        cboSecurityLevel.setSelectedItem("noAuthNoPriv"); // NOI18N
        cboSecurityLevel.setEnabled(false);
        
        final JTextField txtContextName = (JTextField) setSize(new JTextField());
        txtContextName.setName("txtContextName");
        txtContextName.setEditable(false);        
        
        final JTextField txtSecurityName = (JTextField) setSize(new JTextField());
        txtSecurityName.setName("txtSecurityName");
        txtSecurityName.setEditable(false);
        
        final JComboBox cboPrivacyProtocol = (JComboBox) setSize(new JComboBox());
        cboPrivacyProtocol.setName("cboPrivacyProtocol");
        cboPrivacyProtocol.addItem(none);
        cboPrivacyProtocol.addItem("DES"); // NOI18N
        //cboPrivacyProtocol.addItem("AES"); // NOI18N Not supported yet
        cboPrivacyProtocol.setSelectedItem(none);
        cboPrivacyProtocol.setEnabled(false);        
        
        final JPasswordField txtPrivacyPass = (JPasswordField) setSize(new JPasswordField());
        txtPrivacyPass.setName("txtPrivacyProtocolPassPhrase");
        txtPrivacyPass.setEditable(false);
        
        cboPrivacyProtocol.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!none.equals((String) cboPrivacyProtocol.getSelectedItem()))
                    txtPrivacyPass.setEditable(true);
                else {
                    txtPrivacyPass.setText("");
                    txtPrivacyPass.setEditable(false);                
                }
            }
        });
        final List<JLabel> mandatoryAttrs = new ArrayList();
        
        JPanel pnlCommunity = new JPanel();
        pnlCommunity.setName(Constants.PROPERTY_COMMUNITY);
        final JTextField txtCommunity = (JTextField) pnlCommunity.add(setSize(new JTextField("public")));
        txtCommunity.setToolTipText("Default value \"public\"");
        txtCommunity.setName("txtcomunity");
        txtCommunity.setForeground(Color.GRAY);
        txtCommunity.setEditable(false);        
        JLabel lblCommunity = (JLabel) pnlCommunity.add(new JLabel(" "));
        lblCommunity.setName(Constants.PROPERTY_COMMUNITY);
        mandatoryAttrs.add(lblCommunity);
        
        JPanel pnlAuthProtocol = new JPanel();
        pnlAuthProtocol.setName(Constants.PROPERTY_AUTH_PROTOCOL);
        pnlAuthProtocol.add(cboAuthProtocol);
        JLabel lblAuthProtocol = (JLabel) pnlAuthProtocol.add(new JLabel(" "));
        lblAuthProtocol.setName(Constants.PROPERTY_AUTH_PROTOCOL);
        mandatoryAttrs.add(lblAuthProtocol);
                
        JPanel pnlAuthPass = new JPanel();
        pnlAuthPass.setName(Constants.PROPERTY_AUTH_PASS);
        pnlAuthPass.add(txtAuthPass);
        JLabel lblAuthPass = (JLabel) pnlAuthPass.add(new JLabel(" "));
        lblAuthPass.setName(Constants.PROPERTY_AUTH_PASS);
        mandatoryAttrs.add(lblAuthPass);
        
        JPanel pnlSecurityLevel = new JPanel();
        pnlSecurityLevel.setName(Constants.PROPERTY_SECURITY_LEVEL);
        pnlSecurityLevel.add(cboSecurityLevel);
        JLabel lblSecurityLevel = (JLabel) pnlSecurityLevel.add(new JLabel(" "));
        lblSecurityLevel.setName(Constants.PROPERTY_SECURITY_LEVEL);
        mandatoryAttrs.add(lblSecurityLevel);
        
        JPanel pnlContextName = new JPanel();
        pnlContextName.setName(Constants.PROPERTY_CONTEXT_NAME);
        pnlContextName.add(txtContextName);
        JLabel lblContextName = (JLabel) pnlContextName.add(new JLabel(" "));
        lblContextName.setName(Constants.PROPERTY_SECURITY_LEVEL);
                
        JPanel pnlSecurityName = new JPanel();
        pnlSecurityName.setName(Constants.PROPERTY_SECURITY_NAME);
        pnlSecurityName.add(txtSecurityName);
        JLabel lblSecurityName = (JLabel) pnlSecurityName.add(new JLabel(" "));
        lblSecurityName.setName(Constants.PROPERTY_SECURITY_NAME);
        mandatoryAttrs.add(lblSecurityName);        
        
        JPanel pnlPrivacyProtocol = new JPanel();
        pnlPrivacyProtocol.setName(Constants.PROPERTY_PRIVACY_PROTOCOL);
        pnlPrivacyProtocol.add(cboPrivacyProtocol);
        JLabel lblPrivacyProtocol = (JLabel) pnlPrivacyProtocol.add(new JLabel(" "));
        lblPrivacyProtocol.setName(Constants.PROPERTY_PRIVACY_PROTOCOL);
        mandatoryAttrs.add(lblPrivacyProtocol);
        
        JPanel pnlPrivacyPass = new JPanel();
        pnlPrivacyPass.setName(Constants.PROPERTY_PRIVACY_PASS);
        pnlPrivacyPass.add(txtPrivacyPass);        
        JLabel lblPrivacyPass = (JLabel) pnlPrivacyPass.add(new JLabel(" "));
        lblPrivacyPass.setName(Constants.PROPERTY_PRIVACY_PASS);
        lblPrivacyPass.setForeground(Color.GRAY);
        mandatoryAttrs.add(lblPrivacyPass);
                                                
        final JComplexDialogPanel pnlSyncDataSourceProperties = new JComplexDialogPanel(
            new String[] {I18N.gm("sync_datasource_config_name"),
                I18N.gm("device"), I18N.gm("ip_address"), 
                I18N.gm("port"), 
                I18N.gm("snmp_version"),
                I18N.gm("community"), 
                I18N.gm("snmp_version_3_security_level"), I18N.gm("snmp_version_3_security_name"), I18N.gm("snmp_version_3_auth_protocol"), I18N.gm("snmp_version_3_auth_pass"), I18N.gm("snmp_version_3_privacy_protocol"), I18N.gm("snmp_version_3_privacy_pass"), I18N.gm("snmp_version_3_context_name")},
            new JComponent[] { pnlName, pnlDevices, pnlIpAddress, pnlPort, 
                pnlVersion, 
                pnlCommunity,
                pnlSecurityLevel, pnlSecurityName, pnlAuthProtocol, pnlAuthPass, pnlPrivacyProtocol, pnlPrivacyPass, pnlContextName});
        
        cboVersion.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (JLabel mandatoryAttr :mandatoryAttrs)
                    mandatoryAttr.setText(" ");
                
                List<String> attributes = new ArrayList();
                
                String selectedVersion = (String) cboVersion.getSelectedItem();
                
                if (I18N.gm("select_snmp_version").equals(selectedVersion)) {
                    txtCommunity.setText("");
                    txtCommunity.setEditable(false);
                    
                    cboAuthProtocol.setSelectedItem(none);
                    cboAuthProtocol.setEnabled(false);
                    
                    txtAuthPass.setText("");
                    txtAuthPass.setEditable(false);
                    /*
                    txtSecurityEngineID.setEditable(false);
                    txtContextEngineID.setEditable(false);
                    */
                    cboSecurityLevel.setEnabled(false);
                    
                    txtContextName.setText("");
                    txtContextName.setEditable(false);
                    
                    txtSecurityName.setText("");
                    txtSecurityName.setEditable(false);
                    
                    cboPrivacyProtocol.setSelectedItem(none);
                    cboPrivacyProtocol.setEnabled(false);
                                        
                    txtPrivacyPass.setText("");
                    txtPrivacyPass.setEditable(false);
                }
                if (snmpVersion2c.equals(selectedVersion)) {
                    attributes.add(Constants.PROPERTY_COMMUNITY);
                    
                    txtCommunity.setForeground(Color.BLACK);
                    txtCommunity.setEditable(true);
                    
                    cboAuthProtocol.setSelectedItem(none);
                    cboAuthProtocol.setEnabled(false);
                    
                    txtAuthPass.setText("");
                    txtAuthPass.setEditable(false);
                    /*
                    txtSecurityEngineID.setEditable(false);
                    txtContextEngineID.setEditable(false);
                    */
                    cboSecurityLevel.setEnabled(false);
                    
                    txtContextName.setText("");
                    txtContextName.setEditable(false);
                    
                    txtSecurityName.setText("");
                    txtSecurityName.setEditable(false);
                    
                    cboPrivacyProtocol.setSelectedItem(none);
                    cboPrivacyProtocol.setEnabled(false);
                                        
                    txtPrivacyPass.setText("");
                    txtPrivacyPass.setEditable(false);
                }
                if (snmpVersion3.equals(selectedVersion)) {
                    txtCommunity.setText("");
                    txtCommunity.setEditable(false);
                    
                    cboAuthProtocol.setEnabled(true);
                    /*
                    txtSecurityEngineID.setEditable(true);
                    txtContextEngineID.setEditable(true);
                    */
                    cboSecurityLevel.setEnabled(true);
                    cboSecurityLevel.setSelectedItem("noAuthNoPriv");
                    txtContextName.setEditable(true);
                    txtSecurityName.setEditable(true);
                    cboPrivacyProtocol.setEnabled(true);
                }
                for (JLabel mandatoryAttr :mandatoryAttrs) {
                    for (String attribute : attributes) {
                        if (mandatoryAttr.getName().equals(attribute)) {
                            mandatoryAttr.setText("*");
                            mandatoryAttr.setForeground(Color.RED);
                        }
                    }
                }
            }
        });
        
        cboSecurityLevel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                for (JLabel mandatoryAttr :mandatoryAttrs)
                    mandatoryAttr.setText(" ");
                
                List<String> attributes = new ArrayList();
                attributes.add(Constants.PROPERTY_SECURITY_LEVEL);
                
                switch ((String) cboSecurityLevel.getSelectedItem()) {
                    case "noAuthNoPriv":
                        attributes.add(Constants.PROPERTY_SECURITY_NAME);
                        break;
                    case "authNoPriv":
                        attributes.add(Constants.PROPERTY_AUTH_PROTOCOL);
                        attributes.add(Constants.PROPERTY_AUTH_PASS);
                        attributes.add(Constants.PROPERTY_SECURITY_NAME);
                        break;
                    case "authPriv":
                        attributes.add(Constants.PROPERTY_AUTH_PROTOCOL);
                        attributes.add(Constants.PROPERTY_AUTH_PASS);
                        attributes.add(Constants.PROPERTY_SECURITY_NAME);
                        attributes.add(Constants.PROPERTY_PRIVACY_PROTOCOL);
                        attributes.add(Constants.PROPERTY_PRIVACY_PASS);
                        break;
                }
                
                for (JLabel mandatoryAttr :mandatoryAttrs) {
                    for (String attribute : attributes) {
                        if (mandatoryAttr.getName().equals(attribute)) {
                            mandatoryAttr.setText("*");
                            mandatoryAttr.setForeground(Color.RED);
                        }
                    }
                }
            }
        });
        
        if (JOptionPane.showConfirmDialog(null, pnlSyncDataSourceProperties, I18N.gm("new_ds_config"), 
            JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            if (txtSyncDataSourceConfigName.getText().trim().isEmpty() || cmbDevices.getSelectedItem() == null ||
                    txtIPAddress.getText().trim().isEmpty() || txtPort.getText().trim().isEmpty()/* || txtCommunity.getText().trim().isEmpty()*/)
                JOptionPane.showMessageDialog(null, I18N.gm("missing_fields"), I18N.gm("new_ds_config"), JOptionPane.ERROR_MESSAGE);
            else {
                HashMap<String, String> parameters = new HashMap<>();
                parameters.put("ipAddress", txtIPAddress.getText()); //NOI18N
                parameters.put("port", txtPort.getText()); //NOI18N                
                parameters.put("deviceId", Long.toString(((LocalObjectLight)cmbDevices.getSelectedItem()).getOid())); //NOI18N
                parameters.put("deviceClass", ((LocalObjectLight)cmbDevices.getSelectedItem()).getClassName()); //NOI18N
                
                String version = (String) cboVersion.getSelectedItem();                
                parameters.put(Constants.PROPERTY_VERSION, version);
                
                if (snmpVersion2c.equals(version))
                    parameters.put(Constants.PROPERTY_COMMUNITY, txtCommunity.getText()); //NOI18N
                
                if (snmpVersion3.equals(version)) {
                    
                    String authProtocol = (String) cboAuthProtocol.getSelectedItem();
                    if (authProtocol != null)
                        parameters.put(Constants.PROPERTY_AUTH_PROTOCOL, authProtocol);
                    
                    String authrotocolPass = txtAuthPass.getText();
                    if (authrotocolPass != null)
                        parameters.put(Constants.PROPERTY_AUTH_PASS, authrotocolPass);
                    /*
                    String securityEngineID =txtSecurityEngineID.getText();
                    if (securityEngineID != null)
                        parameters.put("securityEngineID", securityEngineID);                    
                    
                    String contextEngineID = txtContextEngineID.getText();
                    if (contextEngineID != null)
                        parameters.put("contextEngineID", contextEngineID);
                    */                                        
                    String securityLevel = (String) cboSecurityLevel.getSelectedItem();
                    if (securityLevel != null)
                        parameters.put(Constants.PROPERTY_SECURITY_LEVEL, securityLevel);
                    
                    String contextName = txtContextName.getText();
                    if (contextName != null)
                        parameters.put(Constants.PROPERTY_CONTEXT_NAME, contextName);
                    
                    String securityName = txtSecurityName.getText();
                    if (securityName != null)
                        parameters.put(Constants.PROPERTY_SECURITY_NAME, securityName);
                    
                    String privacyProtocol = (String) cboPrivacyProtocol.getSelectedItem();
                    if (privacyProtocol != null)
                        parameters.put(Constants.PROPERTY_PRIVACY_PROTOCOL, privacyProtocol);
                    
                    String privacyProtocolPassPhrase = txtPrivacyPass.getText();
                    if (privacyProtocolPassPhrase != null)
                        parameters.put(Constants.PROPERTY_PRIVACY_PASS, privacyProtocolPassPhrase);
                }
                
                LocalSyncDataSourceConfiguration newSyncConfig = CommunicationsStub.getInstance().
                    createSyncDataSourceConfiguration(selectedNode.getLookup().lookup(LocalSyncGroup.class).getId(), 
                            txtSyncDataSourceConfigName.getText(), parameters);
            
                if (newSyncConfig == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
                } else {
                    ((SyncGroupNode.SyncGroupNodeChildren) selectedNode.getChildren()).addNotify();

                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                        NotificationUtil.INFO_MESSAGE, I18N.gm("new_sync_config_created_successfully"));
                }
            }
        }
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SYNC, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
