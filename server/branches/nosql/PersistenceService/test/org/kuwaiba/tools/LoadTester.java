
package org.kuwaiba.tools;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author adrian
 */
public class LoadTester {
public static void main(String argv[]) throws IOException, Exception {

        XMLBackupReader a = new XMLBackupReader();
        File nombre = new File("/home/zim/classhierarchy_v3.xml");
        a.read(XMLBackupReader.getBytesFromFile(nombre));
        a.axu();

    }//fin main

}
