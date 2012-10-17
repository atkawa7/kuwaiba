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
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalLabel;
import org.inventory.core.services.api.visual.LocalNode;
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
    public ViewBuilder(LocalObjectView localView, ViewScene _scene) throws NullPointerException{
        if (_scene != null){
            this.currentView = localView;
            this.scene = _scene;
        }
        else
            throw new NullPointerException("A null ViewScene is not supported by this constructor");
    }

    /**
     * Builds the actual view without refreshing . This method doesn't clean up the scene or refreshes it after building it,
     * that's coder's responsibility
     */
    public void buildView() throws IllegalArgumentException{
        
        //We clean the object-widget mapping has in order to fill it again. So we do with listeners
        scene.clear();

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
                            ObjectNodeWidget widget = new ObjectNodeWidget(scene, lol);
                            widget.setPreferredLocation(new Point((int)xCoordinate, (int)yCoordinate));
                            scene.getNodesLayer().addChild(widget);
                            scene.addObject(lol, widget);
                                
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
                                    ObjectConnectionWidget newEdge = new ObjectConnectionWidget(scene,
                                           container,scene.getFreeRouter(), ObjectConnectionWidget.getConnectionColor(container.getClassName()));
                                    newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(aSideWidget, 3));
                                    newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(bSideWidget, 3));
                                    while(true){
                                        reader.nextTag();
                                        List<Point> localControlPoints = new ArrayList<Point>();
                                        if (reader.getName().equals(qControlPoint)){
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"y")), Integer.valueOf(reader.getAttributeValue(null,"x"))));
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
        List<LocalNode> myLocalNodes = new ArrayList<LocalNode>();
        List<LocalEdge> myLocalEdges = new ArrayList<LocalEdge>();

        for (LocalObjectLight node : myNodes){ //Add the nodes
            //Puts an element after another
            LocalNode ln = LocalStuffFactory.createLocalNode(node, lastX, 0);
            myLocalNodes.add(ln);
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
            LocalEdge le = LocalStuffFactory.createLocalEdge(container,null);

            for (LocalNode myNode : myLocalNodes){
                
                if (com.getSpecialAttribute(container.getClassName(), container.getOid(),aSideString)[0] == myNode.getObject().getOid()) //NOI18N
                    le.setaSide(myNode);
                else{
                    if (com.getSpecialAttribute(container.getClassName(), container.getOid(),bSideString)[0] == myNode.getObject().getOid()) //NOI18N
                       le.setbSide(myNode);
                }
                if (le.getaSide() != null && le.getbSide() != null)
                    break;
            }
            myLocalEdges.add(le);
        }
        //currentView = LocalStuffFactory.createLocalObjectView();
        buildView();
    }

    /**
     * This method takes the current view and adds/removes the nodes/connections according to a recalculation
     * of the view
     * @param myNodes
     * @param myPhysicalConnections
     */
    public void refreshView(List<LocalObjectLight> newNodes, List<LocalObjectLight> newPhysicalConnections,
            List<LocalObjectLight> nodesToDelete, List<LocalObjectLight> physicalConnectionsToDelete){

   /**     scene.getNodesLayer().removeChildren();
        scene.getEdgesLayer().removeChildren();
        scene.getLabelsLayer().removeChildren();
        scene.getInteractionLayer().removeChildren();
        
        if (nodesToDelete != null){
            for (LocalObjectLight toDelete : nodesToDelete)
                currentView.getNodes().remove(LocalStuffFactory.createLocalNode(toDelete, 0, 0));
        }

        if (physicalConnectionsToDelete != null){
            for (LocalObjectLight toDelete : physicalConnectionsToDelete)
                currentView.getEdges().remove(LocalStuffFactory.createLocalEdge(toDelete));
        }

        int i = 0;
        if (newNodes != null){
            for (LocalObjectLight toAdd : newNodes){
                currentView.getNodes().add(LocalStuffFactory.createLocalNode(toAdd, i, 0));
                i+=100;
            }
        }

        if (newPhysicalConnections != null)
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
                LocalNode nodeA = getNodeMatching(currentView.getNodes(), com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(),aSideString)[0]);
                if (nodeA == null)
                    continue;
                LocalNode nodeB = getNodeMatching(currentView.getNodes(), com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(),bSideString)[0]);
                if (nodeB == null)
                    continue;
                currentView.getEdges().add(LocalStuffFactory.createLocalEdge(toAdd, nodeA, nodeB, null));
            }

        buildView();*/
    }

    public LocalObjectView getcurrentView(){
        return this.currentView;
    }

    /**
     * Helper to get a local node matching an inner
     * @param list
     * @param id
     * @return the node matching the oid or null if the object is not present
     */
    private LocalNode getNodeMatching(List<LocalNode> list, Long id){
        for (LocalNode node : list){
            if (node.getObject().getOid() == id)
                return node;
        }
        return null;
    }
}
