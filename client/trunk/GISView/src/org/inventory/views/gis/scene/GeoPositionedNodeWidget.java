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

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.netbeans.api.visual.widget.LayerWidget;

/**
 * An ObjectNodeWidget with extra geo attributes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GeoPositionedNodeWidget extends AbstractNodeWidget {
    /**
     * Widget's longitude
     */
    private double longitude;
    /**
     * Widget's latitude
     */
    private double latitude;

    public GeoPositionedNodeWidget(GISViewScene scene, LocalObjectLight object, 
            double latitude, double longitude, LayerWidget labelWidget) {
        super(scene, object, labelWidget);
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Convenience method to set longitude and latitude in the same call
     * @param latitude new latitude
     * @param longitude new longitude
     */
    public void setCoordinates(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
