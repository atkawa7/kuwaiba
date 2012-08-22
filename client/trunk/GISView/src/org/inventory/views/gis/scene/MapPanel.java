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

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerListener;
import java.beans.PropertyChangeListener;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.Painter;

/**
 * This class is more a simplified version of JXMapKit
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MapPanel extends JXPanel{
    /**
     * Default tile size (in pixels)
     */
    public final int DEFAULT_TILE_SIZE = 256;
    /**
     * Default minimum zoom level
     */
    public final int DEFAULT_MINIMUM_ZOOM_LEVEL = 1;
    /**
     * Default maximum zoom level
     */
    public final int DEFAULT_MAXIMUM_ZOOM_LEVEL = 15;
    /**
     * Open Street Maps tile server base URL
     */
    public final String OSM_BASE_URL = "http://tile.openstreetmap.org";
    /**
     * Default providers available. Use <strong>OSM</strong> is Open Street maps and <strong>Custom</strong> if you would like to set your own parameters
     */
    public enum Providers {OSM, Custom};
    /**
     * The actual map viewer component
     */
    private JXMapViewer map;

    public MapPanel() {
        map = new JXMapViewer();
        setLayout(new BorderLayout());
        add(map);
        map.setOverlayPainter(new Painter() {

            @Override
            public void paint(Graphics2D gd, Object t, int i, int i1) {
                map.firePropertyChange("painted", 1, 2);
            }
        });
    }

    public void setCenterPosition(GeoPosition coordinates){
        map.setCenterPosition(coordinates);
    }
    /**
     * Sets the tile provider
     * @param provider Open Street maps or Custom
     */
    public void setProvider(Providers provider){
        if (provider == Providers.OSM){
            TileFactoryInfo info = new TileFactoryInfo(DEFAULT_MINIMUM_ZOOM_LEVEL,DEFAULT_MAXIMUM_ZOOM_LEVEL,DEFAULT_MAXIMUM_ZOOM_LEVEL + 2,
                    DEFAULT_TILE_SIZE, true, true, // tile size is 256 and x/y orientation is normal
                    OSM_BASE_URL,
                    "x","y","z") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    zoom = DEFAULT_MAXIMUM_ZOOM_LEVEL - zoom + 2;
                    String url = this.baseURL +"/"+zoom+"/"+x+"/"+y+".png";
                    return url;
                }

            };
            TileFactory tf = new DefaultTileFactory(info);
            map.setTileFactory(tf);
            map.setZoom(10);
            map.setCenterPosition(new GeoPosition(0,0));
        }
    }


    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener){
        map.addPropertyChangeListener(propertyName, listener);
    }

    public JXMapViewer getMainMap(){
        return map;
    }
}
