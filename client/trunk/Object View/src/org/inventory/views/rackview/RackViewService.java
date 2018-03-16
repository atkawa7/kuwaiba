/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.rackview;

import org.inventory.views.rackview.scene.RackViewScene;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.CommunicationsStubTask;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.NestedDeviceWidget;
import org.inventory.views.rackview.widgets.PortWidget;
import org.inventory.views.rackview.widgets.RackViewConnectionWidget;
import org.inventory.views.rackview.widgets.RackWidget;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Service used to load data to render a rack view
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewService {
    //this need to be replace, the VirtualPort should be moved under GenericLogicalPort, 
    //find a better place for the other classes under GenericBoard, should be GenericCommunitacionsBoard 
    //to make a diference between the PowerBoards and the Communitacions Boards
    private final LocalObject rack;
    private final RackViewScene scene;
    private static ProgressHandle progressHandle;
    
    public RackViewService(RackViewScene scene, LocalObject rack) {
        this.rack = rack;
        this.scene = scene;
    }
    
    public static ProgressHandle getProgressHandle() {
        return progressHandle;
    }
    
    public static void setProgressHandle(ProgressHandle ph) {
        progressHandle = ph;
    }
    
    public static void setProgress(int workUnit) {
        if (progressHandle != null)
            progressHandle.progress(workUnit);
    }
    
    public static void setProgress(String message) {
        if (progressHandle != null)
            progressHandle.progress(message);
    }
    
    public static void setProgress(String message, int workunits) {
        if (progressHandle != null)
            progressHandle.progress(message, workunits);
    }
    
    public static void switchToDeterminate(int workunits) {
        if (progressHandle != null)
            progressHandle.switchToDeterminate(workunits);
    }
    
//    public static void switchToIndeterminate(int workunits) {
//        if (progressHandle != null)
//            progressHandle.switchToIndeterminate();
//    }
    
    public void shownRack() {
        
        if (rack == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            Boolean ascending = (Boolean) rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING);
            if (ascending == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), NotificationUtil.WARNING_MESSAGE, 
                    "The rack unit sorting has not been set. Ascending is assumed");
            }
            scene.render(rack);
            
            if (scene.getShowConnections()) {
                Widget widget = scene.findWidget(rack);
                if (widget instanceof RackWidget) {
                    for (LocalObject equipment : ((RackWidget) widget).getLocalEquipment()) {
                        Widget equipmentWidget = scene.findWidget(equipment);
                                
                        if(equipmentWidget instanceof EquipmentWidget && ((EquipmentWidget) equipmentWidget).hasLayout())
                            setEquipmentParent(equipmentWidget, equipmentWidget);
                    }
                    List<LocalObjectLight> specialChildren = CommunicationsStub.getInstance().getObjectSpecialChildren(rack.getClassName(), rack.getOid());
                    
                    if (specialChildren == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), //NOI18N
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    } else {
                        List<LocalObjectLight> connections = new ArrayList();
                        for (LocalObjectLight specialChild : specialChildren) {
                            if (CommunicationsStub.getInstance().isSubclassOf(specialChild.getClassName(), Constants.CLASS_GENERICPHYSICALLINK))
                                connections.add(specialChild);
                        }
                        createConnections(connections);
                    }
                }
            }
            ((RackWidget) scene.findWidget(rack)).resizeRackWidget();
            scene.validate();
            scene.repaint();
        }
    }
    /**
     * Used to create the connection router
     */
    private void setEquipmentParent(Widget equipmentWidget, Widget parentWidget) {
        
        for (Widget child : parentWidget.getChildren()) {
            Object objectChild = scene.findObject(child);
            if (objectChild instanceof LocalObjectLight) {
                if (CommunicationsStub.getInstance().isSubclassOf(((LocalObjectLight) objectChild).getClassName(), "GenericPhysicalPort")) { //NOI18N
                    if (child instanceof PortWidget)
                        ((PortWidget) child).setParent((NestedDeviceWidget) equipmentWidget);
                }
            }
            setEquipmentParent(equipmentWidget, child);
        }
    }
    
    private void createConnections(List<LocalObjectLight> connections) {
               
        ExecutorService fixedThreadPool = CommunicationsStubTask.getFixedThreadPool();

        HashMap<LocalObjectLight, Future<HashMap<String, LocalObjectLight[]>>> futures = new HashMap();
        for (LocalObjectLight connection : connections)
            futures.put(connection, fixedThreadPool.submit(CommunicationsStubTask.getInstance().getSpecialAttributesCallable(connection.getClassName(), connection.getOid())));

        HashMap<LocalObjectLight, HashMap<String, LocalObjectLight[]>> connectionsMap = new HashMap();
        for (LocalObjectLight connection : connections) {
            try {
                connectionsMap.put(connection, futures.get(connection).get());
            } catch (InterruptedException | ExecutionException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, 
                    "An unexpected error occurred in the Rack Widget. Contact your administrator.");
                Exceptions.printStackTrace(ex);
                fixedThreadPool.shutdown();
            }
        }
        RackViewService.switchToDeterminate(connections.size());
        RackViewService.setProgress("Loading connections");
                        
        for (int i = 0; i < connections.size(); i += 1) {
            LocalObjectLight connection = connections.get(i);
            
            HashMap<String, LocalObjectLight[]> specialAttributes = connectionsMap.get(connection);
            
            if (specialAttributes == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                continue;
            }
            
            LocalObjectLight[] endpointsA = specialAttributes.get("endpointA"); //NOI18N
            if (endpointsA == null)
                continue;
                            
            LocalObjectLight[] endpointsB = specialAttributes.get("endpointB"); //NOI18N
            if (endpointsB == null)
                continue;
            
            if (endpointsA.length == 0) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("The endpointA was removed in the link %s", connection.toString()));
                continue;
            }
            if (endpointsB.length == 0) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("The Endpoint B was removed in the link %s", connection.toString()));
                continue;
            }
            LocalObjectLight aSide = endpointsA[0];
            if (!CommunicationsStub.getInstance().isSubclassOf(aSide.getClassName(), Constants.CLASS_GENERICPORT)) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("The endpointA in link %s is not a %s", connection.toString(), Constants.CLASS_GENERICPORT));
                continue;
            }
            LocalObjectLight bSide = endpointsB[0];
            if (!CommunicationsStub.getInstance().isSubclassOf(bSide.getClassName(), Constants.CLASS_GENERICPORT)) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("The endpointB in link %s is not a %s", connection.toString(), Constants.CLASS_GENERICPORT));
                continue;
            }
            
            Widget aSideWidget = scene.findWidget(aSide);
            Widget bSideWidget = scene.findWidget(bSide);
            
            if (aSideWidget != null && bSideWidget != null) { 
                ObjectConnectionWidget lastConnectionWidget = (ObjectConnectionWidget)scene.addEdge(connection);
                lastConnectionWidget.getLabelWidget().setLabel((aSide.getName() == null ? "" : aSide.getName()) + " ** " + (bSide.getName() == null ? "" : bSide.getName()));
                scene.setEdgeSource(connection, aSide);
                scene.setEdgeTarget(connection, bSide);
            }
            if (aSideWidget instanceof PortWidget)
                ((PortWidget) aSideWidget).setFree(false);

            if (bSideWidget instanceof PortWidget)
                ((PortWidget) bSideWidget).setFree(false);
            
            scene.validate();
            scene.repaint();
            
            RackViewService.setProgress("Loading " + connection.toString(), i + 1);
        }
    }
        
    public List<List<LocalObjectLight>> getRackTable() {
        List<List<LocalObjectLight>> result = new ArrayList<>();
        
        for (LocalObjectLight edge : scene.getEdges()) {
            RackViewConnectionWidget conn = (RackViewConnectionWidget) scene.findWidget(edge);
            
            PortWidget sourcePort = (PortWidget) conn.getSourceAnchor().getRelatedWidget();
            PortWidget targetPort = (PortWidget) conn.getTargetAnchor().getRelatedWidget();

            NestedDevice sourceEquipment = sourcePort;
            while (sourceEquipment.getParent() != null)
                sourceEquipment = sourceEquipment.getParent();

            NestedDevice targetEquipment = targetPort;
            while (targetEquipment.getParent() != null)
                targetEquipment = targetEquipment.getParent();

            EquipmentWidget sourceDevice = (EquipmentWidget) sourceEquipment;
            EquipmentWidget targetDevice = (EquipmentWidget) targetEquipment;

            LocalObjectLight sourcePortObj = sourcePort.getLookup().lookup(LocalObjectLight.class);
            LocalObjectLight targetPortObj = targetPort.getLookup().lookup(LocalObjectLight.class);

            List<LocalObjectLight> row = new ArrayList<>();
            row.add(sourceDevice.getLookup().lookup(LocalObject.class));
            row.add(sourcePortObj);
            row.add(targetDevice.getLookup().lookup(LocalObject.class));
            row.add(targetPortObj);
            
            result.add(row);
        }
        
        return result;
    }
}
