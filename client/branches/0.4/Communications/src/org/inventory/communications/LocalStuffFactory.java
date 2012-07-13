/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications;

import java.awt.Point;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.core.LocalAttributeWrapperImpl;
import org.inventory.communications.core.LocalClassMetadataImpl;
import org.inventory.communications.core.LocalClassWrapperImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.communications.core.queries.LocalTransientQueryImpl;
import org.inventory.communications.core.views.LocalEdgeImpl;
import org.inventory.communications.core.views.LocalNodeImpl;
import org.inventory.communications.core.views.LocalObjectViewImpl;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.metadata.LocalAttributeWrapper;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.metadata.LocalClassWrapper;
import org.inventory.core.services.api.queries.LocalQuery;
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalLabel;
import org.inventory.core.services.api.visual.LocalNode;
import org.inventory.core.services.api.visual.LocalObjectView;

/**
 * This is a factory used to provide implementations for all the interfaces implemented in this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalStuffFactory {
    public static LocalObject createLocalObject(){
        return new LocalObjectImpl();
    }

    public static LocalObjectLight createLocalObjectLight(){
        return new LocalObjectLightImpl();
    }

    public static LocalAttributeWrapper createLocalAttributeWrapper() {
        return new LocalAttributeWrapperImpl();
    }

    public static LocalClassWrapper createLocalClassWrapper() {
        return new LocalClassWrapperImpl();
    }

    public static LocalTransientQuery createLocalTransientQuery(LocalQuery localQuery) throws XMLStreamException {
        return new LocalTransientQueryImpl(localQuery);
        //return null;
    }

    public static LocalTransientQuery createLocalTransientQuery(String nodeName, int logicalConnector, boolean isJoin, int limit, int page) {
        return new LocalTransientQueryImpl(nodeName, logicalConnector, isJoin, limit, page);
        //return null;
    }

    public static LocalEdge createLocalEdge(LocalObjectLight toAdd, LocalNode nodeA, LocalNode nodeB, Point[] controlPoints) {
        return new LocalEdgeImpl(toAdd, nodeB, nodeB, controlPoints);
    }

    public static LocalEdge createLocalEdge(LocalObjectLight container, Point[] controlPoints) {
        return new LocalEdgeImpl(container, controlPoints);
    }

    public static LocalEdge createLocalEdge(LocalObjectLight obj){
        return new LocalEdgeImpl(obj);
    }

    public static LocalNode createLocalNode(LocalObjectLight node, int lastX, int i) {
        return new LocalNodeImpl(node, lastX, i);
    }

    public static LocalObjectView createLocalObjectView(LocalNode[] myNodes, LocalEdge[] myEdges, LocalLabel[] myLabels) {
        return new LocalObjectViewImpl(myNodes, myEdges, myLabels);
    }

    public static LocalClassMetadata createLocalClassMetadata() {
        return new LocalClassMetadataImpl();
    }
}
