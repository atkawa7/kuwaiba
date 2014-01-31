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
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.actions.EditObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.RelateToServiceAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
import org.netbeans.swing.etable.ETable;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor for a given object embedding a PropertySheetView
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalPathTopComponent extends TopComponent implements ExplorerManager.Provider{

    private ExplorerManager em = new ExplorerManager();
    private ETable aTable;
    
    public PhysicalPathTopComponent(final LocalObjectLight[] trace) {
        this.setDisplayName("Physical Path");
        setLayout(new BorderLayout());
        aTable = new ETable(new TableModel() {
            LocalObjectLight[] myTrace = trace;
            @Override
            public int getRowCount() {
                return myTrace.length;
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return "";
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return LocalObjectLight.class;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return myTrace[rowIndex];
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            }

            @Override
            public void addTableModelListener(TableModelListener l) {
            }

            @Override
            public void removeTableModelListener(TableModelListener l) {
            }
        });
        Mode myMode = WindowManager.getDefault().findMode("properties");
        myMode.dockInto(this);
        add(aTable, BorderLayout.CENTER);
        aTable.addMouseListener(new PopupProvider());
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
        aTable.removeAll();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    private class PopupProvider extends MouseAdapter{
        @Override
        public void mousePressed(MouseEvent e) {
          showPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
          showPopup(e);
        }
        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                LocalObjectLight singleRecord = (LocalObjectLight)aTable.getValueAt(aTable.rowAtPoint(new Point(e.getX(), e.getY())), 0);
                JPopupMenu  menu = new JPopupMenu();
                menu.add(new RelateToServiceAction(singleRecord));
                menu.add(new ShowObjectIdAction(singleRecord.getOid(), singleRecord.getClassName()));
                menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
