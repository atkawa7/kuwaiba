/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.attachments;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.special.attachments.nodes.AttachmentsRootNode;
import org.inventory.navigation.special.children.nodes.SpecialChildren;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Displays the files attached to an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.navigation.special.attachments//Attachments//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "AttachmentsTopComponent",
        iconBase="org/inventory/navigation/special/res/attachments_explorer.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.navigation.special.attachments.AttachmentsTopComponent")
@ActionReferences(value = { @ActionReference(path = "Menu/Tools/Navigation"),
    @ActionReference(path = "Toolbars/01_Navigation", position = 7 )})
@TopComponent.OpenActionRegistration(
        displayName = "Attachment Explorer",
        preferredID = "AttachmentsTopComponent"
)
public class AttachmentsTopComponent extends TopComponent 
        implements ExplorerManager.Provider, LookupListener {
    
    private static ImageIcon ICON_REFRESH = new ImageIcon(AttachmentsTopComponent.class.getResource("/org/inventory/navigation/special/res/refresh.png"));
    
    private ExplorerManager em;
    private ListView lstAttachments;
    private JButton btnRefresh;
    //Singleton
    private static AttachmentsTopComponent self;
    private Lookup.Result<ObjectNode> lookupResult;
    private boolean open = false;
    
    private AttachmentsTopComponent() {
        this.em = new ExplorerManager();
        this.lstAttachments = new ListView();
        this.lstAttachments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JToolBar barMain = new JToolBar();
        btnRefresh = new JButton(ICON_REFRESH);
        btnRefresh.setToolTipText(I18N.gm("refresh")); //NOI18N
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ((AttachmentsRootNode.AttachmentsRootNodeChildren)em.getRootContext().getChildren()).addNotify();
            }
        });
        
        barMain.add(btnRefresh);
        
        this.em.setRootContext(Node.EMPTY);
        this.em.getRootContext().setDisplayName(I18N.gm("select_a_node_from_a_view_or_tree"));  //NO18N
        
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        setDisplayName(I18N.gm("attachment_explorer")); //NOI18N
        
        setLayout(new BorderLayout());
        add(barMain, BorderLayout.NORTH);
        add(lstAttachments, BorderLayout.CENTER);
    }
    
    public static AttachmentsTopComponent getInstance() {
        if (self  == null) {
            self = new AttachmentsTopComponent();
            Mode navigator = WindowManager.getDefault().findMode("navigator"); //NOI18N
            navigator.dockInto(self);
        }
        return self;
    }

    @Override
    public void componentOpened() {
        this.open = true;
        this.lookupResult = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class);
        this.lookupResult.addLookupListener(this);
        this.resultChanged(null);
    }
    
    @Override
    public void componentClosed() {
        this.em.setRootContext(Node.EMPTY);
        this.lookupResult.removeLookupListener(this);
        this.open = false;
        this.btnRefresh.setEnabled(false);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    void writeProperties(java.util.Properties p) { }

    void readProperties(java.util.Properties p) { }

    @Override
    public void resultChanged(LookupEvent ev) {
        if(lookupResult.allInstances().size() == 1) {
            LocalObjectLight inventoryObject = ((ObjectNode)lookupResult.allInstances().iterator().next()).getObject();
            em.setRootContext(new AttachmentsRootNode(inventoryObject));
            this.btnRefresh.setEnabled(true);
        } else
            this.btnRefresh.setEnabled(false);
    }
    
    public void refresh() {
        ((SpecialChildren)em.getRootContext().getChildren()).addNotify();
    }
    
    /**
     * Since isOpened doesn't seem to be working fine, this is a rewrite
     * @return 
     */
    public boolean isOpen(){
        return open;
    }
}
