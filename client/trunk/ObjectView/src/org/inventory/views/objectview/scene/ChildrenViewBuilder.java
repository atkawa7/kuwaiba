/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import java.awt.event.ActionEvent;
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
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.views.objectview.ObjectViewService;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * This class builds every view so it can be rendered by the scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ChildrenViewBuilder implements AbstractViewBuilder {
    /**
     * Number of pixels between elements in a default view
     */
    public static final int X_OFFSET = 100;
    /**
     * Wraps the view to be built
     */
    private LocalObjectView currentView;
    /**
     * Reference to the scene
     */
    private ChildrenViewScene scene;
    /**
     * Reference to the singleton of CommunicationStub
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * Reference to the TC service
     */
    private ObjectViewService service;
    
    /**
     * This constructor should be used if there's already a view
     * @param localView
     * @throws NullPointerException if the LocalObjectViewImpl or the ViewScene provided are null
     */
    public ChildrenViewBuilder(ObjectViewService service) {
        this.scene = new ChildrenViewScene();
        this.service = service;
    }

    /**
     * Builds the actual view without refreshing . This method doesn't clean up the scene or refreshes it after building it,
     * that's coder's responsibility
     */
    @Override
    public void buildView(LocalObjectLight object) throws IllegalArgumentException {
        try {
            
            List<LocalObjectViewLight> views = com.getObjectRelatedViews(object.getOid(), object.getClassName());
            List<LocalObjectLight> myChildren = com.getObjectChildren(object.getOid(), com.getMetaForClass(object.getClassName(),false).getOid());
            List<LocalObject> myConnections = com.getChildrenOfClass(object.getOid(),object.getClassName(), Constants.CLASS_GENERICCONNECTION);

            if(views.isEmpty()){ //There are no saved views
                
                buildDefaultView(myChildren, myConnections);
                currentView = null;
            }else{
                currentView = com.getObjectRelatedView(object.getOid(),object.getClassName(), views.get(0).getId());
                
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

                            LocalObjectLight lol = com.getObjectInfoLight(objectClass, objectId);
                            if (lol != null){
                                Widget widget = scene.addNode(lol);
                                widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                widget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                                myChildren.remove(lol);
                            }
                            else
                                currentView.setDirty(true);
                        }else{
                            if (reader.getName().equals(qEdge)){
                                long objectId = Long.valueOf(reader.getAttributeValue(null,"id"));
                                long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                                long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                                String className = reader.getAttributeValue(null,"class");
                                LocalObjectLight container = com.getObjectInfoLight(className, objectId);
                                if (container != null){
                                    myConnections.remove(container);
                                    LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                    Widget aSideWidget = scene.findWidget(aSideObject);

                                    LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                    Widget bSideWidget = scene.findWidget(bSideObject);

                                    if (aSideWidget == null || bSideWidget == null)
                                        currentView.setDirty(true);
                                    else{
                                        ConnectionWidget newEdge = (ConnectionWidget)scene.addEdge(container);
                                        newEdge.setLineColor(Utils.getConnectionColor(container.getClassName()));
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
                
                //We check here if there are new elements but those in the save view
                if (!myChildren.isEmpty() || !myConnections.isEmpty())
                    currentView.setDirty(true);
                
                buildDefaultView(myChildren, myConnections);
                
                scene.setBackgroundImage(currentView.getBackground());
                if (currentView.isDirty()){
                    service.getComponent().getNotifier().showSimplePopup("Information", NotificationUtil.WARNING_MESSAGE, "Some elements in the view has been deleted since the last time it was opened. They were removed");
                    scene.fireChangeEvent(new ActionEvent(this, ChildrenViewScene.SCENE_CHANGEANDSAVE, "Removing old objects"));
                    currentView.setDirty(false);
                }
            }
        } catch (XMLStreamException ex) {
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO)
                Exceptions.printStackTrace(ex);
        }

        scene.validate();
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
            Widget widget = scene.addNode(node);
            widget.setPreferredLocation(new Point(lastX, 0));
            widget.setBackground(com.getMetaForClass(node.getClassName(), false).getColor());

            lastX += X_OFFSET;
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

            ConnectionWidget newEdge = (ConnectionWidget)scene.addEdge(container);
            newEdge.setLineColor(Utils.getConnectionColor(container.getClassName()));
            newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget));
            newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget));
        }
    }

    public LocalObjectView getCurrentView(){
        return this.currentView;
    }

    @Override
    public String getName() {
        return "Default View";
    }

    @Override
    public AbstractScene getScene() {
        return scene;
    }

    //Supports all classes
    @Override
    public boolean supportsClass(String className) {
        return true;
    }

    @Override
    public void refresh() {
        scene.clear();
        buildView(service.getCurrentObject());
    }

    @Override
    public void saveView() {
        byte[] viewStructure = scene.getAsXML();
        if (currentView == null){
            long viewId = com.createObjectRelatedView(service.getCurrentObject().getOid(),
                    service.getCurrentObject().getClassName(), null, null,0, viewStructure, scene.getBackgroundImage());
            if (viewId != -1) //Success
                currentView = new LocalObjectView(viewId, null, null, 0, viewStructure, scene.getBackgroundImage());
            else{
                service.getComponent().getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }else{
            if (!com.updateObjectRelatedView(service.getCurrentObject().getOid(),
                     service.getCurrentObject().getClassName(), currentView.getId(),
                    null, null,viewStructure, scene.getBackgroundImage()))
                service.getComponent().getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            else
                service.getComponent().setHtmlDisplayName(service.getComponent().getDisplayName());
        }
    }
}