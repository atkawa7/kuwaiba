/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.web.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Actor;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessTest {
    public static long processDefinitionCounter = 1;
    public static long processInstancesCounter = 1;
    public static long activityDefinitionCounter = 1;
    public static long artifactDefinitionCounter = 1;
    public static long actorCounter = 1;
    private final List<RemoteProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<Long, RemoteProcessInstance> processInstances = new HashMap();
    private final HashMap<RemoteProcessDefinition, List<RemoteProcessInstance>> relatedProcessInstances = new HashMap();
    private final HashMap<RemoteArtifactDefinition, List<RemoteArtifact>> artifacts = new HashMap();
    private final HashMap<RemoteProcessInstance, List<RemoteArtifact>> processArtifacts = new HashMap();
    
    private static ProcessTest instance;
    private final RemoteActor commercial;
    private final RemoteActor engineering;
    private final RemoteActor serviceDelivery;
    
    private ProcessTest() {
        RemoteProcessDefinition altaServicioProcess = createAltaServicioProcess();
                
        processDefinitions.add(altaServicioProcess);
        relatedProcessInstances.put(altaServicioProcess, new ArrayList());
        
        commercial = new RemoteActor(actorCounter, "commercial", Actor.TYPE_GROUP); actorCounter += 1;
        
        engineering = new RemoteActor(actorCounter, "engineering", Actor.TYPE_GROUP); actorCounter += 1;
        
        serviceDelivery = new RemoteActor(actorCounter, "serviceDelivery", Actor.TYPE_GROUP); actorCounter += 1;
    }
    
    public static ProcessTest getInstance() {
        return instance == null ? instance = new ProcessTest() : instance;
    }
    
    private RemoteActivityDefinition getActivityDefinitionInicio() {
        
        RemoteArtifactDefinition inicioArtifact = new RemoteArtifactDefinition(artifactDefinitionCounter, "", "", "0", ArtifactDefinition.TYPE_FORM, null);
                
        RemoteActivityDefinition inicio = new RemoteActivityDefinition(
            activityDefinitionCounter, "Incio", "Inicio", ActivityDefinition.TYPE_START, inicioArtifact, commercial);                
        
        artifactDefinitionCounter += 1;
        activityDefinitionCounter += 1;
        
        return inicio;
    }
    
    private RemoteActivityDefinition getActivityDefinitionFin() {
        
        RemoteArtifactDefinition inicioArtifact = new RemoteArtifactDefinition(artifactDefinitionCounter, "", "", "0", ArtifactDefinition.TYPE_FORM, null);
                
        RemoteActivityDefinition fin = new RemoteActivityDefinition(
            activityDefinitionCounter, "Fin", "Fin", ActivityDefinition.TYPE_START, inicioArtifact, commercial);                
        
        artifactDefinitionCounter += 1;
        activityDefinitionCounter += 1;
        
        return fin;
    }
    
    private RemoteProcessDefinition createAltaServicioProcess() {
        
        RemoteActivityDefinition inicio = getActivityDefinitionInicio();
        inicio.setNextActivity(getActivityDefinitionFin());
        
        RemoteProcessDefinition processDefinition = new RemoteProcessDefinition(processDefinitionCounter, "Alta de Servicio", "Alta de Servicio", new Date().getTime(), "0.0", true, inicio);
        

        processDefinitionCounter += 1;
        return processDefinition;
    }
    
    public RemoteProcessDefinition getProcessDefinition(long id) throws InventoryException {
        for (RemoteProcessDefinition processDefinition : processDefinitions) {
            if (processDefinition.getId() == id)            
                return processDefinition;                
        }
        throw new InventoryException("Process Definition can not be found") {};
    }
    
    public long createProcessInstance(long processDefId, String name, String description) throws InventoryException {
        RemoteProcessDefinition processDef = getProcessDefinition(processDefId);
        
        long currentActivity = processDef.getStartAction().getId();
        
        RemoteProcessInstance processInstance = new RemoteProcessInstance(processInstancesCounter, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        
        processInstancesCounter += 1;
        return processInstance.getId();
    }
    
    public RemoteProcessInstance getProcessInstance(long processInstanceId) throws InventoryException {
        RemoteProcessInstance processInstance = processInstances.get(processInstanceId);
        if (processInstance != null)        
            return processInstance;
        throw new InventoryException("Process Instances can not be found") {};
    }
    
    public RemoteArtifact getArtifactForActivity(long processInstanceId, long activityId) throws InventoryException {
        RemoteProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        RemoteProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        
        RemoteActivityDefinition activity = processDef.getStartAction();
        
        while ((activity != null) && (activity.getId() != activityId))
            activity = activity.getNextActivity();
        
        if (activity != null) {
            
            RemoteArtifactDefinition arfifact = activity.getArfifact();
        
            List<RemoteArtifact> poolArtifacts = artifacts.get(arfifact);

            List<RemoteArtifact> poolProcessInsArtifacts = processArtifacts.get(processInstance);

            for (RemoteArtifact artifact : poolArtifacts) {
                
                for (RemoteArtifact processArtifact : poolProcessInsArtifacts) {
                    
                    if (processArtifact.getId() == artifact.getId())
                        return processArtifact;
                }
            }
        }
        throw new InventoryException("Process Instances Artifact can not be found") {};
    }
    
    public RemoteArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId) throws InventoryException {
        RemoteProcessDefinition processDef = getProcessDefinition(processDefinitionId);
        
        RemoteActivityDefinition activityDefinition = processDef.getStartAction();
        
        while (activityDefinition != null && activityDefinition.getId() != activityDefinitionId)
            activityDefinition = activityDefinition.getNextActivity();
        
        if (activityDefinition != null)
            return activityDefinition.getArfifact();
        
        throw new InventoryException("Artifact Definition can not be found") {};
    }
    
    public RemoteActivityDefinition getNextActivityForProcessInstance(long processInstanceId) throws InventoryException {
        RemoteProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        RemoteProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        RemoteActivityDefinition activity = processDefinition.getStartAction();
        
        while (activity != null) {
            
            if (activity.getId() == processInstance.getCurrentActivity())
                return activity.getNextActivity();
            
            activity = activity.getNextActivity();
        }
        throw new InventoryException("Next Activity can not be found") {};
    }
    
    public void commitActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact) throws InventoryException {
        RemoteProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        RemoteProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        RemoteActivityDefinition activity = processDefinition.getStartAction();
        
        while (activity != null && activity.getId() != activityDefinitionId)
            activity = activity.getNextActivity();
        
        if (activity != null) {
            
            if (!processArtifacts.containsKey(processInstance))
                processArtifacts.put(processInstance, new ArrayList());

            if (!artifacts.containsKey(activity.getArfifact()))
                artifacts.put(activity.getArfifact(), new ArrayList());

            processArtifacts.get(processInstance).add(artifact);
            artifacts.get(activity.getArfifact()).add(artifact);
        }
    }
    
    public List<RemoteProcessInstance> getProcessInstances(long processDefinitionId) throws InventoryException {
        RemoteProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            return relatedProcessInstances.get(processDefinition);
        
        throw new InventoryException("Process Instances can not be found") {};
    }
    
    public List<RemoteProcessDefinition> getProcessDefinitions() throws InventoryException {
        return processDefinitions;
    }
}
