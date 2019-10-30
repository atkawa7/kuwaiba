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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Artifact;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.apis.persistence.application.process.ConditionalActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.ParallelActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinitionLoader;
import org.kuwaiba.apis.persistence.application.process.ProcessInstance;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.openide.util.Exceptions;

/**
 * Cache of Process Definitions, Process Instances and Form Definitions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public final class ProcessCache {    
    private final HashMap<Long, List<ActivityDefinition>> processActivityDefinitions = new HashMap();
    private final List<ProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<Long, ProcessInstance> processInstances = new HashMap();
    private final HashMap<ProcessDefinition, List<ProcessInstance>> relatedProcessInstances = new HashMap();
    private final HashMap<ProcessInstance, HashMap<ArtifactDefinition, Artifact>> processInstanceArtifacts = new HashMap();
        
    private static ProcessCache instance;
        
    private ProcessCache() {
        System.out.println(String.format("[KUWAIBA] [%s] Initiating process definitions cache...", Calendar.getInstance().getTime()));
        updateProcessDefinitions();
    }
        
    public static ProcessCache getInstance() {
        return instance == null ? instance = new ProcessCache() : instance;
    }
    
    public void cacheProcessDefinition(ProcessDefinition processDefinition) {
        if (processDefinition != null) {
            for (ProcessDefinition processDef : processDefinitions) {
                if (processDef.getId() == processDefinition.getId()) {
                    processDefinitions.remove(processDef);
                    break;
                }
            }
            processDefinitions.add(processDefinition);
                        
            relatedProcessInstances.put(processDefinition, new ArrayList());
            
            ActivityDefinition startActivity = processDefinition.getStartActivity();
            
            if (startActivity != null) {
                processActivityDefinitions.put(processDefinition.getId(), new ArrayList());
                
                initActivitiesCache(processDefinition, startActivity);
            }
        }
    }
    
    private void initActivitiesCache(ProcessDefinition processDefinition, ActivityDefinition activity) {
        if (activity != null) {
            
            for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinition.getId())) {
                if (activityDef.getId() == activity.getId())
                    return;
            }
            processActivityDefinitions.get(processDefinition.getId()).add(activity);

            if (activity instanceof ConditionalActivityDefinition) {
                initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfTrue());
                initActivitiesCache(processDefinition, ((ConditionalActivityDefinition) activity).getNextActivityIfFalse());
            } 
            else if (activity instanceof ParallelActivityDefinition && 
                    ((ParallelActivityDefinition) activity).getPaths() != null) {
                
                for (ActivityDefinition activityDef : ((ParallelActivityDefinition) activity).getPaths())
                    initActivitiesCache(processDefinition, activityDef);                    
            }
            else {
                initActivitiesCache(processDefinition, activity.getNextActivity());
            }
        }
    }
        
    public void updateProcessDefinitions() {     
        String processEnginePath = String.valueOf(PersistenceService.getInstance().
                getApplicationEntityManager().getConfiguration().get("processEnginePath")); //NOI18N
        File processDefDir = new File(processEnginePath + "/process/definitions"); //NOI18N
        
        if (processDefDir.exists()) {
            for (File processDefFile : processDefDir.listFiles()) {
                if (processDefFile.isFile()) {
                    long processDefId = Long.valueOf(processDefFile.getName().substring(0, 1));

                    ProcessDefinition processDef = getProcessDefinition(processDefId, processDefFile);
                    cacheProcessDefinition(processDef);
                }
            }
        } else
            System.out.println(String.format("[KUWAIBA] [%s] Process definitions directory (%s) not found", 
                    Calendar.getInstance().getTime(), processDefDir.getAbsolutePath()));
    }
    
    public ProcessDefinition getProcessDefinition(long processDefId, File processDefFile) {
        ProcessDefinitionLoader processDefinitionLoader = new ProcessDefinitionLoader(processDefId);
        byte[] processDefinitionStructure = ProcessDefinitionLoader.getFileAsByteArray(processDefFile);
        try {
            ProcessDefinition processDefinition = processDefinitionLoader.loadProcessDefinition(processDefinitionStructure);
            return processDefinition;
        } catch (XMLStreamException | NumberFormatException | ProcessDefinitionLoader.XMLProcessDefinitionException ex) {
            return null;
        }
    }
    
    /**
     * Retrieves a process definition either from the database or a process definition repository (like a file or an external provider)
     * @param id The id of the process definition
     * @return The process definition mapped as a Java object
     * @throws ApplicationObjectNotFoundException When the process definition could not be found
     */
    public ProcessDefinition getProcessDefinition(long id) throws ApplicationObjectNotFoundException {
        for (ProcessDefinition processDefinition : processDefinitions) {
            if (processDefinition.getId() == id)            
                return processDefinition;                
        }
        throw new ApplicationObjectNotFoundException("Process definition can not be found");
    }
    
    public long createProcessInstance(long processInstanceId, long processDefId, String name, String description) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processDefId);
        
        long currentActivity = processDef.getStartActivity().getId();
        
        ProcessInstance processInstance = new ProcessInstance(processInstanceId, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        processInstanceArtifacts.put(processInstance, new HashMap());
        
        return processInstance.getId();
    }
    
    public void setProcessInstance(ProcessInstance processInstance) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        if (relatedProcessInstances.containsKey(processDef)) {
            for (ProcessInstance processIns : relatedProcessInstances.get(processDef)) {
                if (processIns.getId() == processInstance.getId()) {
                    relatedProcessInstances.get(processDef).remove(processIns);
                    break;
                }
            }
        } else
            relatedProcessInstances.put(processDef, new ArrayList());            
        
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        
        if (!processInstanceArtifacts.containsKey(processInstance))
            processInstanceArtifacts.put(processInstance, new HashMap());
                
        renderProcessInstance(processInstance);
    }
    
    public ProcessInstance getProcessInstance(long processInstanceId) throws ApplicationObjectNotFoundException {
        ProcessInstance processInstance = processInstances.get(processInstanceId);
        if (processInstance != null)        
            return processInstance;
        throw new ApplicationObjectNotFoundException("Process instance can not be found");
    }
    
    public ActivityDefinition getActivityDefinition(long processDefinitionId, long activityDefinitionId) throws InventoryException {
        if (processActivityDefinitions.containsKey(processDefinitionId)) {
            
            if (processActivityDefinitions.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinitionId))
                    
                    if (activityDef.getId() == activityDefinitionId)
                        return activityDef;
            }
        }
        throw new InventoryException("Activity definition can not be found") {};
    }
    
    public Artifact getArtifactForActivity(long processInstanceId, long activityId) throws ApplicationObjectNotFoundException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity;
        try {
            activity = getActivityDefinition(processDef.getId(), activityId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
        
        if (activity != null) {
            
            ArtifactDefinition artifactDef = activity.getArfifact();
            
            if (processInstanceArtifacts.containsKey(processInstance)) {
                
                HashMap<ArtifactDefinition, Artifact> artifactInstances = processInstanceArtifacts.get(processInstance);
                
                if (artifactInstances.containsKey(artifactDef))
                    return artifactInstances.get(artifactDef);
            }
        }
        throw new ApplicationObjectNotFoundException("Process Instances Artifact can not be found");
    }
    
    public ArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId) throws InventoryException {
        
        if (processActivityDefinitions.containsKey(processDefinitionId)) {
            
            if (processActivityDefinitions.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : processActivityDefinitions.get(processDefinitionId))
                    if (activityDef.getId() == activityDefinitionId)
                        return activityDef.getArfifact();
                        
            }
        }
        
        throw new InventoryException("Artifact definition can not be found") {};
    }
    
    private boolean getConditionalArtifactContent(Artifact artifact) throws ApplicationObjectNotFoundException {
        if (artifact == null)
            throw new ApplicationObjectNotFoundException("Conditional artifact can not be found");
        
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
            
            reader.close();
            
        } catch (Exception ex) {
            throw new ApplicationObjectNotFoundException("Conditional artifact content malformed");
        }
        return false;
    }
        
    public List<ActivityDefinition> getProcessInstanceActivitiesPath(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        List<ActivityDefinition> result = new ArrayList();              
        result.addAll(getPath(processInstanceId, processDefinition.getId(), processInstance.getCurrentActivity(), activity));
        result.add(getActivityDefinition(processDefinition.getId(), processInstance.getCurrentActivity()));
        return result;
    }
    
    private List<ActivityDefinition> getPath(long processInstanceId, long processDefinitionId, long currentActivityId, ActivityDefinition activityDef) throws InventoryException {
        List<ActivityDefinition> result = new ArrayList();
        
        if (activityDef == null)
            return result;
                        
        if (activityDef.getId() == currentActivityId)
            return result;
        
        if (activityDef instanceof ParallelActivityDefinition &&
            ((ParallelActivityDefinition) activityDef).getSequenceFlow() ==  ParallelActivityDefinition.JOIN) {
            
            return result;
        }
        
        result.add(activityDef);
        
        if (activityDef instanceof ParallelActivityDefinition) {
            
            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDef;
            
            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK && 
                parallelActivityDef.getPaths() != null && 
                parallelActivityDef.getIncomingSequenceFlowId() != -1) {
                
                for (ActivityDefinition path : parallelActivityDef.getPaths())
                    result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, path));
                
                ActivityDefinition joinActivityDef = getActivityDefinition(processDefinitionId, parallelActivityDef.getIncomingSequenceFlowId());
                
                if (joinActivityDef.getId() == currentActivityId)
                    return result;
                
                result.add(joinActivityDef);
                
                if (joinActivityDef instanceof ParallelActivityDefinition) {
                    parallelActivityDef = (ParallelActivityDefinition) joinActivityDef;

                    if(parallelActivityDef.getSequenceFlow() ==  ParallelActivityDefinition.JOIN && 
                       parallelActivityDef.getPaths() != null) {

                        for (ActivityDefinition path : parallelActivityDef.getPaths())
                            result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, path));
                    }
                }
            }
        }
        else if (activityDef instanceof ConditionalActivityDefinition) {
            
            ConditionalActivityDefinition conditionalActivityDef = (ConditionalActivityDefinition) activityDef;
            
            boolean isTrue = false;
            try {
                Artifact artifact = getArtifactForActivity(processInstanceId, conditionalActivityDef.getId());
                isTrue = getConditionalArtifactContent(artifact);
            } catch(Exception ex) {
            }
            if (isTrue)
                result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, conditionalActivityDef.getNextActivityIfTrue()));
            else
                result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, conditionalActivityDef.getNextActivityIfFalse()));
        }
        else
            result.addAll(getPath(processInstanceId, processDefinitionId, currentActivityId, activityDef.getNextActivity()));
                
        return result;
    }
    
    public ActivityDefinition getNextActivityToParallelActivity(long processDefinitionId, long activityDefinitionId) throws ApplicationObjectNotFoundException {
        ActivityDefinition activityDefinition;    
        
        try {
            activityDefinition = getActivityDefinition(processDefinitionId, activityDefinitionId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
        
        if (activityDefinition instanceof ParallelActivityDefinition) {
            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;
            
            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.FORK) {
                    try {
                        long incomingSeqFlowId = ((ParallelActivityDefinition) activityDefinition).getIncomingSequenceFlowId();
                        return getActivityDefinition(processDefinitionId, incomingSeqFlowId);
                        
                    } catch (InventoryException ex) {
                        throw new ApplicationObjectNotFoundException(ex.getMessage());
                    }
            }
        }
        return activityDefinition;
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(long processInstanceId, long currentActivityId) throws ApplicationObjectNotFoundException {
        
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activityDefinition;    

        try {
            activityDefinition = getActivityDefinition(processDefinition.getId(), currentActivityId);
        } catch (InventoryException ex) {
            throw new ApplicationObjectNotFoundException(ex.getMessage());
        }
                
        if (activityDefinition instanceof ConditionalActivityDefinition) {
            Artifact artifact = null;

            try {
                artifact = getArtifactForActivity(processInstanceId, activityDefinition.getId());
            } catch(Exception ex) {
            }
            boolean isTrue = false;

            if (artifact != null)
                isTrue = getConditionalArtifactContent(artifact);

            if (isTrue)
                return getNextActivityToParallelActivity(processDefinition.getId(), ((ConditionalActivityDefinition) activityDefinition).getNextActivityIfTrue().getId());
            else
                return getNextActivityToParallelActivity(processDefinition.getId(), ((ConditionalActivityDefinition) activityDefinition).getNextActivityIfFalse().getId());
        }
        if (activityDefinition instanceof ParallelActivityDefinition) {

            ParallelActivityDefinition parallelActivityDef = (ParallelActivityDefinition) activityDefinition;

            if (parallelActivityDef.getSequenceFlow() == ParallelActivityDefinition.JOIN && 
                parallelActivityDef.getPaths() != null && 
                !parallelActivityDef.getPaths().isEmpty()) {

                return parallelActivityDef.getPaths().get(0);
            }
            else {
                throw new ApplicationObjectNotFoundException("Next activity can not be found");
            }
        }
        return getNextActivityToParallelActivity(processDefinition.getId(), activityDefinition.getNextActivity().getId());
    }
    
    public ActivityDefinition getNextActivityForProcessInstance(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        return getNextActivityForProcessInstance(processInstanceId, processInstance.getCurrentActivity());
    }
        
    public void updateActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
                
        ActivityDefinition activity = getActivityDefinition(processDefinition.getId(), activityDefinitionId);
        
        if (activity != null && artifact != null) {
            if (processInstanceArtifacts.containsKey(processInstance)) {
                                
                if (activity instanceof ConditionalActivityDefinition) {
                    Artifact oldArtifact = null;
                    
                    try {
                        oldArtifact = getArtifactForActivity(processInstanceId, activity.getId());
                    } catch(Exception ex) {
                    }
                    
                    if (oldArtifact != null) {
                        boolean oldValue = getConditionalArtifactContent(oldArtifact);
                        boolean newValue = getConditionalArtifactContent(artifact);
                        
                        if (oldValue != newValue)
                            processInstance.setCurrentActivity(activity.getId());
                    }
                }
                
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                
                artifactInstance.put(activity.getArfifact(), artifact);
                
            } else
                throw new InventoryException("Process instance can not be found") {};
            
            processInstance.setArtifactsContent(processInstanceAsXML(processInstanceId));
        }
    }
    
    public void commitActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
                
        ActivityDefinition activity = getActivityDefinition(processDefinition.getId(), activityDefinitionId);
        
        if (activity != null) {
            if (processInstanceArtifacts.containsKey(processInstance)) {
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                
                if (processInstance.getCurrentActivity() == activityDefinitionId)
                    artifactInstance.put(activity.getArfifact(), artifact);
                else
                    throw new InventoryException("Process instance artifact can no be committed for the first time") {};
            }
                        
            processInstance.setArtifactsContent(processInstanceAsXML(processInstanceId));
            
            ActivityDefinition nextActivity = getNextActivityForProcessInstance(processInstanceId);
            if (nextActivity != null)
                processInstance.setCurrentActivity(nextActivity.getId());
        }
    }
    
    public void setProcessInstances(long processDefinitionId, List<ProcessInstance> lstProcessInstances) throws InventoryException {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            relatedProcessInstances.replace(processDefinition, lstProcessInstances);
        else
            relatedProcessInstances.put(processDefinition, lstProcessInstances);
    }
    
    public List<ProcessInstance> getProcessInstances(long processDefinitionId) throws InventoryException {
        ProcessDefinition processDefinition = getProcessDefinition(processDefinitionId);
        
        if (relatedProcessInstances.containsKey(processDefinition))
            return relatedProcessInstances.get(processDefinition);
        
        throw new InventoryException("Process instance can not be found") {};
    }
    
    public List<ProcessDefinition> getProcessDefinitions() {
        return processDefinitions;
    }
    
    public Artifact getArtifact(long processInstanceId, long activityDefinitionId) throws InventoryException {
        if (processInstances.containsKey(processInstanceId)) {
            ProcessInstance processInstance = getProcessInstance(processInstanceId);
                        
            ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
            
            if (processActivityDefinitions.containsKey(processDefinition.getId())) {
                List<ActivityDefinition> activityDefs = processActivityDefinitions.get(processDefinition.getId());
                
                for (ActivityDefinition activityDef : activityDefs) {
                    
                    if (activityDef.getId() == activityDefinitionId) {
                        if (processInstanceArtifacts.containsKey(processInstance) && 
                            processInstanceArtifacts.get(processInstance).containsKey(activityDef.getArfifact())) 
                                return processInstanceArtifacts.get(processInstance).get(activityDef.getArfifact());
                    }
                }
            }
        }
        return null;
    }
    
    private byte[] processInstanceAsXML(long processInstanceId) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
                
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            final String TAG_PROCESS_INSTANCE = "processInstance";
            final String TAG_PROCESS_ARTIFACTS = "artifacts";
            final String TAG_PROCESS_ARTIFACT = "artifact";
            final String TAG_PROCESS_CONTENT = "content";
            final String TAG_PROCESS_SHARES = "shares";
            final String TAG_PROCESS_SHARE = "share";
            
            final String ATTR_ID = "id";
            final String ATTR_NAME = "name";
            final String ATTR_DESCRIPTION = "description";
            final String ATTR_CURRENT_ACTIVITY_ID = "currentActivityId";
            final String ATTR_PROCESS_DEFINITION_ID = "processDefinitionId";
            final String ATTR_CONTENT_TYPE = "contentType";
            final String ATTR_ARTIFACT_DEFINTION_ID = "artifactDefinitionId";
            final String ATTR_KEY = "key";
            final String ATTR_VALUE = "value";
            final String ATTR_CREATION_DATE = "creationDate";
            final String ATTR_COMMIT_DATE = "commitDate";
            
            QName tagProcessInstance = new QName(TAG_PROCESS_INSTANCE);
            QName tagArtifacts = new QName(TAG_PROCESS_ARTIFACTS);
            QName tagArtifact = new QName(TAG_PROCESS_ARTIFACT);
            QName tagContent = new QName(TAG_PROCESS_CONTENT);
            QName tagShares = new QName(TAG_PROCESS_SHARES);
            QName tagShare = new QName(TAG_PROCESS_SHARE);
            
            xmlew.add(xmlef.createStartElement(tagProcessInstance, null, null));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_ID), Long.toString(processInstance.getId()))); //process instance id to remove
            xmlew.add(xmlef.createAttribute(new QName(ATTR_NAME), Long.toString(processInstance.getId())));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_DESCRIPTION), Long.toString(processInstance.getId())));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_CURRENT_ACTIVITY_ID), Long.toString(processInstance.getId())));
            xmlew.add(xmlef.createAttribute(new QName(ATTR_PROCESS_DEFINITION_ID), Long.toString(processInstance.getId())));
            
            xmlew.add(xmlef.createStartElement(tagArtifacts, null, null));
            
            List<ActivityDefinition> activityDefs = new ArrayList();

            if (processActivityDefinitions.containsKey(processInstance.getProcessDefinition()))
                activityDefs = processActivityDefinitions.get(processInstance.getProcessDefinition());
            
            List<ActivityDefinition> path = getProcessInstanceActivitiesPath(processInstanceId);
            if (path != null) {
                for (ActivityDefinition activityDefinition : activityDefs) {
                    
                    Artifact artifact = null;                            
                    try {                    
                        artifact = getArtifactForActivity(processInstanceId, activityDefinition.getId());
                    } catch(InventoryException ex) {
                    }
                    
                    if (artifact != null) {
                        
                        boolean contain = false;
                        for (ActivityDefinition a : path) {
                            if (activityDefinition.getId() == a.getId()) {
                                contain = true;
                                break;
                            }
                        }
                        if (!contain) {
                            if (artifact.getSharedInformation() != null)
                                artifact.getSharedInformation().add(new StringPair("__interrupted__", "true"));
                        }
                        
                        xmlew.add(xmlef.createStartElement(tagArtifact, null, null));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ID), artifact.getId()));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_NAME), artifact.getName() != null ? artifact.getName() : ""));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_CONTENT_TYPE), artifact.getContentType() != null ? artifact.getContentType() : ""));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ARTIFACT_DEFINTION_ID), Long.toString(activityDefinition.getArfifact().getId())));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_CREATION_DATE), Long.toString(artifact.getCreationDate())));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_COMMIT_DATE), Long.toString(artifact.getCommitDate())));
                        
                        xmlew.add(xmlef.createStartElement(tagContent, null, null));
                        xmlew.add(xmlef.createCData(new String(artifact.getContent())));
                        xmlew.add(xmlef.createEndElement(tagContent, null));
                        
                        if (artifact.getSharedInformation() != null && !artifact.getSharedInformation().isEmpty()) {
                            xmlew.add(xmlef.createStartElement(tagShares, null, null));
                            for (StringPair share : artifact.getSharedInformation()) {
                                xmlew.add(xmlef.createStartElement(tagShare, null, null));                                                                
                                xmlew.add(xmlef.createAttribute(new QName(ATTR_KEY), share.getKey() != null ? share.getKey() : ""));
                                xmlew.add(xmlef.createAttribute(new QName(ATTR_VALUE), share.getValue() != null ? share.getValue() : ""));
                                xmlew.add(xmlef.createEndElement(tagShare, null));
                            }
                            xmlew.add(xmlef.createEndElement(tagShares, null));
                        }

                        xmlew.add(xmlef.createEndElement(tagArtifact, null));
                    }
                }
            }
            xmlew.add(xmlef.createEndElement(tagArtifacts, null));
                        
            xmlew.add(xmlef.createEndElement(tagProcessInstance, null));
            
            xmlew.close();
            return baos.toByteArray();
            
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
        
    private void renderProcessInstance(ProcessInstance processInstance) throws InventoryException {
        if (processInstance.getArtifactsContent() == null)
            return;
        
        List<ActivityDefinition> activityDefs = new ArrayList();
        
        if (processActivityDefinitions.containsKey(processInstance.getProcessDefinition()))
            activityDefs = processActivityDefinitions.get(processInstance.getProcessDefinition());
        
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(processInstance.getArtifactsContent());
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
                        
            final String TAG_PROCESS_ARTIFACT = "artifact";
            final String TAG_PROCESS_CONTENT = "content";
            final String TAG_PROCESS_SHARES = "shares";
            final String TAG_PROCESS_SHARE = "share";
            
            final String ATTR_ID = "id";
            final String ATTR_NAME = "name";
            final String ATTR_CONTENT_TYPE = "contentType";
            final String ATTR_ARTIFACT_DEFINTION_ID = "artifactDefinitionId";
            final String ATTR_KEY = "key";
            final String ATTR_VALUE = "value";
            final String ATTR_CREATION_DATE = "creationDate";
            final String ATTR_COMMIT_DATE = "commitDate";
            
            QName tagArtifact = new QName(TAG_PROCESS_ARTIFACT);
            QName tagContent = new QName(TAG_PROCESS_CONTENT);
            QName tagShares = new QName(TAG_PROCESS_SHARES);
            QName tagShare = new QName(TAG_PROCESS_SHARE);
            
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagArtifact)) {
                        
                        reader.getAttributeValue(null, ATTR_ID);
                        String name = reader.getAttributeValue(null, ATTR_NAME);
                        String contentType = reader.getAttributeValue(null, ATTR_CONTENT_TYPE);
                        long artifactDefId = Long.valueOf(reader.getAttributeValue(null, ATTR_ARTIFACT_DEFINTION_ID));
                        long creationDate = Long.valueOf(reader.getAttributeValue(null, ATTR_CREATION_DATE) != null ? reader.getAttributeValue(null, ATTR_CREATION_DATE) : "0");
                        long commitDate = Long.valueOf(reader.getAttributeValue(null, ATTR_COMMIT_DATE) != null ? reader.getAttributeValue(null, ATTR_COMMIT_DATE) : "0");
                        
                        byte[] content = null;
                        List<StringPair> shares = new ArrayList();
                                                
                        while (true) {
                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(tagShares)) {
                                    while (true) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            
                                            if (reader.getName().equals(tagShare)) {
                                                String key = reader.getAttributeValue(null, ATTR_KEY);
                                                String value = reader.getAttributeValue(null, ATTR_VALUE);
                                                shares.add(new StringPair(key, value));
                                            }
                                        }
                                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {

                                            if (reader.getName().equals(tagShares))
                                                break;
                                        }
                                        reader.next();                                        
                                    }
                                }
                            }
                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(tagContent)) {
                                    content = reader.getElementText().getBytes();
                                }
                            }
                            if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                
                                if (reader.getName().equals(tagArtifact))
                                    break;
                            }
                            reader.next();
                        }
                        Artifact artifact = new Artifact(UUID.randomUUID().toString(), name, contentType, content, shares, creationDate, commitDate);
                                                
                        for (ActivityDefinition activityDef : activityDefs) {
                            
                            if (artifactDefId == activityDef.getArfifact().getId()) {
                                
                                ArtifactDefinition artifactDef = activityDef.getArfifact();
                                
                                processInstanceArtifacts.get(processInstance).put(artifactDef, artifact);
                                
                                break;
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}