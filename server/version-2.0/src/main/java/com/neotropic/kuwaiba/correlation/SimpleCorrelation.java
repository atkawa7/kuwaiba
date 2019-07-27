/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.interfaces.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.ServiceLevelCorrelatedInformation;

/**
 * This is a temporary implementation that provides methods to find the services affected when 
 * a given device/interface is alarmed
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SimpleCorrelation {
    public static AssetLevelCorrelatedInformation servicesInDevice(String deviceName, BusinessEntityManager bem) 
            throws MetadataObjectNotFoundException, ServerSideException, InvalidArgumentException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        List<BusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new ServerSideException(String.format("No resource with name %s could be found", deviceName));

        List<RemoteObjectLight> rawServices = new ArrayList<>();
        for (BusinessObject matchedCommunicationsElement : matchedCommunicationsElements) {

            //First the directly related
            List<BusinessObjectLight> directlyAssociatedServices = 
                    bem.getSpecialAttribute(matchedCommunicationsElement.getClassName(), matchedCommunicationsElement.getId(), "uses");

            for (BusinessObjectLight directlyAssociatedService : directlyAssociatedServices)
                rawServices.add(new RemoteObjectLight(directlyAssociatedService));

            //Now those associated to the ports
            List<BusinessObjectLight> portsInRouter = 
                    bem.getChildrenOfClassLightRecursive(matchedCommunicationsElement.getId(), matchedCommunicationsElement.getClassName(), "GenericPort", -1); //NOI18N

            for (BusinessObjectLight portInRouter : portsInRouter) {
                List<BusinessObjectLight> rawServicesInPort = bem.getSpecialAttribute(portInRouter.getClassName(), portInRouter.getId(), "uses");
                for (BusinessObjectLight serviceInPort : rawServicesInPort) {
                    RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPort);
                    if (!rawServices.contains(newRemoteService))
                        rawServices.add(newRemoteService);
                }

                List<BusinessObjectLight> physicalPath = bem.getPhysicalPath(portInRouter.getClassName(), portInRouter.getId());
                if (physicalPath.size() > 1) {
                    List<BusinessObjectLight> rawServicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                    for (BusinessObjectLight serviceInPhysicalConnection : rawServicesInPhysicalConnection) {
                        RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPhysicalConnection);
                        if (!rawServices.contains(newRemoteService))
                            rawServices.add(newRemoteService);
                    }
                }
            }
        }
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customers
        for (RemoteObjectLight rawService : rawServices) {
            BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
            if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList());
 
                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

        return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedCommunicationsElements), serviceLevelCorrelatedInformation);
    }

    public static AssetLevelCorrelatedInformation servicesInSlotOrBoard(String deviceName, String childName, BusinessEntityManager bem, MetadataEntityManager mem) throws MetadataObjectNotFoundException, InvalidArgumentException, ServerSideException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        List<BusinessObject> matchedCommunicationsElements = bem.getObjectsWithFilter("GenericCommunicationsElement", "name", deviceName);
                        
        if (matchedCommunicationsElements.isEmpty())
            throw new ServerSideException(String.format("No resource with name %s could be found", deviceName));
        
        if (matchedCommunicationsElements.size() > 1)
            throw new ServerSideException(String.format("There is more than one network element with name %s", deviceName));
        
        List<BusinessObject> fullPortsInSlot = new ArrayList<>();

        List<BusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.
                                                                    get(0).getClassName(), matchedCommunicationsElements.get(0).getId(), -1);
            
        for (BusinessObjectLight deviceChild : deviceChildren) {
            if (childName.equals(deviceChild.getName())) {
                List<BusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
                
                for (BusinessObjectLight portInSlot : portsInSlot) {
                    if (mem.isSubclassOf("GenericPort", portInSlot.getClassName())) 
                        fullPortsInSlot.add(bem.getObject(portInSlot.getClassName(), portInSlot.getId()));
                }
                return servicesInPorts(fullPortsInSlot, bem);
            }
        }  
        
        throw new ServerSideException("No slot or port with name " + childName + " was found in device " + deviceName);
    }
    
    public static AssetLevelCorrelatedInformation servicesInPorts(List<BusinessObject> ports, BusinessEntityManager bem) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        List<RemoteObjectLight> rawServices = new ArrayList<>();
        
        for (BusinessObject port : ports) {
            System.out.println("Port: " + port);
            List<BusinessObjectLight> servicesInPort = bem.getSpecialAttribute(port.getClassName(), port.getId(), "uses");
            for (BusinessObjectLight serviceInPort : servicesInPort)
                rawServices.add(new RemoteObjectLight(serviceInPort));

            List<BusinessObjectLight> physicalPath = bem.getPhysicalPath(port.getClassName(), port.getId());
            if (physicalPath.size() > 1) {
                List<BusinessObjectLight> servicesInPhysicalConnection = bem.getSpecialAttribute(physicalPath.get(1).getClassName(), physicalPath.get(1).getId(), "uses");
                for (BusinessObjectLight serviceInPhysicalConnection : servicesInPhysicalConnection) {
                    RemoteObjectLight newRemoteService = new RemoteObjectLight(serviceInPhysicalConnection);
                    if (!rawServices.contains(newRemoteService))
                        rawServices.add(newRemoteService);
                }
            }
        }
        
        List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
        HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

        //Now we organize the rawServices by customers
        for (RemoteObjectLight rawService : rawServices) {
            BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
            if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                if (!rawCorrelatedInformation.containsKey(customer))
                    rawCorrelatedInformation.put(customer, new ArrayList<>());

                rawCorrelatedInformation.get(customer).add(rawService);
            }
        }

        for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
            serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

        return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(ports), serviceLevelCorrelatedInformation);
    }
}
