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

package org.neotropic.kuwaiba.visualization.api;

import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.util.visual.views.AbstractView;

/**
 * Class-specific views.These views work in a similar way to class-level reports: They 
 * are graphical representations spawned directly from the object they are launched from and are 
 * defined per-class basis. In this category fits views for racks, splice boxes and fiber splitters.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The business class that represents the nodes and/or connections in the view.
 */
public abstract class AbstractDetailedView<T> extends AbstractView {
    /**
     * The business object that will be used as parameter to generate the view. Its class 
     * should match that returned by{@link appliesTo}.
     */
    protected BusinessObjectLight businessObject;

    public AbstractDetailedView(BusinessObjectLight businessObject) {
        this.businessObject = businessObject;
    }
    
    /**
     * Returns the class whose instances the view applies to.
     * @return Abstract super classes (such as ViewableObject) 
     * are also supported.
     */
    public abstract String appliesTo();

    public BusinessObjectLight getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(BusinessObjectLight businessObject) {
        this.businessObject = businessObject;
    }
}
