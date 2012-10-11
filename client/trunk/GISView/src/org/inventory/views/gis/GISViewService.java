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
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedConnectionWidget;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.inventory.views.gis.scene.MapPanel;
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
         buildView();
    }

    private void buildView() throws IllegalArgumentException{
        if (currentView == null)
            return;

        if (currentView.getStructure() != null){
            /* Comment this out for debugging purposes
            try{
                FileOutputStream fos = new FileOutputStream("/home/zim/out.xml");
                fos.write(viewStructure);
                fos.close();
            }catch(Exception e){}*/

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

                            int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                            int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                            Long objectId = Long.valueOf(reader.getElementText());

                            LocalObjectLight lol = CommunicationsStub.getInstance().
                                    getObjectInfoLight(objectClass, objectId);
                            if (lol != null){
                                GeoPositionedNodeWidget widget = (GeoPositionedNodeWidget)scene.addNode(lol);
                                widget.setCoordinates(yCoordinate, xCoordinate);
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
                                    LocalObjectLight aSideObject = LocalStuffFactory.createLocalObjectLight();
                                    aSideObject.setOid(aSide);
                                    Widget aSideWidget = scene.findWidget(aSideObject);

                                    LocalObjectLight bSideObject = LocalStuffFactory.createLocalObjectLight();
                                    aSideObject.setOid(bSide);
                                    Widget bSideWidget = scene.findWidget(bSideObject);

                                    if (aSideWidget == null || bSideWidget == null)
                                        currentView.setDirty(true);
                                    else{
                                        GeoPositionedConnectionWidget newEdge = (GeoPositionedConnectionWidget)scene.addEdge(container);
                                        newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(aSideWidget, 3));
                                        newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(bSideWidget, 3));
                                        while(true){
                                            reader.nextTag();
                                            List<Point> localControlPoints = new ArrayList<Point>();
                                            if (reader.getName().equals(qControlPoint)){
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                    localControlPoints.add(scene.coordinateToPixel(Double.valueOf(reader.getAttributeValue(null,"y")), Double.valueOf(reader.getAttributeValue(null,"x")), currentView.getZoom() != 0 ? currentView.getZoom() : MapPanel.DEFAULT_ZOOM_LEVEL ));
                                            }else{
                                                newEdge.setControlPoints(localControlPoints);
                                                break;
                                            }
                                        }
                                    }
                                }else
                                    currentView.setDirty(true);
                            }else{
                                if (reader.getName().equals(qLabel)){
                                    //Unavailable for now
                                }
                                else{
                                    if (reader.getName().equals(qZoom))
                                        currentView.setZoom(Integer.valueOf(reader.getText()));
                                    else{
                                        if (reader.getName().equals(qCenter)){
                                            double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                            double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                            currentView.setCenter(new double[]{x,y});
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
            } catch (XMLStreamException ex) {
                System.out.println("An exception was thrown parsing the XML View: "+ex.getMessage());
            }
        }

        gvtc.toggleButtons(true);
    }

    void saveView(String nameInTxt, String descriptionInTxt) {
        if (currentView == null){
            long viewId = com.createGeneralView(LocalObjectViewLight.TYPE_GIS, nameInTxt, descriptionInTxt, scene.getAsXML(), null);
            if (viewId != -1){
                //currentView = LocalStuffFactory.createLocalObjectViewLight(viewId, nameInTxt, descriptionInTxt, LocalObjectViewLight.TYPE_GIS);
                nu.showSimplePopup("New View", NotificationUtil.INFO, "View created successfully");
            }else
                nu.showSimplePopup("New View", NotificationUtil.ERROR, com.getError());
        }
        else{
            if (com.updateGeneralView(currentView.getId(), nameInTxt, descriptionInTxt, scene.getAsXML(), null))
                nu.showSimplePopup("Save View", NotificationUtil.INFO, "View created successfully");
            else
                nu.showSimplePopup("Save View", NotificationUtil.ERROR, com.getError());
        }
    }
}
