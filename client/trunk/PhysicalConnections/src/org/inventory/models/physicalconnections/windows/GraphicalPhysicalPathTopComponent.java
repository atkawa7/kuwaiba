/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.models.physicalconnections.scene.PhysicalPathScene;
import org.inventory.models.physicalconnections.scene.SelectableWidget;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GraphicalPhysicalPathTopComponent extends TopComponent implements ExplorerManager.Provider{
    private ExplorerManager em;
    private JScrollPane pnlScrollMain;
    private PhysicalPathScene scene;
    private JToolBar barMain;
    private JButton btnExport;

    public GraphicalPhysicalPathTopComponent(PhysicalPathScene scene) {
        this.scene = scene;
        scene.addObjectSceneListener(new LocalSceneListener(), ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        this.setDisplayName("Physical Path");
        setLayout(new BorderLayout());
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        initComponents();
    }
    
    public final void initComponents(){
        barMain = new JToolBar();
        add(barMain, BorderLayout.PAGE_START);
        barMain.setRollover(true);
        btnExport = new JButton(new javax.swing.ImageIcon(getClass().
                getResource("/org/inventory/models/physicalconnections/res/export.png"))); //NOI18N
        btnExport.setToolTipText("Export view");
        barMain.add(btnExport);
        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ImageFilter.getInstance()}, scene);
                DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            }
        });
        
        pnlScrollMain = new JScrollPane(scene.createView());
        add(pnlScrollMain);
        Mode myMode = WindowManager.getDefault().findMode("editor");
        myMode.dockInto(this);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        //This requires that CoreUI to be enable in the project
        
    }
    
    @Override
    public void componentClosed() {
        scene.clear();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    private class LocalSceneListener implements ObjectSceneListener {
            @Override
            public void objectAdded(ObjectSceneEvent event, Object addedObject) {}

            @Override
            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}

            @Override
            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}

            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1){
                    SelectableWidget selectedWidget = (SelectableWidget)scene.findWidget(newSelection.iterator().next());
                    selectedWidget.highlight();
                    setActivatedNodes(new Node[]{selectedWidget.getNode()});
                }
                    
                for (Object object : previousSelection){
                    SelectableWidget widget = (SelectableWidget)scene.findWidget(object);
                    widget.reset();
                }
            }

            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}

            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}

            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
    }
}
