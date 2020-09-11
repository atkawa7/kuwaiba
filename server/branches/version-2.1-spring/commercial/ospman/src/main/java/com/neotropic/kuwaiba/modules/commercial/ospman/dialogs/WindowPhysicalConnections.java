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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowPhysicalConnections extends Dialog {
    
    public WindowPhysicalConnections(BusinessObjectLight businessObject, TranslationService ts, BusinessEntityManager bem) {
        VerticalLayout vlyContainer = new VerticalLayout();
        
        Grid<BusinessObjectLight> grdPort = new Grid();
        DataProvider<BusinessObjectLight, Void> dataProvider = DataProvider.fromCallbacks(
                query -> { 
                    try {
                        /**
                         * query.getOffset() and query.getLimit()
                         * These calls are not necessary and yet they are necessary
                         * Data provider stuff.
                         */
                        query.getOffset();
                        query.getLimit();
                        
                        return bem.getChildrenOfClassLightRecursive(
                            businessObject.getId(), businessObject.getClassName(), Constants.CLASS_GENERICPHYSICALPORT, -1
                        ).stream();
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                    return null;
                },
                query -> {
                    try {
                        int size = bem.getChildrenOfClassLightRecursive(
                            businessObject.getId(), businessObject.getClassName(), Constants.CLASS_GENERICPHYSICALPORT, -1
                        ).size();
                        grdPort.setPageSize(size);
                        return size;
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                    return 0;
                }
        );
        grdPort.setDataProvider(dataProvider);
        grdPort.addColumn(BusinessObjectLight::getName);
        
        vlyContainer.add(grdPort);
        add(vlyContainer);
    }
}
