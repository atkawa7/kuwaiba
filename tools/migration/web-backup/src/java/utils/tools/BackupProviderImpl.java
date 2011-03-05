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

package utils.tools;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import entity.core.metamodel.AttributeMetadata;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import utils.BackupProvider;

/**
 * Simple implementation of the BackupProvider interface
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BackupProviderImpl implements BackupProvider{

    private static final String version = "1.0";

    @Override
    public String getDocumentVersion() {
        return version;
    }

    @Override
    public void startBinaryBackup(Set<EntityType> entities, ByteArrayOutputStream outputStream, String serverVersion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTextBackup(EntityManager em, ByteArrayOutputStream outputStream, String serverVersion) {
        assert (em != null) : "Null EntityManager found";

        WAX xmlWriter = new WAX(outputStream);
        StartTagWAX rootTag = xmlWriter.start("backup");
        rootTag.attr("documentVersion", getDocumentVersion());
        rootTag.attr("serverVersion", serverVersion);
        rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
        StartTagWAX entityTag = rootTag.start("entities");
        StartTagWAX metadataTag = entityTag.start("metadata");

        if (serverVersion.equals("legacy")){ //"legacy" is used for all versions prior to 0.3
            String sql = "SELECT at FROM AttributeMetadata att";
            List<AttributeMetadata> atts = em.createQuery(sql).getResultList();
            for (AttributeMetadata att : atts) {
                StartTagWAX objectTag = metadataTag.start("object");
                objectTag.attr("class", atts.getClass().getSimpleName());
                objectTag.attr("id", att.getId());
            }
        }
        metadataTag.end();
        entityTag.end();
        rootTag.end().close();
    }
}
