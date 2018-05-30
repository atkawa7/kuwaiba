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
package org.kuwaiba.web.view;

import com.vaadin.ui.Component;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;

/**
 * Renders an artifact type
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public interface ArtifactRenderer {
    /**
     * Return the Vaadin Component to render
     * @return
     */
    public Component renderArtifact();
    /**
     * Gets the content
     * @return
     */
    public byte[] getContent();
    /**
     * Gets the shared information
     * @return
     */
    public List<StringPair> getSharedInformation();
}
