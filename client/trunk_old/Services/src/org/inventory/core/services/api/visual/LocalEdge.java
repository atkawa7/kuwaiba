/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */

package org.inventory.core.services.api.visual;

import org.inventory.core.services.api.LocalObjectLight;

/**
 * Represents an edge in an object view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalEdge {
    /**
     * Some constants
     */

    public double[][] getControlPoints();

    public LocalObjectLight getObject();

    public LocalNode getaSide();

    public LocalNode getbSide();

    public void setaSide(LocalNode aSide);

    public void setbSide(LocalNode bSide);

    public void setControlPoints(double[][] controlPoints);
}
