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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.reports.nodes.actions.ReportActionsFactory;
import org.inventory.reports.nodes.properties.BasicProperty;
import org.inventory.reports.nodes.properties.ReportParameterProperty;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;
import org.openide.util.ImageUtilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * A simple node representing a report
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ReportNode extends AbstractNode implements PropertyChangeListener {

    private static final Image ICON = ImageUtilities.loadImage("org/inventory/reports/res/report_node.png");
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public ReportNode(LocalReportLight report) {
        super(Children.LEAF, Lookups.singleton(report));
        setDisplayName(report.getName());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        if (getParentNode() instanceof InventoryLevelReportsRootNode) //It's an inventory level report
            return new Action[] { ReportActionsFactory.getExecuteInventoryLevelReportAction(),
                                  ReportActionsFactory.getAddParameterToReportAction(),
                                  ReportActionsFactory.getRemoveParameterFromReportAction(),
                                  null, ReportActionsFactory.getDeleteClassLevelReportAction() };
        else                                                         //It's a class level report
            return new Action[] { ReportActionsFactory.getDeleteClassLevelReportAction() };
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
    public boolean canRename() {
        return true;
    }
    
    @Override
    public String getName() {
        return getLookup().lookup(LocalReportLight.class).getName();
    }
    
    @Override
    public void setName(String s) {
        propertyChange(new PropertyChangeEvent(new LocalReport(getLookup().lookup(LocalReportLight.class).getId(), 
                        s, null, null, null, null, null), PROP_NAME, s, s));
                
        if (getSheet() != null)
            setSheet(createSheet());
    }
    
    @Override
    public Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        
        Sheet.Set generalPropertySet = Sheet.createPropertiesSet();
        generalPropertySet.setDisplayName("General");
        generalPropertySet.setName("General");
        
        Sheet.Set paramsPropertySet = Sheet.createPropertiesSet();
        paramsPropertySet.setDisplayName("Report Parameters (Read-only)");
        paramsPropertySet.setName("Report Parameters (Read-only)");
        
        LocalReport localReport = com.getReport(getLookup().lookup(LocalReportLight.class).getId());
        if (localReport == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            
            localReport.addPropertyChangeListener(WeakListeners.propertyChange(this, localReport));
            
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_NAME, String.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_DESCRIPTION, String.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_ENABLED, Boolean.class, localReport));
            generalPropertySet.put(new BasicProperty(Constants.PROPERTY_SCRIPT, String.class, localReport));

            if (localReport.getParameters() !=  null) {
                for (String parameter : localReport.getParameters())
                    paramsPropertySet.put(new ReportParameterProperty(parameter, localReport));
            }
        }
        
        sheet.put(generalPropertySet);
        sheet.put(paramsPropertySet);
        
        return sheet;
    }
    
    public void resetPropertySheet() {
        setSheet(createSheet());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LocalReport affectedReport = (LocalReport)evt.getSource();
        
        if (!com.updateReport(affectedReport.getId(), affectedReport.getName(),
                affectedReport.getDescription(), affectedReport.isEnabled(), affectedReport.getType(),
                affectedReport.getScript()))
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (evt.getPropertyName().equals(Constants.PROPERTY_NAME))
                setDisplayName(affectedReport.getName());
        }
    }
}
