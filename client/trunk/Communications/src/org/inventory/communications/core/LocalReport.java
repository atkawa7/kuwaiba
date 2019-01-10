/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.communications.core;

import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * Represents a report descriptor.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalReport extends LocalReportLight {
    /**
     * Script text.
     */
    private String script;
    /**
     * Parameters list.
     */
    private List<String> parameters;


    public LocalReport(long id, String name, String description, Boolean enabled,
            Integer type, String script, List<String> parameters) {
        super(id, name, description, enabled, type);
        this.script = script;
        this.parameters = parameters;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        String oldScript = this.script;
        this.script = script;
        firePropertyChangeListener(this, Constants.PROPERTY_SCRIPT, oldScript, script);
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        List<String> oldParameters = this.parameters;
        this.parameters = parameters;
        firePropertyChangeListener(this, Constants.PROPERTY_PARAMETERS, oldParameters, parameters);
    }
}
