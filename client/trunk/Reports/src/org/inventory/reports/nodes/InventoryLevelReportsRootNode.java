/**
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
package org.inventory.reports.nodes;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.reports.nodes.actions.ReportActionsFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * A simple node representing the root of the inventory level reports
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class InventoryLevelReportsRootNode extends AbstractNode {
    
    private static final Image ICON = ImageUtilities.loadImage("org/inventory/reports/res/inventory_level_reports_node.png");

    public InventoryLevelReportsRootNode() {
        super(new InventoryLevelReportsRootChildren());
        setDisplayName("Inventory Level Reports");
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ReportActionsFactory.getCreateInventoryLevelReportAction()};
    }
    
    public static class InventoryLevelReportsRootChildren extends AbstractReportChildren {
        
        @Override
        public void addNotify() {
            List<LocalReportLight> inventoryLevelReports = CommunicationsStub.getInstance().
                    getInventoryLevelReports(true);
            
            if (inventoryLevelReports == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else {
                setKeys(inventoryLevelReports);
            }
        }
        
        @Override
        protected Node[] createNodes(LocalReportLight key) {
            return new Node[] {new ReportNode(key) };
        }
    }  
}
