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

package com.neotropic.kuwaiba.correlation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.business.ServiceLevelCorrelatedInformation;

/**
 * This is a temporary implementation that provides methods to find the services affected when 
 * a given device/interface is alarmed
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SimpleCorrelation {
    public static AssetLevelCorrelatedInformation servicesInDevice(String deviceName, BusinessEntityManager bem) 
            throws MetadataObjectNotFoundException, ServerSideException, InvalidArgumentException, ObjectNotFoundException, ApplicationObjectNotFoundException {
        List<RemoteBusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new ServerSideException(String.format("No resource with name %s could be found", deviceName));

        List<RemoteObjectLight> rawServices = new ArrayList<>();
        for (RemoteBusinessObject matchedCommunicationsElement : matchedCommunicationsElements) {

            //First the directly related
            List<RemoteBusinessObjectLight> directlyAssociatedServices = 
                    bem.getSpecialAttribute(matchedCommunicationsElement.getClassName(), matchedCommunicationsElement.getId(), "uses");

            for (RemoteBusinessObjectLight directlyAssociatedService : directlyAssociatedServices)
                rawServices.add(new RemoteObjectLight(directlyAssociatedService));

            //Now those associated to the ports
            List<RemoteBusinessObjectLight> portsInRouter = 
                    bem.getChildrenOfClassLightRecursive(matchedCommunicationsElement.getId(), matchedCommunicationsElement.getClassName(), "GenericPort", -1); //NOI18N

            for (RemoteBusinessObjectLight portInRouter : portsInRouter) {
                List<RemoteBusinessObjectLight> rawServicesInPort = bem.getSpecialAttribute(portInRouter.getClassName(), portInRouter.getId(), "uses");
                for (RemoteBusinessObjectLight serviceInPort : rawServicesInPort) {
                    RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPort);
                    if (!rawServices.contains(newRemoteService))
                        rawServices.add(newRemoteService);
                }

                List<RemoteBusinessObjectLight> physicalPath = bem.getPhysicalPath(portInRouter.getClassName(), portInRouter.getId());
                if (physicalPath.size() > 1) {
                    List<RemoteBusinessObjectLight> rawServicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                    for (RemoteBusinessObjectLight serviceInPhysicalConnection : rawServicesInPhysicalConnection) {
                        RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPhysicalConnection);
                        if (!rawServices.contains(newRemoteService))
                            rawServices.add(newRemoteService);
                    }
                }
            }
        }
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<RemoteBusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customers
        for (RemoteObjectLight rawService : rawServices) {
            RemoteBusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getOid(), Constants.CLASS_GENERICCUSTOMER);
            if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList<RemoteObjectLight>());
 
                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        for (RemoteBusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

        return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedCommunicationsElements), serviceLevelCorrelatedInformation);
    }

    public static AssetLevelCorrelatedInformation servicesInSlotOrBoard(String deviceName, String childName, BusinessEntityManager bem, MetadataEntityManager mem) throws MetadataObjectNotFoundException, InvalidArgumentException, ServerSideException, ObjectNotFoundException, ApplicationObjectNotFoundException {
        List<RemoteBusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new ServerSideException(String.format("No resource with name %s could be found", deviceName));
        
        if (matchedCommunicationsElements.size() > 1)
            throw new ServerSideException(String.format("There is more than one network element with name %s", deviceName));
        
        List<RemoteBusinessObject> fullPortsInSlot = new ArrayList<>();

        List<RemoteBusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.
                                                                    get(0).getClassName(), matchedCommunicationsElements.get(0).getId(), -1);
            
        for (RemoteBusinessObjectLight deviceChild : deviceChildren) {
            System.out.println("Seraching " + childName + " in " + deviceChild);
            if (childName.equals(deviceChild.getName())) {
                
                List<RemoteBusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
                
                for (RemoteBusinessObjectLight portInSlot : portsInSlot) {
                    if (mem.isSubClass("GenericPort", portInSlot.getClassName())) 
                        fullPortsInSlot.add(bem.getObject(portInSlot.getId()));
                }
                return servicesInPorts(fullPortsInSlot, bem);
            }
        }  
        
        throw new ServerSideException("No slot or port with name " + childName + " was found in device " + deviceName);
    }
    
    public static AssetLevelCorrelatedInformation servicesInPorts(List<RemoteBusinessObject> ports, BusinessEntityManager bem) throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        List<RemoteObjectLight> rawServices = new ArrayList<>();
        
        for (RemoteBusinessObject port : ports) {
            System.out.println("Port: " + port);
            List<RemoteBusinessObjectLight> servicesInPort = bem.getSpecialAttribute(port.getClassName(), port.getId(), "uses");
            for (RemoteBusinessObjectLight serviceInPort : servicesInPort)
                rawServices.add(new RemoteObjectLight(serviceInPort));

            List<RemoteBusinessObjectLight> physicalPath = bem.getPhysicalPath(port.getClassName(), port.getId());
            if (physicalPath.size() > 1) {
                List<RemoteBusinessObjectLight> servicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                for (RemoteBusinessObjectLight serviceInPhysicalConnection : servicesInPhysicalConnection) {
                    RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPhysicalConnection);
                    if (!rawServices.contains(newRemoteService))
                        rawServices.add(newRemoteService);
                }
            }
        }
        
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<RemoteBusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customers
        for (RemoteObjectLight rawService : rawServices) {
            RemoteBusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getOid(), Constants.CLASS_GENERICCUSTOMER);
            if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList<RemoteObjectLight>());

                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        for (RemoteBusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

        return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(ports), serviceLevelCorrelatedInformation);
    }
}
