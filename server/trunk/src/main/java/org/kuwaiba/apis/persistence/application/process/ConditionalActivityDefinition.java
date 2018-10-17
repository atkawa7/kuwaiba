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

import java.util.List;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConditionalActivityDefinition extends ActivityDefinition {
    private ActivityDefinition nextActivityIfTrue;
    private ActivityDefinition nextActivityIfFalse;
    
    public ConditionalActivityDefinition(long id, String name, String description, 
        int type, boolean confirm, String color, ArtifactDefinition arfifact, Actor actor, List<Kpi> kpis, List<KpiAction> kpiActions) {
        
        super(id, name, description, type, arfifact, actor, kpis, kpiActions, false, confirm, color);
    }
    
    @Override
    public boolean isIdling() {
        return false;
    }
    
    public ActivityDefinition getNextActivityIfTrue() {
        return nextActivityIfTrue;
    }
    
    public void setNextActivityIfTrue(ActivityDefinition nextActivityIfTrue) {
        this.nextActivityIfTrue = nextActivityIfTrue;
    }
    
    public ActivityDefinition getNextActivityIfFalse() {
        return nextActivityIfFalse;
    }
    
    public void setNextActivityIfFalse(ActivityDefinition nextActivityIfFalse) {
        this.nextActivityIfFalse = nextActivityIfFalse;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
