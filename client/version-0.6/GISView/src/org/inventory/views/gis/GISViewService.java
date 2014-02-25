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

package org.inventory.views.gis;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedConnectionWidget;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;

/**
 * Logic associated to the corresponding TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewService {

    private GISViewScene scene;
    private LocalObjectView currentView;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    private GISViewTopComponent gvtc;

    public GISViewService(GISViewScene scene, GISViewTopComponent gvtc) {
        this.scene = scene;
        this.gvtc = gvtc;
    }

    public LocalObjectView getCurrentView(){
        return currentView;
    }

    void setCurrentView(Object object) {
        currentView = null;
    }

    /**
     * Updates the current view
     * @param viewId
     */
    public void loadView(long viewId) {
        this.currentView = com.getGeneralView(viewId);
        if (this.currentView == null)
            nu.showSimplePopup("Loading view", NotificationUtil.ERROR, com.getError());
//        scene.clear();
        buildView();
    }

    private void buildView() throws IllegalArgumentException{
        if (currentView == null)
            return;

        if (currentView.getStructure() != null){
             /*Comment this out for debugging purpose
            try{
                FileOutputStream fos = new FileOutputStream("/home/zim/parsing_"+Calendar.getInstance().getTimeInMillis()+".xml");
                fos.write(currentView.getStructure());
                fos.close();
            }catch(Exception e){}*/

//            scene.activateMap();
            try {
                //Here is where we use Woodstox as StAX provider
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();

                QName qZoom = new QName("zoom"); //NOI18N
                QName qCenter = new QName("center"); //NOI18N
                QName qNode = new QName("node"); //NOI18N
                QName qEdge = new QName("edge"); //NOI18N
                QName qLabel = new QName("label"); //NOI18N
                QName qControlPoint = new QName("controlpoint"); //NOI18N

                ByteArrayInputStream bais = new ByteArrayInputStream(currentView.getStructure());
                XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

                while (reader.hasNext()){
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT){
                        if (reader.getName().equals(qNode)){
                            String objectClass = reader.getAttributeValue(null, "class");

                            double longitude = Double.valueOf(reader.getAttributeValue(null,"x"));
                            double latitude = Double.valueOf(reader.getAttributeValue(null,"y"));
                            Long objectId = Long.valueOf(reader.getElementText());

                            LocalObjectLight lol = CommunicationsStub.getInstance().
                                    getObjectInfoLight(objectClass, objectId);
                            if (lol != null){
                                GeoPositionedNodeWidget widget = (GeoPositionedNodeWidget)scene.addNode(lol);
                                widget.setCoordinates(latitude, longitude);
//                                widget.setPreferredLocation(scene.coordinateToPixel(latitude, longitude, currentView.getZoom()));
                                //Hack: a scene doesn't support negative locations, 
                                //so when the widgets are painted, the coordinates are turned positive
                                if (widget.getPreferredLocation().x < 0 || widget.getPreferredLocation().y < 0)
                                    widget.setVisible(false);
                                scene.validate();
                            }
                            else
                                currentView.setDirty(true);
                        }else{
                            if (reader.getName().equals(qEdge)){
                                Long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                                Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                                Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                                String className = reader.getAttributeValue(null,"class");
                                LocalObjectLight container = CommunicationsStub.getInstance().getObjectInfoLight(className, objectId);
                                if (container != null){
                                    LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                    Widget aSideWidget = scene.findWidget(aSideObject);

                                    LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                    Widget bSideWidget = scene.findWidget(bSideObject);

                                    if (aSideWidget == null || bSideWidget == null)
                                        currentView.setDirty(true);
                                    else{
                                        GeoPositionedConnectionWidget newEdge = (GeoPositionedConnectionWidget)scene.addEdge(container);
                                        newEdge.setSourceAnchor(AnchorFactory.createRectangularAnchor(aSideWidget, true));
                                        newEdge.setTargetAnchor(AnchorFactory.createRectangularAnchor(bSideWidget, true));
                                        newEdge.setLineColor(PhysicalConnectionProvider.getConnectionColor(container.getClassName()));

                                        boolean visible = true;
                                        List<Point> localControlPoints = new ArrayList<Point>();
                                        while(true){
                                            reader.nextTag();
                                            if (reader.getName().equals(qControlPoint)){
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT){
                                                    double longitude = Double.valueOf(reader.getAttributeValue(null,"x"));
                                                    double latitude = Double.valueOf(reader.getAttributeValue(null,"y"));
                                                    newEdge.getGeoPositionedControlPoints().add(new double[]{longitude, latitude});
//                                                    Point newControlPoint = scene.coordinateToPixel(latitude, longitude, currentView.getZoom());
//                                                    localControlPoints.add(newControlPoint);
//                                                    if (newControlPoint.x <= 0 || newControlPoint.y <= 0)
//                                                        visible = false;
                                                }
                                            }else{
                                                if (!localControlPoints.isEmpty())
                                                    newEdge.setControlPoints(localControlPoints, false);
                                                newEdge.setVisible(visible);
                                                break;
                                            }
                                        }
                                        scene.validate();
                                    }
                                }else
                                    currentView.setDirty(true);
                            }else{
                                if (reader.getName().equals(qLabel)){
                                    //Unavailable for now
                                }
                                else{
                                    if (reader.getName().equals(qZoom)){
                                        currentView.setZoom(Integer.valueOf(reader.getElementText()));
//                                        scene.zoom(currentView.getZoom());
                                    }
                                    else{
                                        if (reader.getName().equals(qCenter)){
                                            double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                            double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                            currentView.setCenter(new double[]{x,y});
//                                            scene.setCenterPosition(currentView.getCenter()[1], currentView.getCenter()[0]);
                                        }else {
                                            //Place more tags
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                reader.close();
                scene.validate();
                gvtc.toggleButtons(true);
            } catch (XMLStreamException ex) {
                gvtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, "Error rendering view file (Corrupted File)");
//                scene.clear();
            } catch (IllegalStateException ise){
                gvtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, "Error rendering view file (Illegal State)");
//                scene.clear();
            } catch (NumberFormatException nfe){
                gvtc.getNotifier().showSimplePopup("Object View", NotificationUtil.ERROR, "Error rendering view file (Wrong Number Format)");
//                scene.clear();
            }
        }
    }

    void saveView(String nameInTxt, String descriptionInTxt) {
        if (currentView == null){
//            byte[] structure = scene.getAsXML();
//            long viewId = com.createGeneralView(LocalObjectViewLight.TYPE_GIS, nameInTxt, descriptionInTxt, structure, null);
//            if (viewId != -1){
//                currentView = new LocalObjectView(viewId, nameInTxt, descriptionInTxt, LocalObjectViewLight.TYPE_GIS, structure, null);
//                nu.showSimplePopup("New View", NotificationUtil.INFO, "View created successfully");
//            }else
//                nu.showSimplePopup("New View", NotificationUtil.ERROR, com.getError());
        }
        else{
//            if (com.updateGeneralView(currentView.getId(), nameInTxt, descriptionInTxt, scene.getAsXML(), null)){
                currentView.setName(nameInTxt);
                currentView.setDescription(descriptionInTxt);
                nu.showSimplePopup("Save View", NotificationUtil.INFO, "View created successfully");
            }
//            else
//                nu.showSimplePopup("Save View", NotificationUtil.ERROR, com.getError());
        }
//    }

//    void deleteCurrentView() {
//        if (currentView == null)
//            nu.showSimplePopup("Delete View", NotificationUtil.INFO, "This view has not been saved yet");
//        else{
//            if (com.deleteGeneralViews(new long[]{currentView.getId()})){
//                scene.clear();
//                currentView = null;
//                gvtc.toggleButtons(false);
//                nu.showSimplePopup("Delete View", NotificationUtil.INFO, "View deleted successfully");
//            }
//            else
//                nu.showSimplePopup("Delete View", NotificationUtil.ERROR, com.getError());
//        }
//    }
//
//    void toggleLabels(boolean isVisible) {
//        for (Widget aNode :scene.getNodesLayer().getChildren())
//            ((GeoPositionedNodeWidget)aNode).getLabelWidget().setVisible(isVisible);
//                
//        scene.validate();
//    }
}
