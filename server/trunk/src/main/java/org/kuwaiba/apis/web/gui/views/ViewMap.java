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

import java.util.List;

/**
 * A representation of a view (which in turn a graphical representation of an inventory object or a function in the domain of the inventory system) as a set of java objects. 
 * In general terms, a ViewMap instance is a group of nodes and connections between those nodes, as well as auxiliary components, such as comments, or groups of nodes. This map 
 * does not contain rendering information, such as dimensions or positions, but it is rather a description of the internal structure of the view, which can be used by the consumer 
 * to perform analysis on the information contained by the view.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewMap {
    private List<ViewNode> nodes;
    private List<ViewEdge> edges;
}
