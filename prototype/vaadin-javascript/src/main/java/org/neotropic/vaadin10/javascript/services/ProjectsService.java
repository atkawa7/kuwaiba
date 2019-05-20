/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.vaadin10.javascript.services;

import org.springframework.stereotype.Service;

/**
 * A simple service to demonstrate how to inject services in views
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ProjectsService {
    
    public String createProject() {
        return "var tasks = {\n" +
"		data: [\n" +
"			{\n" +
"				id: 1, text: \"Project #2\", start_date: \"01-04-2018\", duration: 18, order: 10,\n" +
"				progress: 0.4, open: true\n" +
"			},\n" +
"			{\n" +
"				id: 2, text: \"Task #1\", start_date: \"02-04-2018\", duration: 8, order: 10,\n" +
"				progress: 0.6, parent: 1\n" +
"			},\n" +
"			{\n" +
"				id: 3, text: \"Task #2\", start_date: \"11-04-2018\", duration: 8, order: 20,\n" +
"				progress: 0.6, parent: 1\n" +
"			}\n" +
"		],\n" +
"		links: [\n" +
"			{id: 1, source: 1, target: 2, type: \"1\"},\n" +
"			{id: 2, source: 2, target: 3, type: \"0\"}\n" +
"		]\n" +
"	};\n" +
"\n" +
"	gantt.init(\"gantt\");\n" +
"\n" +
"\n" +
"	gantt.parse(tasks);";
    }
}
