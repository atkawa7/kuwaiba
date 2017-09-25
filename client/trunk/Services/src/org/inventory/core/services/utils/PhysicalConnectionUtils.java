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
 */
package org.inventory.core.services.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;

/**
 * Utilities for connection wizards
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class PhysicalConnectionUtils {
    
    /**
     * Retrieves the existing containers between two given nodes 
     * @param specialRelationshipsA relations of the node A
     * @param specialRelationshipsB relations of the node B
     * @return a list with the common wire containers between the two nodes
     */
    public static List<LocalObjectLight> checkForExistingContainers(
            HashMap<String, LocalObjectLight[]> specialRelationshipsA, 
            HashMap<String, LocalObjectLight[]> specialRelationshipsB)
    {
        List<LocalObjectLight> wireContainersListA = new  ArrayList<>();
        List<LocalObjectLight> existintWireContainersList = new  ArrayList<>();    
        
        if(specialRelationshipsA.get("endpointA") != null){
            for(LocalObjectLight connection : specialRelationshipsA.get("endpointA")){
                if(connection.getClassName().equals(Constants.CLASS_WIRECONTAINER))
                    wireContainersListA.add(connection);
            }
        }

        if(specialRelationshipsA.get("endpointB") != null){
            for(LocalObjectLight connection : specialRelationshipsA.get("endpointB")){
                if(connection.getClassName().equals(Constants.CLASS_WIRECONTAINER))
                    wireContainersListA.add(connection);
            }
        }

        if(specialRelationshipsB.get("endpointA") != null){
            for(LocalObjectLight connection : specialRelationshipsB.get("endpointA")){
                if(connection.getClassName().equals(Constants.CLASS_WIRECONTAINER)){
                    if(wireContainersListA.contains(connection))
                        existintWireContainersList.add(connection);
                }
            }
        }

        if(specialRelationshipsB.get("endpointB") != null){
            for(LocalObjectLight connection : specialRelationshipsB.get("endpointB")){
                if(connection.getClassName().equals(Constants.CLASS_WIRECONTAINER)){
                    if(wireContainersListA.contains(connection))
                        existintWireContainersList.add(connection);
                }
            }
        }
        return existintWireContainersList;
    }
}
