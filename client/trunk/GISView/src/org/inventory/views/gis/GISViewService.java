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
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedConnectionWidget;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;
import org.openstreetmap.gui.jmapviewer.Coordinate;

/**
 * Logic associated to the corresponding TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewService {

    private GISViewScene scene;
    private LocalObjectView currentView;
    private CommunicationsStub com = CommunicationsStub.getInstance();
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
     * Load a previously saved view
     * @param viewId
     */
    public void loadView(long viewId) throws Exception {
        this.currentView = com.getGeneralView(viewId);
        if (this.currentView == null)
            throw new Exception(com.getError());
        buildView();
        scene.validate();
    }

    private void buildView() throws IllegalArgumentException{
        if (currentView == null)
            return;

        if (currentView.getStructure() != null){
            /* Comment this out for debugging purpose
            try{
                FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/parsing_"+Calendar.getInstance().getTimeInMillis()+".xml");
                fos.write(currentView.getStructure());
                fos.close();
            }catch(Exception e){}
            */
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
                                widget.setPreferredLocation(scene.getMap().getMapPosition(latitude, longitude, false));
                                widget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                                if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                                    System.out.println(String.format("%s --> lon=%s lat=%s (x,y)=%s", 
                                            lol, longitude, latitude, widget.getPreferredLocation()));
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
                                        newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget));
                                        newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget));
                                        newEdge.setLineColor(Utils.getConnectionColor(container.getClassName()));

                                        List<Point> localControlPoints = new ArrayList<Point>();
                                        while(true){
                                            reader.nextTag();
                                            if (reader.getName().equals(qControlPoint)){
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT){
                                                    double longitude = Double.valueOf(reader.getAttributeValue(null,"x"));
                                                    double latitude = Double.valueOf(reader.getAttributeValue(null,"y"));
                                                    newEdge.getGeoPositionedControlPoints().add(new double[]{longitude, latitude});
                                                    Point newControlPoint = scene.getMap().getMapPosition(latitude, longitude, false);
                                                    localControlPoints.add(newControlPoint);
                                                }
                                            }else{
                                                if (!localControlPoints.isEmpty())
                                                    newEdge.setControlPoints(localControlPoints, false);
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
                                        scene.getMap().setZoom(currentView.getZoom());
                                    }
                                    else{
                                        if (reader.getName().equals(qCenter)){
                                            double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                            double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                            currentView.setCenter(new double[]{x,y});
                                            scene.getMap().setDisplayPosition(
                                                    new Coordinate(y, x), currentView.getZoom());
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
                scene.resetDefaultLastPositions();
                gvtc.toggleButtons(true);
            } catch (XMLStreamException ex) {
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Error rendering view file (Corrupted File)");
            } catch (IllegalStateException ise){
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Error rendering view file (Illegal State)");
            } catch (NumberFormatException nfe){
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Error rendering view file (Wrong Number Format)");
            }
            gvtc.setDisplayName(currentView.getName());
        }
    }

    void saveView(String nameInTxt, String descriptionInTxt) {
        if (currentView == null){
            byte[] structure = scene.getAsXML();
            long viewId = com.createGeneralView(LocalObjectViewLight.TYPE_GIS, nameInTxt, descriptionInTxt, structure, null);
            if (viewId != -1){
                currentView = new LocalObjectView(viewId, nameInTxt, descriptionInTxt, LocalObjectViewLight.TYPE_GIS, structure, null);
                gvtc.getNotifier().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "View created successfully");
            }else
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        else{
            if (com.updateGeneralView(currentView.getId(), nameInTxt, descriptionInTxt, scene.getAsXML(), null)){
                currentView.setName(nameInTxt);
                currentView.setDescription(descriptionInTxt);
                gvtc.getNotifier().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "View saved successfully");
            }
            else
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        gvtc.setDisplayName(currentView.getName() == null ? "No Name" : currentView.getName());
    }

    public boolean deleteCurrentView() {
        if (currentView == null)
            scene.clear();
        else{
            if (com.deleteGeneralViews(new long[]{currentView.getId()})){
                scene.clear();
                currentView = null;
                gvtc.getNotifier().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "View deleted successfully");
            }else{
                gvtc.getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
        }
        return true;
    }
}
