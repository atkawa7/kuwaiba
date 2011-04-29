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

package org.kuwaiba.tools.kadmin.migration.exporting;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import entity.core.RootObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.kuwaiba.tools.kadmin.api.ExportProvider;
import org.kuwaiba.tools.kadmin.utils.Util;

/**
 * Default implementation, used to backup 0.2.x and 0.3.x series
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExportProviderImpl03 implements ExportProvider{

    private static final String[] MAP_APPLICATION = new String[]{
            "User", "UserGroup","DefaultView"
        };

    private EntityManager em;

    @Override
    public String getDocumentVersion() {
        return DOCUMENT_VERSION_10;
    }

    public String getSourceVersion() {
        return SERVER_VERSION_03;
    }

    @Override
    public void startBinaryBackup(ByteArrayOutputStream outputStream, String serverVersion, int backupType) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void startTextBackup(ByteArrayOutputStream outputStream, String serverVersion, int backupType) {
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
            classTag.child("description", classInfo.getDescription() == null ? "" : classInfo.getDescription());

            StartTagWAX possibleChildrenTag = classTag.start("possibleChildren");
            if (classInfo.getPossibleChildren() != null){
                for (ClassMetadata possibleChild : classInfo.getPossibleChildren())
                    possibleChildrenTag.child("name", possibleChild.getName());
            }

            possibleChildrenTag.end();
            
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

        StartTagWAX applicationTag = entityTag.start("application");
        for (String className : MAP_APPLICATION){
            List<Object> allInstances = em.createQuery("SELECT x FROM " + className +" x").getResultList();
            for (Object obj : allInstances)
                createObjectNode(applicationTag, obj);
        }
        applicationTag.end();

        StartTagWAX businessTag = entityTag.start("business");
        List<Object> allInstances = em.createQuery("SELECT x FROM RootObject x").getResultList();
        for (Object obj : allInstances)
                createObjectNode(applicationTag, obj);
        businessTag.end();
        entityTag.end();
        rootTag.end().close();
    }
    
    private void createObjectNode(StartTagWAX parent, Object object){
        StartTagWAX objectTag = parent.start("object");
        List<Field> allAttributes = Util.getAllFields(object.getClass(), true);

        objectTag.attr("class", object.getClass().getSimpleName());

        for (Field f : allAttributes ){
            StartTagWAX attributeTag = objectTag.start("attribute");
            attributeTag.attr("name", f.getName());
            attributeTag.attr("isMultiple", f.getAnnotation(OneToOne.class) != null ||
                                            f.getAnnotation(OneToMany.class) != null ||
                                            f.getAnnotation(ManyToMany.class) != null ||
                                            f.getAnnotation(ManyToOne.class) != null);
            attributeTag.attr("isBinary", f.getType().equals(byte[].class));
            try{

                Method m;
                if (f.getType().equals(Boolean.class))
                    m = object.getClass().getMethod("is"+Util.capitalize(f.getName()),
                                                        new Class[]{});
                else
                    m = object.getClass().getMethod("get"+Util.capitalize(f.getName()),
                                                        new Class[]{});
                Object value = m.invoke(object, new Object[]{});
                if (value == null)  continue; //No need to add a "value" tag
                else{
                    //If this attribute is a reference to any other business object, we use a lazy approach
                    //by setting as value the object id
                    if(value instanceof RootObject)
                        attributeTag.child("value",String.valueOf(((RootObject)value).getId()));
                    else
                        if (value instanceof Date)
                            attributeTag.child("value",String.valueOf(((Date)value).getTime()));
                        else
                            attributeTag.child("value",value.toString());
                }
            } catch (NoSuchMethodException nsme){
                Logger.getLogger("ExportProvider").log(Level.WARNING, "NoSuchM: {0}", nsme.getMessage());
            }
            catch (IllegalAccessException iae){
                Logger.getLogger("ExportProvider").log(Level.WARNING, "IllegalAccess: {0}", iae.getMessage());
            }
            catch(InvocationTargetException ite){
                Logger.getLogger("ExportProvider").log(Level.WARNING, "invocationTarget: {0}", ite.getMessage());
            }
            catch(SecurityException se){
                Logger.getLogger("ExportProvider").log(Level.WARNING, "Security: {0}", se.getMessage());
            }
            catch (IllegalArgumentException iae2){
                Logger.getLogger("ExportProvider").log(Level.WARNING, "IllegalArgument: {0}", iae2.getMessage());
            }
            finally{
                attributeTag.end();
            }
        }
        objectTag.end();
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }
}
