/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package com.neotropic.kuwaiba.modules.reporting.model;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * A remote representation of a report (class or inventory level). 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteReport extends RemoteReportLight {
    /**
     * Script text.
     */
    private String script;
    /**
     * Parameters list.
     */
    private List<StringPair> parameters;

    public RemoteReport() {}

    public RemoteReport(long id, String name, String description, boolean enabled,
            int type, String script, List<StringPair> parameters) {
        super(id, name, description, enabled, type);
        this.script = script;
        this.parameters = parameters;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }
}