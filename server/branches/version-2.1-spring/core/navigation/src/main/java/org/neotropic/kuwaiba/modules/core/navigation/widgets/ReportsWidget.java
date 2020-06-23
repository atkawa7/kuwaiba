/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.modules.core.navigation.widgets;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Shows the list of class level reports associated to a give object and allows to launch it.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ReportsWidget extends AbstractDashboardWidget {
    /**
     * The inventory object we need the report list for.
     */
    private BusinessObjectLight inventoryObject;
    public ReportsWidget(BusinessObjectLight inventoryObject, MetadataEntityManager mem, ApplicationEntityManager aem, 
            BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.inventoryObject = inventoryObject;
        setTitle(ts.getTranslatedString("module.navigation.widgets.reports.title"));
        createCover();
        coverComponent.addClassName("widgets-colors-good-green");
    }
    
    @Override
    public void createContent() {
        try {
            List<ReportMetadataLight> classLevelReports = bem.getClassLevelReports(inventoryObject.getClassName(), false, false);
            if (classLevelReports.isEmpty()) {
                Label lblNoReports = new Label(ts.getTranslatedString("module.navigation.widgets.reports.ui.no-reports"));
                lblNoReports.addClassName("text-padded");
                contentComponent = lblNoReports;
                return;
            }
                
            Grid<ReportMetadataLight> tblReports = new Grid<>();
            tblReports.setItems(classLevelReports);
            tblReports.addColumn(ReportMetadataLight::getName).setHeader(ts.getTranslatedString("module.widgets.messages.general.name"));
            tblReports.setSizeFull();
            VerticalLayout lytContent = new VerticalLayout(tblReports);
            lytContent.addClassName("widgets-layout-dialog-list");
            contentComponent = lytContent;
        } catch (InventoryException ex) {
            Label lblUnexpectedError = new Label(ex.getLocalizedMessage());
            lblUnexpectedError.addClassName("text-padded");
            contentComponent = lblUnexpectedError;
        }
    }

}
