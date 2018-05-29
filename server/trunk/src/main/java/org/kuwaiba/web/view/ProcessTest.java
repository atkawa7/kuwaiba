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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
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
    public static long processInstancesCounter = 10;
    public static long activityDefinitionCounter = 100;
    public static long artifactDefinitionCounter = 1000;
    public static long actorCounter = 10000;
    public static long artifactCounter = 100000;
    private final List<RemoteProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<Long, RemoteProcessInstance> processInstances = new HashMap();
    private final HashMap<RemoteProcessDefinition, List<RemoteProcessInstance>> relatedProcessInstances = new HashMap();
    private final HashMap<RemoteArtifactDefinition, List<RemoteArtifact>> artifacts = new HashMap();
    private final HashMap<RemoteProcessInstance, List<RemoteArtifact>> processArtifacts = new HashMap();
    
    private static ProcessTest instance;
    private final RemoteActor commercial = new RemoteActor(actorCounter++, "Commercial", Actor.TYPE_GROUP);
    private final RemoteActor engineering = new RemoteActor(actorCounter++, "Engineering", Actor.TYPE_GROUP);
    private final RemoteActor serviceDelivery = new RemoteActor(actorCounter++, "ServiceDelivery", Actor.TYPE_GROUP);
    
    private ProcessTest() {
        RemoteProcessDefinition altaServicioProcess = createAltaServicioProcess();
                
        processDefinitions.add(altaServicioProcess);
        relatedProcessInstances.put(altaServicioProcess, new ArrayList());
    }
    
    public static ProcessTest getInstance() {
        return instance == null ? instance = new ProcessTest() : instance;
    }
    
    private RemoteArtifactDefinition getArtifactDefinitionIncio() {
        File file = new File("/data/formDefinitions/incio.xml");
        byte [] definition = getFileAsByteArray(file);
        
        return new RemoteArtifactDefinition(artifactDefinitionCounter++, "", "", "", ArtifactDefinition.TYPE_FORM, definition);
    }
    
    private RemoteArtifactDefinition getArtifactDefinitionOrdenServicio() {
        File file = new File("/data/formDefinitions/1_formcustomerorder.xml");
        byte [] definition = getFileAsByteArray(file);
        
        return new RemoteArtifactDefinition(artifactDefinitionCounter++, "", "", "", ArtifactDefinition.TYPE_FORM, definition);
    }
    
    private RemoteArtifactDefinition getArtifactDefinitionFin() {
        File file = new File("/data/formDefinitions/fin.xml");
        byte [] definition = getFileAsByteArray(file);
        
        return new RemoteArtifactDefinition(artifactDefinitionCounter++, "", "", "", ArtifactDefinition.TYPE_FORM, definition);
    }
    
    private RemoteActivityDefinition getActivityDefinitionInicio() {
        
        RemoteArtifactDefinition inicioArtifact = getArtifactDefinitionIncio();
        artifacts.put(inicioArtifact, new ArrayList());
                
        RemoteActivityDefinition inicio = new RemoteActivityDefinition(
            activityDefinitionCounter++, "Incio", "Inicio", ActivityDefinition.TYPE_START, inicioArtifact, commercial);                
        
        return inicio;
    }
    
    private RemoteActivityDefinition getActivityDefinitionNuevaOrdenDeServicio() {
        
        RemoteArtifactDefinition ordenServicioArtifact = getArtifactDefinitionOrdenServicio();
        artifacts.put(ordenServicioArtifact, new ArrayList());
                
        RemoteActivityDefinition ordenServicio = new RemoteActivityDefinition(
            activityDefinitionCounter++, "Orden de Servicio", "Orden de Servicio", ActivityDefinition.TYPE_START, ordenServicioArtifact, commercial);                
                
        return ordenServicio;
    }
    
    private RemoteActivityDefinition getActivityDefinitionSeleccionDeEquipo() {
        
        RemoteArtifactDefinition inicioArtifact = new RemoteArtifactDefinition(artifactDefinitionCounter, "", "", "0", ArtifactDefinition.TYPE_FORM, null);
        artifacts.put(inicioArtifact, new ArrayList());
                
        RemoteActivityDefinition inicio = new RemoteActivityDefinition(
            activityDefinitionCounter++, "Seleccion de Equipos", "Seleccion de Equipos", ActivityDefinition.TYPE_START, inicioArtifact, commercial);                
                
        return inicio;
    }
    
    private RemoteActivityDefinition getActivityDefinitionAsignacionDeEquipo() {
        
        RemoteArtifactDefinition inicioArtifact = new RemoteArtifactDefinition(artifactDefinitionCounter++, "", "", "0", ArtifactDefinition.TYPE_FORM, null);
        artifacts.put(inicioArtifact, new ArrayList());
                
        RemoteActivityDefinition inicio = new RemoteActivityDefinition(
            activityDefinitionCounter++, "Asignacion de Equipos", "Asignacion de Equipos", ActivityDefinition.TYPE_START, inicioArtifact, commercial);                
        
        return inicio;
    }
    
    private RemoteActivityDefinition getActivityDefinitionFin() {
        
        RemoteArtifactDefinition finArtifact = getArtifactDefinitionFin();
        artifacts.put(finArtifact, new ArrayList());
                
        RemoteActivityDefinition fin = new RemoteActivityDefinition(
            activityDefinitionCounter++, "Fin", "Fin", ActivityDefinition.TYPE_START, finArtifact, commercial);                
                
        return fin;
    }
    
    private RemoteProcessDefinition createAltaServicioProcess() {
        
        RemoteActivityDefinition inicio = getActivityDefinitionInicio();
        RemoteActivityDefinition ordenServicio = getActivityDefinitionNuevaOrdenDeServicio();
        RemoteActivityDefinition fin = getActivityDefinitionFin();
        
        inicio.setNextActivity(ordenServicio);
        ordenServicio.setNextActivity(fin);
        
        RemoteProcessDefinition processDefinition = new RemoteProcessDefinition(processDefinitionCounter++, "Alta de Servicio", "Alta de Servicio", new Date().getTime(), "0.0", true, inicio);
        
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
        
        RemoteProcessInstance processInstance = new RemoteProcessInstance(processInstancesCounter++, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        processArtifacts.put(processInstance, new ArrayList());
        
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
            
            if (activity.getNextActivity() != null)
                processInstance.setCurrentActivity(activity.getNextActivity().getId());
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
    
    public static byte[] getFileAsByteArray(File file) {
        try {
            Scanner in = new Scanner(file);

            String line = "";

            while (in.hasNext())
                line += in.nextLine();

            byte [] structure = line.getBytes();

            in.close();

            return structure;

        } catch (FileNotFoundException ex) {

            return null;
        }
    }
}
