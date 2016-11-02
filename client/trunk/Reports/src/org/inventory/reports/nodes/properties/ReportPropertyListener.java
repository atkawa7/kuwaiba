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
 *  under the License.
 */
package org.inventory.reports.nodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalReport;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Listener that commits a change performed in a LocalReport instance
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ReportPropertyListener implements PropertyChangeListener {
    private static CommunicationsStub com = CommunicationsStub.getInstance();
    private static ReportPropertyListener instance;
    
    private ReportPropertyListener() {}
    
    public static ReportPropertyListener getInstance() {
        if (instance == null)
            instance = new ReportPropertyListener();
        return instance;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LocalReport affectedReport = (LocalReport)evt.getSource();
        if (!com.updateReport(affectedReport.getId(), affectedReport.getName(),
                affectedReport.getDescription(), affectedReport.isEnabled(), affectedReport.getType(),
                affectedReport.getScript(), affectedReport.getParameters()))
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
    }
    
}
