/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.beans;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class WebServiceBean implements WebServiceBeanRemote {

    private MetadataEntityManagerRemote mem;
    
    private MetadataEntityManagerRemote getMemInstance(){
        try{
            if (mem == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                mem = (MetadataEntityManagerRemote) registry.lookup("mem");
            }
        }
        catch(Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            mem = null;
        }
        return mem;
    }

    @Override
    public String getMyMetadata() {
        if (getMemInstance() == null)
            return "";
        try{
            return getMemInstance().getMyMetadata().getClassName();
        }catch(RemoteException ex){
            ex.printStackTrace();
            return "";
        }
    }
}
