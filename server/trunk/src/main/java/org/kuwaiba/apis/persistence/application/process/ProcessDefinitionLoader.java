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
package org.kuwaiba.apis.persistence.application.process;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessDefinitionLoader {
    private class Tag {
        public static final String PROCESS_DEFINITION = "processDefinition"; //NOI18N
        public static final String ACTIVITY_DEFINITIONS = "activityDefinitions"; //NOI18N
        public static final String ACTIVITY_DEFINITION = "activityDefinition"; //NOI18N
        public static final String ARTIFACT_DEFINITION = "artifactDefinition"; //NOI18N
        public static final String DEFINITION = "definition"; //NOI18N
        public static final String ACTORS = "actors"; //NOI18N
        public static final String ACTOR = "actor"; //NOI18N
        public static final String PATHS = "paths"; //NOI18N
        public static final String PATH = "path"; //NOI18N
        public static final String PARAMETERS = "parameters"; //NOI18N
        public static final String PARAMETER = "parameter"; //NOI18N
    }    
    
    private class Attribute {
        public static final String NAME = "name"; //NOI18N
        public static final String DESCRIPTION = "description"; //NOI18N
        public static final String CREATION_DATE = "creationDate"; //NOI18N
        public static final String VERSION = "version"; //NOI18N
        public static final String ENABLED = "enabled"; //NOI18N
        public static final String ID = "id"; //NOI18N
        public static final String TYPE = "type"; //NOI18N
        public static final String SHARED = "shared"; //NOI18N
        public static final String NEXT_ACTIVITY = "nextActivity"; //NOI18N
        public static final String START_ACTIVITY_ID="startActivityId"; //NOI18N
        public static final String NEXT_ACTIVITY_DEFINITION_ID = "nextActivityDefinitionId"; //NOI18N
        public static final String ACTOR_ID = "actorId"; //NOI18N
    }
    public class XMLProcessDefinitionException extends Exception {
        
        public XMLProcessDefinitionException(String message) {
            super(message);
        }      
    }
    private final long processDefinitionId;
        
    public ProcessDefinitionLoader(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;        
    }
    
    public ProcessDefinition loadProcessDefinition(byte[] processDefinitionStructure) throws XMLStreamException, NumberFormatException, XMLProcessDefinitionException {
        
        if (processDefinitionStructure == null)
            return null;
        
        ProcessDefinition processDefinition = null;
        long startActivityId = -1;
        
        HashMap<Long, Actor> actors = new HashMap();
        HashMap<Long, ActivityDefinition> activityDefinitions = new HashMap();
        HashMap<Long, ArtifactDefinition> artifactDefinitions = new HashMap();
        HashMap<Long, List<Long>> paths = new HashMap();
        HashMap<Long, Long> activityartifact = new HashMap();
        HashMap<Long, Long> activityactor = new HashMap();
        
        QName tagProcessDefinition = new QName(Tag.PROCESS_DEFINITION);
        QName tagActors = new QName(Tag.ACTORS);
        QName tagActor = new QName(Tag.ACTOR);
        QName tagActivityDefinitions = new QName(Tag.ACTIVITY_DEFINITIONS);
        QName tagActivityDefinition = new QName(Tag.ACTIVITY_DEFINITION);
        QName tagArtifactDefinition = new QName(Tag.ARTIFACT_DEFINITION);
        QName tagPaths = new QName(Tag.PATHS);
        QName tagPath = new QName(Tag.PATH);
        QName tagParameters = new QName(Tag.PARAMETERS);
        QName tagParameter = new QName(Tag.PARAMETER);
                
        XMLInputFactory xif = XMLInputFactory.newInstance();
        ByteArrayInputStream bais = new ByteArrayInputStream(processDefinitionStructure);
        XMLStreamReader reader = xif.createXMLStreamReader(bais);
                
        while (reader.hasNext()) {
            reader.next();
            
            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagProcessDefinition)) {
                    
                    processDefinition = new ProcessDefinition(
                        processDefinitionId,
                        reader.getAttributeValue(null, Attribute.NAME),
                        reader.getAttributeValue(null, Attribute.DESCRIPTION),
                        Long.valueOf(reader.getAttributeValue(null, Attribute.CREATION_DATE)),
                        reader.getAttributeValue(null, Attribute.VERSION),
                        Boolean.valueOf(reader.getAttributeValue(null, Attribute.ENABLED)),
                        null
                    );
                    startActivityId = Long.valueOf(reader.getAttributeValue(null, Attribute.START_ACTIVITY_ID));
                }
                if (reader.getName().equals(tagActors)) {
                    while(true) {
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                            reader.getName().equals(tagActor)) {
                            
                            Actor actor = new Actor(
                                Long.valueOf(reader.getAttributeValue(null, Attribute.ID)), 
                                reader.getAttributeValue(null, Attribute.NAME), 
                                Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE)));
                            
                            if (!actors.containsKey(actor.getId()))
                                actors.put(actor.getId(), actor);
                            else
                                throw new XMLProcessDefinitionException(String.format("The Process Definition Structure is malformed the Actor id %s is in used", actor.getId()));
                        }
                        
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                            reader.getName().equals(tagActors)) {
                            
                            break;
                        }
                        reader.next();
                    }
                }
                if (reader.getName().equals(tagActivityDefinitions)) {
                    while(true) {
                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                            reader.getName().equals(tagActivityDefinition)) {
                            
                            long activityDefinitionId = Long.valueOf(reader.getAttributeValue(null, Attribute.ID));
                            if (!activityDefinitions.containsKey(activityDefinitionId)) {
                                
                                int activityDefinitionType = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));
                                
                                ActivityDefinition activityDefinition = null;
                                
                                if (activityDefinitionType == ActivityDefinition.TYPE_CONDITIONAL) {
                                    
                                    activityDefinition = new ConditionalActivityDefinition(
                                        activityDefinitionId, 
                                        reader.getAttributeValue(null, Attribute.NAME), 
                                        reader.getAttributeValue(null, Attribute.DESCRIPTION), 
                                        activityDefinitionType, 
                                        null, 
                                        null);
                                    
                                } else {
                                    
                                    activityDefinition = new ActivityDefinition(
                                        activityDefinitionId, 
                                        reader.getAttributeValue(null, Attribute.NAME), 
                                        reader.getAttributeValue(null, Attribute.DESCRIPTION), 
                                        activityDefinitionType, 
                                        null, 
                                        null);
                                }
                                activityDefinitions.put(activityDefinitionId, activityDefinition);
                                activityactor.put(activityDefinitionId, Long.valueOf(reader.getAttributeValue(null, Attribute.ACTOR_ID)));
                                                            
                            while (true) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagPaths)) {
                                    
                                    if (!paths.containsKey(activityDefinitionId))
                                        paths.put(activityDefinitionId, new ArrayList());
                                                                        
                                    reader.nextTag();
                                    
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                        reader.getName().equals(tagPath)) {
                                        
                                        long nextActivityDefinitionId = Long.valueOf(reader.getElementText());
                                        
                                        if (!paths.get(activityDefinitionId).contains(nextActivityDefinitionId))
                                            paths.get(activityDefinitionId).add(nextActivityDefinitionId);
                                        else {
                                            throw new XMLProcessDefinitionException(String.format(
                                                "The Process Definition Structure is malformed the Path Next Activity Definition Id %s in the Activity Definition id %s is in used", 
                                                nextActivityDefinitionId, activityDefinitionId));
                                        }
                                    }
                                    if (activityDefinition.getType() == ActivityDefinition.TYPE_CONDITIONAL) {
                                        reader.nextTag();

                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT
                                                && reader.getName().equals(tagPath)) {

                                            long nextActivityDefinitionId = Long.valueOf(reader.getElementText());

                                            if (!paths.get(activityDefinitionId).contains(nextActivityDefinitionId)) {
                                                paths.get(activityDefinitionId).add(nextActivityDefinitionId);
                                            } else {
                                                throw new XMLProcessDefinitionException(String.format(
                                                        "The Process Definition Structure is malformed the Path Next Activity Definition Id %s in the Activity Definition id %s is in used",
                                                        nextActivityDefinitionId, activityDefinitionId));
                                            }
                                        }
                                    }
                                }
                                
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT && 
                                    reader.getName().equals(tagArtifactDefinition)) {
                                                
                                    long artifactDefinitionId = Long.valueOf(reader.getAttributeValue(null, Attribute.ID));

                                    if (!artifactDefinitions.containsKey(artifactDefinitionId)) {
                                        int type = Integer.valueOf(reader.getAttributeValue(null, Attribute.TYPE));

                                        HashMap<String, String> artifactParameters = new HashMap();

                                        reader.nextTag();
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT &&
                                                reader.getName().equals(tagParameters)) {
                                                                                        
                                            while (true) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                    if (reader.getName().equals(tagParameter)) {
                                                        
                                                        String attributeName = reader.getAttributeValue(null, Attribute.NAME);
                                                        String attributeValue = reader.getElementText();

                                                        artifactParameters.put(attributeName, attributeValue);
                                                    }
                                                }
                                                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                                                    
                                                    if (reader.getName().equals(tagParameters))
                                                        break;
                                                }
                                                reader.next();
                                            }
                                        }
                                        byte[] definition = null;
                                        if (type == ArtifactDefinition.TYPE_ATTACHMENT || type == ArtifactDefinition.TYPE_CONDITIONAL) {
                                            definition = artifactParameters.get("definition").getBytes();
                                        } else if (type == ArtifactDefinition.TYPE_FORM) {
                                            definition = getFormArtifactDefinition(artifactParameters.get("definition"));                                            
                                        }
                                        ArtifactDefinition artifactDefinition = new ArtifactDefinition(
                                            artifactDefinitionId,
                                            artifactParameters.get("name"), 
                                            artifactParameters.get("description"), 
                                            artifactParameters.get("version"), 
                                            type, 
                                            definition);
                                        
                                        artifactDefinition.setPreconditionsScript(artifactParameters.containsKey("preconditionsScript") ? artifactParameters.get("preconditionsScript").getBytes() : null);
                                        artifactDefinition.setPostconditionsScript(artifactParameters.containsKey("postconditionsScript") ? artifactParameters.get("postconditionsScript").getBytes() : null);

                                        artifactDefinitions.put(artifactDefinition.getId(), artifactDefinition);
                                        activityartifact.put(activityDefinitionId, artifactDefinition.getId());
                                    }
                                    break;
                                }
                                reader.next();
                            }
                        } else
                            throw new XMLProcessDefinitionException(String.format("The Process Definition Structure is malformed the Activity Definition id %s is in used", activityDefinitionId));
                        }
                        if (reader.getEventType() == XMLStreamConstants.END_ELEMENT && 
                            reader.getName().equals(tagActivityDefinitions)) {
                            
                            break;
                        }
                        reader.next();
                    }
                }
            }
        }
        if (processDefinition != null && activityDefinitions.containsKey(startActivityId)) {
            
            processDefinition.setStartActivity(activityDefinitions.get(startActivityId));
            
            for (Long activityDefinitionId : activityDefinitions.keySet()) {
                
                ActivityDefinition activityDefinition = activityDefinitions.get(activityDefinitionId);
                
                if (activityartifact.containsKey(activityDefinitionId)) {
                    long artifactId = activityartifact.get(activityDefinitionId);
                    
                    if (artifactDefinitions.containsKey(artifactId))
                        activityDefinition.setArfifact(artifactDefinitions.get(artifactId));
                } else {
                    //TODO: exception activity no has artifact
                }
                
                if (activityactor.containsKey(activityDefinitionId)) {
                    long actorId = activityactor.get(activityDefinitionId);
                    
                    if (actors.containsKey(actorId))
                        activityDefinition.setActor(actors.get(actorId));
                    
                } else {
                    //TODO: exception activity no has actor
                }
                
                if (activityDefinition.getType() == ActivityDefinition.TYPE_CONDITIONAL) {
                    if (paths.containsKey(activityDefinitionId)) {
                        List<Long> activityPaths = paths.get(activityDefinitionId);
                        if (activityPaths.size() == 2) {
                            Long nextActivityDefinitionId = activityPaths.get(0);

                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                ((ConditionalActivityDefinition) activityDefinition).setNextActivityIfTrue(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                            
                            nextActivityDefinitionId = activityPaths.get(1);
                            
                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                ((ConditionalActivityDefinition) activityDefinition).setNextActivityIfFalse(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                        }
                    }

                } else {
                    if (paths.containsKey(activityDefinitionId)) {
                        List<Long> activityPaths = paths.get(activityDefinitionId);
                        if (activityPaths.size() == 1) {
                            Long nextActivityDefinitionId = activityPaths.get(0);

                            if (activityDefinitions.containsKey(nextActivityDefinitionId)) {
                                activityDefinition.setNextActivity(activityDefinitions.get(nextActivityDefinitionId));
                            } else {
                                //TODO: exception id next activity no found
                            }
                        }
                    }
                }
                
            }
        } else {
            throw new XMLProcessDefinitionException("The Process Definition Structure is malformed Start Activity Definition cannot be found");
        }
        return processDefinition;
    }
    
    private byte[] getFormArtifactDefinition(String artifactDefinitionId) {
        File file = new File(artifactDefinitionId);
        return getFileAsByteArray(file);
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
