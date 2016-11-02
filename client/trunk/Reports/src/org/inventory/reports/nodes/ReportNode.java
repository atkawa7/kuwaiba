/**
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
package org.inventory.reports.nodes;

import java.awt.Image;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.reports.nodes.actions.ReportActionsFactory;
import org.inventory.reports.nodes.properties.BasicProperty;
import org.inventory.reports.nodes.properties.ReportParameterProperty;
import org.inventory.reports.nodes.properties.ReportPropertyListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * A simple node representing a report
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ReportNode extends AbstractNode {

    private static final Image ICON = ImageUtilities.loadImage("org/inventory/reports/res/report_node.png");
    
    public ReportNode(LocalReportLight report) {
        super(Children.LEAF, Lookups.singleton(report));
        setDisplayName(report.toString());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ReportActionsFactory.getDeleteClassLevelReportAction()};
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
    public Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();
        
        LocalReport localReport = CommunicationsStub.getInstance().getReport(getLookup().lookup(LocalReportLight.class).getId());
        if (localReport == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
        else {
            
            localReport.addPropertyChangeListener(ReportPropertyListener.getInstance());
            
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_NAME, String.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_DESCRIPTION, String.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_ENABLED, Boolean.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_SCRIPT, String.class, localReport));

            if (localReport.getParameters() !=  null) {
                for (String parameter : localReport.getParameters())
                    generalPropertySet.put(new ReportParameterProperty(parameter, localReport));
            }
        }
        
        sheet.put(generalPropertySet);
        return sheet;
    }
}
