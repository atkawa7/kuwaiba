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
import entity.core.metamodel.ClassMetadata;
import entity.core.metamodel.PackageMetadata;
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
        String sql = "SELECT p.id, p.name, p.displayName, p.description FROM PackageMetadata p";
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
        sql = "SELECT cm FROM ClassMetadata cm";
        StartTagWAX classesTag = metadataTag.start("classes");
        List<ClassMetadata> classes = em.createQuery(sql).getResultList();
        for (ClassMetadata classInfo : classes){
            StartTagWAX classTag = classesTag.start("class");
            classTag.attr("id", classInfo.getId());
            classTag.attr("name", classInfo.getName());
            classTag.attr("displayName", classInfo.getDisplayName() == null ? "" : classInfo.getDisplayName());
            classTag.attr("isCustom", classInfo.getIsCustom());
            classTag.attr("color", classInfo.getColor() == null ? "0" : classInfo.getColor());
            StartTagWAX attributesTag = classTag.start("attributes");
            if (classInfo.getAttributes() != null){
                for (AttributeMetadata attributeInfo : classInfo.getAttributes()){
                    StartTagWAX attributeTag = attributesTag.start("attributes");
                    attributeTag.attr("id", attributeInfo.getId());
                    attributeTag.attr("name", attributeInfo.getName());
                    attributeTag.attr("displayName", attributeInfo.getDisplayName() == null ? "" : attributeInfo.getDisplayName());
                    attributeTag.attr("isVisible", attributeInfo.IsVisible());
                    attributeTag.attr("isReadOnly", false);
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
