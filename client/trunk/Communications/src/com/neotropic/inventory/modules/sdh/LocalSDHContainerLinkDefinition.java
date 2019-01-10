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
package com.neotropic.inventory.modules.sdh;

import java.util.List;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Mirror of the remote class with the same name
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalSDHContainerLinkDefinition {
    private LocalObjectLight container;
    /**
     * Is this container structured?
     */
    private boolean structured;
    /**
     * The positions used by the container
     */
    private List<LocalSDHPosition> positions;

    public LocalSDHContainerLinkDefinition(LocalObjectLight container, boolean structured, List<LocalSDHPosition> positions) {
        this.container = container;
        this.structured = structured;
        this.positions = positions;
    }       

    public LocalObjectLight getContainerName() {
        return container;
    }

    public void setContainerName(LocalObjectLight container) {
        this.container = container;
    }

    public List<LocalSDHPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<LocalSDHPosition> positions) {
        this.positions = positions;
    }

    public LocalObjectLight getContainer() {
        return container;
    }

    public boolean isStructured() {
        return structured;
    }
}
