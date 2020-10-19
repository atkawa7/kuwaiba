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
import com.neotropic.kuwaiba.modules.commercial.ospman.OutsidePlantService;
import com.vaadin.flow.component.html.Label;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.optional.physcon.PhysicalConnectionsService;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectEdge;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.mxgraph.MxSpliceBox;
import org.neotropic.util.visual.mxgraph.MxTree;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * View to Outside Plant Locations (manholes, hand holes, etc.)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspLocationView extends MxGraph {
    /**
     * Reference to the Translation Service
     */
    private final TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private final BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private final MetadataEntityManager mem;
    
    private final LinkedHashMap<String, String> EDGE_STYLE = new LinkedHashMap();
    {
        EDGE_STYLE.put(MxConstants.STYLE_STROKEWIDTH, String.valueOf(FiberWrapperNode.FIBER_HEIGHT - 2));
        EDGE_STYLE.put(MxConstants.STYLE_ENDARROW, MxConstants.NONE);
        EDGE_STYLE.put(MxConstants.STYLE_STARTARROW, MxConstants.NONE);
        EDGE_STYLE.put(MxConstants.STYLE_EDGE, MxConstants.EDGESTYLE_ENTITY_RELATION);
    }
    private final String IN = "in"; //NOI18N
    private final String OUT = "out"; //NOI18N
    private final BusinessObjectLight location;
    private final BiConsumer<List<BusinessObjectLight>, String> consumerReleaseFiber;
    
    public OspLocationView(BusinessObjectLight location, BusinessObjectLight cable, BusinessObjectLight device,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts,
            PhysicalConnectionsService physicalConnectionsService) {
        super();
        Objects.requireNonNull(cable);
        Objects.requireNonNull(device);
        Objects.requireNonNull(ts);
        setOverrideCurrentStyle(true);
        this.ts = ts;
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.location = location;
        setSizeFull();
        setConnectable(true);
        setTooltips(true);
        consumerReleaseFiber = (portFiber, specialAttrName) -> {
            ConfirmDialog confirmDialog = new ConfirmDialog(ts, 
                ts.getTranslatedString("module.general.labels.confirmation"),
                new Label(ts.getTranslatedString("module.ospman.port-tools.tool.release-port.confirm")), 
                ts.getTranslatedString("module.general.messages.ok"), () -> {
                BusinessObjectLight port = portFiber.get(0);
                BusinessObjectLight fiber = portFiber.get(1);
                    
                MxBusinessObjectNode portNode = findNode(port);
                MxBusinessObjectEdge fiberEdge = findEdge(fiber);
                if (portNode instanceof PortNode && fiberEdge instanceof FiberEdge) {
                    removeEdge(fiberEdge);
                    ((PortNode) portNode).releasePort();
                    
                    MxBusinessObjectNode fiberNode = findNode(fiber);
                    if (fiberNode instanceof FiberNode)
                        ((FiberNode) fiberNode).releaseFiber();
                    try {
                        bem.releaseSpecialRelationship(
                            port.getClassName(), port.getId(),
                            fiber.getId(), specialAttrName);
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                }
            });
            confirmDialog.open();
        };
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
                    String color = FiberWrapperNode.getColor(node, aem, bem, mem, ts);
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, node.getClassName()))
                        return new CableNode(graph, node, color);
                    if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, node.getClassName())) {
                        FiberWrapperNode fiberNode = new FiberWrapperNode(graph, node, color, bem, ts);
                        fiberNode.addFiberRightClickListener(event -> cutFiber(fiberNode));
                        return fiberNode;
                    }
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
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
        tree.addTreeAddEndListener(event -> {
            if (event.getRoots() != null && !event.getRoots().isEmpty())
                tree.expand(cable);
        });
        LinkedHashMap<BusinessObjectLight, BusinessObjectLight> ports = new LinkedHashMap();
        try {
            List<BusinessObjectLight> devicePorts = bem.getChildrenOfClassLight(
                device.getId(), device.getClassName(),
                Constants.CLASS_GENERICPHYSICALPORT, -1);
            Collections.sort(devicePorts);
            if (devicePorts.size() > 1) {
                for (int i = 0; i < devicePorts.size(); i++) {
                    BusinessObjectLight port = devicePorts.get(i);
                    if (port.getName().toLowerCase().startsWith(IN)) { //NOI18N
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
        MxSpliceBox<BusinessObjectLight> spliceBox = new MxSpliceBox<>(this, ports,
            device.getName(), deviceClass != null ? UtilHtml.toHexString(new Color(deviceClass.getColor())) : null,
            null, null, null, null, 
            port -> {
                PortNode portNode = new PortNode(port, this, aem, bem, mem, ts);
                portNode.addRightClickCellListener(event -> new WindowPortTools(port, aem, bem, mem, ts, 
                        physicalConnectionsService, consumerReleaseFiber).open());
                return portNode;
            },  
            null
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
        tree.addExpandListener(event -> expandFibers(event.getParent(), event.getChildren(), ports));
        tree.addCollapseListener(event -> collapsedFibers(event.getParent()));
    }
    private void collapsedFibers(BusinessObjectLight parent) {
        if (parent == null)
            return;
        MxBusinessObjectNode parentNode = findNode(parent);
        if (parentNode == null)
            return;
        for (MxGraphEdge edge : getEdges()) {
            if (edge instanceof MxBusinessObjectEdge) {
                MxBusinessObjectEdge fiberEdge = (MxBusinessObjectEdge) edge;
                if (fiberEdge.getBusinessObject() != null) {
                    try {
                        if (bem.isParent(parent.getClassName(), parent.getId(), fiberEdge.getBusinessObject().getClassName(), fiberEdge.getBusinessObject().getId()))
                            fiberEdge.setSource(parentNode.getUuid());
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"),
                            ex.getLocalizedMessage()
                        ).open();
                    }
                }
            }
        }
    }
    private void expandFibers(BusinessObjectLight parent, List<BusinessObjectLight> children, LinkedHashMap<BusinessObjectLight, BusinessObjectLight> ports) {
        if (parent == null || children == null || ports == null)
            return;
        ports.forEach((key, value) -> {
            expandFiber(key, children);
            expandFiber(value, children);
        });
    }
    private void expandFiber(BusinessObjectLight port, List<BusinessObjectLight> children) {
        if (port == null || children == null)
            return;
        if (children.isEmpty())
            return;
        MxBusinessObjectNode businessObjectNode = findNode(port);
        if (!(businessObjectNode instanceof PortNode))
            return;
        PortNode portNode = (PortNode) businessObjectNode;
        BusinessObjectLight fiber = portNode.getFiber();
        if (fiber == null)
            return;
        
        try {
            BusinessObjectLight fiberParent = null;
            for (BusinessObjectLight child : children) {
                if (child.getId().equals(fiber.getId())) {
                    // The fiber parent are the same fiber
                    fiberParent = child;
                    break;
                }
            }
            if (fiberParent == null) {
                for (BusinessObjectLight child : children) {
                    if (bem.isParent(child.getClassName(), child.getId(), fiber.getClassName(), fiber.getId())) {
                        fiberParent = child;
                        break;
                    }
                }
            }
            if (fiberParent != null) {
                MxBusinessObjectNode fiberParentNode = findNode(fiberParent);
                MxBusinessObjectEdge fiberEdge = findEdge(fiber);
                
                if (fiberEdge == null) {
                    fiberEdge = new FiberEdge(fiber);
                    
                    LinkedHashMap<String, String> edgeStyle = new LinkedHashMap(EDGE_STYLE);
                    String color = portNode.getFiberColor();
                    if (color != null)
                        edgeStyle.put(MxConstants.STYLE_STROKECOLOR, color);
                    fiberEdge.setRawStyle(edgeStyle);
                    fiberEdge.setSource(fiberParentNode.getUuid());
                    fiberEdge.setTarget(portNode.getUuid());

                    fiberEdge.addCellAddedListener(event -> {
                        setCellsLocked(false);
                        event.getSource().overrideStyle();
                        event.getSource().setTooltip(fiber.getName());
                        setCellsLocked(true);
                    });
                    addEdge(fiberEdge);
                } else {
                    this.setCellsLocked(true);
                    fiberEdge.setSource(fiberParentNode.getUuid());
                    this.setCellsLocked(false);
                }
            }
        } catch (InventoryException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage()
            ).open();
        }
    }
    private void cutFiber(FiberWrapperNode fiberNode) {
        new ConfirmDialog(ts,
            ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.title"), 
            new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.cut-fiber.text")),
            ts.getTranslatedString("module.general.messages.ok"), 
            () -> {
                try {
                    BusinessObjectLight fiber = fiberNode.getBusinessObject();
                    BusinessObjectLight fiberParent = bem.getParent(fiber.getClassName(), fiber.getId());
                    
                    HashMap<String, String[]> objects = new HashMap();
                    objects.put(fiber.getClassName(), new String[] {fiber.getId()});
                    
                    String[] newFibersId = bem.copySpecialObjects(fiberParent.getClassName(), fiberParent.getId(), objects, false);
                    if (newFibersId.length == 1) {
                        BusinessObjectLight newFiber = bem.getObject(fiber.getClassName(), newFibersId[0]);
                        HashMap<String, String> attrs = new HashMap();
                        attrs.put(Constants.PROPERTY_NAME, fiber.getName());
                        bem.updateObject(newFiber.getClassName(), newFiber.getId(), attrs);
                        newFiber.setName(fiber.getName());
                        bem.createSpecialRelationship(
                            fiber.getClassName(), fiber.getId(), 
                            location.getClassName(), location.getId(), 
                            OutsidePlantService.SPECIAL_RELATIONSHIP_OSPMAN_HAS_LOCATION, true);
                        fiberNode.cutFiber(fiber, newFiber);
                    } else {
                        for (String newFiberId : newFibersId)
                            bem.deleteObject(fiber.getClassName(), newFiberId, true);
                        throw new InventoryException(
                            ts.getTranslatedString(
                                String.format("module.ospman.copy-fiber.error", fiber.getId())
                            )
                        );
                    }
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
                
            }).open();
    }
    private void spliceFiber(FiberNode fiberNode, PortNode portNode) {
        if (fiberNode == null && portNode == null)
            return;
        
        LinkedHashMap<String, String> edgeStyle = new LinkedHashMap(EDGE_STYLE);
        edgeStyle.put(MxConstants.STYLE_STROKECOLOR, fiberNode.getColor());
        
        new ConfirmDialog(ts,
            ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.title"), 
            new Label(ts.getTranslatedString("module.ospman.mid-span-access.confirmation.splice-fiber.text")),
            ts.getTranslatedString("module.general.messages.ok"), 
            () -> {
                try {
                    BusinessObjectLight fiberObject = fiberNode.getBusinessObject();
                    BusinessObjectLight portObject = portNode.getBusinessObject();
                    
                    String specialRelationshipName = null;
                    if (portObject.getName().startsWith(IN)) {
                        specialRelationshipName = PortNode.SPECIAL_ATTR_ENDPOINT_B;
                    } else if (portObject.getName().startsWith(OUT)) {
                        specialRelationshipName = PortNode.SPECIAL_ATTR_ENDPOINT_A;
                    } else {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.warning"), 
                            ts.getTranslatedString("module.ospman.warning.port-name")
                        ).open();
                        return;
                    }
                    bem.createSpecialRelationship(
                        fiberObject.getClassName(), fiberObject.getId(),
                        portObject.getClassName(), portObject.getId(),
                        specialRelationshipName, true
                    );
                    MxBusinessObjectEdge fiberEdge = new FiberEdge(fiberObject);
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
                    fiberNode.spliceFiber();
                    portNode.setFiber(fiberObject);
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
            }
        ).open();
    }
    // <editor-fold defaultstate="collapsed" desc="Helpers">
    private void excecuteLayout() {
        setCellsLocked(false);
        executeStackLayout(null, true, 200, 50);
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
    private MxBusinessObjectNode findNode(BusinessObjectLight object) {
        if (object != null) {
            for (MxGraphNode node : getNodes()) {
                if (node instanceof MxBusinessObjectNode && 
                    ((MxBusinessObjectNode) node).getBusinessObject() != null && 
                    ((MxBusinessObjectNode) node).getBusinessObject().getId().equals(object.getId())) {
                    return (MxBusinessObjectNode) node;
                }
            }
        }
        return null;
    }
    private MxBusinessObjectEdge findEdge(BusinessObjectLight businessObject) {
        if (businessObject != null) {
            for (MxGraphEdge edge : getEdges()) {
                if (edge instanceof MxBusinessObjectEdge && 
                    ((MxBusinessObjectEdge) edge).getBusinessObject() != null && 
                    ((MxBusinessObjectEdge) edge).getBusinessObject().getId().equals(businessObject.getId())) {
                    return (MxBusinessObjectEdge) edge;
                }
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
}
