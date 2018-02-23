/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.util.dynamicname;

import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * Splits a given list of ports into side A and B in order to relate mirror ports
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MirrorPortsPairingUtil {
    
    /**
     * The created mirror ports
     */
    private List<StringPair> ports;
    /**
     * The class of the ports created
     */
    private String objectClassName;

    public MirrorPortsPairingUtil(List<StringPair> ports, String className) {
        this.ports = ports;
        this.objectClassName = className;
    }
    
    public void mirrorProts() throws InvalidArgumentException{
        List<Long> endPointsA = new ArrayList();
        List<Long> endPointsB = new ArrayList();
        
        if(ports.size() % 2 == 0){
            for (int i=0; i < ports.size(); i++) {
                StringPair endPointA = ports.get(i);
                if(endPointA != null){
                    for (int j=i+1; j < ports.size(); j++) {
                        StringPair endPointB = ports.get(j);
                        if(endPointB != null && !(endPointB.getKey().equals(endPointA.getKey())) ){
                            if(matchMirrorPortsNames(endPointA.getValue(), endPointB.getValue())){
                                endPointsA.add(Long.valueOf(endPointA.getKey()));
                                endPointsB.add(Long.valueOf(ports.get(j).getKey()));
                                ports.set(j, null);
                                ports.set(i, null);
                                break;
                            }
                        }
                    }//end for
                }
            }//end for
            if(endPointsA.size() != endPointsB.size())
                throw new InvalidArgumentException("The number of created mirror ports is odd");
            else{
                try {
                    BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
                    
                    for (int i=0; i < endPointsA.size(); i++) 
                        bem.createSpecialRelationship(objectClassName, endPointsA.get(i), objectClassName, endPointsB.get(i), "mirror", true); //NOI18N
                
                } catch (ObjectNotFoundException | OperationNotPermittedException | MetadataObjectNotFoundException ex) {
                    throw new InvalidArgumentException("The list of created ports could not be mirrored");
                }
            }
        }
        else
            throw new InvalidArgumentException("The number of created mirror ports is odd");
    }
    
    
    private boolean matchMirrorPortsNames(String back, String front){
        back = back.toLowerCase();
        front = front.toLowerCase();
        String frontNumericPart = "";
        for (int i=1; i < front.length(); i++){
            if(isNumeric(front.substring(i-1, i)))
                frontNumericPart += front.substring(i-1,i);
        }
        String backNumericPart = "";
        
        for (int i=1; i < back.length(); i++){
            if(isNumeric(back.substring(i-1, i)))
                backNumericPart += back.substring(i-1, i);
        }

        return backNumericPart.equals(frontNumericPart);
    }
    
    private static boolean isNumeric(String s) {  
        return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
    } 
    
}
