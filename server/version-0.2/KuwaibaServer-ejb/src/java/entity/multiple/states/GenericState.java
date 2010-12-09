/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package entity.multiple.states;

import entity.multiple.GenericObjectList;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;


/**
 * Simple state. States are cycled depending on rules, implementing a simple state machine
 * that's why there is next states defined
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class GenericState extends GenericObjectList{
    protected Long nextState; //Id of the next state. I prefer to handle the relations
                              //within the same table using plain Longs values rather than a relationship
                              //to a GenericState

    public Long getNextState() {
        return nextState;
    }

    public void setNextState(Long nextState) {
        this.nextState = nextState;
    }

}
