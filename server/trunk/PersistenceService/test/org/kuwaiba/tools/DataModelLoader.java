
package org.kuwaiba.tools;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author adrian
 */
public class DataModelLoader {
public static void main(String argv[]) throws IOException, Exception {
        XMLBackupReader a = new XMLBackupReader();
        File classHierarchyFile = new File(System.getProperty("user.home") + "/class_hierarchy.xml");
        a.read(XMLBackupReader.getBytesFromFile(classHierarchyFile));
        a.load();
    }
}
