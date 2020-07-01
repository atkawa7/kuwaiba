/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.reports.nodes.ClassLevelReportsRootNode;
import org.inventory.reports.nodes.InventoryLevelReportsRootNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Service class for this module. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportsService  {
    private static ReportsService instance;
    private ReportsTopComponent topComponent;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public ReportsService(ReportsTopComponent topComponent) {
        this.topComponent = topComponent;
    }
    
    public void setRoot() {
        
        topComponent.getExplorerManager().setRootContext(
                new AbstractNode(new Children.Array() {

                    @Override
                    protected Collection<Node> initCollection() {
                        List<Node> reportRootNodes = new ArrayList<>();
                        reportRootNodes.add(new ClassLevelReportsRootNode());
                        reportRootNodes.add(new InventoryLevelReportsRootNode());

                        return reportRootNodes;
                    }
                    
                })
        );
    }

}
