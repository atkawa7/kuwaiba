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

package utils;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

/**
 * Implementors of this interface should perform a complete backup of the data base
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface BackupProvider {
    /**
     * Gets the document version used for the class implementing this interface
     * @return
     */
    public String getDocumentVersion();
    /**
     * Makes a XML-based backup according to the guidelines available at the <a href="https://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_backup.2Fexport_the_current_database">wiki page</a>
     * @param entities the list of available entities
     * @param outputStream the stream to write the backup
     * @param serverVersion the version of the server to be backed up
     */
    public void startTextBackup(EntityManager em, ByteArrayOutputStream outputStream, String serverVersion);
    /**
     * Makes a binary backup according to the guidelines available at the wiki page (format not yet available)
     * @param entities the list of available entities
     * @param outputStream the stream to write the backup
     * @param serverVersion the version of the server to be backed up
     */
    public void startBinaryBackup(Set<EntityType> entities, ByteArrayOutputStream outputStream, String serverVersion);
}
