/*
 * 
 */

package org.kuwaiba.test.wsclient;

import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.test.wsclient.fixtures.Containment;
import org.kuwaiba.test.wsclient.fixtures.ListTypes;
import org.kuwaiba.wsclient.Exception_Exception;
import org.kuwaiba.wsclient.Kuwaiba;
import org.kuwaiba.wsclient.KuwaibaService;
import org.kuwaiba.wsclient.RemoteSession;

/**
 * Uploads initial, test information
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MainTest {
    public static void main (String[] args){
        RemoteSession session = null;
        Kuwaiba port = null;
        try{
            System.out.println("Starting at: " + Calendar.getInstance().getTime());
            URL serverURL = new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?wsdl"); //NOI18n
            KuwaibaService service = new KuwaibaService(serverURL);
            port = service.getKuwaibaPort();
            session = port.createSession("admin", "kuwaiba");
            Containment c = new Containment();
            ListTypes lt = new ListTypes();
            //Let's create the default containment hierarchy
            port.addPossibleChildrenByClassName(null, Arrays.asList(new String[]{"City"}), session.getSessionId());
            for (String parentClass : c.containmentHierarchy.keySet()){
                try{
                    port.addPossibleChildrenByClassName(parentClass, Arrays.asList(c.containmentHierarchy.get(parentClass)), session.getSessionId());
                }catch (Exception ex){
                    System.out.println("ERROR: "+ex.getMessage());
                }
            }

            /*for (String listType : lt.listTypes.keySet()){
                try{
                    for (String listTypeItem : lt.listTypes.get(listType))
                        port.createListTypeItem(listType, listTypeItem, null, session.getSessionId());
                }catch (Exception ex){
                    System.out.println("ERROR: "+ex.getMessage());
                }
            }*/
            
            port.closeSession(session.getSessionId());
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
}
