 /*
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
package org.kuwaiba.web.custom.osp;

import com.google.common.eventbus.EventBus;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;
import org.kuwaiba.web.modules.osp.google.GoogleMapWrapper;

/**
 * GISView implementation for Google Maps
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GoogleMapsGISView extends AbstractTooledComponent implements AbstractGISView {
    private GoogleMapWrapper mapWrapper;    
    
    public GoogleMapsGISView(final EventBus eventBus) {
        super(new AbstractAction[]{ new AbstractAction("Connection") {
            
            @Override
            public void actionPerformed(Object sourceComponent, Object targetObject) {
                eventBus.post(targetObject);
            }
        }}, AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL, 
                    AbstractTooledComponent.ToolBarSize.NORMAL);
        
        mapWrapper = new GoogleMapWrapper(eventBus);
        mapWrapper.setSizeFull();
        setMainComponent(mapWrapper);
    }

    @Override
    public String getName() {
        return "OSP Module for Google Maps";
    }

    @Override
    public String getDescription() {
        return "OSP Module that uses Google Maps as map provider";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }
}
