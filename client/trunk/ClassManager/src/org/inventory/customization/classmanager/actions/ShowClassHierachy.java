/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.customization.classmanager.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.io.FileOutputStream;
//import java.io.IOException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.customization.classmanager.ClassHierarchyTopComponent;
import org.openide.util.Lookup;

public final class ShowClassHierachy implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        byte[] hierarchyAsXML = CommunicationsStub.getInstance().getClassHierarchy(true);
        if (hierarchyAsXML == null){
            NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
            nu.showSimplePopup("Error", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        }else{
            ClassHierarchyTopComponent tc = new ClassHierarchyTopComponent(hierarchyAsXML);
            tc.open();
            tc.requestActive();
        }
        /**
         * Enable for debugging purposes
         try{
            FileOutputStream fos = new FileOutputStream("/home/zim/classhierarchy.xml");
            fos.write(hierarchyAsXML);
            fos.flush();
            fos.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }*/
         
        
    }
}
