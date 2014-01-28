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
import java.util.Collection;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
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
                            ObjectNodeWidget widget = (ObjectNodeWidget)scene.addNode(lol);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
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
                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                Widget aSideWidget = scene.findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                Widget bSideWidget = scene.findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    currentView.setDirty(true);
                                else{
                                    ObjectConnectionWidget newEdge = (ObjectConnectionWidget)scene.addEdge(container);
                                    newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget));
                                    newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget));
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
            ObjectNodeWidget widget = (ObjectNodeWidget)scene.addNode(node);
            widget.setPreferredLocation(new Point(lastX, 0));

            lastX +=100;
        }

        //TODO: This algorithm to find the endpoints for a connection could be improved in many ways
        for (LocalObject container : myPhysicalConnections){

            LocalObjectLight[] aSide = com.getSpecialAttribute(container.getClassName(), container.getOid(),"endpointA");
            if (aSide == null)
                return;

            Widget aSideWidget = scene.findWidget(aSide[0]);

            LocalObjectLight[] bSide = com.getSpecialAttribute(container.getClassName(), container.getOid(),"endpointB");
            if (bSide == null)
                return;

            Widget bSideWidget = scene.findWidget(bSide[0]);

            ObjectConnectionWidget newEdge = (ObjectConnectionWidget)scene.addEdge(container);
            newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget));
            newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget));
        }
        currentView = null;
    }

    /**
     * This method takes the current view and adds/removes the nodes/connections according to a recalculation
     * of the view
     * @param myNodes
     * @param myPhysicalConnections
     */
    public void refreshView(Collection<LocalObjectLight> newNodes, Collection<LocalObjectLight> newPhysicalConnections,
            Collection<LocalObjectLight> nodesToDelete, Collection<LocalObjectLight> physicalConnectionsToDelete){

        for (LocalObjectLight node : nodesToDelete){
            Widget toDelete = scene.findWidget(node);
            scene.getNodesLayer().removeChild(toDelete);
            scene.removeNode(node);
        }

        for (LocalObjectLight connection : physicalConnectionsToDelete){
            Widget toDelete = scene.findWidget(connection);
            scene.getEdgesLayer().removeChild(toDelete);
            scene.removeEdge(connection);
        }

        int lastX = 0;
        for (LocalObjectLight node : newNodes){ //Add the nodes
            //Puts an element after another
            ObjectNodeWidget widget = (ObjectNodeWidget)scene.addNode(node);
            widget.setPreferredLocation(new Point(lastX, 20));
            lastX +=100;
        }

        for (LocalObjectLight toAdd : newPhysicalConnections){
            
            LocalObjectLight[] aSide = com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(), "endpointA");
            if (aSide == null)
                return;

            Widget aSideWidget = scene.findWidget(aSide[0]);

            LocalObjectLight[] bSide = com.getSpecialAttribute(toAdd.getClassName(), toAdd.getOid(),"endpointB");
            if (bSide == null)
                return;

            Widget bSideWidget = scene.findWidget(bSide[0]);

            ObjectConnectionWidget newEdge = (ObjectConnectionWidget)scene.addEdge(toAdd);
            newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget));
            newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget));
        }
    }

    public LocalObjectView getcurrentView(){
        return this.currentView;
    }
}
