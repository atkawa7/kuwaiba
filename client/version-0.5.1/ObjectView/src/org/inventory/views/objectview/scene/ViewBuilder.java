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

package org.inventory.views.objectview.scene;

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
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * This class builds every view so it can be rendered by the scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ViewBuilder {

    /**
     * Wraps the view to be built
     */
    private LocalObjectView currentView;
    /**
     * Reference to the scene
     */
    private ViewScene scene;
    /**
     * Reference to the singleton of CommunicationStub
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();

    /**
     * This constructor should be used if there's already a view
     * @param localView
     * @throws NullPointerException if the LocalObjectViewImpl or the ViewScene provided are null
     */
    public ViewBuilder(LocalObjectView localView, ViewScene scene) throws NullPointerException{
        if (scene != null){
            this.currentView = localView;
            this.scene = scene;
        }
        else
            throw new NullPointerException("A null ViewScene is not supported by this constructor");
    }

    /**
     * Builds the actual view without refreshing . This method doesn't clean up the scene or refreshes it after building it,
     * that's coder's responsibility
     */
    public void buildView() throws IllegalArgumentException{
        try {

            /*Comment this out for debugging purposes
            try{
                FileOutputStream fos = new FileOutputStream("/home/zim/oview_"+currentView.getId()+".xml");
                fos.write(currentView.getStructure());
                fos.close();
            }catch(Exception e){}*/

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
                        long objectId = Long.valueOf(reader.getElementText());

                        LocalObjectLight lol = CommunicationsStub.getInstance().
                                getObjectInfoLight(objectClass, objectId);
                        if (lol != null){
                            ObjectNodeWidget widget = new ObjectNodeWidget(scene, lol);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                            scene.getNodesLayer().addChild(widget);
                            scene.addObject(lol, widget);
                        }
                        else
                            currentView.setDirty(true);
                    }else{
                        if (reader.getName().equals(qEdge)){
                            long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                            long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                            long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                            String className = reader.getAttributeValue(null,"class");
                            LocalObjectLight container = CommunicationsStub.getInstance().getObjectInfoLight(className, objectId);
                            if (container != null){
                                LocalObjectLight aSideObject = LocalStuffFactory.createLocalObjectLight();
                                aSideObject.setOid(aSide);
                                Widget aSideWidget = scene.findWidget(aSideObject);

                                LocalObjectLight bSideObject = LocalStuffFactory.createLocalObjectLight();
                                bSideObject.setOid(bSide);
                                Widget bSideWidget = scene.findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    currentView.setDirty(true);
                                else{
                                    ObjectConnectionWidget newEdge = new ObjectConnectionWidget(scene,
                                           container,scene.getFreeRouter(), ObjectConnectionWidget.getConnectionColor(container.getClassName()));
                                    scene.getEdgesLayer().addChild(newEdge);
                                    scene.addObject(container, newEdge);
                                    newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(aSideWidget, 3));
                                    newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(bSideWidget, 3));
                                    List<Point> localControlPoints = new ArrayList<Point>();
                                    while(true){
                                        reader.nextTag();
                                        
                                        if (reader.getName().equals(qControlPoint)){
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        }else{
                                            newEdge.setControlPoints(localControlPoints,false);
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

        scene.setBackgroundImage(currentView.getBackground());
    }

    /**
     * Builds a simple default view using the object's children and putting them one after another
     * @param myChildren
     */
    public void buildDefaultView(List<LocalObjectLight> myNodes,
            List<LocalObject> myPhysicalConnections) {
        int lastX = 0;

        for (LocalObjectLight node : myNodes){ //Add the nodes
            //Puts an element after another
            ObjectNodeWidget widget = new ObjectNodeWidget(scene, node);
            widget.setPreferredLocation(new Point(lastX, 0));
            scene.getNodesLayer().addChild(widget);
            scene.addObject(node, widget);

            lastX +=100;
        }

        //TODO: This algorithm to find the endpoints for a connection could be improved in many ways
        for (LocalObject container : myPhysicalConnections){

            String aSideString, bSideString;

            //Hardcoded for now
            if (container.getClassName().equals("WireContainer") || container.getClassName().equals("WirelessContainer")){ //NOI18N
                aSideString = "nodeA";
                bSideString = "nodeB";
            }else{
                aSideString = "endpointA";
                bSideString = "endpointB";
            }

            long[] aSide = com.getSpecialAttribute(container.getClassName(), container.getOid(),aSideString);
            if (aSide == null)
                return;

            LocalObjectLight aSideObject = LocalStuffFactory.createLocalObjectLight();
            aSideObject.setOid(aSide[0]);
            Widget aSideWidget = scene.findWidget(aSideObject);

            long[] bSide = com.getSpecialAttribute(container.getClassName(), container.getOid(),bSideString);
            if (bSide == null)
                return;

            LocalObjectLight bSideObject = LocalStuffFactory.createLocalObjectLight();
            bSideObject.setOid(bSide[0]);
            Widget bSideWidget = scene.findWidget(bSideObject);

            ObjectConnectionWidget newEdge = new ObjectConnectionWidget(scene,
                                           container,scene.getFreeRouter(), ObjectConnectionWidget.getConnectionColor(container.getClassName()));
            newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(aSideWidget, 3));
            newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(bSideWidget, 3));
            
        }
        currentView = null;
    }

    /**
     * This method takes the current view and adds/removes the nodes/connections according to a recalculation
     * of the view
     * @param myNodes
     * @param myPhysicalConnections
     */
    public void refreshView(List<LocalObjectLight> newNodes, List<LocalObjectLight> newPhysicalConnections,
            List<LocalObjectLight> nodesToDelete, List<LocalObjectLight> physicalConnectionsToDelete){

        for (LocalObjectLight node : nodesToDelete){
            Widget toDelete = scene.findWidget(node);
            scene.getNodesLayer().removeChild(toDelete);
            scene.removeObject(node);
        }

        for (LocalObjectLight connection : physicalConnectionsToDelete){
            Widget toDelete = scene.findWidget(connection);
            scene.getEdgesLayer().removeChild(toDelete);
            scene.removeObject(connection);
        }

        int lastX = 0;
        for (LocalObjectLight node : newNodes){ //Add the nodes
            //Puts an element after another
            ObjectNodeWidget widget = new ObjectNodeWidget(scene, node);
            widget.setPreferredLocation(new Point(lastX, 20));
            scene.getNodesLayer().addChild(widget);
            scene.addObject(node, widget);

            lastX +=100;
        }

        for (LocalObjectLight toAdd : newPhysicalConnections){
            String aSideString, bSideString;
            //Hardcoded for now
            if (toAdd.getClassName().equals("WireContainer") || toAdd.getClassName().equals("WirelessContainer")){ //NOI18N
                aSideString = "nodeA";
                bSideString = "nodeB";
            }else{
                aSideString = "endpointA";
                bSideString = "endpointB";
            }
            long[] aSide = com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(),aSideString);
            if (aSide == null)
                return;

            LocalObjectLight aSideObject = LocalStuffFactory.createLocalObjectLight();
            aSideObject.setOid(aSide[0]);
            Widget aSideWidget = scene.findWidget(aSideObject);

            long[] bSide = com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(),bSideString);
            if (bSide == null)
                return;

            LocalObjectLight bSideObject = LocalStuffFactory.createLocalObjectLight();
            bSideObject.setOid(bSide[0]);
            Widget bSideWidget = scene.findWidget(bSideObject);

            ObjectConnectionWidget newEdge = new ObjectConnectionWidget(scene,
                                           toAdd,scene.getFreeRouter(), ObjectConnectionWidget.getConnectionColor(toAdd.getClassName()));
            newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(aSideWidget, 3));
            newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(bSideWidget, 3));
        }
    }

    public LocalObjectView getcurrentView(){
        return this.currentView;
    }
}
