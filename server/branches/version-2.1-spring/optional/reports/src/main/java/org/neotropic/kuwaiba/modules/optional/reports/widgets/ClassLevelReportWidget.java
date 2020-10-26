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
package org.neotropic.kuwaiba.modules.optional.reports.widgets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.reports.ReportsUI;
import org.neotropic.util.visual.general.BoldLabel;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * Shows the class level report of the given business Object
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class ClassLevelReportWidget extends AbstractDashboardWidget {

    /**
     * The main business object
     */
    private BusinessObjectLight businessObject;

    public ClassLevelReportWidget(BusinessObjectLight businessObject, MetadataEntityManager mem, ApplicationEntityManager aem,
            BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);

        this.businessObject = businessObject;
        setTitle(ts.getTranslatedString(ts.getTranslatedString("module.reporting.widget.class-level-report")));
        createCover();
        coverComponent.addClassName("widgets-colors-magenta");
    }

    @Override
    public void createContent() {
        try {

            BoldLabel lblTitle = new BoldLabel(String.format(ts.getTranslatedString("module.reporting.widget.class-level-report-available"), businessObject.getClassName()));
            List<ReportMetadataLight> reports = bem.getClassLevelReports(businessObject.getClassName(), true, false);
          
            Grid<ReportMetadataLight> tblInventoryReports = new Grid();
            tblInventoryReports.setItems(reports);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_COMPACT);
            tblInventoryReports.addThemeVariants(GridVariant.MATERIAL_COLUMN_DIVIDERS);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            tblInventoryReports.addThemeVariants(GridVariant.LUMO_NO_BORDER);
            tblInventoryReports.addColumn(ReportMetadataLight::getName)
                    .setHeader(ts.getTranslatedString(ts.getTranslatedString("module.reporting.report-name")))
                    .setKey(ts.getTranslatedString("module.general.labels.name"));          
          
            tblInventoryReports.addItemClickListener(item -> {
                 executeInventoryReport(item.getItem());
            });
           
            Button btnClose = new Button(ts.getTranslatedString("module.general.messages.close"), evt -> {
                getWdwContent().close();
            });
            VerticalLayout lytContent = new VerticalLayout(lblTitle, tblInventoryReports, btnClose);
            lytContent.setWidth("400px");
            contentComponent = lytContent;

        } catch (InventoryException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ex.getMessage()).open();
        }
    }
    /**
     * Executes the given report and creates a downloadable file 
     * @param selectedReport 
     */
    private void executeInventoryReport(ReportMetadataLight selectedReport) {
        try {
            byte[] reportBody = bem.executeClassLevelReport(businessObject.getClassName(), businessObject.getId(), 
                    selectedReport.getId());
            
            final StreamResource resource = new StreamResource("Report",
                () -> new ByteArrayInputStream(reportBody));
            resource.setContentType("text/html");         
            final StreamRegistration registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
            UI.getCurrent().getPage().open(registration.getResourceUri().toString());
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(ReportsUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Logger.getLogger(ClassLevelReportWidget.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
}
