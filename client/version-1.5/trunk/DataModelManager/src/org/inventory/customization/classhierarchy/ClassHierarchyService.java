/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;
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
    private final ClassHierarchyScene scene;
    private LocalClassMetadata root;
    private List<LocalClassMetadata> roots;
        
    public ClassHierarchyService(ClassHierarchyScene scene) {
        this.scene = scene;
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
    
    public LocalClassMetadata getRootClass() {
        return root;
    }
            
    private List<LocalClassMetadata> getSubclasses(LocalClassMetadata parent) {
        List<LocalClassMetadata> children = new ArrayList();
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
    


    public void refreshScene(List<LocalClassMetadata> classes) {
        if (classes.remove(root)) {
            scene.clear();
            initClassHierarchy(); // Updating the class hierarchy
            
            scene.render(root);
            updateHierarchyRecursive(root, classes);            
        }
    }
    
    private void findAddedOrRemovedClasses(LocalClassMetadata parent, List<LocalClassMetadata> children, List<LocalClassMetadata> classes) {
        List<LocalClassMetadata> added = new ArrayList();
        List<LocalClassMetadata> removed = new ArrayList();
        // Added
        for (LocalClassMetadata child : children) {
            if (!classes.contains(child))
                added.add(child);
        }
        // Removed
        for (LocalClassMetadata _class : classes) {
            if (parent.getClassName().equals(_class.getParentName())) {
                if (!children.contains(_class))
                    removed.add(_class);
            }                                                
        }
        
        for (LocalClassMetadata _class : added)
            classes.add(_class);
            
        for (LocalClassMetadata _class : removed)
            classes.remove(_class);
    }
    
    private void updateHierarchyRecursive(LocalClassMetadata parent, List<LocalClassMetadata> classes) {
        boolean showSubclasses = false;
        
        for (LocalClassMetadata _class : classes) {
            if (parent.getClassName().equals(_class.getParentName())) {
                showSubclasses = true;
                break;
            }
        }        
            
        if (showSubclasses) {
            List<LocalClassMetadata> subclasses = getSubclasses(parent);
            
            findAddedOrRemovedClasses(root, subclasses, classes);
            
            for (LocalClassMetadata subclass : subclasses)
                classes.remove(subclass);                
            
            addSubclasses(parent, false);
            
            for (LocalClassMetadata subclass : subclasses)
                updateHierarchyRecursive(subclass, classes);
        }
    }
}

