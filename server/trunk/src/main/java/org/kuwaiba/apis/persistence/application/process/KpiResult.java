/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class KpiResult {
    /** 
     * Compliance level values between 1 and 10, where 1 no compliant and is 10 fully compliant
     */
    private int complianceLevel;
    private List<String> observations;
    private HashMap<String, Object> values;
    
    public KpiResult(int complianceLevel, List<String> observations, HashMap<String, Object> values) {
        this.complianceLevel = complianceLevel;
        this.observations = observations;
        this.values = values;
    }
    
    public int getComplianceLevel() {
        return complianceLevel;
    }
    
    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }
    
    public List<String> getObservations() {
        return observations;
    }
    
    public void setObservations(List<String> observations) {
        this.observations = observations;
    }
    
    public HashMap<String, Object> getValues() {
        return values;
    }
    
    public void getValues(HashMap<String, Object> values) {
        this.values = values;
    }
        
    public static KpiResult runActivityKpiAction(
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
    
    public static KpiResult runProcessKpiAction(Kpi kpi, ProcessDefinition processDefinition) {
        return null;        
    }
}
