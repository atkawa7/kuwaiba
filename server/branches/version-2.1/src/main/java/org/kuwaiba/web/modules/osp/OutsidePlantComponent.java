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
 */
package org.kuwaiba.web.modules.osp;

import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import javax.inject.Inject;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Main window of the Outside Plant module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@PageTitle("OSP")
@Route(value = "osp")
public class OutsidePlantComponent extends AbstractTopComponent {
    /**
     * The name of the view
     */
    public static String ROUTE_VALUE = "osp";
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    
    public OutsidePlantComponent() {
        init();
    }
    
    public void init() {
        add(new Label(">>> OSP"));
    }
    
    @Override
    public void registerComponents() {
    }

    @Override
    public void unregisterComponents() {
    }

}
