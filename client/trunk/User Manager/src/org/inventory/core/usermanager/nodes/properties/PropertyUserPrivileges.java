/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.core.usermanager.nodes.properties;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalUserObject;
import org.openide.nodes.PropertySupport;

/**
 * Represents the list of privileges of a given user
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertyUserPrivileges extends PropertySupport.ReadWrite<String> {
    private LocalUserObject user;


    public PropertyUserPrivileges(LocalUserObject user) {
        super("privileges", String.class, "Privileges", "Privileges applied to this element. User privileges override group privileges");
        this.user = user;
    }

    @Override
    public String getValue() throws IllegalAccessException, InvocationTargetException {
        return "Click the button to set the privileges...";
    }

    @Override
    public void setValue(String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //Let the PropertyEditorSupport to handle this
    }

    @Override
    public  PropertyEditor getPropertyEditor() {
        return new PrivilegeEditorSupport();
    }
    
    private class PrivilegeEditorSupport extends PropertyEditorSupport {

        @Override
        public boolean supportsCustomEditor() {
            return true;
        }

        @Override
        public Component getCustomEditor() {
            return new PrivilegeEditorPanel();
        }
    }
    
    private class PrivilegeEditorPanel extends JScrollPane {
        
        private JTable privilegeListTable;
                
        public PrivilegeEditorPanel() {
            this.privilegeListTable = new JTable(new PrivilegeTableModel());
            this.privilegeListTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            setViewportView(this.privilegeListTable);
        }
        
        private class PrivilegeTableModel implements TableModel {

            @Override
            public int getRowCount() {
                return 22;
            }

            @Override
            public int getColumnCount() {
                return 3;
            }

            @Override
            public String getColumnName(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return "Module Name";
                    case 1:
                        return "Read Access";
                    case 2:
                        return "Write Access";
                    default:
                        throw new RuntimeException("Invalid Column");
                }
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return String.class;
                    case 1:
                    case 2:
                        return Boolean.class;
                    default:
                        throw new RuntimeException("Invalid column");
                }
                
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex != 0;
                
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                switch (columnIndex) { 
                    case 0: //The first column is the description of the module
                        return LocalPrivilege.DEFAULT_PRIVILEGES[ 2 * rowIndex + 1 ];
                    case 1:
                        LocalPrivilege privilege = user.getPrivilege(LocalPrivilege.DEFAULT_PRIVILEGES[ 2 * rowIndex ]);
                        return (privilege != null && privilege.getAccessLevel() == LocalPrivilege.ACCESS_LEVEL_READ); //Neither read nor write
                    case 2:
                        privilege = user.getPrivilege(LocalPrivilege.DEFAULT_PRIVILEGES[ 2 * rowIndex ]);
                        return (privilege != null && privilege.getAccessLevel() == LocalPrivilege.ACCESS_LEVEL_READ_WRITE); //Neither read nor write
                }
                return null;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                if (columnIndex == 1) //Read access
                    user.setPrivilege(LocalPrivilege.DEFAULT_PRIVILEGES[ rowIndex * 2 ], 
                            (boolean)aValue ? LocalPrivilege.ACCESS_LEVEL_READ : LocalPrivilege.ACCESS_LEVEL_UNSET);
                else
                    user.setPrivilege(LocalPrivilege.DEFAULT_PRIVILEGES[ rowIndex * 2 ], 
                            (boolean)aValue ? LocalPrivilege.ACCESS_LEVEL_READ_WRITE : LocalPrivilege.ACCESS_LEVEL_UNSET);
                
                repaint(); //Oddly, it's required to properly uncheck a box when the other is checked
            }

            @Override
            public void addTableModelListener(TableModelListener l) {}

            @Override
            public void removeTableModelListener(TableModelListener l) {}
        }
    }
}
