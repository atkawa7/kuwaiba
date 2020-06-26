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
package com.neotropic.kuwaiba.modules.commercial.ospman.widgets;

import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantView;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.views.ViewNodeIconGenerator;
import org.neotropic.util.visual.widgets.AbstractDashboardWidget;

/**
 * A simple widget with an empty map.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class EmptyMapDashboardWidget extends AbstractDashboardWidget {
    /**
     * A reference to the service that generates icons. This is not necessary for this class, but 
     * it will be for subclasses, such as {@link AllBuildingsMapWidget}.
     */
    protected ResourceFactory resourceFactory;
    
    public EmptyMapDashboardWidget(ApplicationEntityManager aem, BusinessEntityManager bem, 
                                        MetadataEntityManager mem, TranslationService ts, ResourceFactory resourceFactory) {
        super(mem, aem, bem, ts);
        this.resourceFactory = resourceFactory;
        setSizeFull();
        createContent();
    }

    @Override
    public void createContent() {
        OutsidePlantView viewOsp = new OutsidePlantView(mem, aem, bem, ts, 
            new ViewNodeIconGenerator(resourceFactory), false, () -> {});
        viewOsp.buildEmptyView();
        try {
            add(viewOsp.getAsComponent());
        } catch (InvalidArgumentException ex) {
            add(new Label(ex.getLocalizedMessage()));
        }
    }
}
