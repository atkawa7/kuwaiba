/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.predefinedshapes;

import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.i18n.I18N;
import org.inventory.predefinedshapes.nodes.PredefinedShapeChildren;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Displays the custom shapes.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class PredefinedShapesTopComponent extends TopComponent implements ExplorerManager.Provider {
    private final ExplorerManager em = new ExplorerManager();
    private final BeanTreeView beanTreeView = new BeanTreeView();

    public PredefinedShapesTopComponent() {
        initComponents();
        initCustomComponents();
        setName(I18N.gm("top_component_name_predefined_shapes"));
        setToolTipText(I18N.gm("top_component_tool_tip_text_predefined_shapes"));
    }
    
    private void initCustomComponents() {
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        add(beanTreeView);
        //Workaround, because when you close a TC whose mode is "explorer" and open it again,
        //it docks as "explorer". This forces the TC to be always docked "explorer"
        Mode myMode = WindowManager.getDefault().findMode("explorer"); //NOI18N
        myMode.dockInto(this);
    }
    
    @Override
    protected String preferredID() {
        return "PredefinedShapesTopComponent"; //NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
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
    @Override
    public void componentOpened() {
        if (CommunicationsStub.getInstance().getMetaForClass("GenericApplicationListType", true) == null) { //NOI18N
            JOptionPane.showMessageDialog(null, I18N.gm("database_seems_outdated"), 
                I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            this.close();
            return;
        }
        AbstractNode root = new AbstractNode(new PredefinedShapeChildren());
        em.setRootContext(root);
        em.getRootContext().setDisplayName(I18N.gm("node_lbl_predefined_shapes"));
    }

    @Override
    public void componentClosed() {
        em.setRootContext(Node.EMPTY);
        
        Mode myMode = WindowManager.getDefault().findMode("explorer"); //NOI18N
        myMode.dockInto(this);
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

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
