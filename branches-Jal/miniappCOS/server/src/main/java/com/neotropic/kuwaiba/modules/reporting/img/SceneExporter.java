/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.img;

import com.neotropic.kuwaiba.modules.reporting.img.rackview.DeviceLayoutRenderer;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.DeviceLayoutScene;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewImage;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewScene;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewService;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.ejb.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.web.procmanager.rackview.ComponentRackView;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 *
 * @author adrian
 */
public class SceneExporter {
    
    public static String PATH = "../docroot/reports/imgs/paths/";
    private static BusinessEntityManager bem;
    private static MetadataEntityManager mem;
    private static SceneExporter sceneExporter = null;

    private SceneExporter() {}
    
    public static SceneExporter getInstance(/*BusinessEntityManager bem, MetadataEntityManager mem*/) {
        if(sceneExporter == null){
            sceneExporter = new SceneExporter();
            SceneExporter.bem = PersistenceService.getInstance().getBusinessEntityManager();//bem;
            SceneExporter.mem = PersistenceService.getInstance().getMetadataEntityManager();//mem;
        }
        return sceneExporter;
    }
    
    public String buildRackView(
        String ipAddress, RemoteSession remoteSession, WebserviceBean webserviceBean, 
        String rackClassName, long rackId) {
        
        RackViewImage rackViewImage = RackViewImage.getInstance();
        rackViewImage.setIpAddress(ipAddress);
        rackViewImage.setRemoteSession(remoteSession);
        rackViewImage.setWebserviceBean(webserviceBean);
        
        try {
            RemoteObject rackObject = webserviceBean.getObject(rackClassName, rackId, ipAddress, remoteSession.getSessionId());
            
            RackViewScene rackViewScene = new RackViewScene(RackViewImage.getInstance().getDevices(rackObject));
            rackViewScene.setShowConnections(true);
            
            RackViewService service = new RackViewService(rackViewScene, rackObject);
            
            service.shownRack();
            
            try {
                org.netbeans.api.visual.export.SceneExporter.createImage(rackViewScene,
                        new File(PATH + rackClassName + "_" + rackId +".png"),
                        org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                        org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                        false, false, 100,
                        0,  //Not used
                        0); //Not used
                
                return rackClassName + "_" + rackId;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
        
    public String buildDeviceLayout(String deviceClassName, long deviceId, WebserviceBean webserviceBean, String ipAddress, RemoteSession remoteSession) {
        try {
            RackViewImage rackViewImage = RackViewImage.getInstance();
            rackViewImage.setIpAddress(ipAddress);
            rackViewImage.setRemoteSession(remoteSession);
            rackViewImage.setWebserviceBean(webserviceBean);
            
            RemoteObject remoteObject = webserviceBean.getObject(deviceClassName, deviceId, ipAddress, remoteSession.getSessionId());
            
            int rackUnits = remoteObject.getAttribute("rackUnits") != null ? Integer.valueOf(remoteObject.getAttribute("rackUnits")) : 1;
            
            int rackUnitWidth = (int) ComponentRackView.RACK_UNIT_WIDTH_PX;
            int rackUnitHeight = (int) ComponentRackView.RACK_UNIT_HEIGHT_PX * rackUnits;
                        
            DeviceLayoutScene deviceLayoutScene = new DeviceLayoutScene();
            DeviceLayoutRenderer deviceLayoutRenderer = new DeviceLayoutRenderer(
                remoteObject, deviceLayoutScene, 
                new Point(0, 0), new Rectangle(0, 0, rackUnitWidth, rackUnitHeight), 
                null, null);
            deviceLayoutRenderer.render();
            deviceLayoutScene.setPreferredBounds(new Rectangle(0, 0, rackUnitWidth, rackUnitHeight));
            deviceLayoutScene.revalidate();
            deviceLayoutScene.repaint();
            
            org.netbeans.api.visual.export.SceneExporter.createImage(deviceLayoutScene,
                new File(PATH + remoteObject.getClassName() + "_" + remoteObject.getId() +".png"),
                org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                false, false, 100,
                0,  //Not used
                0); //Not used

            return remoteObject.getClassName() + "_" + remoteObject.getId();
            
        } catch (ServerSideException | IOException ex) {
            Exceptions.printStackTrace(ex);
            
        }
        return null;
    }
        
    public String buildPhysicalPathView(String portClassName, long portId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
            ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException
    {
        PhysicalPathScene scene = new PhysicalPathScene();
        ObjectBoxWidget lastPortWidget = null;
        ConnectionWidget lastConnectionWidget = null;
        
        List<BusinessObjectLight> trace = bem.getPhysicalPath(portClassName, portId);
        
        for (BusinessObjectLight element : trace){
            if (!mem.isSubClass(Constants.CLASS_GENERICPHYSICALLINK, element.getClassName())) { //It's a port
                List<BusinessObjectLight> ancestors = bem.getParents(element.getClassName(), element.getId());
                
                lastPortWidget = (ObjectBoxWidget)scene.addNode(element);
                
                if (lastConnectionWidget != null)
                    lastConnectionWidget.setTargetAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                lastConnectionWidget = null;
                Widget lastWidget = lastPortWidget;
                
                for (int i = 0 ; i < ancestors.size() - 1; i++) { //We ignore the dummy root
                    Widget possibleParent = scene.findWidget(ancestors.get(i));
                    if (possibleParent == null){
                        Widget node = scene.addNode(ancestors.get(i));
                        ((ObjectBoxWidget)node).addBox(lastWidget);
                        lastWidget = node;
                    }else{
                        ((ObjectBoxWidget)possibleParent).addBox(lastWidget);
                        break;
                    }
                    if (mem.isSubClass(Constants.CLASS_GENERICPHYSICALNODE, (ancestors.get(i)).getClassName()) || //Only parents up to the first physical node (say a building) will be displayed
                                            i == ancestors.size() - 2){ //Or if the next level is the dummy root
                        scene.addRootWidget(lastWidget);
                        scene.validate();
                        break;
                    }
                }
            }else{
                lastConnectionWidget = (ConnectionWidget)scene.addEdge(element);
                if (lastPortWidget != null)
                    lastConnectionWidget.setSourceAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                lastPortWidget = null;
            }
        }
        
        if(!trace.isEmpty()){
            try {
                org.netbeans.api.visual.export.SceneExporter.createImage(scene,
                        new File(PATH + portClassName + "_" + portId +".png"),
                        org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                        org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                        false, false, 100,
                        0,  //Not used
                        0); //Not used
                return portClassName + "_" + portId;
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            }
        return null;
    }
    
}