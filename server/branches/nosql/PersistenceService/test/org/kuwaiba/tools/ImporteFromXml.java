
package org.kuwaiba.tools;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author adrian
 */
public class ImporteFromXml {
public static void main(String argv[]) throws IOException, Exception {

        readXMLBackup a = new readXMLBackup();
        File nombre = new File("/home/zim/classhierarchy_v3.xml");
        a.read(readXMLBackup.getBytesFromFile(nombre));
        a.axu();

    }//fin main

}
