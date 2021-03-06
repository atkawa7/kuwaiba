/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.persistenceservice.impl;

import org.neo4j.graphdb.RelationshipType;


/**
 * Possible relationship types
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public enum RelTypes implements RelationshipType{
    
    ROOT, //Relationship to the root node
    EXTENDS, //Inheritance
    HAS_ATTRIBUTE, //A class has attributes
    IMPLEMENTS, //A class implements an interface
    BELONGS_TO_CATEGORY, //A class belongs to a category
    INSTANCE_OF, //An object is instance of a given class
    CHILD_OF, //An object is child of a given object
    RELATED_TO, //Represents the many-to-one, many-to-may relationships (like type, responsible, etc)
    BELONGS_TO_GROUP, //Used to associate a user to a group (group of user)
    OWNS_QUERY, //Used to asociate a user to a query
    OWNS_POOL, //Used to asociate a user to a pool
    DUMMY_ROOT, //Used to associate the db root object with application dummy root
    POSSIBLE_CHILD, //Used to build the containment hierarchy
    HAS_VIEW, //Used to link an object to a particular view
    HAS_HISTORY_ENTRY, //Used to link an object to a particular historic entry
    RELATED_TO_SPECIAL, //Used to implement relationships for domain specific models
    CHILD_OF_SPECIAL, //Used to implement the parent-child relationship for domain specific models
    GROUPS_ROOT_NODE, //Used to associate root node whit the root group node
    HAS_PRIVILEGE, //Used to associate the groups nodes with methods group node
}
