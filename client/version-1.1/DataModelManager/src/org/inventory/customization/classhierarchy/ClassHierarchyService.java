/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.inventory.customization.classhierarchy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.xml.ClassHierarchyReader;
import org.inventory.customization.classhierarchy.scene.ClassHierarchyScene;
import org.inventory.customization.classhierarchy.scene.xml.ClassHierarchyReaderImpl;
import org.netbeans.api.visual.vmd.VMDNodeWidget;

/**
 * Provides the business logic for the related TopComponent
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassHierarchyService {
    private ClassHierarchyScene scene;
    private LocalClassMetadata root;
    private List<LocalClassMetadata> roots;
        
    public ClassHierarchyService() {
        initClassHierarchy();
    }
    
    public String getHierarchyAsString() {
        byte [] hierarchyAsXML = CommunicationsStub.getInstance().getClassHierarchy(true);
        if (hierarchyAsXML != null) 
            return new String(hierarchyAsXML);
        return null;
    }
    
    private void initClassHierarchy() {
        byte [] hierarchyAsXML = CommunicationsStub.getInstance().getClassHierarchy(true);
        
        if (hierarchyAsXML != null) {
            ClassHierarchyReader xmlReader = new ClassHierarchyReaderImpl();
            
            try {
                xmlReader.read(hierarchyAsXML);
                root = ((ClassHierarchyReaderImpl) xmlReader).getRoot();
                roots = ((ClassHierarchyReaderImpl) xmlReader).getRoots();
            }
            catch (Exception e) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, 
                    e.getMessage());
            }
        }
    }
    
    public void setScene(ClassHierarchyScene scene) {
        this.scene = scene;
    }
    
    public void expandClassHierarchy() {
        LocalClassMetadata rootClass = root;
        addSubclasses(rootClass, true);
        scene.reorganizeNodes();
    }
    
    public void collapseClassHierarchy() {
        LocalClassMetadata rootClass = root;
        removeSubclasses(rootClass);
        scene.reorganizeNodes();
    }
    
    public void addRootClass() {
        scene.addRootNodeClass(root);
    }
    
    public LocalClassMetadata getRootClass() {
        return root;
    }
        
    private List<LocalClassMetadata> getSubclasses(LocalClassMetadata parent) {
        List children = new ArrayList();
        for (LocalClassMetadata possibleChild : roots) {
            if (parent.getClassName().equals(possibleChild.getParentName()))
                children.add(possibleChild);
        }
        return children;
    }
    
    public void addSubclasses(LocalClassMetadata parent, boolean recursive) {
        List<LocalClassMetadata> subclasses = getSubclasses(parent);
        scene.createSubHierarchyRecursively(parent, subclasses, recursive);
        scene.revalidate();
    }
    
    public void removeSubclasses(LocalClassMetadata parent) {
        List<LocalClassMetadata> subclasses = getSubclasses(parent);
        for (LocalClassMetadata subclass : subclasses) {
            if (scene.findWidget(subclass) != null) {
                removeSubclasses(subclass);
                scene.removeNodeWithEdges(subclass);
                scene.revalidate();
            }
            else
                return;
        }
    }

    void showAllAttributes() {
        for (LocalClassMetadata node : scene.getNodes()) 
            ((VMDNodeWidget) scene.findWidget(node)).expandWidget();
        scene.reorganizeNodes();
    }
}
