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

package org.inventory.core.services.api.xml;

/**
 * This interface is the root of all further XML readers. AN XML reader is basically a class
 * which reads an XML document and converts it into a code-friendly object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface GenericXMLReader {
    public void read(byte[] xmlDocument) throws Exception;
}
