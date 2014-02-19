/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.special;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.TopComponent;

/**
 * Used to explore a link or a container 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@TopComponent.Description(
    preferredID = "SpecialObjectExplorerTopComponent",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
public class SpecialObjectExplorerTopComponent extends TopComponent 
    implements ExplorerManager.Provider, ActionListener {
    private ExplorerManager em;
    private JScrollPane pnlScrollMain;
    private BeanTreeView aTree;
    
    public SpecialObjectExplorerTopComponent() {
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        initComponents();
    }
        
    public final void initComponents(){
        setLayout(new BorderLayout());
        setDisplayName("Connections Explorer");
        aTree = new BeanTreeView();
        pnlScrollMain = new JScrollPane(aTree);
        add(pnlScrollMain);
    }

    @Override
    public void componentOpened() { }
    
    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        em.setRootContext(new SpecialNode(((ObjectNode)e.getSource()).getObject()));
        setDisplayName(String.format("Exploring %s", ((ObjectNode)e.getSource()).getObject()));
    }
    
}
