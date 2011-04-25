/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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
 * 
 */

package org.kuwaiba.tools.kadmin.api;

import java.util.HashMap;
import org.kuwaiba.tools.kadmin.VersionNotValidException;
import org.kuwaiba.tools.kadmin.XMLParseException;
import org.kuwaiba.tools.kadmin.migration.importing.mappings.ClassMapping;

/**
 * Implemented by those who want to provide importing services into Kuwaiba
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ImportProvider {
    public static final String TARGET_VERSION_03 = "0.3";
    public static final String TARGET_VERSION_04 = "0.4";
    public static final String TARGET_VERSION_05 = "0.5";

    public static final String SOURCE_VERSION_LEGACY = "legacy";
    public static final String SOURCE_VERSION_03 = "0.3";

    /**
     * Gets the server version which is supported to import from
     * @return
     */
    public String getSourceVersion();
    /**
     * Gets the server version which is supported to import to
     * @return
     */
    public String getTargetVersion();
    /**
     * Imports from an XML format
     * @param data byte array with the file
     * @param mappings the mappings used to know what changed
     */
    public void importTextData(byte[] data, HashMap<String, ClassMapping> mappings) throws VersionNotValidException, XMLParseException;
}
