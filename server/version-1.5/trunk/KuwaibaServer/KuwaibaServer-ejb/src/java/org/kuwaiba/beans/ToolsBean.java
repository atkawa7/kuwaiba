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

package org.kuwaiba.beans;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.util.ChangeDescriptor;

/**
 * Simple bean used to perform administrative tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class ToolsBean implements ToolsBeanRemote {
        
    @Override
    public void resetAdmin()  throws ServerSideException, NotAuthorizedException {
        
        try {
            PersistenceService.getInstance().getApplicationEntityManager().setUserProperties(UserProfile.DEFAULT_ADMIN,null, "kuwaiba", null, null, 1, UserProfile.USER_TYPE_GUI);
        }catch(ApplicationObjectNotFoundException ex){ //If the user does not exist the database might not be initialized, so display an error
            throw new ServerSideException("The user \"admin\" does not exist. Make sure you are using a database with a default schema.");
            
        } catch(InvalidArgumentException | IllegalStateException ex){
            throw new ServerSideException(ex.getMessage());
        }
        
    }
    
    @Override
    public void loadDataModel(byte[] dataModelFileAsByteArray) throws ServerSideException {
        try{
            PersistenceService.getInstance().getDataModelLoader().loadDataModel(dataModelFileAsByteArray);
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }        
    }

    @Override
    public String[] executePatches(String[] patches) {
        String[] results = new String[patches.length];
        
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
                    
        if (aem == null) {
            results[0] = "The Persistence Service doesn't seem to be running. Passwords could no be reset.";
            return results;
        }
        
        for (int i = 0; i < patches.length; i++) {
            switch (patches[i]) {
                //<editor-fold desc="Implementation for version 1.0 -> 1.1" defaultstate="collapsed">
                case "1": 
                    try {
                        //Reset passwords
                        List<UserProfile> users = aem.getUsers();
                        for (UserProfile user : users)
                            aem.setUserProperties(user.getId(), null, user.getUserName(), //Sets the new password to the "username" value 
                                    null, null, -1, UserProfile.USER_TYPE_GUI);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_MASSIVE_UPDATE_APPLICATION_OBJECT, 
                                new ChangeDescriptor("password", "", "", "Passwords reset due to security patch"));
                        
                    } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
                break;
                
                case "2": //Migrate hard-coded reports
                    
                    if (bem == null || mem == null) {
                        results[i] = "The Persistence Service doesn't seem to be running. The reports won't be migrated.";
                        continue;
                    }
                    
                    try {
                        //First, we rename the class GenericMPLService to GenericMPLSService if it hasn't been renamed yet.
                        ClassMetadata classToRename = mem.getClass("GenericMPLService");
                        ClassMetadata fixedClass = new ClassMetadata();
                        fixedClass.setId(classToRename.getId());
                        fixedClass.setName("GenericMPLSService");
                        mem.setClassProperties(fixedClass);
                    } catch (InvalidArgumentException | ApplicationObjectNotFoundException | ObjectNotFoundException | MetadataObjectNotFoundException ex) {
                        //Do nothing. The class probably was already renamed
                    }
            
                    
                    String template = "/**\n" +
                                        "* Wrapper for the original, hard-coded report that %s\n" +
                                        "* Neotropic SAS - version 1.1\n" +
                                        "* Parameters: None\n" +
                                        "*/\n" +
                                        "import com.neotropic.kuwaiba.modules.reporting.defaults.DefaultReports;\n" +
                                        "import com.neotropic.kuwaiba.modules.reporting.html.*;\n" +
                                        "\n" +
                                        "try {\n" +
                                        "    return defaultReports.%s;\n" +
                                        "} catch (Exception ex) {\n" +
                                        "    def htmlReport = new HTMLReport(\"%s\", \"Neotropic SAS\", \"1.1\");\n" +
                                        "    htmlReport.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());\n" +
                                        "    htmlReport.getComponents().add(new HTMLMessage(null, \"error\", ex.getMessage()));\n" +
                                        "    return htmlReport;\n" +
                                        "}";
             
                    try {
                        bem.createClassLevelReport("Rack", "Rack Usage", "Shows the rack usage and the elements contained within",
                                String.format(template, "Shows the rack usage and the elements contained within", "buildRackUsageReport(objectId)",
                                        "Rack Usage"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericDistributionFrame", "Frame Details", "Shows the distribution frame usage",
                                String.format(template, "Shows the distribution frame usage", "buildDistributionFrameDetailReport(objectClassName, objectId)",
                                        "Frame Details"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericSDHTransportLink", "TransportLink Structure", "Shows the TransportLink Structure",
                                String.format(template, "Shows the TransportLink Structure", "buildTransportLinkUsageReport(objectClassName, objectId)",
                                        "TransportLink Structure"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericSDHHighOrderTributaryLink", "TributaryLink Resources", "Shows the resources used by a TributaryLink",
                                String.format(template, "Shows the resources used by a TributaryLink", "buildHighOrderTributaryLinkDetailReport(objectClassName, objectId)",
                                        "TributaryLink Resources"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericSDHLowOrderTributaryLink", "TributaryLink Resources", "Shows the resources used by a TributaryLink",
                                String.format(template, "Shows the resources used by a TributaryLink", "buildLowOrderTributaryLinkDetailReport(objectClassName, objectId)",
                                        "TributaryLink Resources"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericSubnet", "Subnet Details", "Shows the IPs created in that subnet and some of their attributes",
                                String.format(template, "Shows the IPs created in that subnet and some of their attributes", "subnetUsageReport(objectClassName, objectId)",
                                        "Subnet details"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericService", "Service Details", "Shows the resources used by a given service",
                                String.format(template, "Shows the resources used by a given service", "buildServiceResourcesReport(objectClassName, objectId)",
                                        "Service Details"), RemoteReportLight.TYPE_HTML, true);

                        bem.createClassLevelReport("GenericLocation", "Network Equipment", "A detailed list of all the network equipment in a particular location",
                                String.format(template, "A detailed list of all the network equipment in a particular location", "buildNetworkEquipmentInLocationReport(objectClassName, objectId)",
                                        "Network Equipment"), RemoteReportLight.TYPE_HTML, true);
                        
                        bem.createClassLevelReport("Country", "Network Equipment", "A detailed list of all the network equipment in a particular location",
                                String.format(template, "A detailed list of all the network equipment in a particular location", "buildNetworkEquipmentInLocationReport(objectClassName, objectId)",
                                        "Network Equipment"), RemoteReportLight.TYPE_HTML, true);
                        
                        bem.createClassLevelReport("Continent", "Network Equipment", "A detailed list of all the network equipment in a particular location",
                                String.format(template, "A detailed list of all the network equipment in a particular location", "buildNetworkEquipmentInLocationReport(objectClassName, objectId)",
                                        "Network Equipment"), RemoteReportLight.TYPE_HTML, true);
                        
                        bem.createClassLevelReport("SupportContract", "Contract Status", "Shows the status of the support contracts in the inventory",
                                String.format(template, "Shows the status of the support contracts in the inventory", "buildContractStatusReport(objectId)",
                                        "Contract Status"), RemoteReportLight.TYPE_HTML, true);
                        
                        bem.createClassLevelReport("GenericMPLSService", "MPLS Service Details", "Special resources report for MPLS services",
                                String.format(template, "Special resources report for MPLS services", "buildMPLSServiceReport(objectClassName, objectId)",
                                        "MPLS Service Details"), RemoteReportLight.TYPE_HTML, true);
                        
                        bem.createClassLevelReport("BridgeDomainInterface", "Configuration Details", "Logical configuration of some MPLS-related entities",
                                String.format(template, "Logical configuration of some MPLS-related entities", "buildLogicalConfigurationInterfacesReport(objectClassName, objectId)",
                                        "Configuration Details"), RemoteReportLight.TYPE_HTML, true);
                                                
                        bem.createClassLevelReport("VRFInstance", "Configuration Details", "Logical configuration of some MPLS-related entities",
                                String.format(template, "Logical configuration of some MPLS-related entities", "buildLogicalConfigurationInterfacesReport(objectClassName, objectId)",
                                        "Configuration Details"), RemoteReportLight.TYPE_HTML, true);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                                new ChangeDescriptor("reports", "", "", "Hard-coded reports migrated"));
                        
                    } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
            
                break;
                //</editor-fold>
                case "3": //Move all users without group to a default group. Required for Kuwaiba 1.5
                    try {
                        List<UserProfile> allUsers = aem.getUsers();
                        List<Long> usersToMove = new ArrayList<>();

                        for (UserProfile user : allUsers) {
                            if (aem.getGroupsForUser(user.getId()).isEmpty())
                                usersToMove.add(user.getId());
                        }

                        if (!usersToMove.isEmpty()) {
                            SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd yyyy");
                            String defaultGroupName = "Default Group " + formatter.format(Calendar.getInstance().getTime());

                            aem.createGroup(defaultGroupName, "Default group created by the Migration Wizard", usersToMove);

                            aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                                    new ChangeDescriptor("reports", "", "", usersToMove.size() + " groups moved to " + defaultGroupName));
                        }
                    } catch (InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
                break;
                case "4": //This action adds the abstract classes GenericProject, GenericActivity and some project and activities subclasses for the Projects Module.
                    try {
                        ClassMetadata cm = new ClassMetadata();
                        cm.setName("GenericProject"); //NOI18N
                        cm.setDisplayName("");
                        cm.setDescription("");
                        cm.setParentClassName("AdministrativeItem"); //NOI18N
                        cm.setAbstract(true);
                        cm.setColor(0);
                        cm.setCountable(true);
                        cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
                        cm.setIcon(null);
                        cm.setSmallIcon(null);
                        cm.setCustom(true);
                        cm.setViewable(true);
                        cm.setInDesign(false);
                        
                        long genericProjectId = mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        AttributeMetadata attributeMetadata = new AttributeMetadata();
                        attributeMetadata.setName("notes"); //NOI18N
                        attributeMetadata.setDisplayName("notes"); 
                        attributeMetadata.setDescription("");
                        attributeMetadata.setReadOnly(false);
                        attributeMetadata.setType("String"); //NOI18N
                        attributeMetadata.setUnique(false);
                        attributeMetadata.setVisible(true);
                        attributeMetadata.setNoCopy(false);                        
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("projectManager"); //NOI18N
                        attributeMetadata.setDisplayName("projectManager");
                        attributeMetadata.setType("Employee"); //NOI18N
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("startDate"); //NOI18N
                        attributeMetadata.setDisplayName("startDate");
                        attributeMetadata.setType("Date"); //NOI18N
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("status"); //NOI18N
                        attributeMetadata.setDisplayName("status");
                        attributeMetadata.setType("ProjectStatusType"); //NOI18N
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                            String.format("Added attributes to class %s", "GenericProject"));
                        
                        cm.setName("GenericActivity"); //NOI18N
                        cm.setParentClassName("AdministrativeItem"); //NOI18N
                        cm.setAbstract(true);
                        long genericActivityId = mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("ActivityType"); //NOI18N
                        cm.setParentClassName("GenericType"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("ProjectStatusType"); //NOI18N
                        cm.setParentClassName("GenericType"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("ActivityStatusType"); //NOI18N
                        cm.setParentClassName("GenericType"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                                                
                        attributeMetadata.setName("notes"); //NOI18N
                        attributeMetadata.setDisplayName("notes");
                        attributeMetadata.setType("String"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("startDate"); //NOI18N
                        attributeMetadata.setDisplayName("startDate");
                        attributeMetadata.setType("Date"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("endDate"); //NOI18N
                        attributeMetadata.setDisplayName("endDate");
                        attributeMetadata.setType("Date"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("lastUpdate"); //NOI18N
                        attributeMetadata.setDisplayName("lastUpdate");
                        attributeMetadata.setType("Date"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("status"); //NOI18N
                        attributeMetadata.setDisplayName("status");
                        attributeMetadata.setType("ActivityStatusType"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("duration"); //NOI18N
                        attributeMetadata.setDisplayName("duration");
                        attributeMetadata.setType("Float"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("cost"); //NOI18N
                        attributeMetadata.setDisplayName("cost");
                        attributeMetadata.setType("Float"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("owner"); //NOI18N
                        attributeMetadata.setDisplayName("owner");
                        attributeMetadata.setType("Employee"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("risk"); //NOI18N
                        attributeMetadata.setDisplayName("risk");
                        attributeMetadata.setType("Integer"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("activityType"); //NOI18N
                        attributeMetadata.setDisplayName("activityType");
                        attributeMetadata.setType("ActivityType"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("sequecing"); //NOI18N
                        attributeMetadata.setDisplayName("sequecing");
                        attributeMetadata.setType("ActivityType"); //NOI18N
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                            String.format("Added attributes to class %s", "Generic Activity"));
                        
                        cm.setName("GeneralPurposeActivity"); //NOI18N
                        cm.setParentClassName("GenericActivity"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("PlanningActivity"); //NOI18N
                        cm.setParentClassName("GenericActivity"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("RollOutActivity"); //NOI18N
                        cm.setParentClassName("GenericActivity"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("DesignActivity"); //NOI18N
                        cm.setParentClassName("GenericActivity"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("AuditActivity"); //NOI18N
                        cm.setParentClassName("GenericActivity"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                        cm.setName("GeneralPurposeProject"); //NOI18N
                        cm.setParentClassName("GenericProject"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class ", cm.getName()));
                        
                        cm.setName("NetworkProject"); //NOI18N
                        cm.setParentClassName("GenericProject"); //NOI18N
                        cm.setAbstract(false);
                        cm.setId(mem.createClass(cm));
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));
                        
                    } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
                break;
                case "5":
                {
                    try {
                        AttributeMetadata attributeMetadata = new AttributeMetadata();
                        attributeMetadata.setName("rackUnitsNumberingDescending"); //NOI18N
                        attributeMetadata.setDisplayName("rackUnitsNumberingDescending"); //NOI18N
                        attributeMetadata.setDescription("");
                        attributeMetadata.setReadOnly(false);
                        attributeMetadata.setType("Boolean"); //NOI18N
                        attributeMetadata.setUnique(false);
                        attributeMetadata.setVisible(true);
                        attributeMetadata.setNoCopy(false); 
                        
                        mem.createAttribute("Rack", attributeMetadata); //NOI18N
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Added attributes to class %s", "Rack")); //NOI18N
                    } catch (MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
                }
                break;
                case "6":
                {
                    try {
                        ClassMetadata cm = new ClassMetadata();
                        cm.setName("LinkConnectorType"); //NOI18N
                        cm.setDisplayName("");
                        cm.setDescription("");
                        cm.setParentClassName("GenericType"); //NOI18N
                        cm.setAbstract(false);
                        cm.setColor(0);
                        cm.setCountable(false);
                        cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
                        cm.setIcon(null);
                        cm.setSmallIcon(null);
                        cm.setCustom(false);
                        cm.setViewable(true);
                        cm.setInDesign(false);

                        mem.createClass(cm);

                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));

                        cm.setName("PortConnectorType"); //NOI18N
                        cm.setParentClassName("GenericType"); //NOI18N

                        mem.createClass(cm);

                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                            String.format("Created class %s", cm.getName()));

                        AttributeMetadata attributeMetadata = new AttributeMetadata();
                        attributeMetadata.setName("connectorType"); //NOI18N
                        attributeMetadata.setDisplayName("connectorType"); //NOI18N
                        attributeMetadata.setDescription("");
                        attributeMetadata.setReadOnly(false);
                        attributeMetadata.setType("LinkConnectorType"); //NOI18N
                        attributeMetadata.setUnique(false);
                        attributeMetadata.setVisible(true);
                        attributeMetadata.setNoCopy(false);                    

                        mem.createAttribute("GenericPhysicalLink", attributeMetadata); //NOI18N

                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                            String.format("Added attributes to class %s", "GenericPhysicalLink")); //NOI18N

                        attributeMetadata.setType("PortConnectorType"); //NOI18N

                        mem.createAttribute("GenericPort", attributeMetadata); //NOI18N

                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, 
                            ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                            String.format("Added attributes to class %s", "GenericPort")); //NOI18N


                    } catch (DatabaseException | MetadataObjectNotFoundException | InvalidArgumentException | ApplicationObjectNotFoundException ex) {
                        results[i] = ex.getMessage();
                    }
                }
                break;
                default:
                    results[i] = String.format("Invalid patch id %s", i);
            }
        }
        return results;
    }
}