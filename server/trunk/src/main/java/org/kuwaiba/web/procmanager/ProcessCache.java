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
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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
import org.kuwaiba.apis.persistence.util.StringPair;
import org.openide.util.Exceptions;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class ProcessCache {
    public static long artifactCounter = 100000;
    
    private final HashMap<Long, List<ActivityDefinition>> activityDefinitions = new HashMap();
    private final List<ProcessDefinition> processDefinitions = new ArrayList();
    private final HashMap<Long, ProcessInstance> processInstances = new HashMap();
    private final HashMap<ProcessDefinition, List<ProcessInstance>> relatedProcessInstances = new HashMap();
////    private final HashMap<ArtifactDefinition, Artifact> artifacts = new HashMap();
    private final HashMap<ProcessInstance, HashMap<ArtifactDefinition, Artifact>> processInstanceArtifacts = new HashMap();
        
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
                activityDefinitions.put(processDefinition.getId(), new ArrayList());
                initActivitiesCache(processDefinition, startActivity);
            }
        }
    }
    
    private void initActivitiesCache(ProcessDefinition processDefinition, ActivityDefinition activity) {
        if (activity != null) {
            if (!activityDefinitions.get(processDefinition.getId()).contains(activity)) {
                
////                artifacts.put(activity.getArfifact(), new ArrayList());
                activityDefinitions.get(processDefinition.getId()).add(activity);
                
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
    
    public long createProcessInstance(long processInstanceId, long processDefId, String name, String description) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processDefId);
        
        long currentActivity = processDef.getStartActivity().getId();
        
        ProcessInstance processInstance = new ProcessInstance(processInstanceId, name, description, currentActivity, processDefId);
        
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
////        processInstanceArtifacts.put(processInstance, new ArrayList());
        processInstanceArtifacts.put(processInstance, new HashMap());
        
        return processInstance.getId();
    }
    
    public void setProcessInstance(ProcessInstance processInstance) throws InventoryException {
        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        if (relatedProcessInstances.containsKey(processDef)) {
            if (relatedProcessInstances.get(processDef).contains(processInstance))
                relatedProcessInstances.get(processDef).remove(processInstance);                
        } else
            relatedProcessInstances.put(processDef, new ArrayList());            
        
        relatedProcessInstances.get(processDef).add(processInstance);
        
        if (processInstances.containsKey(processInstance.getId()))
            processInstances.replace(processInstance.getId(), processInstance);
        else
            processInstances.put(processInstance.getId(), processInstance);
        
        if (!processInstanceArtifacts.containsKey(processInstance))
            processInstanceArtifacts.put(processInstance, new HashMap());
//        if (processInstanceArtifacts.containsKey(processInstance))
//            processInstanceArtifacts.replace(processInstance, new HashMap());
//        else
//            processInstanceArtifacts.put(processInstance, new HashMap());
        
        renderProcessInstance(processInstance);
            
////        if (processInstances.containsKey(processInstance.getId())) {
////            processInstances.replace(processInstance.getId(), processInstance);
////        } else {
////            processInstances.put(processInstance.getId(), processInstance);
////        }
/*
        if (!relatedProcessInstances.containsKey(processDef))
            relatedProcessInstances.put(processDef, new ArrayList());
                
        relatedProcessInstances.get(processDef).add(processInstance);
        
        processInstances.put(processInstance.getId(), processInstance);
        processInstanceArtifacts.put(processInstance, new HashMap());
*/
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
            
////            ArtifactDefinition arfifact = activity.getArfifact();
        
////            List<Artifact> poolArtifacts = artifacts.get(arfifact);
            ArtifactDefinition artifactDef = activity.getArfifact();
            
            if (processInstanceArtifacts.containsKey(processInstance)) {
                
                HashMap<ArtifactDefinition, Artifact> artifactInstances = processInstanceArtifacts.get(processInstance);
                
                if (artifactInstances.containsKey(artifactDef))
                    return artifactInstances.get(artifactDef);
            }

//            List<Artifact> poolProcessInsArtifacts = processInstanceArtifacts.get(processInstance);

////            for (Artifact artifact : poolArtifacts) {
                
//                for (Artifact processArtifact : poolProcessInsArtifacts) {
//                    
//                    if (processArtifact.getId() == artifact.getId())
//                        return processArtifact;
//                }
////            }
        }
        throw new InventoryException("Process Instances Artifact can not be found") {};
    }
    
    public ArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, long activityDefinitionId) throws InventoryException {
        
        if (activityDefinitions.containsKey(processDefinitionId)) {
            
            if (activityDefinitions.get(processDefinitionId) != null) {
                
                for (ActivityDefinition activityDef : activityDefinitions.get(processDefinitionId))
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
            if (processInstanceArtifacts.containsKey(processInstance)) {
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                
                if (artifactInstance.containsKey(activity.getArfifact()))
                    artifactInstance.replace(activity.getArfifact(), artifact);
                else
                    throw new InventoryException("Process Instances Artifact can not be found") {};
            } else
                throw new InventoryException("Process Instances can not be found") {};
            
            
////            if (!processInstanceArtifacts.containsKey(processInstance))
////                processInstanceArtifacts.put(processInstance, new ArrayList());
            
////            if (!artifacts.containsKey(activity.getArfifact()))
////                artifacts.put(activity.getArfifact(), new ArrayList());
            
////            if (!processInstanceArtifacts.get(processInstance).contains(artifact))
////                processInstanceArtifacts.get(processInstance).add(artifact);
                        
////            if (!artifacts.get(activity.getArfifact()).contains(artifact))
////                artifacts.get(activity.getArfifact()).add(artifact);
////            if (artifacts.containsKey(activity.getArfifact()))
////                artifacts.replace(activity.getArfifact(), artifact);
////            else
////                artifacts.put(activity.getArfifact(), artifact);
            
            processInstance.setArtifactsContent(processInstanceAsXML(processInstanceId));
        }
    }
    
    public void commitActivity(long processInstanceId, long activityDefinitionId, Artifact artifact) throws InventoryException {
        ProcessInstance processInstance = getProcessInstance(processInstanceId);
        
        ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
        
        ActivityDefinition activity = processDefinition.getStartActivity();
        
        while (activity != null && activity.getId() != activityDefinitionId)
            activity = getNextActivityForProcessInstance(processInstanceId, activity.getId());
        
        if (activity != null) {
            if (processInstanceArtifacts.containsKey(processInstance)) {
                HashMap<ArtifactDefinition, Artifact> artifactInstance = processInstanceArtifacts.get(processInstance);
                if (!artifactInstance.containsKey(activity.getArfifact()))
                    artifactInstance.put(activity.getArfifact(), artifact);
                else
                    throw new InventoryException("Process Instances Artifact can no be committed newly") {};
            }
            /*
            
            */
            
////            if (!processInstanceArtifacts.containsKey(processInstance))
////                processInstanceArtifacts.put(processInstance, new ArrayList());

////            if (!artifacts.containsKey(activity.getArfifact()))
////                artifacts.put(activity.getArfifact(), new ArrayList());
            
////            if (!processInstanceArtifacts.get(processInstance).contains(artifact))
////                processInstanceArtifacts.get(processInstance).add(artifact);
            
////            if (!artifacts.get(activity.getArfifact()).contains(artifact))
////                artifacts.get(activity.getArfifact()).add(artifact);
////            if (artifacts.containsKey(activity.getArfifact()))
////                artifacts.replace(activity.getArfifact(), artifact);
////            else
////                artifacts.put(activity.getArfifact(), artifact);
            
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
        
        throw new InventoryException("Process Instances can not be found") {};
    }
    
    public List<ProcessDefinition> getProcessDefinitions() throws InventoryException {
        return processDefinitions;
    }
    
    public Artifact getArtifact(long processInstanceId, long activityDefinitionId) throws InventoryException {
        if (processInstances.containsKey(processInstanceId)) {
            ProcessInstance processInstance = getProcessInstance(processInstanceId);
                        
            ProcessDefinition processDefinition = getProcessDefinition(processInstance.getProcessDefinition());
            
            if (activityDefinitions.containsKey(processDefinition.getId())) {
                List<ActivityDefinition> activityDefs = activityDefinitions.get(processDefinition.getId());
                
                for (ActivityDefinition activityDef : activityDefs) {
                    
                    if (activityDef.getId() == activityDefinitionId) {
                        if (processInstanceArtifacts.containsKey(processInstance) && 
                            processInstanceArtifacts.get(processInstance).containsKey(activityDef.getArfifact())) {
                            
                            return processInstanceArtifacts.get(processInstance).get(activityDef.getArfifact());
                        }
                    }
                }
            }
        }
        
////        for (Artifact lstArtifact : artifacts.values()) {
////            for (Artifact actifact : lstArtifact) {
////                if (lstArtifact.getId() == id)
////                    return lstArtifact;
////            }
////        }
        
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
            
            List<ActivityDefinition> path = getProcessInstanceActivitiesPath(processInstanceId);
            if (path != null) {
                for (ActivityDefinition activityDefinition : path) {
                    try {                    
                        Artifact artifact = getArtifactForActivity(processInstanceId, activityDefinition.getId());

                        xmlew.add(xmlef.createStartElement(tagArtifact, null, null));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ID), Long.toString(artifact.getId())));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_NAME), artifact.getName()));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_CONTENT_TYPE), artifact.getContentType()));
                        xmlew.add(xmlef.createAttribute(new QName(ATTR_ARTIFACT_DEFINTION_ID), Long.toString(activityDefinition.getArfifact().getId())));

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
                    } catch(InventoryException ex) {
                                                
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
////        ProcessDefinition processDef = getProcessDefinition(processInstance.getProcessDefinition());
        List<ActivityDefinition> activityDefs = new ArrayList();
        
////        if (!relatedProcessInstances.containsKey(processDef))
////            relatedProcessInstances.put(processDef, new ArrayList());
////                
////        relatedProcessInstances.get(processDef).add(processInstance);
////        
////        processInstances.put(processInstance.getId(), processInstance);
////        processInstanceArtifacts.put(processInstance, new HashMap());
        ////                
        if (activityDefinitions.containsKey(processInstance.getProcessDefinition()))
            activityDefs = activityDefinitions.get(processInstance.getProcessDefinition());
        
        try {
            XMLInputFactory xmlif = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(processInstance.getArtifactsContent());
            XMLStreamReader reader = xmlif.createXMLStreamReader(bais);
            
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
            
            QName tagProcessInstance = new QName(TAG_PROCESS_INSTANCE);
            QName tagArtifacts = new QName(TAG_PROCESS_ARTIFACTS);
            QName tagArtifact = new QName(TAG_PROCESS_ARTIFACT);
            QName tagContent = new QName(TAG_PROCESS_CONTENT);
            QName tagShares = new QName(TAG_PROCESS_SHARES);
            QName tagShare = new QName(TAG_PROCESS_SHARE);
            
////            HashMap<Long, Artifact> artifactDefIds = new HashMap();
            
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagArtifact)) {
                        
                        reader.getAttributeValue(null, ATTR_ID);
                        String name = reader.getAttributeValue(null, ATTR_NAME);
                        String contentType = reader.getAttributeValue(null, ATTR_CONTENT_TYPE);
                        long artifactDefId = Long.valueOf(reader.getAttributeValue(null, ATTR_ARTIFACT_DEFINTION_ID));
                        
                        byte[] content = null;
                        List<StringPair> shares = new ArrayList();
                                                
                        while (true) {
                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                if (reader.getName().equals(tagShares)) {
                                    while (true) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                            
                                            if (reader.getName().equals(tagShare)) {
                                                String key = reader.getAttributeValue(null, ATTR_KEY);;
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
                        Artifact artifact = new Artifact(artifactCounter++, name, contentType, content, shares);
                                                
                        for (ActivityDefinition activityDef : activityDefs) {
                            
                            if (artifactDefId == activityDef.getArfifact().getId()) {
                                
                                ArtifactDefinition artifactDef = activityDef.getArfifact();
                                
                                if (processInstanceArtifacts.get(processInstance).containsKey(artifactDef))
                                    processInstanceArtifacts.get(processInstance).replace(artifactDef, artifact);
                                else
                                    processInstanceArtifacts.get(processInstance).put(artifactDef, artifact);
                                break;
                            }
                        }
////                        artifactDefIds.put(artifactDefId, artifact);
                    }
                }
            }
            reader.close();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    } 
    /*
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        Long objectId = Long.valueOf(reader.getElementText());

                        LocalObjectLight lol = CommunicationsStub.getInstance().
                                getObjectInfoLight(objectClass, objectId);
                        if (lol != null)
                            this.addNode(lol).setPreferredLocation(new Point(x, y));
                        else
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("ViewAbleObject of class %s and id %s could not be found and was removed from the topology view", objectClass, objectId));
                    } else {
                        if (reader.getName().equals(qIcon)){ // FREE CLOUDS
                                if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                                    int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                                    int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                                    
                                    long oid = Long.valueOf(reader.getAttributeValue(null,"id"));
                                    LocalObjectLight lol = new LocalObjectLight(oid, reader.getElementText(), null);
                                    this.addNode(lol).setPreferredLocation(new Point(x, y));
                                }
                            }
                        else {
                            if (reader.getName().equals(qEdge)) {
                                Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                                Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                Widget aSideWidget = this.findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                Widget bSideWidget = this.findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, "One or both of the endpoints of a connection could not be found. The connection was removed from the topology view");
                                else {
                                    String edgeName = "topologyEdge" + aSideObject.getId() + bSideObject.getId() + randomGenerator.nextInt(1000);
                                    ConnectionWidget newEdge = (ConnectionWidget)this.addEdge(edgeName);
                                    this.setEdgeSource(edgeName, aSideObject);
                                    this.setEdgeTarget(edgeName, bSideObject);
                                    List<Point> localControlPoints = new ArrayList<>();
                                    while (true) {
                                        reader.nextTag();
                                        if (reader.getName().equals(qControlPoint)) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                String cpx = reader.getAttributeValue(null, "x");
                                                String cpy = reader.getAttributeValue(null, "y");
                                                Point point = new Point();
                                                point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                                localControlPoints.add(point);
                                            }
                                        } else {
                                            newEdge.setControlPoints(localControlPoints, false);
                                            break;
                                        }
                                    }
                                }
                            }// edges endign 
                            else{ // FREE FRAMES
                                if (reader.getName().equals(qPolygon)) { 
                                    long oid = randomGenerator.nextInt(1000);
                                    LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), null);
                                    Widget myPolygon = addNode(lol);
                                    Point p = new Point();
                                    p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                                    myPolygon.setPreferredLocation(p);
                                    Dimension d = new Dimension();
                                    d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                                    Rectangle r = new Rectangle(d);
                                    myPolygon.setPreferredBounds(r);
                                }
                            }//end qPolygon
                        } //end qIcons
                    } // end qNodes
                } // end if
            } // end while
            reader.close();
            
            this.validate();
            this.repaint();
        } catch (NumberFormatException | XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            clear();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }
    */
}
