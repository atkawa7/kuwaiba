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
import java.util.Properties;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * Defines the behavior of views that can be plugged and played such as End to End views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <T>The type of the object used to build the view. For example, in an Object View, 
 */
public abstract class AbstractView<T> {
    /**
     * Reference to the Metadata Entity Manager.
     */
    protected MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    protected ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    protected BusinessEntityManager bem;
    /**
     * The default view map. This view map must be created when either {@link  #buildEmptyView()} or {@link  #buildWithBusinessObject(java.lang.Object)} is called.
     */
    protected ViewMap viewMap;
    /**
     * The properties associated an instance of the view, typically an id, a name and a description.
     */
    protected Properties properties;
    
    public AbstractView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem= bem;
        this.properties = new Properties();
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
     * The properties associated to an instance of the view, typically an id, a name and a description.
     * @return The set or properties.
     */
    public Properties getProperties() {
        return this.properties;
    }
    /**
     * Who wrote the view.
     * @return A string with the name of the creator of the view, and preferably a way to contact him/her.
     */
    public abstract String getVendor();
    /**
     * Exports the view to XML. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return A byte array with an XML document representing the view. The format of the document must follow the Standard Kuwaiba View Text Format (SKTF)
     */
    public abstract byte[] getAsXml();
    /**
     * Exports the view to a PNG image. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return A byte array with a PNG formatted image of the view.
     */
    public abstract byte[] getAsImage();
    /**
     * Gets an embeddable  Vaadin component that can be rendered in a dashboard. It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return An embeddable component (Panel, VerticalLayout, etc)
     * @throws org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException If the component could not be created for some reason (most likely, misconfiguration).
     */
    public abstract AbstractComponent getAsComponent() throws InvalidArgumentException;
    /**
     * Exports the view as a ViewMap (a representation of the view as a set of Java objects related each other). It most likely will have to be called after calling {@link #build() } or {@link  #build(java.lang.Object) }.
     * @return The view map of the view.
     */
    public ViewMap getAsViewMap() {
        return viewMap;
    }
    /**
     * Builds the view. Call this method if no business object is required to build the view. It just loads the elements from an view definition structure (like an XML document).
     * @param view The view to be rendered.
     */
    public abstract void buildWithSavedView(byte[] view);
    /**
     * Builds the view. Call this method if a business object is required to build the view (e.g. a service or a room).
     * @param businessObject The business object that will be used
     */
    public abstract void buildWithBusinessObject(T businessObject);
    /**
     * Builds an empty view map that can be use to render default views.
     */
    public abstract void buildEmptyView();
    /**
     * Adds a node to views that are not generated automatically.
     * @param businessObject The business object behind the node to be added. Nodes that already exist will not be added.
     * @param properties The properties associated to this object, such as the location that will be used to place it or the URL of the icon that will represent the node.
     * @return A reference to the newly added node.
     */
    public abstract AbstractViewNode addNode(T businessObject, Properties properties);
    /**
     * Adds an edge to views that are not generated automatically.
     * @param businessObject The business object behind the edge to be added. Edges that already exist will not be added.
     * @param sourceBusinessObject The business object behind the source node to the edge to be created. 
     * @param targetBusinessObject The business object behind the target node to the edge to be created.
     * @param properties The properties associated to this object, such as the control points of the edge, or its color.
     * @return A reference to the newly added edge.
     */
    public abstract AbstractViewEdge addEdge(T businessObject, T sourceBusinessObject, T targetBusinessObject, Properties properties);
    /**
     * Adds a listener to the node click events.
     * @param listener The listener object.
     */
    public abstract void addNodeClickListener(ViewEventListener listener);
    /**
     * Adds a listener to the node right click event
     * @param listener The listener object.
     */
    public abstract void addNodeRightClickListener(ViewEventListener listener);
    /**
     * Adds a listener to the edge click events.
     * @param listener The listener object.
     */
    public abstract void addEdgeClickListener(ViewEventListener listener);
}
