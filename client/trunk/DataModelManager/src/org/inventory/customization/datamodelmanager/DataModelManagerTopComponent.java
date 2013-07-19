/*
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.inventory.customization.datamodelmanager;

import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.api.behaviors.RefreshableTopComponent;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataChildren;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Data model manager Top component.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ConvertAsProperties(
    dtd = "-//org.inventory.customization.datamodelmanager//DataModelManager//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "DataModelManagerTopComponent",
iconBase = "org/inventory/customization/datamodelmanager/res/icon.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.customization.datamodelmanager.DataModelManagerTopComponent")
@ActionReference(path = "Menu/Tools/Administrative/Class Management" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_DataModelManagerAction",
preferredID = "DataModelManagerTopComponent")
@Messages({
    "CTL_DataModelManagerAction=DataModelManager",
    "CTL_DataModelManagerTopComponent=DataModelManager Tools",
    "HINT_DataModelManagerTopComponent=This is a DataModelManager Tools"
})
public final class DataModelManagerTopComponent extends TopComponent 
        implements ExplorerManager.Provider, RefreshableTopComponent{

    private final ExplorerManager em = new ExplorerManager();
    private DataModelManagerService dmms;
    private NotificationUtil nu;
       
    public DataModelManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_DataModelManagerTopComponent());
        setToolTipText(Bundle.HINT_DataModelManagerTopComponent());
        initComponentsCustom();
    }
    
    public void initComponentsCustom(){
        dmms = new DataModelManagerService(this);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        
        associateLookup(ExplorerUtils.createLookup(em, map));
        treeView = new BeanTreeView();
        treeView.setRootVisible(false);
        add(treeView);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    private BeanTreeView treeView;
    
    @Override
    public void componentOpened() {
        setRoot();
        ExplorerUtils.activateActions(em, true);
    }

    @Override
    public void componentClosed() {
        ExplorerUtils.activateActions(em, false);
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
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
    
    public void setRoot(){
        LocalClassMetadataLight[] allMeta = dmms.getRootChildren();
        em.setRootContext(new AbstractNode(new ClassMetadataChildren(allMeta)));
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    

    @Override
    public void refresh() {

    }
    
    public NotificationUtil getNotifier(){
         if (nu == null)
             return Lookup.getDefault().lookup(NotificationUtil.class);
         return nu;
    }
}
