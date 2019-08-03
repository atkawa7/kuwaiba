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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.openide.util.Exceptions;

/**
 * Class to manage the import of device layout files
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutImporter {
    private final String path;
    private final byte[] structure;
    private final List<String> layouts;
    private final List<LocalObjectLight> objects;
    private final List<LocalFileObject> icons;
    private final HashMap<LocalObjectLight, LocalObjectListItem> localMap;
    private final LocalObjectListItem deviceModel;
    private final DeviceLayoutScene scene;
    
    public DeviceLayoutImporter(String path, byte[] structure, LocalObjectListItem deviceModel, DeviceLayoutScene scene) {
        this.path = path;
        this.structure = structure;        
        this.layouts = new ArrayList();
        this.objects = new ArrayList();
        this.icons = new ArrayList();
        this.localMap = new HashMap();
        this.deviceModel = deviceModel;
        this.scene = scene;
    }
    
    public boolean importDeviceLayout() {
        String strStructure = new String(structure);
        int idx = 0;
        idx = strStructure.indexOf("<layout x=\"", idx);
        while (idx != -1) {
            int beginIdx = idx;
            
            idx = strStructure.indexOf("</layout>", idx);
            
            int endIdx = idx;
            
            String layoutStructure = strStructure.substring(beginIdx, endIdx + "</layout>".length());
            layouts.add(layoutStructure);
            
            idx = strStructure.indexOf("<layout x=\"", idx);
        }
        getObjects();
        
        return selectCustomShapeToImport();
    }
    
    public boolean selectCustomShapeToImport() {
        List<LocalObjectLight> customShapes = new ArrayList();
        
        for (LocalObjectLight obj : objects) {
            if (obj.getClassName().equals(Constants.CLASS_CUSTOMSHAPE))
                customShapes.add(obj);
        }  
        List<String> labels = new ArrayList();
        List<JPanel> components = new ArrayList();
                
        labels.add("");
        JPanel pnlMessage = new JPanel();
        pnlMessage.add(new JLabel("The following custom shapes were found in the import file"));
        components.add(pnlMessage);
                
        for (LocalObjectLight customShape : customShapes) {
            JPanel pnlCustomShape = new JPanel();
            
            JButton btnImport = new JButton();
            btnImport.setPreferredSize(new Dimension(120, 25));
            btnImport.setMinimumSize(new Dimension(120, 25));
            btnImport.setMinimumSize(new Dimension(120, 25));
            btnImport.setName("action" + String.valueOf(customShape.getId()));
            
            final JRadioButton btnIsSelected = new JRadioButton();
            btnIsSelected.setPreferredSize(new Dimension(25, 25));
            btnIsSelected.setMinimumSize(new Dimension(25, 25));
            btnIsSelected.setMinimumSize(new Dimension(25, 25));
            btnIsSelected.setEnabled(false);
            btnIsSelected.setName("execute" + String.valueOf(customShape.getId()));
            
            btnImport.setAction(new AbstractAction() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (btnIsSelected.isSelected())
                        btnIsSelected.setSelected(false);
                    else
                        btnIsSelected.setSelected(true);
                }
            });            
            if (getLocalCustomShape(customShape) != null)
                btnImport.setText("Replace");
            else {
                btnImport.setText("Import");
                btnImport.setEnabled(false);
                btnIsSelected.setSelected(true);
            }
            pnlCustomShape.add(btnImport);
            pnlCustomShape.add(btnIsSelected);
            pnlCustomShape.setName("pnlCustomShape" + String.valueOf(customShape.getId()));
                        
            labels.add(customShape.getName());
            components.add(pnlCustomShape);
        }
        
        JComplexDialogPanel pnlImportDeviceLayout = new JComplexDialogPanel(labels.toArray(new String[0]), components.toArray(new JPanel[0]));
        if (JOptionPane.showConfirmDialog(null, pnlImportDeviceLayout, "Import file: " + path, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            List<LocalObjectLight> customShapesToImport = new ArrayList();
            
            for (LocalObjectLight customShape : customShapes) {
                String action = ((JButton) ((JPanel) pnlImportDeviceLayout.getComponent("pnlCustomShape" + String.valueOf(customShape.getId()))).getComponent(0)).getText();
                boolean execute = ((JRadioButton) ((JPanel) pnlImportDeviceLayout.getComponent("pnlCustomShape" + String.valueOf(customShape.getId()))).getComponent(1)).isSelected();
                
                if ("Import".equals(action) && execute)
                    customShapesToImport.add(customShape);
                else if ("Replace".equals(action) && execute)
                    customShapesToImport.add(customShape);
                
                if ("Replace".equals(action) && !execute)
                    localMap.put(customShape, getLocalCustomShape(customShape));
            }
            
            importCustomShapes(customShapesToImport);
            return setDeviceLayout();
        }
        return false;
    }
    
    private boolean replaceCustomShapes(LocalObjectLight anObject) {
        int deviceIdx = objects.indexOf(anObject);

        if (deviceIdx != -1) {
            String layoutStr = layouts.get(deviceIdx);
            
            // Is possible that the ids from import are equal to the local then to solve this, we use a procedure of two steps
            
            // First: Replaces the ids from the import file with the local ids but append an x
            for (LocalObjectLight object : objects) {
                
                if (Constants.CLASS_CUSTOMSHAPE.equals(object.getClassName())) {
                    LocalObjectListItem localCustomShape = getLocalCustomShape(object);
                    if (localCustomShape != null) {                                                
                        String id = "id=\"" + object.getId() + "\"";
                        String localId = "id=\"" + localCustomShape.getId() + "x\"";

                        layoutStr = layoutStr.replaceAll(id, localId);
                    }
                }
            }
            // Second: Removes the x
            for (LocalObjectLight object : objects) {
                
                if (Constants.CLASS_CUSTOMSHAPE.equals(object.getClassName())) {
                    LocalObjectListItem localCustomShape = getLocalCustomShape(object);
                    if (localCustomShape != null) {                                                
                        String id = "id=\"" + localCustomShape.getId() + "x\"";
                        String localId = "id=\"" + localCustomShape.getId() + "\"";

                        layoutStr = layoutStr.replaceAll(id, localId);
                    }
                }
            }
            
            layouts.set(deviceIdx, layoutStr);
            return true;
        }
        return false;                
    }
    
    private boolean setDeviceLayout() {
        LocalObjectLight device = null;
        
        for (LocalObjectLight object : objects) {
            if (!Constants.CLASS_CUSTOMSHAPE.equals(object.getClassName())) {
                device = object;
                break;
            }
        }
        
        if (device != null) {
            boolean importView = true;

            if (scene.getNodes() != null && !scene.getNodes().isEmpty()) {
                if (JOptionPane.showConfirmDialog(null, "Are you sure you want to replace the current device layout?", 
                        I18N.gm("confirmation"), JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION)
                    importView =  false;
            }

            if (importView) {
                replaceCustomShapes(device);
                byte[] viewStructure = getViewAsXML(device);
                setView(deviceModel, viewStructure);
                scene.render(viewStructure);
            }
            return importView;
        }
        return false;
    }
    
    private void importCustomShapes(List<LocalObjectLight> customShapesToImport) {
        for (LocalObjectLight customShape : customShapesToImport) {
            LocalObjectListItem localCustomShape = getLocalCustomShape(customShape);
            
            if (getLocalCustomShape(customShape) != null) {
                localMap.put(customShape, getLocalCustomShape(customShape));                
            } else {
                localCustomShape = CommunicationsStub.getInstance().createListTypeItem(customShape.getClassName());
            
                if (localCustomShape == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    continue;
                }
                localMap.put(customShape, localCustomShape);
                // Calling newly to getLocalCustomShape method to update the cache
                getLocalCustomShape(customShape);
            }
            int customShapeIdx = objects.indexOf(customShape);
            LocalFileObject customShapeIcon = icons.get(customShapeIdx);            
            String customShapeIconEncode = customShapeIcon.getName() + ";/;" + "-" + ";/;" + DatatypeConverter.printBase64Binary(customShapeIcon.getFile());
                        
            HashMap<String, Object> attributesToUpdate = new HashMap<>();
            attributesToUpdate.put(Constants.PROPERTY_NAME, customShape.getName());
            attributesToUpdate.put(Constants.PROPERTY_ICON, customShapeIconEncode);

            if(!CommunicationsStub.getInstance().updateObject(localCustomShape.getClassName(), 
                    localCustomShape.getId(), attributesToUpdate)) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
        
        for (LocalObjectLight customShape : customShapesToImport) {
            LocalObjectListItem localCustomShape = localMap.get(customShape);
            
            replaceCustomShapes(customShape);
            
            byte[] viewStructure = getViewAsXML(customShape);
                        
            setView(localCustomShape, viewStructure);
        }
    }
    
    private void setView(LocalObjectListItem listItem, byte[] viewStructure) {
        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(listItem.getId(), listItem.getClassName());
        if (relatedViews != null) {
            LocalObjectView relatedView = null;

            if (!relatedViews.isEmpty()) {
                relatedView = CommunicationsStub.getInstance().getListTypeItemRelatedView(listItem.getId(), listItem.getClassName(), relatedViews.get(0).getId());

                if (relatedView == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return;
                }
            }
            if (relatedView == null) {
                long newRelatedViewId = CommunicationsStub.getInstance().createListTypeItemRelatedView(
                    listItem.getId(), listItem.getClassName(), "DeviceLayoutView", null, null, viewStructure, null); //NOI18N

                if (newRelatedViewId == -1) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            } else {
                if (!CommunicationsStub.getInstance().updateListTypeItemRelatedView(
                    listItem.getId(), listItem.getClassName(), relatedView.getId(), null, null, viewStructure, null)) {

                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                }
            }
        } else {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
    
    private byte[] getViewAsXML(LocalObjectLight customShape) {
        int customShapeIdx = objects.indexOf(customShape);
        String layoutStructure = layouts.get(customShapeIdx);
        
        String viewStructure = "<view version=\"1.1\">" + layoutStructure + "</view>";
        
        return viewStructure.getBytes();
        // UTF-8
    }
    
    private LocalObjectListItem getLocalCustomShape(LocalObjectLight customShape) {
        List<LocalObjectListItem> localCustomShapes = CommunicationsStub.getInstance().getList(Constants.CLASS_CUSTOMSHAPE, false, true);
        
        if (localCustomShapes == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        }
        
        for (LocalObjectListItem localCustomShape : localCustomShapes) {
            if (localCustomShape.getName().equals(customShape.getName()))
                return localCustomShape;
        }
        return null;
    }
    
    public void getObjects() {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
            
            QName tagCustomShape = new QName("customShape"); //NOI18N
            QName tagIcon = new QName("icon"); //NOI18N
            QName tagDevice = new QName("device"); //NOI18N            
            
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagCustomShape)) {
                        String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                        String className = reader.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                        String name = reader.getAttributeValue(null, Constants.PROPERTY_NAME);
                        
                        objects.add(new LocalObjectLight(id, name, className));
                    }
                    if (reader.getName().equals(tagIcon)) {
                        String strIcon = reader.getElementText();
                        icons.add(new LocalFileObject(0l, "-", new Date().getTime(), "", new byte[0]));
                    }
                    if (reader.getName().equals(tagDevice)) {
                        String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                        String className = reader.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                        String name = reader.getAttributeValue(null, Constants.PROPERTY_NAME);
                        
                        objects.add(new LocalObjectLight(id, name, className));
                    }
                }
                reader.close();
            }
        } catch (XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }
}
