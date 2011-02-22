/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.customization.classmanager;

import java.awt.BorderLayout;
import javax.swing.JScrollPane;
import org.inventory.customization.classmanager.scene.ClassHierarchyScene;
import org.openide.util.ImageUtilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top Component to display the class hierarchy
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ClassHierarchyTopComponent extends TopComponent{

    private ClassHierarchyScene scene;
    private JScrollPane pnlScrollMain;

    public ClassHierarchyTopComponent(byte[] hierarchyAsXML) {
        this.setName("Current Class Hierarchy");
        //scene = new ClassHierarchyScene();
        Mode editorMode = WindowManager.getDefault().findMode("editor"); //NOI18N
        editorMode.dockInto(this);
        setIcon(ImageUtilities.loadImage("org/inventory/customization/classmanager/res/class-hierarchy.png"));
        setLayout(new BorderLayout());
        pnlScrollMain = new JScrollPane();
        add(pnlScrollMain, BorderLayout.CENTER);
        pnlScrollMain.setViewportView(scene.getView());
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
    }

    @Override
    protected void componentOpened() {
        scene.cleanScene();
    }


}
