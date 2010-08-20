/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.core.usermanager;

import java.awt.BorderLayout;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.core.usermanager.actions.AddGroup;
import org.inventory.core.usermanager.actions.AddUser;
import org.inventory.core.usermanager.actions.UpdateList;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.TableView;
import org.openide.util.Lookup;

/**
 * This component show the available options for users and groups management
 */
@ConvertAsProperties(
    dtd="-//org.inventory.core.usermanager//UserManager//EN",
    autostore=false
)
public final class UserManagerTopComponent extends TopComponent
    implements Provider{

    private static UserManagerTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/core/usermanager/res/icon.png";

    private static final String PREFERRED_ID = "UserManagerTopComponent";

    private ExplorerManager em = new ExplorerManager();
    private TableView tblUsers = null;
    private TableView tblGroups = null;
    private UserManagerService ums;
    private NotificationUtil nu = null;

    public UserManagerTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(UserManagerTopComponent.class, "CTL_UserManagerTopComponent"));
        setToolTipText(NbBundle.getMessage(UserManagerTopComponent.class, "HINT_UserManagerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        
    }

    public void initCustomComponents(){
        this.ums = new UserManagerService(this);
        ums.populateUsersList();
      
        pnlUsers.add(tblUsers,BorderLayout.CENTER);
       // pnlUsers.add(tblGroups);
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        btnAddUser.addActionListener(new AddUser(ums));
        btnRefresh.addActionListener(new UpdateList(pnlTabbedMain, ums));
        btnAddGroup.addActionListener(new AddGroup(ums));
        pnlTabbedMain.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                switch(pnlTabbedMain.getSelectedIndex()){
                    case 0: //The users tab
                        ums.setRootToUsers();
                        break;
                    case 1: //The groups tab
                       if (pnlGroups.getComponentCount() == 0){
                           if (tblGroups == null)
                                ums.populateGroupsList();
                           
                           //tblGroups may be null if there are no groups at all
                           if (tblGroups != null){
                                pnlGroups.add(tblGroups,BorderLayout.CENTER);
                                pnlGroups.revalidate();
                           }
                        }
                       ums.setRootToGroups();
                }
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barMain = new javax.swing.JToolBar();
        btnAddUser = new javax.swing.JButton();
        btnAddGroup = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        pnlTabbedMain = new javax.swing.JTabbedPane();
        pnlTabUsers = new javax.swing.JPanel();
        pnlScrollUsers = new javax.swing.JScrollPane();
        pnlUsers = new javax.swing.JPanel();
        pnlTabGroups = new javax.swing.JPanel();
        pnlScrollGroups = new javax.swing.JScrollPane();
        pnlGroups = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        barMain.setRollover(true);

        btnAddUser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/usermanager/res/addUser.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddUser, org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnAddUser.text")); // NOI18N
        btnAddUser.setToolTipText(org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnAddUser.toolTipText")); // NOI18N
        btnAddUser.setFocusable(false);
        btnAddUser.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddUser.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnAddUser);

        btnAddGroup.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/usermanager/res/addGroup.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddGroup, org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnAddGroup.text")); // NOI18N
        btnAddGroup.setToolTipText(org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnAddGroup.toolTipText")); // NOI18N
        btnAddGroup.setFocusable(false);
        btnAddGroup.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddGroup.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnAddGroup);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/usermanager/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnRefresh);

        add(barMain, java.awt.BorderLayout.PAGE_START);

        pnlTabbedMain.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pnlTabbedMainFocusGained(evt);
            }
        });

        pnlTabUsers.setLayout(new java.awt.BorderLayout());

        pnlUsers.setLayout(new java.awt.BorderLayout());
        pnlScrollUsers.setViewportView(pnlUsers);

        pnlTabUsers.add(pnlScrollUsers, java.awt.BorderLayout.CENTER);

        pnlTabbedMain.addTab(org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.pnlTabUsers.TabConstraints.tabTitle"), pnlTabUsers); // NOI18N

        pnlTabGroups.setLayout(new java.awt.BorderLayout());

        pnlGroups.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                pnlGroupsFocusGained(evt);
            }
        });
        pnlGroups.setLayout(new java.awt.BorderLayout());
        pnlScrollGroups.setViewportView(pnlGroups);

        pnlTabGroups.add(pnlScrollGroups, java.awt.BorderLayout.CENTER);

        pnlTabbedMain.addTab(org.openide.util.NbBundle.getMessage(UserManagerTopComponent.class, "UserManagerTopComponent.pnlTabGroups.TabConstraints.tabTitle"), pnlTabGroups); // NOI18N

        add(pnlTabbedMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * This is the callback method to attend the focus of the "Groups" tab. At the beginning,
     * the panel is empty, but when focused for the very first time, the groups list should be populated
     * @param evt
     */
    private void pnlTabbedMainFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlTabbedMainFocusGained

    }//GEN-LAST:event_pnlTabbedMainFocusGained

    private void pnlGroupsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_pnlGroupsFocusGained
    
    }//GEN-LAST:event_pnlGroupsFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnAddGroup;
    private javax.swing.JButton btnAddUser;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JPanel pnlGroups;
    private javax.swing.JScrollPane pnlScrollGroups;
    private javax.swing.JScrollPane pnlScrollUsers;
    private javax.swing.JPanel pnlTabGroups;
    private javax.swing.JPanel pnlTabUsers;
    private javax.swing.JTabbedPane pnlTabbedMain;
    private javax.swing.JPanel pnlUsers;
    // End of variables declaration//GEN-END:variables

    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized UserManagerTopComponent getDefault() {
        if (instance == null) {
            instance = new UserManagerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the UserManagerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized UserManagerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(UserManagerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof UserManagerTopComponent) {
            return (UserManagerTopComponent)win;
        }
        Logger.getLogger(UserManagerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID +
                "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        //ToolbarPool.getDefault().findToolbar("UserManager").setVisible(true);
    }

    @Override
    public void componentClosed() {
        //ToolbarPool.getDefault().findToolbar("UserManager").setVisible(false);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }
    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public TableView getTblGroups() {
        return tblGroups;
    }

    public TableView getTblUsers() {
        return tblUsers;
    }

    public NotificationUtil getNotifier(){
        if(this.nu == null)
            this.nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return this.nu;
    }

    public void setTblUsers(TableView tableView) {
        this.tblUsers = tableView;
    }

    public void setTblGroups(TableView tableView){
        this.tblGroups = tableView;
    }

    public JTabbedPane getPnlTabbedMain(){
        return this.pnlTabbedMain;
    }

    public JPanel getPnlGroups(){
        return this.pnlGroups;
    }

    public UserManagerService getUserManagerServiceInstance(){
        if (ums == null)
            ums = new UserManagerService(this);
        return ums;
    }
}
