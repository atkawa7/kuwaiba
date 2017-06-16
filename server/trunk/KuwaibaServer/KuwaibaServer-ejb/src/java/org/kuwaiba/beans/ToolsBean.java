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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    public void resetAdmin()  throws ServerSideException, NotAuthorizedException{
        
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
                case "4": // Update data model: This action add the abstract classes GenericProject, GenericActivity and some project and activities subclasses for the Projects Module.
                    try {
                        ClassMetadata cm = new ClassMetadata();
                        cm.setName("GenericProject");
                        cm.setDisplayName("");
                        cm.setDescription("");
                        cm.setParentClassName("AdministrativeItem");
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
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add GenericProject Class"));
                        
                        AttributeMetadata attributeMetadata = new AttributeMetadata();
                        attributeMetadata.setName("notes");
                        attributeMetadata.setDisplayName("notes");
                        attributeMetadata.setDescription("");
                        attributeMetadata.setReadOnly(false);
                        attributeMetadata.setType("String");
                        attributeMetadata.setUnique(false);
                        attributeMetadata.setVisible(true);
                        attributeMetadata.setNoCopy(false);                        
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("manager");
                        attributeMetadata.setDisplayName("manager");
                        attributeMetadata.setType("String");
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("startDate");
                        attributeMetadata.setDisplayName("startDate");
                        attributeMetadata.setType("Date");
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        attributeMetadata.setName("status");
                        attributeMetadata.setDisplayName("status");
                        attributeMetadata.setType("ProjectStatusType");
                        mem.createAttribute(genericProjectId, attributeMetadata);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                                new ChangeDescriptor("Add attributes", "", "", "Add attributes GenericProject Class"));
                        
                        cm.setName("GenericActivity");
                        cm.setParentClassName("AdministrativeItem");
                        cm.setAbstract(true);
                        long genericActivityId = mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add GenericActivity Class"));
                        
                        cm.setName("ActivityType");
                        cm.setParentClassName("GenericType");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add ActivityType Class"));
                        
                        cm.setName("ProjectStatusType");
                        cm.setParentClassName("GenericType");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add ProjectStatusType Class"));
                        
                        cm.setName("ActivityStatusType");
                        cm.setParentClassName("GenericType");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add ActivityStatusType Class"));
                        
                        
                        
                        attributeMetadata.setName("notes");
                        attributeMetadata.setDisplayName("notes");
                        attributeMetadata.setType("String");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("startDate");
                        attributeMetadata.setDisplayName("startDate");
                        attributeMetadata.setType("Date");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("endDate");
                        attributeMetadata.setDisplayName("endDate");
                        attributeMetadata.setType("Date");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("lastUpdate");
                        attributeMetadata.setDisplayName("lastUpdate");
                        attributeMetadata.setType("Date");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("status");
                        attributeMetadata.setDisplayName("status");
                        attributeMetadata.setType("ActivityStatusType");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("duration");
                        attributeMetadata.setDisplayName("duration");
                        attributeMetadata.setType("Integer");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("cost");
                        attributeMetadata.setDisplayName("cost");
                        attributeMetadata.setType("Float");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("manager");
                        attributeMetadata.setDisplayName("manager");
                        attributeMetadata.setType("String");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("risk");
                        attributeMetadata.setDisplayName("risk");
                        attributeMetadata.setType("Integer");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("activityType");
                        attributeMetadata.setDisplayName("activityType");
                        attributeMetadata.setType("ActivityType");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        attributeMetadata.setName("sequecing");
                        attributeMetadata.setDisplayName("sequecing");
                        attributeMetadata.setType("ActivityType");
                        mem.createAttribute(genericActivityId, attributeMetadata);
                        
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                                new ChangeDescriptor("Add attributes", "", "", "Add attributes GenericActivity Class"));
                        
                        cm.setName("GeneralPurposeActivity");
                        cm.setParentClassName("GenericActivity");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add GeneralPurposeActivity Class"));
                        
                        cm.setName("PlanningActivity");
                        cm.setParentClassName("GenericActivity");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add PlanningActivity Class"));
                        
                        cm.setName("RollOutActivity");
                        cm.setParentClassName("GenericActivity");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add RollOutActivity Class"));
                        
                        cm.setName("DesignActivity");
                        cm.setParentClassName("GenericActivity");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add DesignActivity Class"));
                        
                        cm.setName("AuditActivity");
                        cm.setParentClassName("GenericActivity");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add AuditActivity Class"));
                        
                        cm.setName("GeneralPurposeProject");
                        cm.setParentClassName("GenericProject");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add GeneralPurposeProject Class"));
                        
                        cm.setName("NetworkProject");
                        cm.setParentClassName("GenericProject");
                        cm.setAbstract(false);
                        mem.createClass(cm);
                        aem.createGeneralActivityLogEntry(UserProfile.DEFAULT_ADMIN, ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                                new ChangeDescriptor("DataBase", "", "", "Add NetworkProject Class"));
                        
                    } catch (DatabaseException ex) {
                        Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (MetadataObjectNotFoundException ex) {
                        Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InvalidArgumentException ex) {
                        Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ApplicationObjectNotFoundException ex) {
                        Logger.getLogger(ToolsBean.class.getName()).log(Level.SEVERE, null, ex);
                 }
                break;
                default:
                    results[i] = String.format("Invalid patch id %s", i);
            }
        }
        return results;
    }
}