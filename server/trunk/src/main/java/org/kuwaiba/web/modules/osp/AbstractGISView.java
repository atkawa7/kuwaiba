 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp;


/**
 * This interface should be implemented by all map components to be used for OSP (Outside Plant) support
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface AbstractGISView {
    /**
     * Gets the module's name. Must be unique, otherwise, the system will only take last one loaded at application's startup
     * @return The module's name
     */
    public abstract String getName();
    /**
     * Gets the module description
     * @return he module's description
     */
    public abstract String getDescription();
    /**
     * Gets the module's version
     * @return The module's version
     */
    public abstract String getVersion();
    /**
     * Gets the module's vendor
     * @return The module's vendor
     */
    public abstract String getVendor();
    
    /**
     * Remove all elements on the view.
     */
    public abstract void clear();
    
    /**
     * The XML representation of the view. Typically used to serialize it
     * @return XML document as a byte arrays
     */
    public abstract byte[] getAsXML();
    
    /**
     * Renders a view. That is, reads its structure and renders its content
     * @param structure The XML structure as an byte array (which is actually a dump of the stored xml file)
     * @throws IllegalArgumentException If the XML has an unexpected format or value
     */
    public abstract void render(byte[] structure) throws IllegalArgumentException;
}
