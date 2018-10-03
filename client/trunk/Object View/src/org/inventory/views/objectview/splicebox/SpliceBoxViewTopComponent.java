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
package org.inventory.views.objectview.splicebox;

import java.awt.BorderLayout;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.views.objectview.splicebox.scene.SpliceBoxViewScene;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Shows a custom view for SpliceBoxes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@TopComponent.Description(
        preferredID = "SpliceBoxViewTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.views.objectview.splicebox.SpliceBoxViewTopComponent")
@ActionReference(path = "Menu/Tools")

@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SpliceBoxViewAction",
        preferredID = "SpliceBoxViewTopComponent"
)
@NbBundle.Messages({
    "CTL_SpliceBoxViewAction=SP Action",
    "CTL_SpliceBoxViewTopComponent=SP Action",
    "HINT_ContactManagerTopComponent=Manage your contacts"
})
public class SpliceBoxViewTopComponent extends TopComponent implements ExplorerManager.Provider {
    /**
     * The scene
     */
    private SpliceBoxViewScene scene;
    private ExplorerManager em;
    
    public SpliceBoxViewTopComponent() {
        this.em = new ExplorerManager();
        this.scene = new SpliceBoxViewScene();
        setLayout(new BorderLayout());
        add(scene.createView());
        associateLookup(scene.getLookup());
    }

    @Override
    protected void componentOpened() {
        scene.render(new LocalObjectLight(45930, "Test SP", "SpliceBox"));
    }

    @Override
    protected void componentClosed() {
        scene.clear();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
