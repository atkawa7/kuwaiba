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
package org.inventory.bookmarks;

import org.inventory.bookmarks.nodes.BookmarkRootNode;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle.Messages;
import java.awt.event.KeyEvent;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.navigation.navigationtree.nodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Top component which displays favorites categories.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.bookmarks//BookmarkManager//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "BookmarkManagerTopComponent",
        iconBase="org/inventory/bookmarks/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "leftSlidingSide", openAtStartup = false)
@ActionID(category = "Window", id = "org.inventory.bookmarks.BookmarkManagerTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools"), 
    @ActionReference(path = "Toolbars/Tools")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_BookmarkManagerAction",
        preferredID = "BookmarkManagerTopComponent"
)
@Messages({
    "CTL_BookmarkManagerAction=Bookmark Manager",
    "CTL_BookmarkManagerTopComponent=Bookmark Manager",
    "HINT_BookmarkManagerTopComponent=Bookmark Manager"
})
public final class BookmarkManagerTopComponent extends TopComponent implements 
    ExplorerManager.Provider, Refreshable {
    
    private BeanTreeView treeMain;
    private ExplorerManager em;

    public BookmarkManagerTopComponent() {
        em = new ExplorerManager();
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_BookmarkManagerTopComponent());
        setToolTipText(Bundle.HINT_BookmarkManagerTopComponent());

    }
    
    public void initCustomComponents() {
        getActionMap().put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        getActionMap().put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        getActionMap().put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        getActionMap().put(DeleteBusinessObjectAction.ACTION_MAP_KEY, SystemAction.get(DeleteBusinessObjectAction.class));

        //Now the keystrokes
        InputMap keys = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.pasteAction);
        
        treeMain = new BeanTreeView();
        pnlScrollMain.setViewportView(treeMain);
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlScrollMain = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane pnlScrollMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        em.setRootContext(new BookmarkRootNode());
    }

    @Override
    public void componentClosed() {
        em.setRootContext(Node.EMPTY);
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

    @Override
    public void refresh() {
        componentClosed();
        componentOpened();
    }
}
