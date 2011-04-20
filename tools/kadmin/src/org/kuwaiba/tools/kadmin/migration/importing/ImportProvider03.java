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
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.tools.kadmin.api.ImportProvider;
import org.kuwaiba.tools.kadmin.migration.importing.mappings.AttributeMapping;
import org.kuwaiba.tools.kadmin.migration.importing.mappings.ClassMapping;

/**
 * Migrates from 0.2.x to 0.3.x
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ImportProvider03 implements ImportProvider{
    public static final String SOURCE_VERSION = "legacy";

    public String getTargetVersion() {
        return TARGET_VERSION_03;
    }

    public boolean importData(byte[] data, HashMap<ClassMapping,AttributeMapping> mappings) {
        try{
            assert data != null : "The data to be imported can't be null";
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();

            QName qBacukp = new QName("backup"); //NOI18N
            QName qClass = new QName("class"); //NOI18N
            QName qPackage = new QName("package"); //NOI18N
            QName qAttribute = new QName("attribute"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()){
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qBacukp)){

                    }
                }
            }
            return true;
        }catch(Exception ex){
            return false;
        }
    }

    private void processMetadataNode(StartTagWAX applicationNode){
    }

    private void processApplicationNode(StartTagWAX applicationNode){
    }

    private void processBusinessNode(StartTagWAX applicationNode){
    }

    private void processObjectNode(StartTagWAX applicationNode){
    }
}
