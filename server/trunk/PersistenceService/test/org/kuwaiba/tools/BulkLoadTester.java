/*
 * 
 */

package org.kuwaiba.tools;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.test.fixtures.Containment;
import org.test.fixtures.ListTypes;


/**
 * Uploads initial, test information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BulkLoadTester {
        private static Containment c = new Containment();
        private static ListTypes lt = new ListTypes();
        private static int objectCount = 0;
        private static ApplicationEntityManagerRemote aem;
        private static BusinessEntityManagerRemote bem;
        private static MetadataEntityManagerRemote mem;

    public static void main (String[] args){
        
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            bem = (BusinessEntityManagerRemote) registry.lookup(BusinessEntityManagerRemote.REFERENCE_BEM);
            aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);

            System.out.println("Starting at: " + Calendar.getInstance().getTime());
            System.out.println("Generating a containment hierarchy...");
            //Let's create the default containment hierarchy
            mem.addPossibleChildren(null, new String[]{"City"});
            for (String parentClass : c.containmentHierarchy.keySet()){
                try{
                    mem.addPossibleChildren(parentClass, c.containmentHierarchy.get(parentClass));
                }catch (Exception ex){
                    System.out.println("ERROR: "+ex.getMessage());
                }
            }
            System.out.println("Containment hierarchy generated successfully");

            System.out.println("Generating a set of list types...");
            //We create the default list types here
            for (String listType : lt.listTypes.keySet()){
                try{
                    for (String listTypeItem : lt.listTypes.get(listType))
                        aem.createListTypeItem(listType, listTypeItem, null);
                }catch (Exception ex){
                    System.out.println("ERROR: "+ex.getMessage());
                }
            }
            System.out.println("List type set generated successfully");

            System.out.println("Generating a sample data set");
            //We create a test dataset here
            createObjects("City", 2, null, null);

            System.out.println("Data set created successfully");

            System.out.println("Number of objects created: "+objectCount);
            System.out.println("Ending at: " + Calendar.getInstance().getTime());
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    private static void createObjects(String className, int numInstances, String parentClass, Long parentId){
        for (int i = 0; i < numInstances; i++){
            HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
            attributes.put("name",Arrays.asList(new String[]{className + " " + i}));

            try{
                long newObjectId = bem.createObject(className, parentClass, parentId, attributes, null);
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
}
