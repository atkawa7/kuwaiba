/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.img;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.ejb.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.util.Constants;
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
    
    public static SceneExporter getInstance(BusinessEntityManager bem, MetadataEntityManager mem) {
        if(sceneExporter == null){
            sceneExporter = new SceneExporter();
            SceneExporter.bem = bem;
            SceneExporter.mem = mem;
        }
        return sceneExporter;
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
