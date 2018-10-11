/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.web.modules.navtree.views;

import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.vaadin.server.Page;
import com.vaadin.ui.Label;
import java.awt.Color;
import com.neotropic.vaadin.lienzo.client.core.shape.Point;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * The scene in the ObjectView component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectViewScene extends AbstractScene {
    /**
     * Reference to the related inventory object
     */
    private RemoteObjectLight businessObject;
    
    public ObjectViewScene(RemoteObjectLight businessObject, WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
        this.businessObject = businessObject;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                        long objectId = Long.valueOf(reader.getElementText());
                        
                        SrvNodeWidget nodeWidget = findNodeWidget(objectId);
                        
                        if (nodeWidget != null) {
                            nodeWidget.setX(xCoordinate);
                            nodeWidget.setY(yCoordinate);
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                            SrvEdgeWidget edgeWidget = findEdgeWidget(objectId);
                            
                            if (edgeWidget != null) {
                                List<Point> controlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        else {
                                            edgeWidget.setControlPoints(controlPoints);
                                            break;
                                        }
                                    }
                                }
                            }
                            
                        } 
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Notifications.showError("There was an unexpected error parsing the view structure");
        }
    }

    @Override
    public void render() {
        try {
            List<RemoteObjectLight> nodeChildren = wsBean.getObjectChildren(businessObject.getClassName(), businessObject.getId(), -1, 
                    Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            
            if (nodeChildren.isEmpty())
                addComponent(new Label(String.format("%s does not have children to be displayed", businessObject)));
            else { 
                List<RemoteObjectLight> connectionChildren = wsBean.getSpecialChildrenOfClassLight(businessObject.getId(), businessObject.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, -1, 
                    Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            
                int lastX = 0;

                for (RemoteObjectLight child : nodeChildren) { // Add the nodes
                    SrvNodeWidget newNode = attachNodeWidget(child);
                    newNode.setX(lastX);
                    newNode.setY(0);

                    lastX += 200;
                }

                //TODO: This algorithm to find the endpoints for a connection could be improved in many ways
                for (RemoteObjectLight connection : connectionChildren) {            
                    List<RemoteObjectLight> aSide = wsBean.getSpecialAttribute(connection.getClassName(), connection.getId(), "endpointA", 
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()); 

                    if (aSide.isEmpty()) {
                        Notifications.showInfo(String.format("Connection %s has a loose endpoint and won't be displayed", connection));
                        continue;
                    }

                    List<RemoteObjectLight> bSide = wsBean.getSpecialAttribute(connection.getClassName(), connection.getId(), "endpointB", 
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()); //NOI18N

                    if (bSide.isEmpty()) {
                        Notifications.showInfo(String.format("Connection %s has a loose endpoint and won't be displayed", connection));
                        continue;
                    }

                    //The nodes in the view correspond to equipment or infrastructure, not the actual ports
                    //so we have to find the equipment being displayed so we can find them in the scene            
                    List<RemoteObjectLight> parentsASide = wsBean
                        .getParents(aSide.get(0).getClassName(), aSide.get(0).getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                    List<RemoteObjectLight> parentsBSide = wsBean
                        .getParents(bSide.get(0).getClassName(), bSide.get(0).getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());

                    int currentObjectIndexASide = parentsASide.indexOf(businessObject);
                    if (currentObjectIndexASide == -1) {
                        Notifications.showError(String.format("The endpoint A of connection %s is not located in this object", connection));
                        continue;
                    }

                    SrvNodeWidget aSideWidget = currentObjectIndexASide == 0 ? findNodeWidget(aSide.get(0)) : findNodeWidget(parentsASide.get(currentObjectIndexASide - 1));

                    int currentObjectIndexBSide = parentsBSide.indexOf(businessObject);
                    if (currentObjectIndexBSide == -1) {
                        Notifications.showError(String.format("The endpoint B of connection %s is not located in this object", connection));
                        continue;
                    }

                    SrvNodeWidget bSideWidget = currentObjectIndexBSide == 0 ? findNodeWidget(bSide.get(0)) : findNodeWidget(parentsBSide.get(currentObjectIndexBSide - 1));

                    attachEdgeWidget(connection, aSideWidget, bSideWidget);

                }
            }
            
            addComponent(lienzoComponent);
            
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        } 
    }

    protected SrvNodeWidget attachNodeWidget(RemoteObjectLight node) {
        SrvNodeWidget newNode = new SrvNodeWidget(node.getId());
        lienzoComponent.addNodeWidget(newNode);
            
        newNode.setUrlIcon("/icons/" + node.getClassName() + ".png");

        newNode.setCaption(node.toString());
        newNode.setX(nodes.size() * 200);
        newNode.setY((nodes.size() % 2) * 200 );
        nodes.put(node, newNode);
        return newNode;
    }
    
    protected SrvEdgeWidget attachEdgeWidget(RemoteObjectLight edge, SrvNodeWidget sourceNode, SrvNodeWidget targetNode) {
        try {
            SrvEdgeWidget newEdge = new SrvEdgeWidget(edge.getId());
            newEdge.setSource(sourceNode);
            newEdge.setTarget(targetNode);
            lienzoComponent.addEdgeWidget(newEdge);
            RemoteClassMetadata classMetadata = wsBean.getClass(edge.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            newEdge.setColor(toHexString(new Color(classMetadata.getColor())));
            newEdge.setCaption(edge.toString());
            edges.put(edge, newEdge);
            return newEdge; 
        } catch (ServerSideException ex) {
            return new SrvEdgeWidget(323927373);
        }
    }
}
