/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
 */

package org.kuwaiba.util.bre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A temporary place to put the business rules while we develop a decent engine
 * Core rules to implement:
 * What objects could be added to what views
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TempBusinessRulesEngine {
    /**
     * Hard-coded (for now) valid mappings for physical connections 
     * (this is, instances of what classes can be connected each other -i.e. 
     * ports with GenericPhysicalLink instances- )
     * They key is the connecting element (say WireContainer) and 
     * the value is a list with the pairs of elements that can be connected
     */
    private HashMap<String, List<String[]>> relationshipMappings;
    private HashMap<String,String[]> possibleChildrenAccordingToModels;
    private HashMap<String, String> subClassOfValidators;

    public TempBusinessRulesEngine() {
        relationshipMappings = new HashMap<>();
        possibleChildrenAccordingToModels = new HashMap<>();
        List<String[]> links = new ArrayList<>();
        links.add(new String[]{"GenericPort", "GenericPort"});
        relationshipMappings.put("GenericPhysicalLink", links);
        
        subClassOfValidators = new HashMap<>();
        subClassOfValidators.put("GenericPhysicalNode", "physicalNode");
        subClassOfValidators.put("ServiceInstance", "serviceInstance");
        subClassOfValidators.put("Subnet", "subnet");
        subClassOfValidators.put("ELANService", "elanservice");
        subClassOfValidators.put("ELINEService", "elineservice");
        subClassOfValidators.put("ETREEService", "etreeservice");
        //Beware! The line below may potentially be creating conflicts with the three lines above, however 
        //the one below seems to cover the most important the use cases, so we will leave it like this while we
        //fine a suitable solution for this temporal rule engine
        subClassOfValidators.put("GenericCommunicationsElement", "communicationsElement");
        //TODO: These validators will be used to enable special actions for certain type of objects
        //Perhaps we should use some other type of solution for this in the future
        subClassOfValidators.put("GenericSDHTransportLink", "sdhTransportLink");
    }

    public HashMap<String, List<String[]>> getMappings(){       
        return relationshipMappings;
    }

    public HashMap<String, String> getSubclassOfValidators(){
        return subClassOfValidators;
    }
    
    public HashMap<String, String[]> getPossibleChildrenAccordingToModels(){
        return possibleChildrenAccordingToModels;
    }
}
