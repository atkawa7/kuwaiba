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
package org.kuwaiba.web.procmanager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Artifact;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.apis.persistence.application.process.ConditionalActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinitionLoader;
import org.kuwaiba.apis.persistence.application.process.ProcessInstance;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class ProcessCache {
    public static long processInstancesCounter = 10;
    public static long artifactCounter = 100000;
    
    private final HashMap<Long, List<ActivityDefinition>> activities = new HashMap();
    private final List<ProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<Long, ProcessInstance> processInstances = new HashMap();
    private final HashMap<ProcessDefinition, List<ProcessInstance>> relatedProcessInstances = new HashMap();
    private final HashMap<ArtifactDefinition, List<Artifact>> artifacts = new HashMap();
    private final HashMap<ProcessInstance, List<Artifact>> processArtifacts = new HashMap();
        
    private static ProcessCache instance;
        
    private ProcessCache() {
        ProcessDefinition altaServicioProcess = getProcessDefinition2(1L);
        
        if (altaServicioProcess != null)
            cacheProcessDefinition(altaServicioProcess);
    }
        
    public static ProcessCache getInstance() {
        return instance == null ? instance = new ProcessCache() : instance.updateArtifacts();
    }
    
    public void cacheProcessDefinition(ProcessDefinition processDefinition) {
        if (processDefinition != null) {
            processDefinitions.add(processDefinition);
            relatedProcessInstances.put(processDefinition, new ArrayList());
            
            ActivityDefinition startActivity = processDefinition.getStartActivity();
            
            if (startActivity != null) {
                activities.put(processDefinition.getId(), new ArrayList());
                initActivitiesCache(processDefinition, startActivity);
            }
        }
    }
    
    private void initActivitiesCache(ProcessDefinition processDefinition, ActivityDefinition activity) {
        if (activity != null) {
            if (!activities.get(processDefinition.getId()).contains(activity)) {
                
                artifacts.put(activity.getArfifact(), new ArrayList());
                activities.get(processDefinition.getId()).add(activity);
                
                if (activity instanceof ConditionalActivityDefinition) {
                    initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfTrue());
                    initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfFalse());
                } else {
                    initActivitiesCache(processDefinition, activity.getNextActivity());
                }
            }
        }
    }
    
    public ProcessCache updateArtifacts() {        
        if (!processDefinitions.isEmpty()) {
            
        }
        return instance;
    }
    
    private ProcessDefinition getProcessDefinition2(long processDefinitionId) {
        ProcessDefinitionLoader processDefinitionLoader = new ProcessDefinitionLoader(processDefinitionId);
        File file = new File("/data/processDefinition/altaServicio.xml");
        byte[] processDefinitionStructure = ProcessDefinitionLoader.getFileAsByteArray(file);
        
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.loadProcessDefinition(processDefinitionStructure);
            return processDefinition;
        } catch (XMLStreamException | NumberFormatException | ProcessDefinitionLoader.XMLProcessDefinitionException ex) {
            ex.printStackTrace();
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    public ProcessDefinition getProcessDefinition(long id) throws InventoryException {
        for (ProcessDefinition processDefinition : processDefinitions) {
            if (processDefinition.getId() == id)            
                return processDefinition;                
        }
        throw new InventoryException("Process Definition can not be found") {};
    }
    
    public long createProcessInstance(long processDefId, String name, String description) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processDefId);
        
        long currentActivity = processDef.getStartActivity().getId();
        
        ProcessInstance processInstance = new ProcessInstance(processInstancesCounter++, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        processArtifacts.put(processInstance, new ArrayList());
        
        return processInstance.getId();
    }
    
    public ProcessInstance getProcessInstance(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = processInstances.get(processInstanceId);
        if (processInstance != null)        
            return processInstance;
        throw new InventoryException("Process Instances can not be found") {};
    }
    
    public Artifact getArtifactForActivity(long processInstanceId, long activityId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity = processDef.getStartActivity();
        
        while ((activity != null) && (activity.getId() != activityId))
            activity = getNextActivityForProcessInstance(processInstanceId, activity.getId());
        
        if (activity != null) {
            
            ArtifactDefinition arfifact = activity.getArfifact();
        
            List<Artifact> poolArtifacts = artifacts.get(arfifact);

            List<Artifact> poolProcessInsArtifacts = processArtifacts.get(processInstance);

            for (Artifact artifact : poolArtifacts) {
                
                for (Artifact processArtifact : poolProcessInsArtifacts) {
                    
                    if (processArtifact.getId() == artifact.getId())
                        return processArtifact;
                }
            }
        }
        throw new InventoryException("Process Instances Artifact can not be found") {};
    }
    
    public ArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId) throws InventoryException {
        
        if (activities.containsKey(processDefinitionId)) {
            
            if (activities.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : activities.get(processDefinitionId))
                    if (activityDef.getId() == activityDefinitionId)
                        return activityDef.getArfifact();
                        
            }
        }
        
        throw new InventoryException("Artifact Definition can not be found") {};
    }
    
    private boolean getConditionalArtifactContent(Artifact artifact) throws InventoryException {
        if (artifact == null)
            throw new InventoryException("Conditional Artifact can not be found") {};
        
        try {
            byte[] content = artifact.getContent();
            
            XMLInputFactory xif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            XMLStreamReader reader = xif.createXMLStreamReader(bais);
            
            QName tagValue = new QName("value"); //NOI18N
            
            while (reader.hasNext()) {
                
                int event = reader.next();
                
                if (event == XMLStreamConstants.START_ELEMENT) {
                    
                    if (reader.getName().equals(tagValue))
                        return Boolean.valueOf(reader.getElementText());
                }
            }
            
        } catch (Exception ex) {
            throw new InventoryException("Conditional Artifact Content Malformed") {};
        }
        return false;
    }
    
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        List<ActivityDefinition> result = new ArrayList();
        
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        while (activity != null && processInstance.getCurrentActivity() != activity.getId()) {
            
            result.add(activity);
            activity = getNextActivityForProcessInstance(processInstanceId, activity.getId());
        }
        if (activity != null && processInstance.getCurrentActivity() == activity.getId())
            result.add(activity);
        
        return result;
        //throw new InventoryException("Process Not Found") {};
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(long processInstanceId, long currentActivityId) throws InventoryException {
        
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        while (activity != null) {
            
            if (activity.getId() == currentActivityId) {
                
                if (activity instanceof ConditionalActivityDefinition) {
                    Artifact artifact = null;
                    
                    try {
                        artifact = getArtifactForActivity(processInstanceId, activity.getId());
                    } catch(Exception ex) {
                    }
                    boolean isTrue = false;
                    
                    if (artifact != null)
                        isTrue = getConditionalArtifactContent(artifact);
                                        
                    if (isTrue)
                        return ((ConditionalActivityDefinition) activity).getNextActivityIfTrue();
                    else
                        return ((ConditionalActivityDefinition) activity).getNextActivityIfFalse();
                }
                return activity.getNextActivity();
            }
            
            if (activity instanceof ConditionalActivityDefinition) {
                Artifact artifact = getArtifactForActivity(processInstanceId, activity.getId());
                boolean isTrue = getConditionalArtifactContent(artifact);
                                        
                if (isTrue)
                    activity = ((ConditionalActivityDefinition) activity).getNextActivityIfTrue();
                else
                    activity = ((ConditionalActivityDefinition) activity).getNextActivityIfFalse();
                
            } else {
                activity = activity.getNextActivity();
            }
        }
        throw new InventoryException("Next Activity can not be found") {};
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        return getNextActivityForProcessInstance(processInstanceId, processInstance.getCurrentActivity());
    }
        
    public void updateActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        while (activity != null && activity.getId() != activityDefinitionId)
            activity = getNextActivityForProcessInstance(processInstanceId, activity.getId());
        
        if (activity != null) {
            
            if (!processArtifacts.containsKey(processInstance))
                processArtifacts.put(processInstance, new ArrayList());

            if (!artifacts.containsKey(activity.getArfifact()))
                artifacts.put(activity.getArfifact(), new ArrayList());
            
            if (!processArtifacts.get(processInstance).contains(artifact))
                processArtifacts.get(processInstance).add(artifact);
            
            if (!artifacts.get(activity.getArfifact()).contains(artifact))
                artifacts.get(activity.getArfifact()).add(artifact);
        }
    }
    
    public void commitActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        while (activity != null && activity.getId() != activityDefinitionId)
            activity = getNextActivityForProcessInstance(processInstanceId, activity.getId());
        
        if (activity != null) {
            
            if (!processArtifacts.containsKey(processInstance))
                processArtifacts.put(processInstance, new ArrayList());

            if (!artifacts.containsKey(activity.getArfifact()))
                artifacts.put(activity.getArfifact(), new ArrayList());
            
            if (!processArtifacts.get(processInstance).contains(artifact))
                processArtifacts.get(processInstance).add(artifact);
            
            if (!artifacts.get(activity.getArfifact()).contains(artifact))
                artifacts.get(activity.getArfifact()).add(artifact);
            
            ActivityDefinition nextActivity = getNextActivityForProcessInstance(processInstanceId);
            if (nextActivity != null)
                processInstance.setCurrentActivity(nextActivity.getId());
        }
    }
    
    public List<ProcessInstance> getProcessInstances(long processDefinitionId) throws InventoryException {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            return relatedProcessInstances.get(processDefinition);
        
        throw new InventoryException("Process Instances can not be found") {};
    }
    
    public List<ProcessDefinition> getProcessDefinitions() throws InventoryException {
        return processDefinitions;
    }
    
    public Artifact getArtifact(long id) {
        for (List<Artifact> lstArtifact : artifacts.values()) {
            for (Artifact actifact : lstArtifact) {
                if (actifact.getId() == id)
                    return actifact;
            }
        }
        return null;
    }
    
}
