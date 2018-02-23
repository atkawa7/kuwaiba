/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts;

import java.util.HashMap;
import java.util.List;
import javax.xml.bind.DatatypeConverter;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Binary;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;

/**
 * Class to manage the export of device layouts
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutExporter {
    public DeviceLayoutScene scene;
    
    public DeviceLayoutExporter(DeviceLayoutScene scene) {
        this.scene = scene;
    }
    
    public String getAsXMl() {
        HashMap<LocalObjectListItem, String> customShapes = new HashMap();
        
        for (Shape node : scene.getNodes()) {
            if (node instanceof CustomShape) {
                LocalObjectListItem customShape = ((CustomShape) node).getListItem();
                
                if (!customShapes.containsKey(customShape))
                    customShapes.put(customShape, "");
            }
        }
        for (LocalObjectListItem customShape : customShapes.keySet()) {
            byte[] structure = getCustomShapeStructure(customShape);
            
            if (structure != null)
                customShapes.put(customShape, prepareStructure(structure));
        }       
        
        String sceneStructure = prepareStructure(scene.getAsXML());
        
        String result = "<view version=\"1.1\">"; 
        for (LocalObjectListItem customShape : customShapes.keySet()) {
            result += "<customShape " + Constants.PROPERTY_CLASSNAME + "=\"" + customShape.getClassName() + "\" "+ Constants.PROPERTY_ID +"=\"" + customShape.getId() + "\" " + Constants.PROPERTY_NAME + "=\"" + customShape.getName() + "\">";
            
            LocalObject customShapeObj = CommunicationsStub.getInstance().getObjectInfo(customShape.getClassName(), customShape.getId());
            if (customShapeObj == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return null;
            }
            Binary binaryIcon = (Binary) customShapeObj.getAttribute("icon");
            if (binaryIcon != null) {
                byte[] byteIcon = binaryIcon.getByteArray();
                
                if (byteIcon.length > 0)
                    result += "<icon>" + binaryIcon.getFileName() + ";/;" + binaryIcon.getFileExtension() + ";/;" + DatatypeConverter.printBase64Binary(byteIcon) + "</icon>";
            }
            String customShapeStrStructure = customShapes.get(customShape);
            
            if (!customShapeStrStructure.equals(""))
                result += customShapeStrStructure;
            
            result += "</customShape>";
        }   
        result += "<device " + Constants.PROPERTY_CLASSNAME + "=\"" + scene.getModel().getClassName() + "\" "+ Constants.PROPERTY_ID +"=\"" + scene.getModel().getId() + "\" " + Constants.PROPERTY_NAME + "=\"" + scene.getModel().getName() + "\">";
        result += sceneStructure;
        result += "</device>";
        result += "</view>";
        return result;
    }
    
    private String prepareStructure(byte[] structure) {
        String strStructure = new String(structure);
        strStructure = strStructure.replaceFirst("<view version=\"1.1\">", "");
        strStructure = strStructure.replaceFirst("</view>", "");
        return strStructure;
    }
    
    private byte[] getCustomShapeStructure(LocalObjectListItem customShape) {
        List<LocalObjectViewLight> views = CommunicationsStub.getInstance().getListTypeItemRelatedViews(customShape.getId(), customShape.getClassName());
        if (views != null) {
            if (!views.isEmpty()) {
                LocalObjectView view = CommunicationsStub.getInstance().getListTypeItemRelatedView(customShape.getId(), customShape.getClassName(), views.get(0).getId());
                if (view != null) {
                    byte [] structure = view.getStructure();
                    if (structure != null)
                        return structure;
                }
            }
        }        
        return null;
    }
}
