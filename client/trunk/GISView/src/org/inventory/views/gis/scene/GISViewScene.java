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

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.inventory.communications.SharedInformation;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.gis.scene.actions.MapWidgetPanAction;
import org.inventory.views.gis.scene.actions.MoveAction;
import org.inventory.views.gis.scene.actions.ZoomAction;
import org.inventory.views.gis.scene.providers.AcceptActionProvider;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ComponentWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Scene used by the GISView component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewScene extends GraphScene<LocalObjectLight, LocalObjectLight> implements PropertyChangeListener, Lookup.Provider{

    /**
     * Default node icon path
     */
    private final String GENERIC_ICON_PATH="org/inventory/views/gis/res/default.png"; //NOI18
    /**
     * Icon radius
     */
    private final int ICON_RADIUS = 8;
    /**
     * Default coordinates to center the map
     */
    public final GeoPosition DEFAULT_CENTER_POSITION = new GeoPosition(2.451627, -76.624424);
    /**
     * Default icon
     */
    private final Image defaultIcon = ImageUtilities.loadImage(GENERIC_ICON_PATH);
    /**
     * Layer to contain the map and its additional components
     */
    private LayerWidget mapLayer;
    /**
     * Layer to contain the nodes (poles, cabinets, etc)
     */
    private LayerWidget nodesLayer;
    /**
     * Layer to contain the connections (containers, links, etc)
     */
    private LayerWidget connectionsLayer;
    /**
     * Layer to contain additional labels (free text)
     */
    private LayerWidget labelsLayer;
    /**
     * Layer to contain cosmetic polygons
     */
    private LayerWidget polygonsLayer;
    /**
     * The widget to contain the map component
     */
    private ComponentWidget mapWidget;
    /**
     * Scene lookup
     */
    private SceneLookup lookup;
    /**
     * Local connect provider
     */
    private PhysicalConnectionProvider connectProvider;

    public GISViewScene() {
        
        mapLayer = new LayerWidget(this);
        nodesLayer = new LayerWidget(this);
        connectionsLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);
        polygonsLayer = new LayerWidget(this);

        addChild(mapLayer);
        addChild(connectionsLayer);
        addChild(nodesLayer);

        this.lookup = new SceneLookup(Lookup.EMPTY);
        this.connectProvider = new PhysicalConnectionProvider(this);
        
        MapPanel myMap = new MapPanel();
        myMap.setProvider(MapPanel.Providers.OSM);
        myMap.getMainMap().setAddressLocation(DEFAULT_CENTER_POSITION);
        myMap.addPropertyChangeListener("painted", this);
        mapWidget = new ComponentWidget(this, myMap);

        mapWidget.getActions().addAction(new MapWidgetPanAction(myMap, MouseEvent.BUTTON1));

        addObjectSceneListener(new ObjectSceneListener() {
            @Override
            public void objectAdded(ObjectSceneEvent event, Object addedObject) { }
            @Override
            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}
            @Override
            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}
            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1)
                    lookup.updateLookup((LocalObjectLight)newSelection.iterator().next());
            }
            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}
            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}
            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        //Actions
        getActions().addAction(new ZoomAction());
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));

        setActiveTool(ObjectNodeWidget.ACTION_SELECT);
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        GeoPositionedNodeWidget myWidget =  new GeoPositionedNodeWidget(this,node, 0, 0);
        nodesLayer.addChild(myWidget);
        myWidget.setImage(defaultIcon);
        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(createSelectAction());
        myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(new MoveAction());
        myWidget.getActions(ObjectNodeWidget.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(connectionsLayer, connectProvider));
        return myWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        GeoPositionedConnectionWidget myWidget =  new GeoPositionedConnectionWidget(this, edge);
        connectionsLayer.addChild(myWidget);
        myWidget.getActions().addAction(createSelectAction());
        myWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        myWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        myWidget.setStroke(new BasicStroke(3));
        myWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setRouter(RouterFactory.createFreeRouter());
        return myWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * This method adds the map to the scene. Due to the nature of the JXMapViewer component, The map is built 
     * when the component is painted. If there are network problems, you could get some nasty exceptions.
     */
    public void activateMap(){
        
    }

    /**
     * Updates the map widget bounds to fit the container's ones
     */
    public void updateMapBounds() {
        mapWidget.setPreferredSize(this.getBounds().getSize());
    }

    /**
     * Called when the map is repainted and the nodes must be updated depending on the zoom
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
        Rectangle realViewport = map.getViewportBounds();

        for (Widget node : nodesLayer.getChildren()){
            Point2D point2D = map.getTileFactory().geoToPixel(
                    new GeoPosition(((GeoPositionedNodeWidget)node).getLatitude(), ((GeoPositionedNodeWidget)node).getLongitude()),
                    map.getZoom());
            node.setPreferredLocation(new Point((int)point2D.getX() - realViewport.x - ICON_RADIUS, (int)point2D.getY() - realViewport.y - ICON_RADIUS));
        }

        for (Widget edge : connectionsLayer.getChildren()){
            if (edge instanceof GeoPositionedConnectionWidget ){
                List<Point> newControlPoints = new ArrayList<Point>();
                for (double[] controlPoint : ((GeoPositionedConnectionWidget)edge).getGeoPositionedControlPoints()){
                    Point2D point2D = map.getTileFactory().geoToPixel(
                        new GeoPosition(controlPoint),
                        map.getZoom());
                    newControlPoints.add(new Point((int)point2D.getX() - realViewport.x, (int)point2D.getY() - realViewport.y));
                }
                ((GeoPositionedConnectionWidget)edge).setControlPoints(newControlPoints);
            }
        }

        validate();
        repaint();
    }

    public PhysicalConnectionProvider getConnectProvider() {
        return connectProvider;
    }

    /**
     * Translate a point (Cartesian coordinates) within the map viewport into a GeoPosition object
     * @param point Point to be translated
     * @return the resulting coordinates as a pair (latitude, longitude)
     */
    public double[] pixelToCoordinate(Point point){
        JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
        Rectangle realViewport = map.getViewportBounds();
        GeoPosition coordinates = map.getTileFactory().pixelToGeo(new Point(point.x + realViewport.x, point.y + realViewport.y), map.getZoom());
        return new double[]{coordinates.getLatitude(), coordinates.getLongitude()};
    }

    /**
     * Translate a point (Polar coordinates) into a Point object (Cartesian coordinates)  within the map viewport
     * @param latitude latitude
     * @param longitude longitude
     * @return the resulting Point object
     */
    public Point coordinateToPixel(double latitude, double longitude, int zoom){
        JXMapViewer map = ((MapPanel)mapWidget.getComponent()).getMainMap();
        Rectangle realViewport = map.getViewportBounds();
        Point2D point2D = map.getTileFactory().geoToPixel(new GeoPosition(latitude, longitude), zoom);
        return new Point((int)point2D.getX() - realViewport.x, (int)point2D.getY() - realViewport.y);
    }

    @Override
    public Lookup getLookup(){
        return this.lookup;
    }

    /**
     * Zooms in the inner map
     */
    public void zoomIn() {
        MapPanel mapComponent = (MapPanel)mapWidget.getComponent();
        int currentZoom = mapComponent.getMainMap().getZoom();
        if (currentZoom > mapComponent.getMinZoom())
            mapComponent.getMainMap().setZoom(currentZoom - 1);
    }

    /**
     * Zooms out the inner map
     */
    public void zoomOut() {
        MapPanel mapComponent = (MapPanel)mapWidget.getComponent();
        int currentZoom = mapComponent.getMainMap().getZoom();
        if (currentZoom < mapComponent.getMaxZoom())
            mapComponent.getMainMap().setZoom(currentZoom + 1);
    }

    /**
     * Cleans up the scene and release resources
     */
    public void clear() {
        labelsLayer.removeChildren();
        mapLayer.removeChildren();
        nodesLayer.removeChildren();
        connectionsLayer.removeChildren();
        polygonsLayer.removeChildren();
    }

    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", SharedInformation.VIEW_FORMAT_VERSION); //NOI18N
        //TODO: Get the class name from some else
        mainTag.start("class").text("GISView").end();
        mainTag.start("zoom").text(String.valueOf(((MapPanel)mapWidget.getComponent()).getMainMap().getZoom()));
        mainTag.start("center").attr("y", ((MapPanel)mapWidget.getComponent()).getMainMap().getAddressLocation().getLatitude()).attr("x", ((MapPanel)mapWidget.getComponent()).getMainMap().getAddressLocation().getLongitude()).end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren())
            nodesTag.start("node").attr("x", ((GeoPositionedNodeWidget)nodeWidget).getLongitude()).
            attr("y", ((GeoPositionedNodeWidget)nodeWidget).getLatitude()).
            attr("class", ((GeoPositionedNodeWidget)nodeWidget).getObject().getClassName()).
            text(String.valueOf(((GeoPositionedNodeWidget)nodeWidget).getObject().getOid())).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : connectionsLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((GeoPositionedConnectionWidget)edgeWidget).getObject().getClassName());
            edgeTag.attr("aside", ((GeoPositionedConnectionWidget)((ObjectConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            edgeTag.attr("bside", ((GeoPositionedConnectionWidget)((ObjectConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
            for (double[] point : ((GeoPositionedConnectionWidget)edgeWidget).getGeoPositionedControlPoints())
                edgeTag.start("controlpoint").attr("x", point[1]).attr("y", point[0]).end();
            edgeTag.end();
        }
        edgesTag.end();
        mainTag.end().close();
        return bas.toByteArray();
    }

    public void setCenterPosition(double latitude, double longitude) {
        ((MapPanel)mapWidget.getComponent()).getMainMap().setAddressLocation(new GeoPosition(latitude, longitude));
    }

    public void setZoom(int zoom) {
        ((MapPanel)mapWidget.getComponent()).getMainMap().setZoom(zoom);
    }

    public void loadDefault() {
        mapLayer.addChild(mapWidget);
        validate();
    }

    public boolean hasView() {
        return !mapLayer.getChildren().isEmpty();
    }

    /**
     * Helper class to let us launch a lookup event every time a widget is selected
     */
    private class SceneLookup extends ProxyLookup{

        public SceneLookup(Lookup initialLookup) {
            super(initialLookup);
        }

        public void updateLookup(Lookup newLookup){
            setLookups(newLookup);
        }

        public void updateLookup(LocalObjectLight newElement){
            setLookups(Lookups.singleton(new ObjectNode(newElement)));
        }
    }
}
