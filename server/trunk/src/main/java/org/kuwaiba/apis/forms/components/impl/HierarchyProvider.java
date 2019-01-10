/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import java.util.List;

/**
 * Provides a set of methods to access to a hierarchy
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @param <T> The data type of the hierarchy
 */
public interface HierarchyProvider<T> {
    /**
     * Gets the Root of the Hierarchy
     * @return The Root of the Hierarchy
     */
    T getRoot();
    /**
     * Sets the Root of the Hierarchy
     * @param root Root of the Hierarchy
     */        
    void setRoot(T root);
    /**
     * Gets a set of children to the given parent
     * @param parent The Parent of the Subhierarchy
     * @return A set of children to the given parent
     */
    List<T> getChildren(T parent);
}
