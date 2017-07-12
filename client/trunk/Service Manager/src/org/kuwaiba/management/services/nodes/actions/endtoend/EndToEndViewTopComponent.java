/*
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
 */

package org.kuwaiba.management.services.nodes.actions.endtoend;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.inventory.communications.core.LocalObjectLight;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;

/**
 * Top Component that displays the end-to-end view of service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewTopComponent extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager em = new ExplorerManager();
    private EndToEndViewScene scene;
    private JScrollPane pnlScrollMain;
    private LocalObjectLight service;

    public EndToEndViewTopComponent(LocalObjectLight service) {
        setLayout(new BorderLayout());
        this.service = service;
        this.scene = new EndToEndViewScene();
        pnlScrollMain = new JScrollPane(scene.createView());
        add(pnlScrollMain);
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
        setDisplayName(String.format("End-to-end view for service %s", service));
    }

    @Override
    protected void componentClosed() {
        scene.clear();
    }

    @Override
    protected void componentOpened() {
        scene.render(service);
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

}
