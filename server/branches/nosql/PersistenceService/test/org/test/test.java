package org.test;

import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.persistenceservice.impl.ConnectionManagerImpl;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;

/**
 *
 * @author adrian
 */
public class test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MetadataObjectNotFoundException, Exception {
        ConnectionManager cm = new ConnectionManagerImpl();
        cm.openConnection();
        MetadataEntityManager mem = new MetadataEntityManagerImpl(cm);

        ClassMetadata clsMtdt = new ClassMetadata();
        clsMtdt = mem.getClass((long)1331);
        System.out.println(clsMtdt.getName());
        System.out.println("D: "+clsMtdt.getDescription());
        List<AttributeMetadata> listAttr = new ArrayList<AttributeMetadata>();
        listAttr = clsMtdt.getAttributes();
        for (AttributeMetadata attr : listAttr) {
            System.out.println(attr.getName());
            System.out.println(attr.getDescription());
        }

        mem.setAttributePropertyValue((long)1331, "acronym", "description", "funciona");

        clsMtdt = mem.getClass((long)1331);
        System.out.println(clsMtdt.getName());
        listAttr = clsMtdt.getAttributes();
        for (AttributeMetadata attr : listAttr) {
            System.out.println(attr.getName());
            System.out.println(attr.getDescription());
        }

        cm.closeConnection();
    }

}
