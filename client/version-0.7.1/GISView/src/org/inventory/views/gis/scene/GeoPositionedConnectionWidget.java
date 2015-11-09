/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.gis.scene;

import org.inventory.core.visual.scene.AbstractConnectionWidget;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.anchor.PointShape;

/**
 * An ObjectConnectionWidget with geopositioned control points
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GeoPositionedConnectionWidget extends AbstractConnectionWidget {

    /**
     * Geopositioned control points as a set of pairs (latitude, longitude)
     */
    private List<double[]> geoPositionedControlPoints;
    

    public GeoPositionedConnectionWidget(GISViewScene scene, LocalObjectLight object, ArrayList<double[]> controlPoints) {
        super(scene, object);

        if (controlPoints == null)
            throw new NullPointerException("The set of control points can not be null");
        
        this.geoPositionedControlPoints = controlPoints;
        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
    }

    public GeoPositionedConnectionWidget(GISViewScene scene, LocalObjectLight object) {
        this(scene, object, new ArrayList<double[]>());
    }
    
    /**
     * These control points can only be set by the setControlPoints method
     * @return the set of
     */
    public List<double[]> getGeoPositionedControlPoints() {
        return geoPositionedControlPoints;
    }

    public void setGeoPositionedControlPoints(List<double[]> geoPositionedControlPoints) {
        this.geoPositionedControlPoints = geoPositionedControlPoints;
    }
}
