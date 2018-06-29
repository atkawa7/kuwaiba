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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteConditionalActivityDefinition extends RemoteActivityDefinition implements Serializable {
    
    private RemoteActivityDefinition nextActivityIfTrue;
    private RemoteActivityDefinition nextActivityIfFalse;
        
    public RemoteConditionalActivityDefinition(long id, String name, String description, 
        int type, RemoteArtifactDefinition arfifact, RemoteActor actor) {
        
        super(id, name, description, type, arfifact, actor, false);
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public RemoteActivityDefinition getNextActivityIfTrue() {
        return nextActivityIfTrue;
    }
    
    public void setNextActivityIfTrue(RemoteActivityDefinition nextActivityIfTrue) {
        this.nextActivityIfTrue = nextActivityIfTrue;
    }
    
    public RemoteActivityDefinition getNextActivityIfFalse() {
        return nextActivityIfFalse;
    }
    
    public void setNextActivityIfFalse(RemoteActivityDefinition nextActivityIfFalse) {
        this.nextActivityIfFalse = nextActivityIfFalse;
    }
        
}
