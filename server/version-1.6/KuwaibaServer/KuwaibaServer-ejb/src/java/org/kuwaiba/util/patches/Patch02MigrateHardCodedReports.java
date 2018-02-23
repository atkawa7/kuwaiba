/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.kuwaiba.util.patches;

import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.util.ChangeDescriptor;

/**
 * Creates wrapper dynamic reports for the old hard-coded ones
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Patch02MigrateHardCodedReports extends GenericPatch {
    
    public Patch02MigrateHardCodedReports() {
    }

    @Override
    public String getId() {
        return "2";
    }

    @Override
    public String getTitle() {
        return "Migrate hard-coded reports";
    }

    @Override
    public String getDescription() {
        return "This action creates wrapper dynamic reports for the old hard-coded ones. In future releases, the hard-coded reports will disappear completely. Class GenericMPLService will be renamed to GenericMPLSService as well";
    }

    @Override
    public String getSourceVersion() {
        return "1.1.x";
    }

    @Override
    public String getTargetVersion() {
        return "1.5";
    }
    
    @Override
    public PatchResult executePatch() {
        PatchResult result = new PatchResult();   
        
        BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
        MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
        ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
                
        if (bem == null || mem == null || aem == null) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add("The Persistence Service doesn't seem to be running. The reports won't be migrated.");
            return result;
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
            
            result.setResultType(PatchResult.RESULT_SUCCESS);
        } catch (MetadataObjectNotFoundException | ApplicationObjectNotFoundException ex) {
            result.setResultType(PatchResult.RESULT_ERROR);
            result.getMessages().add(ex.getMessage());
        }
        return result;
    }  
    
    @Override
    public String getMandatory() {
        return "[Mandatory] ";
    }
    
}
