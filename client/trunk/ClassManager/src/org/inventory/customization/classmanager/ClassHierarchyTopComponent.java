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
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.xml.ClassHierarchyReader;
import org.inventory.core.visual.actions.ExportSceneAction;
import org.inventory.customization.classmanager.scene.ClassHierarchyScene;
import org.inventory.customization.classmanager.scene.xml.ClassHierarchyReaderImpl;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
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
    private JToolBar toolMain;
    private JButton btnExport;
    private NotificationUtil nu;

    public ClassHierarchyTopComponent(byte[] hierarchyAsXML) {
        this.setName("Current Class Hierarchy");
        ClassHierarchyReader xmlReader = new ClassHierarchyReaderImpl();
        try{
            xmlReader.read(hierarchyAsXML);
            scene = new ClassHierarchyScene(xmlReader.getRootClasses());
            toolMain = new JToolBar();
            btnExport = new JButton(new ExportSceneAction(scene));
            btnExport.setIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("org/inventory/customization/classmanager/res/export.png")));
            btnExport.setToolTipText("Export as image...");
            toolMain.add(btnExport);
            Mode editorMode = WindowManager.getDefault().findMode("editor"); //NOI18N
            editorMode.dockInto(this);
            setIcon(ImageUtilities.loadImage("org/inventory/customization/classmanager/res/class-hierarchy.png"));
            setLayout(new BorderLayout());
            pnlScrollMain = new JScrollPane();
            add(toolMain,BorderLayout.NORTH);
            add(pnlScrollMain, BorderLayout.CENTER);
            pnlScrollMain.setViewportView(scene.createView());
            pnlScrollMain.validate();
            add(scene.createSatelliteView(), BorderLayout.SOUTH);
        }catch(Exception e){
            getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, "Error parsing class hierarchy: ["+e.getClass().getSimpleName()+"]"+e.getMessage()); //NOI18N
            //e.printStackTrace();
        }
    }

    public final NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }

    @Override
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }

    @Override
    protected void componentClosed() {
        scene.cleanScene();
    }

    @Override
    protected void componentOpened() {
        
    }
}
