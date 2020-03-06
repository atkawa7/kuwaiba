/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.persistence.reference.extras.processman;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class KpiManagerService {
    public KpiResult runActivityKpiAction(
        String kpiName, 
        Artifact artifact, 
        ProcessDefinition processDefinition, 
        ActivityDefinition activityDefinition) {
        
        Binding binding = new Binding();
        
        boolean flag = false;
        Kpi activityKpi = null;
        KpiAction activityKpiAction = null;
        
        if (kpiName != null &&
            artifact != null &&                                
            processDefinition != null &&
            activityDefinition != null && 
            activityDefinition.getKpis() != null) {
            
            for (Kpi akpi : activityDefinition.getKpis()) {
                
                if (akpi.getName() != null && akpi.getName().equals(kpiName)) {
                    
                    if (activityDefinition.getKpiActions() != null) {
                        
                        for (KpiAction kpiAction : activityDefinition.getKpiActions()) {
                            
                            if (akpi.getAction() != null && akpi.getAction().equals(kpiAction.getName())) {
                                flag = true;
                                activityKpiAction = kpiAction;
                                break;
                            }
                        }
                        if (flag) {
                            activityKpi = akpi;
                            break;
                        }
                    }

                    if (processDefinition.getKpiActions() != null) {
                        
                        for (KpiAction kpiAction : processDefinition.getKpiActions()) {
                            if (akpi.getAction() != null && akpi.getAction().equals(kpiAction.getName())) {
                                flag = true;
                                activityKpiAction = kpiAction;
                                break;
                            }
                        }
                        if (flag) {
                            activityKpi = akpi;
                            break;
                        }
                    }
                }
            }
            if (activityKpi != null && activityKpiAction != null && 
                activityKpiAction.getType() == KpiAction.TYPE_ACTIVITY &&
                activityKpiAction.getScript() != null) {

                binding.setVariable("artifact", artifact);
                binding.setVariable("activityKpi", activityKpi);

                GroovyShell shell = new GroovyShell(KpiResult.class.getClassLoader(), binding);

                return (KpiResult) shell.evaluate(activityKpiAction.getScript());
            }
        }

        return null;
    }
}
