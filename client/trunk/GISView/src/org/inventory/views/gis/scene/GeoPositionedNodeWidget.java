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

package org.inventory.views.gis.scene;

import org.inventory.core.services.api.LocalObjectLight;

/**
 * An ObjectNodeWidget with
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GeoPositionedNodeWidget extends ObjectNodeWidget{
    /**
     * Widget's longitude
     */
    private double longitude;
    /**
     * Widget's latitude
     */
    private double latitude;

    public GeoPositionedNodeWidget(GISViewScene scene, LocalObjectLight object, double latitude, double longitude) {
        super(scene, object);
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
     * Convenience method to set both components of the widget coordinates
     * @param latitude new latitude
     * @param longitude new longitude
     */
    public void setCoordinates(double latitude, double longitude){
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     * This method updates the widget geo-coordinates from the scene coordinates
     */
    public void updateCoordinates() {
        double[] coordinates = ((GISViewScene)getScene()).pixelToCoordinate(getPreferredLocation());
        this.latitude = coordinates[0];
        this.longitude = coordinates[1];
        
    }
}
