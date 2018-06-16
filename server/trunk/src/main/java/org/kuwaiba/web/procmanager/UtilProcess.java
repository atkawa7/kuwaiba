/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.procmanager;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Actor;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;

/**
 * Class For test purposes
 * @author johnyortega
 */
public class UtilProcess {
    
    public static RemoteProcessInstance getProcessInstance(RemoteProcessDefinition processDefinition) {
        return new RemoteProcessInstance(5, "005-18", "", 1, processDefinition.getId());
    }
    
    public static List<RemoteProcessInstance> getProcessInstances(RemoteProcessDefinition processDefinition) {
        return Arrays.asList(
            new RemoteProcessInstance(1, "001-18", "", 1, processDefinition.getId()), 
            new RemoteProcessInstance(2, "002-18", "", 1, processDefinition.getId()), 
            new RemoteProcessInstance(3, "003-18", "", 1, processDefinition.getId()), 
            new RemoteProcessInstance(4, "004-18", "", 1, processDefinition.getId()));
    }
    
    public static RemoteProcessDefinition getProcessDefinition1() {
        // Actors
        RemoteActor commercial = new RemoteActor(1, "Commercial", Actor.TYPE_USER);
        RemoteActor engineering = new RemoteActor(2, "Engineering", Actor.TYPE_USER);
        RemoteActor serviceDelivery  = new RemoteActor(3, "ServiceDelivery", Actor.TYPE_USER);
        // Activity Start
        RemoteActivityDefinition startActivityDef = new RemoteActivityDefinition(1, "Inicio", "Inicio", ActivityDefinition.TYPE_START, null, commercial);
        
        RemoteProcessDefinition processDef = new RemoteProcessDefinition(1, "Alta de Servicio", "Alta de Servicio", new Date().getTime(), "1.0", true, startActivityDef);
        // Activity New Service Order
        RemoteArtifactDefinition newServiceOrderForm = new RemoteArtifactDefinition(2, "Service Order Form", "Service Order Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
                
        RemoteActivityDefinition newServiceOrder = new RemoteActivityDefinition(2, "Nueva Orden de Servicio", "Nueva Orden de Servicio", ActivityDefinition.TYPE_NORMAL, newServiceOrderForm, commercial);
        
        startActivityDef.setNextActivity(newServiceOrder);
        // Activity Equipment Selector
        RemoteArtifactDefinition equipmentSelectorForm = new RemoteArtifactDefinition(3, "Equipment Selector Form", "Equipment Selector Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
        
        RemoteActivityDefinition equipmentSelector = new RemoteActivityDefinition(3, "Selección de Equipos", "Equipment Selector", ActivityDefinition.TYPE_NORMAL, equipmentSelectorForm, engineering);
        
        newServiceOrder.setNextActivity(equipmentSelector);
        // Activity Equipment Assignment
        RemoteArtifactDefinition equipmentAssignmentForm = new RemoteArtifactDefinition(4, "Equipment Assignment Form", "Equipment Assignment Form", "1.0", ArtifactDefinition.TYPE_FORM, null); //TODO: get form
        
        RemoteActivityDefinition equipmentAssignment = new RemoteActivityDefinition(4, "Asignación de Equipos", "Equipment Assignment", ActivityDefinition.TYPE_NORMAL, equipmentAssignmentForm, serviceDelivery);
        
        equipmentSelector.setNextActivity(equipmentAssignment);
        // Actity Fin
        RemoteActivityDefinition endActivityDef = new RemoteActivityDefinition(5, "Fin", "Fin", ActivityDefinition.TYPE_END, null, commercial);
        
        equipmentAssignment.setNextActivity(endActivityDef);
        
        return processDef;        
    }
    
}
