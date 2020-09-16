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

import com.neotropic.flow.component.mxgraph.MxGraph;
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
    
    public OspLocationView(BusinessObjectLight cable, BusinessObjectLight device,
        ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem, TranslationService ts) {
        super();
        Objects.requireNonNull(cable);
        Objects.requireNonNull(device);
        setSizeFull();
        setConnectable(true);
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
                                if (colorValue != null)
                                    return new MxTreeLabel(graph, node.getId(), node.getName(), colorValue);
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
            BusinessObjectLight::getId
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
                    return !bem.getSpecialAttribute(port.getClassName(), port.getId(), "endpoitA").isEmpty(); //NOI18N
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"),
                        ex.getLocalizedMessage()
                    ).open();
                }
                return false;
            }
        );
        spliceBox.addCellAddedListener(event -> excecuteLayout());
    }
    
    private void excecuteLayout() {
        setCellsLocked(false);
        executeStackLayout(null, true, 200, 100);
        setCellsLocked(true);
    }
}
