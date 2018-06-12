/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.contacts;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.net.ConnectException;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalContact;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.export.ExportableTable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.etable.ETable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.core.contacts//ContactManager//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ContactManagerTopComponent",
        iconBase="org/inventory/core/contacts/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.core.contacts.ContactManagerTopComponent")
@ActionReferences(value = { @ActionReference(path = "Menu/Tools"),
    @ActionReference(path = "Toolbars/01_Navigation", position = 3)})

@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ContactManagerAction",
        preferredID = "ContactManagerTopComponent"
)
@Messages({
    "CTL_ContactManagerAction=Contact Manager",
    "CTL_ContactManagerTopComponent=Contact Manager",
    "HINT_ContactManagerTopComponent=Manage your contacts"
})
public final class ContactManagerTopComponent extends TopComponent implements ExportableTable, ExplorerManager.Provider {
    private ExplorerManager em;
    private ETable contactsTable;
    private MouseAdapter mouseAdapter;
    
    public ContactManagerTopComponent() {
        this.em = new ExplorerManager();
        this.mouseAdapter = new PopupProvider();
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_ContactManagerTopComponent());
        setToolTipText(Bundle.HINT_ContactManagerTopComponent());

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnGetAllContacts = new javax.swing.JButton();
        btnAddContact = new javax.swing.JButton();
        btnContactsPerCustomer = new javax.swing.JButton();
        btnAdvancedFilters = new javax.swing.JButton();
        btnNoFilter = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        txtSearch = new javax.swing.JTextField();
        pnlScrollMain = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        btnGetAllContacts.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/all.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnGetAllContacts, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnGetAllContacts.text")); // NOI18N
        btnGetAllContacts.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnGetAllContacts.toolTipText")); // NOI18N
        btnGetAllContacts.setFocusable(false);
        btnGetAllContacts.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnGetAllContacts.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnGetAllContacts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetAllContactsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnGetAllContacts);

        btnAddContact.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddContact, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnAddContact.text")); // NOI18N
        btnAddContact.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnAddContact.toolTipText")); // NOI18N
        btnAddContact.setFocusable(false);
        btnAddContact.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddContact.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddContact.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddContactActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddContact);

        btnContactsPerCustomer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/contactsPerCustomer.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnContactsPerCustomer, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnContactsPerCustomer.text")); // NOI18N
        btnContactsPerCustomer.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnContactsPerCustomer.toolTipText")); // NOI18N
        btnContactsPerCustomer.setFocusable(false);
        btnContactsPerCustomer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnContactsPerCustomer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnContactsPerCustomer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContactsPerCustomerActionPerformed(evt);
            }
        });
        jToolBar1.add(btnContactsPerCustomer);

        btnAdvancedFilters.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/advancedFilters.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAdvancedFilters, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnAdvancedFilters.text")); // NOI18N
        btnAdvancedFilters.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnAdvancedFilters.toolTipText")); // NOI18N
        btnAdvancedFilters.setFocusable(false);
        btnAdvancedFilters.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdvancedFilters.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdvancedFilters.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAdvancedFiltersActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAdvancedFilters);

        btnNoFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/no_filter.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNoFilter, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnNoFilter.text")); // NOI18N
        btnNoFilter.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnNoFilter.toolTipText")); // NOI18N
        btnNoFilter.setFocusable(false);
        btnNoFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNoFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNoFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNoFilterActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNoFilter);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/contacts/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnExport);

        txtSearch.setText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.txtSearch.text")); // NOI18N
        txtSearch.setToolTipText(org.openide.util.NbBundle.getMessage(ContactManagerTopComponent.class, "ContactManagerTopComponent.txtSearch.toolTipText")); // NOI18N
        jToolBar1.add(txtSearch);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnGetAllContactsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetAllContactsActionPerformed
        List<LocalContact> allContacts = CommunicationsStub.getInstance().searchForContacts(null, -1);
        
        if (allContacts == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            this.contactsTable.removeAll();
            return;
        }
        
        if (allContacts.isEmpty())
            JOptionPane.showMessageDialog(null, "There are no contacts registered so far", I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
        
        try {
            this.contactsTable.setModel(new ContactsTableModel(allContacts));
            btnNoFilter.setEnabled(false);
        } catch(ConnectException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
        }
    }//GEN-LAST:event_btnGetAllContactsActionPerformed

    private void btnAddContactActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddContactActionPerformed
        List<LocalClassMetadataLight> contactTypes = CommunicationsStub.getInstance().getLightSubclasses("GenericContact", false, false); //NOI18N
        
        if (contactTypes == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        List<LocalObjectLight> customers = CommunicationsStub.getInstance().getObjectsOfClassLight("GenericCustomer"); //NOI18N
        
        if (customers == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        
        JTextField txtContactName = new JTextField();
        txtContactName.setName("txtContactName"); //NOI18N
        JComboBox cmbContactTypes = new JComboBox(contactTypes.toArray());
        cmbContactTypes.setName("cmbContactTypes"); //NOI18N
        JComboBox cmbCustomers = new JComboBox(customers.toArray());
        cmbCustomers.setName("cmbCustomers"); //NOI18N

        JComplexDialogPanel pnlNewContact = new JComplexDialogPanel(new String[] { "Name", "Type", "Company" }, 
                new JComponent[] { txtContactName, cmbContactTypes, cmbCustomers });

        if (JOptionPane.showConfirmDialog(null, pnlNewContact, "Add Contact", 
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String contactName = txtContactName.getText();  //NOI18N
            LocalClassMetadataLight contactType = (LocalClassMetadataLight)cmbContactTypes.getSelectedItem();
            LocalObjectLight customer = (LocalObjectLight)cmbCustomers.getSelectedItem();
            
            if (contactType == null) {
                JOptionPane.showMessageDialog(null, "Please select a contact type", I18N.gm("error"),  JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if (customer == null) {
                JOptionPane.showMessageDialog(null, "Please select a company", I18N.gm("error"),  JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            
            if(!CommunicationsStub.getInstance().createContact(contactType.getClassName(), contactName, customer.getClassName(), customer.getId()))
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else 
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The contact was created successfully");
        }
    }//GEN-LAST:event_btnAddContactActionPerformed

    private void btnContactsPerCustomerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContactsPerCustomerActionPerformed
        List<LocalObjectLight> allCustomers = CommunicationsStub.getInstance().getObjectsOfClassLight("GenericCustomer");
        
        if (allCustomers == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
            this.contactsTable.removeAll();
            return;
        }
        
        JComboBox<LocalObjectLight> cmbAllCustomers = new JComboBox<>(allCustomers.toArray(new LocalObjectLight[0]));
        cmbAllCustomers.setName("cmbAllCustomers"); //NOI18N
        
        JComplexDialogPanel pnlCustomerFilter = new JComplexDialogPanel(new String[] { "Company" }, new JComponent[] { cmbAllCustomers });
        
        if (JOptionPane.showConfirmDialog(null, pnlCustomerFilter, "Contacts by Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalObjectLight selectedCustomer = (LocalObjectLight)cmbAllCustomers.getSelectedItem();
            
            if (selectedCustomer != null) {
            
                List<LocalContact> allContacts = CommunicationsStub.getInstance().getContactsForCustomer(selectedCustomer.getClassName(), selectedCustomer.getId());

                if (allContacts == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                            CommunicationsStub.getInstance().getError());
                    this.contactsTable.removeAll();
                    return;
                }

                if (allContacts.isEmpty())
                    JOptionPane.showMessageDialog(null, "The selected company does not have contacts associated to it", I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);

                try {
                    this.contactsTable.setModel(new ContactsTableModel(allContacts));
                    btnNoFilter.setEnabled(false);
                } catch(ConnectException ex) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
            }
        }
        
        
    }//GEN-LAST:event_btnContactsPerCustomerActionPerformed

    private void btnAdvancedFiltersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAdvancedFiltersActionPerformed
        if (!ContactsTableModel.class.isInstance(this.contactsTable.getModel()))
            JOptionPane.showMessageDialog(null, "The table is not ready to be filtered", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        else {
            ContactsTableModel tableModel = (ContactsTableModel)contactsTable.getModel();
            JComboBox<String> cmbColumnName = new JComboBox<>(tableModel.getColumnNames());
            cmbColumnName.setName("cmbColumnName"); //NOI18N
            
            JComboBox<String> cmbColumnValue = new JComboBox<>(tableModel.collectColumnValuesForColumn(0));
            cmbColumnValue.setName("cmbColumnValue"); //NOI18N
            
            cmbColumnName.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cmbColumnValue.setModel(new DefaultComboBoxModel<>(tableModel.collectColumnValuesForColumn(cmbColumnName.getSelectedIndex())));
                }
            });
            
            JComplexDialogPanel pnlAdvancedFilter = new JComplexDialogPanel(new String[] { "Column", "Filter" }, 
                    new JComponent[] { cmbColumnName, cmbColumnValue});
            
            if (JOptionPane.showConfirmDialog(null, pnlAdvancedFilter, "Choose a Filter", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                this.contactsTable.setQuickFilter(cmbColumnName.getSelectedIndex(), cmbColumnValue.getSelectedItem());
                btnNoFilter.setEnabled(true);
            }
            
        }
    }//GEN-LAST:event_btnAdvancedFiltersActionPerformed

    private void btnNoFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNoFilterActionPerformed
        if (!ContactsTableModel.class.isInstance(this.contactsTable.getModel()))
            JOptionPane.showMessageDialog(null, "The table is not ready to be filtered", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        else {
            this.contactsTable.unsetQuickFilter();
            btnNoFilter.setEnabled(false);
        }
    }//GEN-LAST:event_btnNoFilterActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddContact;
    private javax.swing.JButton btnAdvancedFilters;
    private javax.swing.JButton btnContactsPerCustomer;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnGetAllContacts;
    private javax.swing.JButton btnNoFilter;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane pnlScrollMain;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        try {
            this.btnNoFilter.setEnabled(false);
            this.contactsTable.setModel(new ContactsTableModel());
            this.contactsTable.addMouseListener(mouseAdapter);
            
        } catch (ConnectException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Override
    public void componentClosed() {
        this.contactsTable.removeAll();
        this.contactsTable.removeMouseListener(mouseAdapter);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private void initCustomComponents() {
            this.contactsTable = new ETable();
            this.pnlScrollMain.setViewportView(contactsTable);
            this.txtSearch.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    List<LocalContact> allContacts = CommunicationsStub.getInstance().searchForContacts(txtSearch.getText(), -1);

                    if (allContacts.isEmpty())
                        JOptionPane.showMessageDialog(null, "The search returned 0 contacts", I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);

                    try {
                        contactsTable.setModel(new ContactsTableModel(allContacts));
                    } catch(ConnectException ex) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                    }
                }
            });

            associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        
    }

    @Override
    public Object[][] getResults(Range range) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    private class PopupProvider extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            LocalObjectLight selectedValue = (LocalObjectLight)contactsTable.getValueAt(contactsTable.rowAtPoint(new Point(e.getX(), e.getY())), -1);
            ObjectNode node = new ObjectNode(selectedValue);
            try {
                em.setRootContext(node);
                em.setSelectedNodes(new Node[] { node });
            } catch (PropertyVetoException ex) {} //Should never happen
            
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {  //e.isPopupTrigger works differently depending on the platform, so we just check for the second button          
              JPopupMenu  menu = Utilities.actionsToPopup(node.getActions(true), e.getComponent());
              menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {}
    }
}
