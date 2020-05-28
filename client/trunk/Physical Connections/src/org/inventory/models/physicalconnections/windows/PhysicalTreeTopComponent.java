/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.models.physicalconnections.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.models.physicalconnections.PhysicalConnectionsService;
import org.inventory.models.physicalconnections.windows.nodes.PhysicalTreeObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PhysicalTreeTopComponent extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager em;    
        
    public PhysicalTreeTopComponent(final LocalObjectLight port, final HashMap<LocalObjectLight, List<LocalObjectLight>> tree) {
        setDisplayName(String.format("Physical Tree for %s", port));
        setLayout(new BorderLayout());
        Mode myMode = WindowManager.getDefault().findMode("properties"); // NOI18N
        myMode.dockInto(this);
        
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        
        BeanTreeView treeView = new BeanTreeView();
        add(treeView);
        
        JToolBar barMain = new JToolBar();
        add(barMain, BorderLayout.PAGE_START);
        barMain.setRollover(true);
        JButton btnShowGraphicalTree = new JButton(new ImageIcon(getClass().
            getResource("/org/inventory/models/physicalconnections/res/graphical_path.png")));
        btnShowGraphicalTree.setToolTipText("See graphical representation");
        barMain.add(btnShowGraphicalTree);
        btnShowGraphicalTree.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                TopComponent tc = new GraphicalPhysicalPathTopComponent(
                    PhysicalConnectionsService.buildPhysicalPathView(tree));
                tc.open();
                tc.requestActive();
            }
        });
        
        em.setRootContext(new PhysicalTreeObjectNode(port, tree, true)); 
        expandNode(treeView, em.getRootContext());
    }
    
    private void expandNode(BeanTreeView treeView, Node parentNode) {
        treeView.expandNode(parentNode);
        for (Node childNode : parentNode.getChildren().getNodes())
            expandNode(treeView, childNode);
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public void componentOpened() {}
    
    @Override
    public void componentClosed() {
        em.setRootContext(Node.EMPTY);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;        
    }
    
}