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
package org.kuwaiba.web.modules.servmanager.views;


import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.neotropic.vaadin14.component.Point;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.apis.web.gui.views.util.UtilHtml;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.openide.util.Exceptions;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Adrian Fernando Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class EndToEndViewScene extends AbstractScene {
    
    public final static String VIEW_CLASS = "EndToEndView"; 
    /**
     * The service this view is associated to
     */
    private RemoteObjectLight service;
    
    public EndToEndViewScene(RemoteObjectLight service, WebserviceBean wsBean, RemoteSession session) {
        super (wsBean, session);
        this.service = service;
//        lienzoComponent.addNodeWidgetClickListener(nodeWidgetClickListener);
        setSizeUndefined();
    }
    
//    NodeWidgetClickListener nodeWidgetClickListener = new NodeWidgetClickListener() {
//
//        @Override
//        public void nodeWidgetClicked(String id) {
//            RemoteObjectLight nodeObject = (RemoteObjectLight) lienzoComponent.getNodeObject(id);
//            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(nodeObject);
//            Window wdwTableInfo = new Window(" ");
//            wdwTableInfo.setId("report-forms-container");
//            wdwTableInfo.addStyleName("report-forms");
//            try {
//                Component x = null;
//                for (RemoteObjectLight device : nodes.keySet()) {
//                    if (id.equals(String.valueOf(device.getId()))){
//                        TableCreator formView = new TableCreator(service, wsBean);
//                        
//                        List<SrvEdgeWidget> connectedEdgeWidgets = lienzoComponent.getNodeEdgeWidgets(srvNode);
//                        for(SrvEdgeWidget edge : connectedEdgeWidgets){
//                            
//                            RemoteObjectLight foundEdge = findEdge(edge.getId());
//                            RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(foundEdge.getClassName(), 
//                                    foundEdge.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
//                            List<RemoteObjectLightList> relatedObjects = specialAttributes.getRelatedObjects();
//                            List<String> relationships = specialAttributes.getRelationships();
//                            for(int i = 0; i < relationships.size(); i++){
//                                String relationShipName = relationships.get(i);
//                                if(relationShipName.toLowerCase().contains("endpoint")){
//                                    RemoteObjectLightList get = relatedObjects.get(i);
//                                    RemoteObjectLight port = get.getList().get(0);
//                                    if(port.getClassName().toLowerCase().contains("port") || port.getClassName().equals("Pseudowire")){
//                                        List<RemoteObjectLight> parentsUntilFirstOfClass = 
//                                                wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), 
//                                                        device.getClassName(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
//                                        if(parentsUntilFirstOfClass.contains(device)){
//                                            if(device.getClassName().toLowerCase().contains("router"))
//                                                x = formView.createRouter(device, port);
//                                            else if(device.getClassName().equals("ODF"))
//                                                x = formView.createODF(device, port);
//                                            else if(device.getClassName().toLowerCase().contains("external"))
//                                                x = formView.createExternalEquipment(device);
//                                            else if(device.getClassName().equals("Cloud"))
//                                                x = formView.createPeering(device);
//                                            wdwTableInfo.setCaption(device.toString());
//                                            wdwTableInfo.center();
//                                            HorizontalLayout lytContent = new HorizontalLayout();
//                                            lytContent.setSpacing(true);
//                                            lytContent.setId("report-forms-content");
//                                            lytContent.addComponent(x);
//                                            wdwTableInfo.setContent(lytContent);
//                                            //We close if there are any open window
//                                            closeWindows();
//                                            getUI().addWindow(wdwTableInfo);
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        break;
//                    }
//                }
//            } catch (ServerSideException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//            lienzoComponent.updateNodeWidget(nodeObject);
//        }
//    };
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException { 
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

//<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/end2end_web_in_render.xml");
            fos.write(structure);
            fos.close();
        } catch(Exception e) {}
//</editor-fold>
            
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String nodeClass = reader.getAttributeValue(null, "class");
                        String nodeId = reader.getElementText();
                        RemoteObjectLight rol = wsBean.getObject(nodeClass, nodeId, session.getIpAddress(), session.getSessionId());
                        
                        MxGraphNode aSavedNode = findNodeWidget(rol);
                        if(aSavedNode == null){
                            attachNodeWidget(rol, xCoordinate, yCoordinate);                        
                        } 
                                    
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            String edgeId = reader.getAttributeValue(null, "id"); //NOI18N
                            String edgeClass = reader.getAttributeValue(null, "class"); //NOI18N
                            String aSideid = reader.getAttributeValue(null, "asideid"); //NOI18N
                            String aSideClassName = reader.getAttributeValue(null, "asideclass"); //NOI18N
                            String bSideid = reader.getAttributeValue(null, "bsideid"); //NOI18N
                            String bSideClassName = reader.getAttributeValue(null, "bsideclass"); //NOI18N
                            
                            RemoteObjectLight sideA = new RemoteObjectLight(aSideClassName, aSideid, "");
                            RemoteObjectLight sideB = new RemoteObjectLight(bSideClassName, bSideid, "");
                            
                            RemoteObjectLight edge;
                            if(edgeId.startsWith("@"))
                                edge = new RemoteObjectLight(edgeId, "", edgeClass);
                            else
                                edge = wsBean.getObject(edgeClass, edgeId, session.getIpAddress(), session.getSessionId());
                            
                            MxGraphNode sideANodeWidget = findNodeWidget(sideA);
                            MxGraphNode sideBNodeWidget = findNodeWidget(sideB);
                            
                            MxGraphEdge aSavedEdge = findEdgeWidget(edge);
                            
                            //controlpoints
                            List<Point> localControlPoints = new ArrayList<>();
                            while(true) {
                                reader.nextTag();
                                if (reader.getName().equals(qControlPoint)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                        localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                } else 
                                    break;
                            }
                            
                            if(sideANodeWidget != null && sideBNodeWidget != null && aSavedEdge == null){
                                attachEdgeWidget(edge, sideANodeWidget, sideBNodeWidget, localControlPoints);
                            } else if (aSavedEdge != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
                                aSavedEdge.setPoints(localControlPoints);
                            }
                        }
                    }
                }
            }//end while
                     
            add(mxGraph);
            
        } catch (XMLStreamException ex) {
//            Notifications.showError(ex.getMessage());
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void render() {//no longer need it
    }
    
    public void clear() {
        //this.lienzoComponent.remove
    }
    
     protected MxGraphNode attachNodeWidget(RemoteObjectLight node, int xCoordinate, int yCoordinate) {
        MxGraphNode newNode = new MxGraphNode();
        String uri = StreamResourceRegistry.getURI(ResourceFactory.getInstance().getClassIcon(node.getClassName(), wsBean)).toString();
        System.out.println("uri: " + uri);
        newNode.setImage(uri);
        newNode.setUuid(node.getId());
        newNode.setLabel(node.toString());
        newNode.setWidth(ResourceFactory.DEFAULT_ICON_WIDTH);
        newNode.setHeight(ResourceFactory.DEFAULT_ICON_HEIGHT);
        newNode.setX(100 + (xCoordinate)); //The position is scaled (in this case to a half the original size) so they diagram can fit in a single screen 
        newNode.setY(50+(yCoordinate)); 
        nodes.put(node, newNode);
        mxGraph.addNode(newNode);
        return newNode;
    }
    
    protected MxGraphEdge attachEdgeWidget(RemoteObjectLight edgeObject, MxGraphNode sourceNode, MxGraphNode targetNode,List<Point> points) {
       
        MxGraphEdge newEdge = new MxGraphEdge();
        
        try {          
            newEdge.setSource(sourceNode.getUuid());
            newEdge.setTarget(targetNode.getUuid());
            
            RemoteClassMetadata classMetadata = wsBean.getClass(edgeObject.getClassName(), session.getIpAddress(), session.getSessionId());
            newEdge.setStrokeColor(UtilHtml.toHexString(new Color(classMetadata.getColor())));
            newEdge.setLabel(edgeObject.toString());
            newEdge.setPoints(points);
//            newEdge.setPoints("[{\"x\":200.0,\"y\":200.7},{\"x\":250.9,\"y\":300.0}]");
            edges.put(edgeObject, newEdge);
            mxGraph.addEdge(newEdge);
            return newEdge; 
        } catch (Exception ex) {
            newEdge.setId(UUID.randomUUID().toString());
            return newEdge;
        }
    }
    
    private void closeWindows(){
//        getUI().getWindows().forEach(currentOpenWindow -> {
//            getUI().removeWindow(currentOpenWindow);
//        });
    }
}
