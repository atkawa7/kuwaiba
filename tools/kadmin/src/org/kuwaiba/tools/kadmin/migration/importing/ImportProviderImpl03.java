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

package org.kuwaiba.tools.kadmin.migration.importing;

import com.ociweb.xml.StartTagWAX;
import entity.core.metamodel.ClassMetadata;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.tools.kadmin.VersionNotValidException;
import org.kuwaiba.tools.kadmin.XMLParseException;
import org.kuwaiba.tools.kadmin.api.ImportProvider;
import org.kuwaiba.tools.kadmin.migration.importing.mappings.ClassMapping;

/**
 * Migrates from 0.2.x to 0.3.x
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ImportProviderImpl03 implements ImportProvider{
    private EntityManager em;

    public String getSourceVersion() {
        return SOURCE_VERSION_LEGACY;
    }

    public String getTargetVersion() {
        return TARGET_VERSION_03;
    }

    public void importTextData(byte[] data, HashMap<String, ClassMapping> mappings) throws VersionNotValidException, XMLParseException{
        try{
            assert em != null : "Entity Manager can not be null";
            assert data != null : "The data to be imported can't be null";

            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            QName qBackup = new QName("backup"); //NOI18N
            QName qClass = new QName("class"); //NOI18N
            QName qObject = new QName("object"); //NOI18N
            QName qAttribute = new QName("attribute"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()){
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qBackup)){
                        if (!reader.getAttributeValue(null, "serverVersion").equals(getSourceVersion()))
                            throw new VersionNotValidException(VersionNotValidException.OPERATION_IMPORT,
                                            getSourceVersion(),getTargetVersion());
                        else{
                            if (reader.getName().equals(qClass)){
                                processClassNode(reader, mappings.get(reader.getName().toString()));
                            }
                        }
                    }
                }
            }
        }catch(XMLStreamException ex){
            throw new XMLParseException(ex.getClass().getSimpleName()+": "+ex.getMessage());
        }
    }

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }


    private void processClassNode(XMLStreamReader pointerToNode, ClassMapping classMapping){
        String className;
        //This class is mapped somehow in the new version or the enclosed
                                  //attributes are
        if (classMapping != null){
            if (classMapping.getRenamedTo() == null)
                className = classMapping.getOriginalName();
            else{
                if(classMapping.getRenamedTo().equals("")){
                    Logger.getLogger("ImportProvider").log(Level.WARNING, "renamedTo can not be null for {0}. Class ignored", classMapping.getOriginalName());
                    return;
                }

                className = classMapping.getRenamedTo();
            }
        }else
            className = pointerToNode.getName().toString();

        ClassMetadata metadata;
        try{
            metadata = (ClassMetadata)em.createQuery("SELECT x FROM ClassMetadata WHERE x.name='"+className+"'").getSingleResult();
        }catch (NoResultException nre){
            Logger.getLogger("ImportProvider").log(Level.WARNING, "Class {0} not found", className);
            return;
        }

        metadata.setDisplayName(pointerToNode.getAttributeValue(null, "displayName") == null ? "" : pointerToNode.getAttributeValue(null, "displayName"));
        metadata.setColor(pointerToNode.getAttributeValue(null, "displayName") == null ? null : Integer.valueOf(pointerToNode.getAttributeValue(null, "displayName")));
//        sentence = String.format("UPDATE {0} x SET x.displayName='{1}', x.color={2}, x.description=''", className,
//                pointerToNode.getAttributeValue(null, "displayName") == null ? "" : pointerToNode.getAttributeValue(null, "displayName"),
//                pointerToNode.getAttributeValue(null, "color") == null ? 0 : pointerToNode.getAttributeValue(null, "color"));
    }

    private void processObjectNode(StartTagWAX applicationNode){
    }
}
