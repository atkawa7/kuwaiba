/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.tools.kadmin.api;

import java.io.ByteArrayOutputStream;
import javax.persistence.EntityManager;

/**
 * Implementors of this interface should perform a complete backup of the existing objects
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ExportProvider {
    public static final int TYPE_ALL = 0;
    public static final int TYPE_METADATA = 1;
    public static final int TYPE_LISTTYPES = 2;
    public static final int TYPE_OTHER_APPLICATION_OBJECTS = 4;
    public static final int TYPE_BUSINESS = 8;

    public static final String SERVER_VERSION_LEGACY = "legacy";
    public static final String SERVER_VERSION_03 = "0.3";

    public static final String DOCUMENT_VERSION_10 = "1.0";
    /**
     * Gets the document version used for the class implementing this interface
     * @return
     */
    public String getDocumentVersion();
    /**
     * Gets the server version where the backup/export was extracted from
     * @return
     */
    public String getSourceVersion();
    /**
     * Makes a XML-based backup according to the guidelines available at the <a href="https://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_backup.2Fexport_the_current_database">wiki page</a>
     * @param Current entity manager used to execute the necessary queries
     * @param outputStream the stream to write the backup
     * @param serverVersion the version of the server to be backed up
     */
    public void startTextBackup(ByteArrayOutputStream outputStream, String serverVersion, int backupType);
    /**
     * Makes a binary backup according to the guidelines available at the wiki page (format not yet available)
     * @param Current entity manager used to execute the necessary queries
     * @param outputStream the stream to write the backup
     * @param serverVersion the version of the server to be backed up
     */
    public void startBinaryBackup(ByteArrayOutputStream outputStream, String serverVersion, int backupType);
    /**
     * Sets the entity manager used to manage persistence
     * @param em The new entity manager
     */
    public void setEntityManager(EntityManager em);
}
