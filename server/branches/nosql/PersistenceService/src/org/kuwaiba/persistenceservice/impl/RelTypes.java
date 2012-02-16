/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.persistenceservice.impl;

import org.neo4j.graphdb.RelationshipType;

/**
 *
 * @author adrian
 */
public enum RelTypes implements RelationshipType{
    
    ROOT,
    EXTENDS,
    HAS,
    IMPLEMENTS,
    IS,
    
}
