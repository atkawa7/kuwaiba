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

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementUtil {
    public static boolean hasEventAttribute(AbstractElement element, String eventAttribute) {
        return element.getEvents().containsKey(eventAttribute);
    }
    
    public static void execute() {
//        Binding environmentParameters = new Binding();
//        environmentParameters.setVariable("graphDb", graphDb); //NOI18N
//        environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel);
//        environmentParameters.setVariable("classLabel", classLabel); //NOI18N
//        environmentParameters.setVariable("TaskResult", TaskResult.class); //NOI18N
//        environmentParameters.setVariable("Constants", Constants.class); //NOI18N
//        environmentParameters.setVariable("Direction", Direction.class); //NOI18N
//        environmentParameters.setVariable("RelTypes", RelTypes.class); //NOI18N
//        environmentParameters.setVariable("scriptParameters", scriptParameters); //NOI18N
//        GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
//        Object theResult = shell.evaluate(script);
        //ClassLoader parent, Binding binding
;
        
        
    }    
}
