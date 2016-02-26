/*
 * 
 */

package org.kuwaiba.tests.tools.fixtures;

import java.util.Calendar;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;


/**
 * Uploads initial, test information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BulkLoadTester {
        private static Containment c = new Containment();
        private static ListTypes lt = new ListTypes();
        private static int objectCount = 0;
        private static MetadataEntityManager mem;
        private static BusinessEntityManager bem;
        private static ApplicationEntityManager aem;

    public static void main (String[] args){
        
        try{
            PersistenceService persistenceService = PersistenceService.getInstance();
            persistenceService.start();
            mem = persistenceService.getMetadataEntityManager();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();

            System.out.println("Starting at: " + Calendar.getInstance().getTime());
            System.out.println("Generating a containment hierarchy...");
            //Let's create the default containment hierarchy
//            mem.addPossibleChildren(null, new String[]{"City"});
//            for (String parentClass : c.containmentHierarchy.keySet()){
//                try{
//                    mem.addPossibleChildren(parentClass, c.containmentHierarchy.get(parentClass));
//                }catch (Exception ex){
//                    System.out.println("ERROR: "+ex.getMessage());
//                }
//            }
            System.out.println("Containment hierarchy generated successfully");

            System.out.println("Generating a set of list types...");
            //We create the default list types here
//            for (String listType : lt.listTypes.keySet()){
//                try{
//                    for (String listTypeItem : lt.listTypes.get(listType))
//                        aem.createListTypeItem(listType, listTypeItem, null);
//                }catch (Exception ex){
//                    System.out.println("ERROR: "+ex.getMessage());
//                }
//            }
            System.out.println("List type set generated successfully");

            System.out.println("Generating a sample data set");
//            We create a test dataset here
//            createObjects("City", 2, null, null);

            System.out.println("Data set created successfully");

            System.out.println("Number of objects created: "+objectCount);
            System.out.println("Ending at: " + Calendar.getInstance().getTime());
            persistenceService.stop();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    /*
    private static void createObjects(String className, int numInstances, String parentClass, Long parentId){
        for (int i = 0; i < numInstances; i++){
            HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
            attributes.put("name",Arrays.asList(new String[]{className + " " + i}));

            try{
                long newObjectId = bem.createObject(className, parentClass, parentId, attributes, 0);
                objectCount++;
                if (c.containmentHierarchy.get(className) != null){
                    for (String anotherClass : c.containmentHierarchy.get(className)){
                        if (anotherClass.equals("GenericContainer"))
                            anotherClass = "Rack";
                        else if (anotherClass.equals("GenericNetworkElement"))
                                anotherClass = "Router";
                             else if (anotherClass.equals("GenericPhysicalElement"))
                                    anotherClass = "DWDMMux";
                                  else if (anotherClass.equals("GenericDataLinkElement"))
                                        anotherClass = "SDHMux";
                                        else if (anotherClass.equals("GenericCommunicationsPort"))
                                                anotherClass = "ElectricalPort";
                                             else if (anotherClass.equals("GenericComputerPart"))
                                                    anotherClass = "Monitor";
                                                  else if (anotherClass.equals("GenericAntenna"))
                                                    anotherClass = "DipoleAntenna";
                                                      else if (anotherClass.equals("GenericBoard"))
                                                            anotherClass = "IPBoard";
                                                           else if (anotherClass.equals("GenericPort"))
                                                                anotherClass = "OpticalPort";

                        createObjects(anotherClass, numInstances, className, newObjectId);
                    }
                }
            }catch(Exception ae){
                System.out.println("ERROR: " + ae.getMessage());
            }

        }
    }
    */
}
