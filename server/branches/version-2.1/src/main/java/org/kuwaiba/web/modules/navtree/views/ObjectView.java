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
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.VerticalLayout;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.apis.web.gui.views.AbstractViewEdge;
import org.kuwaiba.apis.web.gui.views.AbstractViewNode;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewEdge;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewNode;
import org.kuwaiba.apis.web.gui.views.ViewEventListener;
import org.kuwaiba.apis.web.gui.views.ViewMap;
import org.kuwaiba.apis.web.gui.views.util.UtilHtml;
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
    public byte[] getAsXml() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractComponent getAsComponent() throws InvalidArgumentException {
        VerticalLayout lytObjectView = new VerticalLayout();
        
        if (viewMap != null) {
            LienzoComponent<BusinessObjectLight, BusinessObjectLight> lienzoComponent = new LienzoComponent();
            
            for (AbstractViewNode aNode : viewMap.getNodes()) {
                SrvNodeWidget nodeWidget = new SrvNodeWidget();
                nodeWidget.setWidth(32); //Use 32x32 px icons
                nodeWidget.setCaption(aNode.getIdentifier().toString());
                nodeWidget.setX((int)aNode.getProperties().get("x") / 2);
                nodeWidget.setY((int)aNode.getProperties().get("y") / 2);
                
                nodeWidget.setUrlIcon("/kuwaiba/icons?class=" + ((BusinessObjectLight)aNode.getIdentifier()).getClassName()); //NOI18N
                lienzoComponent.addNodeWidget((BusinessObjectLight) aNode.getIdentifier(), nodeWidget);
            }
            
            viewMap.getEdges().stream().forEach((anEdge) -> {
                SrvEdgeWidget edgeWidget = new SrvEdgeWidget();
                AbstractViewNode sourceNode = viewMap.getEdgeSource(anEdge);
                if (sourceNode != null) {
                    edgeWidget.setSource(lienzoComponent.getNodeWidget((BusinessObjectLight)sourceNode.getIdentifier()));
                    AbstractViewNode targetNode = viewMap.getEdgeTarget(anEdge);
                    if (targetNode != null) { //The edge is only added if both sides are found
                        edgeWidget.setTarget(lienzoComponent.getNodeWidget((BusinessObjectLight)targetNode.getIdentifier()));
                        edgeWidget.setControlPoints((List<Point>)anEdge.getProperties().get("controlPoints")); 
                        try {
                            ClassMetadata theClass = mem.getClass(((BusinessObjectLight)anEdge.getIdentifier()).getClassName());
                            edgeWidget.setColor(UtilHtml.toHexString(new Color(theClass.getColor())));
                        } catch (MetadataObjectNotFoundException ex) {
                            //In case of error, use a default black line
                        }
                        lienzoComponent.addEdgeWidget((BusinessObjectLight) anEdge.getIdentifier(), edgeWidget);
                    }
                }
            });
            
            lytObjectView.addComponentsAndExpand(lienzoComponent);
        }
        
        lytObjectView.setSizeFull();
        return lytObjectView;
    }

    @Override
    public void buildWithBusinessObject(RemoteObjectLight businessObject) {
        try {
            this.viewMap = new ViewMap();            
            //First we build the default view
            this.buildDefaultView(businessObject);
            //Now, we check if there's a view saved previously..If so, the default location of the nodes will be updated accordingly
            List<ViewObjectLight> objectViews = aem.getObjectRelatedViews(businessObject.getId(), businessObject.getClassName(), -1);

            if (!objectViews.isEmpty()) 
                this.buildWithSavedView(aem.getObjectRelatedView(businessObject.getId(), 
                        businessObject.getClassName(), objectViews.get(0).getId()).getStructure()); 
            
        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            // Should not happen
        }
    }
    
    /**
     * Retrieves the direct children of the selected object and places them in a single row.
     * @param businessObject The selected object
     */
    private void buildDefaultView(RemoteObjectLight businessObject) {
        try {
            BusinessObjectLight asLocalBusinessObject = new BusinessObjectLight(businessObject.getClassName(), businessObject.getId(), businessObject.getName());
            
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
                    viewMap.addEdge(connection);
                    
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
                    int currentObjectIndexASide = parentsASide.indexOf(asLocalBusinessObject);
                    if (currentObjectIndexASide == -1) {
                        Notifications.showError(String.format("The endpoint A of connection %s is not located in this object", child));
                        continue;
                    }
                    AbstractViewNode sourceNode = currentObjectIndexASide == 0 ? viewMap.getNode(aSide.get(0)) : viewMap.getNode(parentsASide.get(currentObjectIndexASide - 1));

                    List<BusinessObjectLight> parentsBSide = bem.getParents(bSide.get(0).getClassName(), bSide.get(0).getId());
                    int currentObjectIndexBSide = parentsBSide.indexOf(asLocalBusinessObject);
                    if (currentObjectIndexBSide == -1) {
                        Notifications.showError(String.format("The endpoint B of connection %s is not located in this object", child));
                        continue;
                    }
                    AbstractViewNode targetNode = currentObjectIndexBSide == 0 ? viewMap.getNode(bSide.get(0)) : viewMap.getNode(parentsBSide.get(currentObjectIndexBSide - 1));

                    viewMap.attachSourceNode(connection, sourceNode);
                    viewMap.attachTargetNode(connection, targetNode);
                }
            }
        } catch (MetadataObjectNotFoundException | InvalidArgumentException | BusinessObjectNotFoundException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
    
    /**
     * If there's a saved view, this method (that should be called <b>after</b> {@link #buildDefaultView(org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight)}), 
     * updates the location of the nodes and the control points of the edges.
     * @param theSavedView The saved view (which contains the XML representation of such view).
     */
    @Override
    public void buildWithSavedView(byte[] theSavedView) {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(theSavedView);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                        String className = reader.getAttributeValue(null, "class"); //NOI18N
                        String objectId = reader.getElementText();
                        
                        AbstractViewNode node = viewMap.getNode(new BusinessObjectLight(className, objectId, "")); //NOI18N
                        
                        if (node != null) {
                            node.getProperties().put("x", xCoordinate);
                            node.getProperties().put("y", yCoordinate);
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String className = reader.getAttributeValue(null, "class"); //NOI18N
                            AbstractViewEdge edge = viewMap.getEdge(new BusinessObjectLight(className, objectId, "")); //NOI18N
                            
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

    @Override
    public void buildEmptyView() {
        // This method is never called, because this kind of view is always created from an object, calling at least once the method buildWithBusinessObject.
    }

    @Override
    public AbstractViewNode addNode(RemoteObjectLight businessObject, Properties properties) {
        throw new UnsupportedOperationException("This view is generated automatically and does not support nodes to be added manually"); 
    }

    @Override
    public AbstractViewEdge addEdge(RemoteObjectLight businessObject, RemoteObjectLight sourceBusinessObject, RemoteObjectLight targetBusinessObject, Properties properties) {
        throw new UnsupportedOperationException("This action is not supported yet.");
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
