/*
 * 
 */

package org.kuwaiba.test.wsclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.test.wsclient.fixtures.Containment;
import org.kuwaiba.test.wsclient.fixtures.ListTypes;
import org.kuwaiba.wsclient.Exception_Exception;
import org.kuwaiba.wsclient.Kuwaiba;
import org.kuwaiba.wsclient.KuwaibaService;
import org.kuwaiba.wsclient.RemoteSession;
import org.kuwaiba.wsclient.StringArray;

/**
 * Uploads initial, test information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MainTest {
        private static RemoteSession session = null;
        private static Kuwaiba port = null;
        private static Containment c = new Containment();
        private static ListTypes lt = new ListTypes();
        private static int objectCount = 0;

    public static void main (String[] args){
        
        try{
            System.out.println("Starting at: " + Calendar.getInstance().getTime());
            URL serverURL = new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?wsdl"); //NOI18n
            KuwaibaService service = new KuwaibaService(serverURL);
            port = service.getKuwaibaPort();
            session = port.createSession("admin", "kuwaiba");

            System.out.println("Generating a containment hierarchy...");
            //Let's create the default containment hierarchy
            port.addPossibleChildrenByClassName(null, Arrays.asList(new String[]{"City"}), session.getSessionId());
            for (String parentClass : c.containmentHierarchy.keySet()){
                try{
                    port.addPossibleChildrenByClassName(parentClass, Arrays.asList(c.containmentHierarchy.get(parentClass)), session.getSessionId());
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
                        port.createListTypeItem(listType, listTypeItem, null, session.getSessionId());
                }catch (Exception ex){
                    System.out.println("ERROR: "+ex.getMessage());
                }
            }
            System.out.println("List type set generated successfully");

            System.out.println("Generating a sample data set");
            //We create a test dataset here
            createObjects("City", 2, null, null);

            System.out.println("Data set created successfully");

            port.closeSession(session.getSessionId());
            System.out.println("Number of objects created: "+objectCount);
            System.out.println("Ending at: " + Calendar.getInstance().getTime());
        }
        catch(Exception ex){
            if (session != null && port != null)
                try {
                port.closeSession(session.getSessionId());
            } catch (Exception_Exception ex1) {
                Logger.getLogger(MainTest.class.getName()).log(Level.SEVERE, null, ex1);
            }
            ex.printStackTrace();
        }
    }

    private static void createObjects(String className, int numInstances, String parentClass, Long parentId){
        for (int i = 0; i < numInstances; i++){
            List<String> attributes = new ArrayList<String>();
            List<StringArray> values = new ArrayList<StringArray>();
            attributes.add("name");
            StringArray entry = new StringArray();
            entry.getItem().add(className + " " + i);
            values.add(entry);
            try{
                long newObjectId = port.createObject(className, parentClass, parentId, attributes, values, null, session.getSessionId());
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
