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
package com.neotropic.api.forms;

import java.util.HashMap;
import java.util.Random;

/**
 * Executes functions on events
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptRunner {
    private HashMap<String, String> scripts = new HashMap();
    
    private final FormStructure formStructure;
    
    public ScriptRunner(FormStructure formStructure) {
        this.formStructure = formStructure;
        
        scripts.put("setLabelValue", "A Label Value");
    }
    
    public FormStructure getFormStructure() {
        return formStructure;
    }
    
    public void run(AbstractElement element, String eventAttribute) {
                        
        if (hasEventAttribute(element, eventAttribute)) {
                
            String elementId = element.getEvents().get(eventAttribute).get(Constants.Function.OPEN).get(0);

            AbstractElement anElement = getFormStructure().getElementById(elementId);

            if (anElement instanceof ElementSubform)
                anElement.fireElementEvent(new EventDescriptor(Constants.Function.OPEN));
            if (anElement instanceof ElementTextField)
                anElement.fireElementEvent(new EventDescriptor(Constants.Function.SET_VALUE));     
            if (anElement instanceof ElementLabel) {
                Random r = new Random();
                ((ElementLabel) anElement).setValue("***A Label Value***"+r.nextInt());
                anElement.fireElementEvent(new EventDescriptor(Constants.Function.SET_VALUE));
            }
        }
    }
    
    public boolean hasEventAttribute(AbstractElement element, String eventAttribute) {
        return element.getEvents() != null && element.getEvents().containsKey(eventAttribute);
    }
}
