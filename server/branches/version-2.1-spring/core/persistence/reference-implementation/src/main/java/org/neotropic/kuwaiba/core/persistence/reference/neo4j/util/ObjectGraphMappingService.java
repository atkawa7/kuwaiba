/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.core.persistence.reference.neo4j.util;

import groovy.lang.GroovyClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterator;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Validator;
import org.neotropic.kuwaiba.core.apis.persistence.application.ValidatorDefinition;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides methods to map nodes into java Objects.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class ObjectGraphMappingService {
    /**
     * A classloader to place all the validator definition classes created on-the-fly.
     */
    private final GroovyClassLoader validatorDefinitionsClassLoader = new GroovyClassLoader();
    /**
     * Reference to the Connection Manager
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the Metadata Entity Manager
     */
    @Autowired
    private MetadataEntityManager mem;
    
    public BusinessObjectLight createObjectLightFromNode (Node instance) {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        
        //First, we create the naked business object, without validators
        BusinessObjectLight res = new BusinessObjectLight(className, (String)instance.getProperty(Constants.PROPERTY_UUID), 
                (String)instance.getProperty(Constants.PROPERTY_NAME));
        
        //Then, we check the cache for validator definitions
        List<ValidatorDefinition> validatorDefinitions = CacheManager.getInstance().getValidatorDefinitions(className);
        if (validatorDefinitions == null) { //Since the validator definitions are not cached, we retrieve them for the object class and its super classes
            validatorDefinitions = new ArrayList<>();
            try {
                List<ClassMetadataLight> classHierarchy = mem.getUpstreamClassHierarchy(className, true);
                //The query returns the hierarchy from the subclass to the super class, and we reverse it so the lower level validator definitions 
                //have a higher priority (that is, are processed the last)
                Collections.reverse(classHierarchy); 
                for (ClassMetadataLight aClass : classHierarchy) {
                    ResourceIterator<Node> validatorDefinitionNodes = connectionManager.getConnectionHandler().findNodes(Label.label(Constants.LABEL_VALIDATOR_DEFINITIONS), 
                            Constants.PROPERTY_CLASS_NAME, 
                            aClass.getName());
                    
                    while (validatorDefinitionNodes.hasNext()) {
                        Node aValidatorDefinitionNode = validatorDefinitionNodes.next();
                        String script = (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT);
                        
                        if (!script.trim().isEmpty()) { //Empty scripts are ignored
                            try {
                                //We will load on-the-fly a ValidatorDefinition subclass and instantiate an object from it. The the signature class defined in the 
                                //script file should be something like "public class %s extends ValidatorDefinition" and implement the "run" mathod. The name of the class
                                //will be built dynamically based on the id of the validator definition and a fixed prefix. This is done so the user doesn't use accidentally a
                                //class name already in use by another validator definition.
                                String validatorDefinitionClassName = "ValidatorDefinition" + aValidatorDefinitionNode.getId();
                                Class validatorDefinitionClass = validatorDefinitionsClassLoader.parseClass(
                                        String.format(script, validatorDefinitionClassName, validatorDefinitionClassName));
                            
                                ValidatorDefinition validatorDefinitionInstance =  (ValidatorDefinition)validatorDefinitionClass.
                                        getConstructor(long.class, String.class, String.class, String.class, String.class, boolean.class).
                                        newInstance(aValidatorDefinitionNode.getId(), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_NAME), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                aClass.getName(), 
                                                (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                                (boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED));

                                validatorDefinitions.add(validatorDefinitionInstance);
                            } catch (Exception ex) { //If there's an error parsing the script or instantiating the class, this validator definition will be ignored and the error logged
                                System.out.println(String.format("[KUWAIBA]   %s", ex.getLocalizedMessage()));
                                //ex.printStackTrace();
                            }
                        }
                    }
                }
                
                //Now we cachethe results
                CacheManager.getInstance().addValidatorDefinitions(className, validatorDefinitions);
            } catch (MetadataObjectNotFoundException ex) {
                //Should not happen
            }    
        }
        
        List<Validator> validators = new ArrayList<>();
        
        //Now we run the applicable validator definitions
        validatorDefinitions.forEach((aValidatorDefinition) -> {
            try {
                String script = aValidatorDefinition.getScript();
                if (aValidatorDefinition.isEnabled()) {
                    Validator validator = aValidatorDefinition.run(className, (String)instance.getProperty(Constants.PROPERTY_UUID));
                    if (validator != null) //It's possible that after evaluating the condition nothing should be done, so the method "run" could actually return null
                        validators.add(validator);
                }
            } catch (Exception ex) { //Errors will be logged and the validator definition skipped
                System.out.println(String.format("[KUWAIBA] An unexpected error occurred while evaluating validator %s in object %s(%s): %s", 
                        aValidatorDefinition.getName(), instance.getProperty(Constants.PROPERTY_NAME), 
                        instance.getId(), ex.getLocalizedMessage()));
            }
        });
        
        res.setValidators(validators);
        return res;
    }
}
