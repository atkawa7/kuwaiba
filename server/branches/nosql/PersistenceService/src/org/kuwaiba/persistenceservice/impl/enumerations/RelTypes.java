/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.kuwaiba.persistenceservice.impl.enumerations;

import org.neo4j.graphdb.RelationshipType;


/**
 * Possible attribute types
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public enum RelTypes implements RelationshipType{
    
    ROOT, //Relationship to the root node
    EXTENDS, //Inheritance
    HAS, //A class has attributes
    IMPLEMENTS, //A class implements an interface
    BELONGS_TO_CATEGORY, //A class belongs to a category
    INSTANCE_OF, //An object is instance of a given class
    CHILD_OF, //An object is child of a given object
    RELATED_TO, //Represents the many-to-one, many-to-may relationships (like type, responsible, etc)
    BELONGS_TO_GROUP, //Used to associate an entity to a group (group of user)
    BELONGS_TO_USER, //Used to associate an entity to a user
    DUMMY_ROOT, //Used to associate dummies class
    POSSIBLE_CHILD //Used to build the containment hierarchy
}
