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

package org.neotropic.kuwaiba.modules.core.navigation;

import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.properties.PropertySheet;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * The main object editing interface. This dashboard provides a property sheet, custom actions,
 * views, explorers (special children, relationships, etc) as well as some room to add widgets 
 * in order to display contextual information about the selected object.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectDashboard extends AbstractDashboardWidget {
    /**
     * The panel containing the accordions with options, property sheets and explorers.
     */
    private Div pnlLeft;
    /**
     * Panel used as view port for the views and custom widgets.
     */
    private Div pnlRight;
    /**
     * Reference to the object being edited/explored currently.
     */
    private BusinessObjectLight selectedObject;
    

    public ObjectDashboard(BusinessObjectLight selectedObject, MetadataEntityManager mem, 
            ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts) {
        super(mem, aem, bem, ts);
        this.selectedObject = selectedObject;
    }
    
    @Override
    public void createContent() {
        HorizontalLayout lytContent = new HorizontalLayout();
        Accordion accOptions = new Accordion();
        PropertySheet shtMain = new PropertySheet(ts);
        accOptions.add(ts.getTranslatedString("module.navigation.widgets.object-dashboard."), shtMain);
        this.contentComponent = accOptions;
        add(this.contentComponent);
    }
}
