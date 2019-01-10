/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.web.procmanager.rackview;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.shared.ui.dnd.DropEffect;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.dnd.DropTargetExtension;
import com.vaadin.ui.dnd.event.DropEvent;
import com.vaadin.ui.dnd.event.DropListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentRackView extends VerticalLayout {
    public static final float RACK_UNIT_WIDTH_PX = 500;
    public static final float RACK_UNIT_HEIGHT_PX = RACK_UNIT_WIDTH_PX / 10.86f; // 10.86 is the proportion to rack unit height repect the withd in all kuwaiba application
    
    private RemoteObject rackObject;
    private final WebserviceBean webserviceBean;
        
    public ComponentRackView(RemoteObjectLight rackObjectLight, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        try {
            this.rackObject = webserviceBean.getObject(
                rackObjectLight.getClassName(),
                rackObjectLight.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
        } catch (ServerSideException ex) {
            //Exceptions.printStackTrace(ex);
        }
        setWidth(100, Unit.PERCENTAGE);
        setHeightUndefined();
        setStyleName("rackview");
        
        initializeComponent();
    }
    
    public static Image getImage(RemoteObject remoteObject, int rackUnits, WebserviceBean webserviceBean) {
        
        if (remoteObject != null && rackUnits > 0) {
            
            SceneExporter sceneExporter = SceneExporter.getInstance();

            String oldPath = SceneExporter.PATH;
            
            String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
            String newPath = processEnginePath + "/temp/"; //NOI18N

            SceneExporter.PATH = newPath;

            String img = sceneExporter.buildDeviceLayout(
                remoteObject.getClassName(), 
                remoteObject.getId(),
                webserviceBean, 
                Page.getCurrent().getWebBrowser().getAddress(), 
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")) //NOI18N
            );

            SceneExporter.PATH = oldPath;

            FileResource resource = new FileResource(new File(newPath + img + ".png"));                    

            Image image = new Image(null, resource);
            image.setWidth(RACK_UNIT_WIDTH_PX, Unit.PIXELS);
            image.setHeight(RACK_UNIT_HEIGHT_PX * rackUnits, Unit.PIXELS);
            
            return image;
        }
        return null;
    }
    
    public final void initializeComponent() {
        removeAllComponents();
                        
        VerticalLayout rackLayout = new VerticalLayout();
        rackLayout.setSpacing(false);
                
        int rackUnits = rackObject.getAttribute("rackUnits") != null ? Integer.valueOf(rackObject.getAttribute("rackUnits")) : 0; //NOI18N
        HashMap<Integer, RemoteObject> indexes = new HashMap();
        List<RemoteObject> children = new ArrayList();
        
        try {
            
            List<RemoteObjectLight> childrenLight = webserviceBean.getObjectChildren(
                rackObject.getClassName(),
                rackObject.getId(),
                0,
                Page.getCurrent().getWebBrowser().getAddress(),
                ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                        
            for (RemoteObjectLight childLight : childrenLight) {
                RemoteObject child = webserviceBean.getObject(
                    childLight.getClassName(), 
                    childLight.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                
                int childRackUnits = child.getAttribute("rackUnits") != null ? Integer.valueOf(child.getAttribute("rackUnits")) : -1;
                int childPosition = child.getAttribute("position") != null ? Integer.valueOf(child.getAttribute("position")) : -1;
                
                if (childRackUnits > 0 && childPosition > 0)
                    indexes.put(childPosition, child);
                
                children.add(child);
            }
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
                
        float sidesWidthPx = 50;
                                
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setSpacing(false);
                        
        VerticalLayout right = new VerticalLayout();
        right.setSpacing(false);
        right.setWidth(sidesWidthPx, Unit.PIXELS);
        right.setHeight(rackUnits * RACK_UNIT_HEIGHT_PX, Unit.PIXELS);
        
        VerticalLayout left = new VerticalLayout();
        left.setSpacing(false);
        left.setWidth(sidesWidthPx, Unit.PIXELS);
        left.setHeight(rackUnits * RACK_UNIT_HEIGHT_PX, Unit.PIXELS);
                
        for (int i = 1; i <= rackUnits; i += 1) {
            
            Label lblLeft = new Label(String.valueOf(i));
            lblLeft.setWidth(sidesWidthPx, Unit.PIXELS);
            lblLeft.setHeight(RACK_UNIT_HEIGHT_PX, Unit.PIXELS);
                        
            Label lblRigth = new Label(String.valueOf(i));
            lblRigth.setWidth(sidesWidthPx, Unit.PIXELS);
            lblRigth.setHeight(RACK_UNIT_HEIGHT_PX, Unit.PIXELS);
            
            left.addComponent(lblLeft);            
            right.addComponent(lblRigth);
        }        
        horizontalLayout.addComponent(right);
        horizontalLayout.addComponent(rackLayout);
        horizontalLayout.addComponent(left);
        
        rackLayout.setWidth(RACK_UNIT_WIDTH_PX, Unit.PIXELS);
        rackLayout.setHeightUndefined();
        
        HashMap<RemoteObject, VerticalLayout> rackUnitContainers = new HashMap();
        
        for (int i = 1; i <= rackUnits; i += 1) {
            
            int usedRackUnits = 1;
            
            ComponentRackUnit rackUnit = new ComponentRackUnit(i);
            
            if (indexes.containsKey(i)) {
                RemoteObject device = indexes.get(i);
                usedRackUnits = device.getAttribute("rackUnits") != null ? Integer.valueOf(device.getAttribute("rackUnits")) : 1;
                
                i += usedRackUnits - 1;
                
                rackUnitContainers.put(device, rackUnit);
            }
            rackUnit.addStyleName("rackunit");
            rackUnit.setWidth(RACK_UNIT_WIDTH_PX, Unit.PIXELS);
            rackUnit.setHeight(RACK_UNIT_HEIGHT_PX * usedRackUnits, Unit.PIXELS);
            rackLayout.addComponent(rackUnit);

            DropTargetExtension<ComponentRackUnit> dropTarget = new DropTargetExtension<>(rackUnit);
            dropTarget.setDropEffect(DropEffect.MOVE);

            dropTarget.addDropListener(new DropListener<ComponentRackUnit>() {
                @Override
                public void drop(DropEvent<ComponentRackUnit> event) {
                    Optional<AbstractComponent> dragSource = event.getDragSourceComponent();

                    if (dragSource.isPresent() &&  dragSource.get() instanceof ComponentDevice) {
                        
                        ComponentDevice componentDevice = (ComponentDevice) dragSource.get();
                                                
                        if (rackObject != null && webserviceBean != null && componentDevice.getDevice() != null) {
                            try {
                                
                                List<StringPair> attributesToBeUpdated = new ArrayList();
                                attributesToBeUpdated.add(new StringPair("position", String.valueOf(rackUnit.getRackUnit())));
                                                                
                                webserviceBean.updateObject(
                                    componentDevice.getDevice().getClassName(), 
                                    componentDevice.getDevice().getId(), 
                                    attributesToBeUpdated, 
                                    Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                                                                
                                webserviceBean.moveObjects(
                                        rackObject.getClassName(),
                                        rackObject.getId(),
                                        new String[] {componentDevice.getDevice().getClassName()}, 
                                        new long[] {componentDevice.getDevice().getId()},
                                        Page.getCurrent().getWebBrowser().getAddress(),
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId()); //NOI18N
                                                                
                            } catch (ServerSideException ex) {
                                //Exceptions.printStackTrace(ex);
                            }
                        }                        
                        
                        initializeComponent();
                    }
                }
            });
        }
        if (rackObject != null && webserviceBean != null) {
            for (RemoteObject child : children) {
                int deviceRackUnits = child.getAttribute("rackUnits") != null ? Integer.valueOf(child.getAttribute("rackUnits")) : 0;

                Image deviceImage = getImage(child, deviceRackUnits, webserviceBean);

                if (deviceImage != null) {
                    if (rackUnitContainers.containsKey(child))
                        rackUnitContainers.get(child).addComponent(deviceImage);
                }
            }
        }
        addComponent(horizontalLayout);
        setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
    }
    
    public class ComponentRackUnit extends VerticalLayout {
        private final int rackUnit;
        
        public ComponentRackUnit(int rackUnit) {
            this.rackUnit = rackUnit;
        }
        
        public int getRackUnit() {
            return rackUnit;
        }
    }
}