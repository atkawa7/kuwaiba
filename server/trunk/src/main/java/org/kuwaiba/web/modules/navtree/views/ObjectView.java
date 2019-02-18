/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.vaadin.lienzo.LienzoComponent;
import com.neotropic.vaadin.lienzo.client.core.shape.Point;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.apis.web.gui.views.AbstractViewEdge;
import org.kuwaiba.apis.web.gui.views.AbstractViewNode;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewEdge;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewNode;
import org.kuwaiba.apis.web.gui.views.ViewMap;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * The embeddable component that displays an object view.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectView extends AbstractView<RemoteObjectLight> {

    public ObjectView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        super(mem, aem, bem);
    }

    @Override
    public String getName() {
        return "Object View";
    }

    @Override
    public String getDescription() {
        return "Display the direct children of the selected object and the physical connections between them whose parent is also the selected object.";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public byte[] getAsXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractComponent getAsComponent() {
        VerticalLayout lytObjectView = new VerticalLayout();
        
        if (viewMap != null) {
            LienzoComponent lienzoComponent = new LienzoComponent();
            
            for (AbstractViewNode aNode : viewMap.getNodes()) {
                SrvNodeWidget nodeWidget = new SrvNodeWidget(((BusinessObjectLight)aNode.getIdentifier()).getId());
                nodeWidget.setCaption(aNode.getIdentifier().toString());
                nodeWidget.setX((int)aNode.getProperties().get("x"));
                nodeWidget.setY((int)aNode.getProperties().get("y"));
//                StreamResource res = ResourceFactory.getFileStream(ResourceFactory.createRectangleIcon(Color.yellow, 16, 16), ((BusinessObjectLight)aNode.getIdentifier()).getClassName() + ".png");
//                lytObjectView.setResource(((BusinessObjectLight)aNode.getIdentifier()).getClassName(), res);
//                ResourceReference resourceReference = ResourceReference.create(res, UI.getCurrent(), ((BusinessObjectLight)aNode.getIdentifier()).getClassName());
                
                nodeWidget.setUrlIcon("/kuwaiba/icons");
                //nodeWidget.setUrlIcon("/icons/City.png");
                
                lienzoComponent.addNodeWidget(nodeWidget);
            }
            
//            viewMap.getNodes().stream().forEach((aNode) -> {
//                SrvNodeWidget nodeWidget = new SrvNodeWidget(((BusinessObjectLight)aNode.getIdentifier()).getId());
//                nodeWidget.setX((double)aNode.getProperties().get("x"));
//                nodeWidget.setY((double)aNode.getProperties().get("y"));
//                StreamResource res = ResourceFactory.getFileStream(ResourceFactory.createRectangleIcon(Color.yellow, 16, 16), "jaja.png");
//                
//                nodeWidget.setUrlIcon(ResourceReference.create(res, UI.getCurrent(), "dl").getURL());
//                
//                lienzoComponent.addNodeWidget(nodeWidget);
//            });
            
            
            //lienzoComponent.addEdgeWidget(srvEdge);
            lienzoComponent.setSizeFull();
            lytObjectView.addComponent(lienzoComponent);
        }
        
        lytObjectView.setSizeFull();
        return lytObjectView;
    }

    @Override
    public void build(RemoteObjectLight businessObject) {
        try {
            
            this.viewMap = new ViewMap();
            
            //First we build the default view
            this.buildDefaultView(businessObject);
 
            //Now, we check if there's a view saved previously..If so, the default location of the nodes will be updated accordingly
            List<ViewObjectLight> objectViews = aem.getObjectRelatedViews(businessObject.getId(), businessObject.getClassName(), -1);

            if (!objectViews.isEmpty()) 
                updateDefaultView(aem.getObjectRelatedView(businessObject.getId(), businessObject.getClassName(), objectViews.get(0).getId())); 
            
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | InvalidArgumentException ex) {
        }
    }
    
    /**
     * Retrieves the direct children of the selected object and places them in a single row.
     * @param businessObject The selected object
     */
    private void buildDefaultView(RemoteObjectLight businessObject) {
        try {
            //First the direct children that not connections
            List<BusinessObjectLight> nodeChildren = bem.getObjectChildren(businessObject.getClassName(), businessObject.getId(), -1);
            
            if (!nodeChildren.isEmpty()) { 
                
                int lastX = 0;

                for (BusinessObjectLight child : nodeChildren) { // Add the nodes
                    BusinessObjectViewNode childNode = new BusinessObjectViewNode(child);
                    childNode.getProperties().put("x", lastX);
                    childNode.getProperties().put("y", 0);
                    viewMap.addNode(childNode);
                    lastX += 200;
                }
                
                List<BusinessObjectLight> connectionChildren = bem.getSpecialChildrenOfClassLight(businessObject.getId(), 
                        businessObject.getClassName(), Constants.CLASS_GENERICPHYSICALLINK, -1);

                for (BusinessObjectLight child : connectionChildren) {
                    BusinessObjectViewEdge connection = new BusinessObjectViewEdge(child);
                    
                    List<BusinessObjectLight> aSide = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointA"); 
                    if (aSide.isEmpty()) {
                        Notifications.showInfo(String.format("Connection %s has a loose endpoint and won't be displayed", child));
                        continue;
                    }

                    List<BusinessObjectLight> bSide = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointB"); //NOI18N
                    if (bSide.isEmpty()) {
                        Notifications.showInfo(String.format("Connection %s has a loose endpoint and won't be displayed", child));
                        continue;
                    }

                    //The endpoints of the connections are ports, but the direct children of the selected object are (most likely) communication devices,
                    //so we need to find the equipment these ports belong to and try to find them among the nodes that were just added above. 
                    List<BusinessObjectLight> parentsASide = bem.getParents(aSide.get(0).getClassName(), aSide.get(0).getId());
                    int currentObjectIndexASide = parentsASide.indexOf(businessObject);
                    if (currentObjectIndexASide == -1) {
                        Notifications.showError(String.format("The endpoint A of connection %s is not located in this object", child));
                        continue;
                    }
                    AbstractViewNode sourceNode = currentObjectIndexASide == 0 ? viewMap.getNode(aSide.get(0)) : viewMap.getNode(parentsASide.get(currentObjectIndexASide - 1));

                    List<BusinessObjectLight> parentsBSide = bem.getParents(bSide.get(0).getClassName(), bSide.get(0).getId());
                    int currentObjectIndexBSide = parentsBSide.indexOf(businessObject);
                    if (currentObjectIndexBSide == -1) {
                        Notifications.showError(String.format("The endpoint B of connection %s is not located in this object", child));
                        continue;
                    }
                    AbstractViewNode targetNode = currentObjectIndexBSide == 0 ? viewMap.getNode(bSide.get(0)) : viewMap.getNode(parentsBSide.get(currentObjectIndexBSide - 1));

                    viewMap.attachSourceNode(connection, sourceNode);
                    viewMap.attachTargetNode(connection, targetNode);

                }
            }
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
    
    /**
     * If there's a saved view, this method (that should be called <b>after</b> {@link #buildDefaultView(org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight)}), 
     * updates the location of the nodes and the control points of the edges.
     * @param theSavedView The saved view (which contains the XML representation of such view).
     */
    private void updateDefaultView(ViewObject theSavedView) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(theSavedView.getStructure());
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                        long objectId = Long.valueOf(reader.getElementText());
                        String className = reader.getAttributeValue(null, "class"); //NOI18N
                        
                        AbstractViewNode node = viewMap.getNode(new BusinessObjectLight(className, objectId, "")); //NOI18N
                        
                        if (node != null) {
                            node.getProperties().put("x", xCoordinate);
                            node.getProperties().put("y", yCoordinate);
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                            String className = reader.getAttributeValue(null, "class"); //NOI18N

                            AbstractViewEdge edge = viewMap.getEdge(new BusinessObjectLight(className, objectId, ""));
                            
                            if (edge != null) {
                                List<Point> controlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        else {
                                            edge.getProperties().put("controlPoints",controlPoints);
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
}
