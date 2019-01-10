/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic>.
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

import java.util.Date;
import java.util.List;
import org.inventory.communications.core.LocalClassWrapper;

/**
 * This readers reads a class hierarchy descriptor as defined in the <a href="http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents">wiki</a> page
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public interface ClassHierarchyReader extends GenericXMLReader{
    public String getDocumentVersion();
    public String getServerVersion();
    public Date getDate();
    public List<LocalClassWrapper> getRootClasses();
}
