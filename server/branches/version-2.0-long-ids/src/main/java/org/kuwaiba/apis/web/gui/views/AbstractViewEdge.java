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

import java.util.Objects;
import java.util.Properties;

/**
 * Represents an edge in an AbstractView. An edge might represent a business object or simply a line. Subclasses should be implemented depending on what the line (edge) is representing.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <E> The type of the object represented by this line
 */
public abstract class AbstractViewEdge<E> {
    /**
     * The object behind the edge is know as its id and should be unique among the existing edges.
     */
    private E identifier;
    /**
     * Properties associated to the edge. These properties can be used, among other things, to render it.
     */
    private Properties properties;
    
    public AbstractViewEdge(E identifier) {
        this.identifier = identifier;
        this.properties = new Properties();
    }
    
    public E getIdentifier() {
        return identifier;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractViewNode))
            return false;
        
        return identifier.equals(((AbstractViewNode)obj).getIdentifier());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.identifier);
        return hash;
    }
}
