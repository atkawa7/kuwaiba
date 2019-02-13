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

package org.kuwaiba.apis.web.gui.views;

import com.vaadin.ui.AbstractComponent;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Defines the behavior of views that can be plugged and played such as End to End views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T>The type of the object used to build the view. For example, in an Object View, 
 */
public abstract class AbstractView<T> {
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    
    public AbstractView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem= bem;
    }
    
    /**
     * The name of the view.
     * @return A short display name of the view
     */
    public abstract String getName();
    /**
     * More details on what the view does.
     * @return 
     */
    public abstract String getDescription();
    /**
     * The current version of the view
     * @return The version of the view.
     */
    public abstract String getVersion();
    /**
     * Who wrote the view.
     * @return A string with the name of the creator of the view, and preferably a way to contact him/her.
     */
    public abstract String getVendor();
    /**
     * Exports the view to XML. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return A byte array with an XML document representing the view. The format of the document must follow the Standard Kuwaiba View Text Format (SKTF)
     */
    public abstract byte[] getAsXML();
    /**
     * Exports the view to a PNG image. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return A byte array with a PNG formatted image of the view.
     */
    public abstract byte[] getAsImage();
    /**
     * Gets an embeddable  Vaadin component that can be rendered in a dashboard. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return An embeddable component (Panel, VerticalLayout, etc)
     */
    public abstract AbstractComponent getAsComponent();
    /**
     * Exports the view as a ViewMap (a representation of the view as a set of Java objects related each other). It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return The view map of the view.
     */
    public abstract ViewMap getAsViewMap();
    /**
     * Builds the view. Call this method if no business object is required to build the view.
     */
    public void build() { /*By default (perhaps most of the times because most of the views are liked to an object), does nothing*/ }
    /**
     * Builds the view. Call this method if a business object is required to build the view (e.g. a service or a room   ).
     * @param businessObject The business object that will be used
     */
    public abstract void build(T businessObject);
    
}
