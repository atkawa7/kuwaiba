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

package org.neotropic.kuwaiba.core.apis.integration.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Views (mainly detailed views) are to be registered here, so they can be embedded or added to context menus at will.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ViewRegistry {
    /**
     * The list of registered detailed views.
     */
    private List<AbstractDetailedView> detailedViews;
    /**
     * All registered views grouped by instances of what class are they applicable to.
     */
    private HashMap<String, List<AbstractDetailedView>> viewsByApplicableClass;
    /**
     * All registered views grouped by the module they are provided by.
     */
    private HashMap<String, List<AbstractDetailedView>> viewsByModule;
    /**
     * Reference to the MetadataEntityManager to access the data model cache.
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public ViewRegistry() {
        this.detailedViews = new ArrayList<>();
        this.viewsByApplicableClass = new HashMap<>();
        this.viewsByModule = new HashMap<>();
    }
    
    /**
     * Checks what views are associated to a given inventory class. For example, a rack view 
     * NewCustomer and DeleteCustomer are part of the returned list if <code>filter</code> is
     * GenericCustomer. Note that the difference between this method and {@link #getActionsApplicableToRecursive(java.lang.String) } is 
     * that this method will return the actions whose appliesTo matches exactly with the provided filter, while the latter 
     * might match even subclasses of the appliesTo return value.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractDetailedView> getDetailedViewsApplicableTo(String filter) {
        return this.viewsByApplicableClass.containsKey(filter) ? this.viewsByApplicableClass.get(filter) : new ArrayList<>();
    }
    
    /**
     * Checks what actions are associated to a given inventory class. For example, 
     * NewCustomer and DeleteCustomer are part of the returned list if <code>filter</code> is
     * CorporateCustomer.
     * @param filter The class to be evaluated.
     * @return The actions that can be executed from an instance of the given class or superclass.
     */
    public List<AbstractDetailedView> getDetailedViewsApplicableToRecursive(String filter) {
        return this.detailedViews.stream().filter((anAction) -> {
            try {
                return anAction.appliesTo() == null ? false : mem.isSubclassOf(filter, anAction.appliesTo());
            } catch (MetadataObjectNotFoundException ex) { // No existing (or cached) classes will be ignored
                return false;
            }
        }).collect(Collectors.toList());
    }
    
    
    
    /**
     * Adds a detailed view to the registry. This method also feeds the detailed view map cache structure, which is a hash map which keys are 
     * all the possible super classes the detailed views are applicable to and the keys are the corresponding detailed views.
     * @param moduleId The id of the module this detailed view is provided by. The id is returned by AbstractModule.getId().
     * @param view The action to be added. Duplicated view ids are not allowed.
     */
    public void registerView(String moduleId, AbstractDetailedView view) {
        this.detailedViews.add(view);
        
        if (!this.viewsByModule.containsKey(moduleId))
            this.viewsByModule.put(moduleId, new ArrayList<>());
        this.viewsByModule.get(moduleId).add(view);

        String applicableTo = view.appliesTo() == null ? "" : view.appliesTo(); 
        
        if (!this.viewsByApplicableClass.containsKey(applicableTo))
            this.viewsByApplicableClass.put(applicableTo, new ArrayList<>());
        
        this.viewsByApplicableClass.get(applicableTo).add(view);
    }
    
    /**
     * Returns all registered detailed views.
     * @return All registered detailed views.
     */
    public List<AbstractDetailedView> getAllDetailedViews() {
        return this.detailedViews;
    }

    /**
     * Returns all detailed views registered by a particular module.
     * @param moduleId The id of the module. Usually the strings that comes from calling AbstractModule.getId().
     * @return The list of detailed views, even if none registered for the given module (in that case, an empty array will be returned).
     */
    public List<AbstractDetailedView> getActionsForModule(String moduleId) {
        return this.viewsByModule.containsKey(moduleId) ? this.viewsByModule.get(moduleId) : new ArrayList<>();
    }
}
