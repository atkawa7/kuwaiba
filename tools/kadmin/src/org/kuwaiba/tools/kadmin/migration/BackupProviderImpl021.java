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

package org.kuwaiba.tools.kadmin.migration;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import entity.core.metamodel.AttributeMetadata;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import org.kuwaiba.tools.kadmin.BackupProvider;

/**
 * Default implementation, used to backup 0.2.x and 0.3.x series
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BackupProviderImpl021 implements BackupProvider{

    private static final String version = "1.0";

    @Override
    public String getDocumentVersion() {
        return version;
    }

    @Override
    public void startBinaryBackup(Set<EntityType> entities, ByteArrayOutputStream outputStream, String serverVersion, int backupType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTextBackup(EntityManager em, ByteArrayOutputStream outputStream, String serverVersion, int backupType) {
        assert (em != null) : "Null EntityManager found";

        WAX xmlWriter = new WAX(outputStream);
        StartTagWAX rootTag = xmlWriter.start("backup");
        rootTag.attr("documentVersion", getDocumentVersion());
        rootTag.attr("serverVersion", serverVersion);
        rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
        StartTagWAX entityTag = rootTag.start("entities");
        StartTagWAX metadataTag = entityTag.start("metadata");

        String sql = "SELECT p.id, p.name, p.displayName, p.description FROM AttributeMetadata att";
        StartTagWAX packagesTag = metadataTag.start("packages");
        List<Object[]> packages = em.createQuery(sql).getResultList();
        for (Object[] packageInfo : packages){
            StartTagWAX packageTag = packagesTag.start("package");
            packageTag.attr("id", packageInfo[0]);
            packageTag.attr("name", packageInfo[1]);
            packageTag.attr("displayName", packageInfo[2] == null ? "":packageInfo[2]);
            packageTag.text(packageInfo[3] == null ? "" : packageInfo[3].toString());
            packageTag.end();
        }
        packagesTag.end();
        sql = "SELECT cm.id, cm.name, cm.displayName, cm.isCustom, cm.color, cm.description, cm.icon, cm.smallIcon, cm.attributes FROM ClassMetadata cm";
        StartTagWAX classesTag = metadataTag.start("classes");
        List<Object[]> classes = em.createQuery(sql).getResultList();
        for (Object[] classInfo : classes){
            StartTagWAX classTag = classesTag.start("class");
            classTag.attr("id", classInfo[0]);
            classTag.attr("name", classInfo[1]);
            classTag.attr("displayName", classInfo[2] == null ? "" : classInfo[2]);
            classTag.attr("isCustom", classInfo[3]);
            classTag.attr("color", classInfo[4] == null ? "0" : classInfo[4]);
            StartTagWAX attributesTag = classTag.start("attributes");
            if (classInfo[3] != null){
                for (AttributeMetadata attributeInfo : (List<AttributeMetadata>)classInfo[3]){
                    StartTagWAX attributeTag = attributesTag.start("attributes");
                    attributeTag.attr("id", attributeInfo.getId());
                    attributeTag.attr("name", attributeInfo.getName());
                    attributeTag.attr("displayName", attributeInfo.getDisplayName() == null ? "" : attributeInfo.getDisplayName());
                    attributeTag.attr("isVisible", attributeInfo.isVisible());
                    attributeTag.attr("isReadOnly", attributeInfo.isReadOnly());
                    attributeTag.text(attributeInfo.getDescription() == null ? "" : attributeInfo.getDescription());
                    attributeTag.end();
                }
            }
            attributesTag.end();
            classTag.end();
        }
        classesTag.end();

        metadataTag.end();
        entityTag.end();
        rootTag.end().close();
    }
}
