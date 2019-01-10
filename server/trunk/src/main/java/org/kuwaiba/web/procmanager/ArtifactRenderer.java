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
package org.kuwaiba.web.procmanager;

import com.vaadin.ui.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * Renders an artifact type
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class ArtifactRenderer {
    private final List<StringPair> sharedInformation = new ArrayList();
    private final HashMap<String, String> sharedMap = new HashMap();
    
    /**
     * Return the Vaadin Component to render
     * @return
     */
    public abstract Component renderArtifact();
    /**
     * Gets the content
     * @return The content of the artifact
     * @throws Exception Throws if the content is no the expected
     */
    public abstract byte[] getContent() throws Exception;
    /**
     * Gets the shared information
     * @return
     */
    public List<StringPair> getSharedInformation() {
        sharedInformation.clear();
        
        for (String key : sharedMap.keySet())
            sharedInformation.add(new StringPair(key, sharedMap.get(key)));
        
        return sharedInformation;
    }
    
    public HashMap<String, String> getSharedMap() {
        return sharedMap;
    }
}
