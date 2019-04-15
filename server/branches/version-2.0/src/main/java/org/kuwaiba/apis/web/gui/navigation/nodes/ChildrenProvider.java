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

package org.kuwaiba.apis.web.gui.navigation.nodes;

import java.util.List;

/**
 * A factory interface that will fetch the children of a given object in a tree structure. These children are not
 * necessarily children as in the containment hierarchy, they're just the children as in a hierarchical representation
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @param <P> The type of the parent object
 * @param <C> The type of the children object
 */
public interface ChildrenProvider<P, C> {
    public List<C> getChildren(P c);
}
