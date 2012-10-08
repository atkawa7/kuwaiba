
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
        File nombre = new File("/home/zim/classhierarchy_v5.xml");
        a.read(XMLBackupReader.getBytesFromFile(nombre));
        a.axu();
    }
}
