/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms;

import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;

/**
 *
 * @author johnyortega
 */
public class FormLook implements FormView<RemoteForm> {
    
    @Override
    public void display(RemoteForm form) {
        if (form == null || form.getStructure() != null)
            return;
        
                
                
    }
    
}
