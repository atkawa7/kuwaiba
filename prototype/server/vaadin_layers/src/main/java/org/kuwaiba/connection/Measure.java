/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.connection;

import org.kuwaiba.custom.overlays.NodeMarker;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Measure {
    NodeMarker source;
    NodeMarker target;
    int i = 0;
    
    public Measure() {
    }

    public NodeMarker getSource() {
        return source;
    }

    public void setSource(NodeMarker source) {
        this.source = source;
    }

    public NodeMarker getTarget() {
        return target;
    }

    public void setTarget(NodeMarker target) {
        this.target = target;
    }

    public int getI() {
        return i;
    }

    public void setI(int i) {
        this.i = i;
    }
    
    
}
