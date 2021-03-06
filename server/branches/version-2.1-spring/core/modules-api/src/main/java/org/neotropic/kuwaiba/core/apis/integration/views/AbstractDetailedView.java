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

package org.neotropic.kuwaiba.core.apis.integration.views;

/**
 * Class-specific views.These views work in a similar way to class-level reports: They 
 * are graphical representations spawned directly from the object they are launched from and are 
 * defined per-class basis.In this category fits views for racks, splice boxes and fiber splitters.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T> The business class that represents the nodes and/or connections in the view.
 * @param <C> The visual component to be returned with the rendered view so it can be embedded.
 */
public abstract class AbstractDetailedView<T, C> extends AbstractView {
    /**
     * The business object that will be used as parameter to generate the view. Its class 
     * should match that returned by{@link appliesTo}.
     */
    protected T businessObject;

    public AbstractDetailedView(T businessObject) {
        this.businessObject = businessObject;
    }
    
    /**
     * Returns the class whose instances the view applies to.
     * @return Abstract super classes (such as ViewableObject) 
     * are also supported.
     */
    public abstract String appliesTo();

    public T getBusinessObject() {
        return businessObject;
    }

    public void setBusinessObject(T businessObject) {
        this.businessObject = businessObject;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
