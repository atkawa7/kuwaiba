/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.visualization.views;

import com.neotropic.vaadin14.component.MxConstants;
import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractDetailedView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;

/**
 * View for graphic visualization of splice box equipment
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class SpliceBoxView extends AbstractDetailedView<BusinessObjectLight, VerticalLayout> {

    MxGraph mxGraph;
    private BusinessEntityManager bem;
    private ApplicationEntityManager aem;
    private MetadataEntityManager mem;
    
    public SpliceBoxView(BusinessObjectLight businessObject) {
        super(businessObject);
    }
    
    public SpliceBoxView(BusinessObjectLight businessObject, BusinessEntityManager bem, ApplicationEntityManager aem, MetadataEntityManager mem) {
        this(businessObject);
        this.bem = bem;  
        this.aem = aem;
        this.mem = mem;
    }

    @Override
    public String appliesTo() {
        return "SpliceBox";
    }

    @Override
    public String getName() {
        return "splice-box-view";
    }

    @Override
    public String getDescription() {
        return "View for graphic visualization of splice box equipment";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
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
    public VerticalLayout getAsComponent() throws InvalidArgumentException {

        if (businessObject != null) {
            int widthPort = 60, heightPort = 50, startY = 30, widthExternalPort= 30, heightExternalPort=30;
            VerticalLayout lytGraph = new VerticalLayout();
            mxGraph = new MxGraph();
            mxGraph.setWidth("600px");
            mxGraph.setHeight("100%");
            mxGraph.setGrid("img/grid.gif");
            lytGraph.add(mxGraph);
            MxGraphNode mainBox = new MxGraphNode();
            mainBox.setUuid("main");
            mainBox.setLabel(businessObject.getName());
            mainBox.setGeometry(250, startY, 100, 100);
            mxGraph.addNode(mainBox);
            try {
                LinkedHashMap<BusinessObjectLight, BusinessObjectLight> mapPorts = new LinkedHashMap<>();
                List<BusinessObjectLight> lstOpticalPorts = bem.getChildrenOfClassLight(businessObject.getId(), businessObject.getClassName(), "OpticalPort", -1);
                lstOpticalPorts = lstOpticalPorts.stream().sorted((object1, object2) -> object1.getName().compareTo(object2.getName())).collect(Collectors.toList());
                for (BusinessObjectLight port : lstOpticalPorts) {
                    if (port.getName().toLowerCase().startsWith("in")) {
                        List<BusinessObjectLight> lstMirrors = bem.getSpecialAttribute(port.getClassName(), port.getId(), "mirror");
                        if (lstMirrors != null && lstMirrors.size() > 0) {
                            mapPorts.put(port, lstMirrors.get(0));
                        } else {
                            mapPorts.put(port, null);
                        }
                    }
                }
                if (mapPorts.isEmpty()) {
                    return new VerticalLayout(new Label("The SpliceBox has no input ports"));
                }
                int i = 1;
                MxGraphNode groupPort;
                MxGraphNode nodeIn;
                MxGraphNode nodeOut;
                MxGraphNode startIn;
                MxGraphNode endOut;
                MxGraphEdge edgeIn;
                MxGraphEdge edgeOut;
                for (BusinessObjectLight inPort : mapPorts.keySet()) {

                    groupPort = new MxGraphNode();
                    groupPort.setUuid("gp" + i);
                    groupPort.setLabel("");
                    groupPort.setGeometry(0, heightPort * (i - 1), widthPort * 2, heightPort);
                    groupPort.setCellParent("main");
                    mxGraph.addNode(groupPort);

                    nodeIn = new MxGraphNode();
                    nodeIn.setUuid("in" + i);
                    nodeIn.setLabel(inPort.getName());
                    nodeIn.setGeometry(0, 0, widthPort, heightPort);
                    nodeIn.setCellParent("gp" + i);
                    nodeIn.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                    mxGraph.addNode(nodeIn);
                    List<BusinessObjectLight> inLinks = bem.getSpecialAttribute(inPort.getClassName(), inPort.getId(), "endpointA");
                    if (inLinks == null || inLinks.isEmpty()) 
                        inLinks = bem.getSpecialAttribute(inPort.getClassName(), inPort.getId(), "endpointB");
                    
                    if (inLinks != null && inLinks.size() > 0) {
                         BusinessObject theWholeLink = bem.getObject(inLinks.get(0).getClassName(), inLinks.get(0).getId());
                            String hexColor;
                            if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR) && theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) != null)
                               hexColor =  (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR);//String.format("#%06x", (0xFFFFFF & (int) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR)));
                            else
                                hexColor = "steelblue";  //default color
                        nodeIn.setFillColor(hexColor);
                        startIn = new MxGraphNode();
                        startIn.setUuid("s" + i);
                        startIn.setLabel("");
                        startIn.setGeometry(10, startY + ((heightPort) * (i - 1)) + ((heightPort - heightExternalPort)/2), widthExternalPort, heightExternalPort);
                        startIn.setFillColor("white");
                        startIn.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
//                        startIn.setShape(MxConstants.SHAPE_ELLIPSE);
                        edgeIn = new MxGraphEdge();
                        edgeIn.setSource("s" + i);
                        edgeIn.setTarget("in" + i);
                        edgeIn.setStrokeWidth(1);
                        edgeIn.setLabel(inLinks.get(0).getName());
                        mxGraph.addNode(startIn);
                        mxGraph.addEdge(edgeIn);
                    } else 
                        nodeIn.setFillColor("gray");
                    

                    BusinessObjectLight outPort = mapPorts.get(inPort);
                    nodeOut = new MxGraphNode();
                    nodeOut.setUuid("out" + i);
                    nodeOut.setGeometry(widthPort, 0, widthPort, heightPort);
                    nodeOut.setCellParent("gp" + i);
                    nodeOut.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
                    mxGraph.addNode(nodeOut);
                    if (outPort != null) {
                        nodeOut.setLabel(outPort.getName());
                        List<BusinessObjectLight> outLinks = bem.getSpecialAttribute(outPort.getClassName(), outPort.getId(), "endpointA");
                        if (outLinks == null || outLinks.isEmpty()) {
                            outLinks = bem.getSpecialAttribute(outPort.getClassName(), outPort.getId(), "endpointB");
                        }
                        if (outLinks != null && outLinks.size() > 0) {
                            BusinessObject theWholeLink = bem.getObject(outLinks.get(0).getClassName(), outLinks.get(0).getId());
                            String hexColor;
                            if (theWholeLink.getAttributes().containsKey(Constants.PROPERTY_COLOR) && theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR) != null)
                                hexColor =  (String) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR);//String.format("#%06x", (0xFFFFFF & (int) theWholeLink.getAttributes().get(Constants.PROPERTY_COLOR)));
                            else
                                hexColor = "steelblue";  //default color
                            nodeOut.setFillColor(hexColor);
                            endOut = new MxGraphNode();
                            endOut.setUuid("e" + i);
                            endOut.setLabel("");
                            endOut.setGeometry(530, startY + (heightPort) * (i - 1) + ((heightPort - heightExternalPort)/2), widthExternalPort, heightExternalPort   );
                            endOut.setFillColor("white");
                            endOut.setVerticalLabelPosition(MxConstants.ALIGN_MIDDLE);
//                            endOut.setShape(MxConstants.SHAPE_ELLIPSE);
                            edgeOut = new MxGraphEdge();
                            edgeOut.setSource("out" + i);
                            edgeOut.setTarget("e" + i);
                            edgeOut.setStrokeWidth(1);
                            edgeOut.setLabel(outLinks.get(0).getName());
                            mxGraph.addNode(endOut);
                            mxGraph.addEdge(edgeOut);
                        } else 
                            nodeOut.setFillColor("gray"); //doenst have a end point
                        
                    } else  // doesnt have a mirror port                   
                        nodeOut.setFillColor("black");
                    

                    i++;
                }

            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                Logger.getLogger(SpliceBoxView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return lytGraph;
        }
        return new VerticalLayout(new Label("The view has no business object associated"));
    }

    @Override
    public void buildWithSavedView(byte[] view) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildWithBusinessObject(Object businessObject) {
        
    }

    @Override
    public void buildEmptyView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractViewNode addNode(Object businessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractViewEdge addEdge(Object businessObject, Object sourceBusinessObject, Object targetBusinessObject, Properties properties) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNode(Object businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEdge(Object businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
