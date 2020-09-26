/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.FiberWrapperNode.FiberNode;
import com.vaadin.flow.component.html.Label;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectEdge;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.mxgraph.MxSpliceBox;
import org.neotropic.util.visual.mxgraph.MxTree;
import org.neotropic.util.visual.mxgraph.MxTreeLabel;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * View to Outside Plant Locations (manholes, hand holes, etc.)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspLocationView extends MxGraph {
    private final String ATTR_COLOR = "color"; //NOI18N
    private final String ATTR_VALUE = "value"; //NOI18N
    /**
     * Reference to the Translation Service
     */
    private final TranslationService ts;
    
    public OspLocationView(BusinessObjectLight cable, BusinessObjectLight device,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super();
        Objects.requireNonNull(cable);
        Objects.requireNonNull(device);
        Objects.requireNonNull(ts);
        this.ts = ts;
        setSizeFull();
        setConnectable(true);
        setTooltips(true);
        
        MxTree<BusinessObjectLight> tree = new MxTree<>(
            this, 
            () -> Arrays.asList(cable), 
            node -> {
                try {
                    return bem.getObjectSpecialChildren(node.getClassName(), node.getId());
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                    return Collections.EMPTY_LIST;
                }
            }, 
            BusinessObjectLight::getName,
            (node, graph) -> {
                try {
                    ClassMetadata itemClass = mem.getClass(node.getClassName());
                    if (itemClass.hasAttribute(ATTR_COLOR)) {
                        ClassMetadata colorClass = mem.getClass(itemClass.getType(ATTR_COLOR));
                        if (colorClass.hasAttribute(ATTR_VALUE)) {
                            BusinessObject itemObject = bem.getObject(node.getClassName(), node.getId());
                            String colorId = (String) itemObject.getAttributes().get(ATTR_COLOR);
                            if (colorId != null) {
                                BusinessObject colorObject = aem.getListTypeItem(itemClass.getType(ATTR_COLOR), colorId);
                                String colorValue = (String) colorObject.getAttributes().get(ATTR_VALUE);
                                if (colorValue != null) {
                                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, node.getClassName()))
                                        return new MxTreeLabel(graph, node.getId(), node.getName(), colorValue);
                                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, node.getClassName())) {
                                        FiberWrapperNode fiberNode = new FiberWrapperNode(graph, node, null, null, colorValue);
                                        fiberNode.addFiberRightClickListener(event -> cutFiber(fiberNode));
                                        return fiberNode;
                                    }
                                }
                            }
                        }
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), //NOI18N
                        ex.getLocalizedMessage()
                    ).open();
                }
                return null;
            },
            BusinessObjectLight::getId,
            object -> {
                try {
                    return bem.countSpecialChildren(object.getClassName(), object.getId()) == 0;
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), //NOI18N
                        ex.getLocalizedMessage()
                    ).open();
                }
                return false;
            }
        );
        tree.addCellAddedListener(event -> excecuteLayout());
        
        LinkedHashMap<BusinessObjectLight, BusinessObjectLight> ports = new LinkedHashMap();
        try {
            List<BusinessObjectLight> devicePorts = bem.getChildrenOfClassLight(
                device.getId(), device.getClassName(),
                Constants.CLASS_GENERICPHYSICALPORT, -1);
            Collections.sort(devicePorts);
            if (devicePorts.size() > 1) {
                for (int i = 0; i < devicePorts.size(); i++) {
                    BusinessObjectLight port = devicePorts.get(i);
                    if (port.getName().toLowerCase().startsWith("in")) { //NOI18N
                        List<BusinessObjectLight> mirrors = bem.getSpecialAttribute(port.getClassName(), port.getId(), "mirror"); //NOI18N
                        BusinessObjectLight mirror = null;
                        if (!mirrors.isEmpty())
                            mirror = mirrors.get(0);
                        ports.put(devicePorts.get(i), mirror);
                    }
                }
            }
            else if (devicePorts.size() == 1)
                ports.put(devicePorts.get(0), null);
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ex.getLocalizedMessage()
            ).open();
        }
        ClassMetadata deviceClass = null;
        try {
            deviceClass = mem.getClass(device.getClassName());
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage()
            ).open();
        }
        MxSpliceBox<BusinessObjectLight> spliceBox = new MxSpliceBox<>(
            this, 
            ports,
            device.getName(),
            deviceClass != null ? UtilHtml.toHexString(new Color(deviceClass.getColor())) : null,
            BusinessObjectLight::getName,
            BusinessObjectLight::getId,
            port -> {
                ClassMetadata portClass = null;
                try {
                    portClass = mem.getClass(port.getClassName());
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage()
                    ).open();
                }
                return portClass != null ? UtilHtml.toHexString(new Color(portClass.getColor())) : null;
            }, 
            port -> {
                try {
                    return bem.getSpecialAttribute(port.getClassName(), port.getId(), "endpoitA").isEmpty(); //NOI18N
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage()
                    ).open();
                }
                return false;
            }, 
            port -> new PortNode(port),
            BusinessObjectLight::getName
        );
        spliceBox.addCellAddedListener(event -> excecuteLayout());
        
        addEdgeCompleteListener(event -> {
            MxBusinessObjectNode sourceNode = findNode(event.getSourceId());
            MxBusinessObjectNode targetNode = findNode(event.getTargetId());
            spliceFiber(
                getFiberNode(sourceNode, targetNode), 
                getPortNode(sourceNode, targetNode)
            );
        });
    }
    
    private void cutFiber(FiberWrapperNode fiberNode) {
        new ConfirmDialog(ts,
            ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.title"), 
            new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.text")),
            ts.getTranslatedString("module.general.messages.ok"), 
            () -> {
                fiberNode.cutFiber(fiberNode.getBusinessObject(), fiberNode.getBusinessObject(), true);
            }).open();
    }    
    private void spliceFiber(FiberNode fiberNode, PortNode portNode) {
        if (fiberNode == null && portNode == null)
            return;
        
        new ConfirmDialog(ts,
            ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.title"), 
            new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.text")),
            ts.getTranslatedString("module.general.messages.ok"), 
            () -> {
                LinkedHashMap<String, String> edgeStyle = new LinkedHashMap();
                edgeStyle.put(MxConstants.STYLE_STROKEWIDTH, String.valueOf(FiberWrapperNode.FIBER_HEIGHT - 2));
                edgeStyle.put(MxConstants.STYLE_ENDARROW, MxConstants.NONE);
                edgeStyle.put(MxConstants.STYLE_STARTARROW, MxConstants.NONE);
                edgeStyle.put(MxConstants.STYLE_EDGE, MxConstants.EDGESTYLE_ENTITY_RELATION);
                edgeStyle.put(MxConstants.STYLE_STROKECOLOR, fiberNode.getFillColor());
                
                LinkedHashMap<String, String> nodeStyle = new LinkedHashMap();
                nodeStyle.put(MxConstants.STYLE_FILL_OPACITY, String.valueOf(25));
                nodeStyle.put(MxConstants.STYLE_STROKECOLOR, fiberNode.getFillColor());
                
                fiberNode.setRawStyle(nodeStyle);
                fiberNode.setConnectable(false);
                
                portNode.setRawStyle(nodeStyle);
                portNode.setConnectable(false);
                
                MxBusinessObjectEdge fiberEdge = new MxBusinessObjectEdge(fiberNode.getBusinessObject());
                fiberEdge.setRawStyle(edgeStyle);
                fiberEdge.setSource(fiberNode.getUuid());
                fiberEdge.setTarget(portNode.getUuid());
                
                fiberEdge.addCellAddedListener(fiberEvent -> {
                    setCellsLocked(false);
                    MxBusinessObjectEdge cell = (MxBusinessObjectEdge) fiberEvent.getSource();
                    cell.overrideStyle();
                    cell.setTooltip(cell.getBusinessObject() != null ? cell.getBusinessObject().getName() : null);
                    setCellsLocked(true);
                    fiberEvent.unregisterListener();
                });
                addEdge(fiberEdge);
            }).open();
    }
    // <editor-fold defaultstate="collapsed" desc="Helpers">
    private void excecuteLayout() {
        setCellsLocked(false);
        executeStackLayout(null, true, 350, 50);
        setCellsLocked(true);
    }
    private MxBusinessObjectNode findNode(String nodeUuid) {
        if (nodeUuid != null) {
            for (MxGraphNode node : getNodes()) {
                if (node instanceof MxBusinessObjectNode && nodeUuid.equals(node.getUuid()))
                    return (MxBusinessObjectNode) node;
            }
        }
        return null;
    }
    private MxBusinessObjectEdge findEdge(String edgeUuid) {
        if (edgeUuid != null) {
            for (MxGraphEdge edge : getEdges()) {
                if (edge instanceof MxBusinessObjectEdge && edgeUuid.equals(edge.getUuid()))
                    return (MxBusinessObjectEdge) edge;
            }
        }
        return null;
    }
    private FiberNode getFiberNode(MxBusinessObjectNode sourceNode, MxBusinessObjectNode targetNode) {
        if (sourceNode instanceof FiberNode && targetNode instanceof FiberNode)
            return null;
        else if (sourceNode instanceof FiberNode)
            return (FiberNode) sourceNode;
        else if (targetNode instanceof FiberNode)
            return (FiberNode) targetNode;
        return null;
    }
    private PortNode getPortNode(MxBusinessObjectNode sourceNode, MxBusinessObjectNode targetNode) {
        if (sourceNode instanceof PortNode && targetNode instanceof PortNode)
            return null;
        else if (sourceNode instanceof PortNode)
            return (PortNode) sourceNode;
        else if (targetNode instanceof PortNode)
            return (PortNode) targetNode;
        return null;
    }
    // </editor-fold>
    /**
     * Class to port node in the splice box
     */
    private class PortNode extends MxBusinessObjectNode {
        public PortNode(BusinessObjectLight businessObject) {
            super(businessObject);
        }
    }
}
