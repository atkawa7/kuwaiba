
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
        //File nombre = new File("/home/adrian/test.xml");
        File nombre = new File("C:/classhierarchy.xml");
        a.read(readXMLBackup.getBytesFromFile(nombre));
        a.axu();

    }//fin main

}
