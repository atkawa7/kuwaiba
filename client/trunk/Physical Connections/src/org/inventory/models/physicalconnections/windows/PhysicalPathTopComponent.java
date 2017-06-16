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
package org.inventory.models.physicalconnections.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.models.physicalconnections.PhysicalConnectionsService;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalPathTopComponent extends TopComponent implements ExplorerManager.Provider{

    private ExplorerManager em = new ExplorerManager();
    private JList lstPath;
    private LocalObjectLight port;
    private ObjectNode selectedObject;
    private LocalObjectLight[] trace;
    
    public PhysicalPathTopComponent(LocalObjectLight port, final LocalObjectLight[] trace) {
        this.port = port;
        this.trace = trace;
        initComponents();
        lstPath.setListData(trace);
    }
    
    public final void initComponents(){
        JToolBar barMain = new JToolBar();
        setLayout(new BorderLayout());
        add(barMain, BorderLayout.PAGE_START);
        barMain.setRollover(true);
        JButton btnShowGraphicalPath = new JButton(new javax.swing.ImageIcon(getClass().
                getResource("/org/inventory/models/physicalconnections/res/graphical_path.png"))); //NOI18N
        btnShowGraphicalPath.setToolTipText("See graphical representation");
        barMain.add(btnShowGraphicalPath);
        btnShowGraphicalPath.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TopComponent tc = new GraphicalPhysicalPathTopComponent(
                        PhysicalConnectionsService.buildPhysicalPathView(trace));
                tc.open();
                tc.requestActive();
            }
        });
        
        this.setDisplayName(String.format("Physical Path for %s", port));
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        
        lstPath = new JList();
        lstPath.setCellRenderer(new CellRenderer());
        Mode myMode = WindowManager.getDefault().findMode("properties");
        myMode.dockInto(this);
        add(lstPath, BorderLayout.CENTER);
        lstPath.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                LocalObjectLight selectedItem = (LocalObjectLight)lstPath.getSelectedValue();
                if (selectedItem == null)
                    return;
                selectedObject = new ObjectNode(selectedItem);
                setActivatedNodes(new Node[]{selectedObject});
            }
        });
        lstPath.addMouseListener(new MouseAdapter() {
            private JPopupMenu  menu = new JPopupMenu();

            @Override
            public void mousePressed(MouseEvent e)  {check(e);}
            @Override
            public void mouseReleased(MouseEvent e) {check(e);}
            
            public void check(MouseEvent e) {
                if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {  //e.isPopupTrigger works differently depending on the platform, so we just check for the second button 
                    lstPath.setSelectedIndex(lstPath.locationToIndex(e.getPoint()));
                    menu = Utilities.actionsToPopup(selectedObject.getActions(true), lstPath);
                    menu.show(lstPath, e.getX(), e.getY());
                }
            }
        });
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {}
    
    @Override
    public void componentClosed() {
        lstPath.removeMouseListener(lstPath.getMouseListeners()[0]);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    private class CellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel cell = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (((LocalObjectLight)value).getOid() == port.getOid())
                cell.setForeground(Color.RED);
            return cell;
        }
    }
}
