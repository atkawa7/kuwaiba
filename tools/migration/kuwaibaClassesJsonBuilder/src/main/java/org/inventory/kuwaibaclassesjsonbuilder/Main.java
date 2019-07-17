/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.kuwaibaclassesjsonbuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kuwaiba Classes JSON builder create a JSON file that contains a snapshot of 
 * the data model given an instance of Kuwaiba neo4j graph database and a set of 
 * class names
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args != null && args.length >= 2) {
            String dbPath = args[0];
            File db = new File(dbPath);
            
            if (!db.exists()) {
                System.out.println(String.format("The specified database path (%s) does not exist", db.getAbsolutePath()));
                return;
            }

            try {
                String classNames = "";
                for (int i = 1; i < args.length; i++)
                    classNames += "'" + args[i] + "',";
                if (classNames.isEmpty())
                    classNames = "[" + classNames + "]";
                else
                    classNames = "[" + classNames.substring(0, classNames.length() - 1) + "]";
                    
                String json = JsonBuilder.getInstance().build(db, classNames).toString();
                PrintWriter writer = new PrintWriter("/data/kuwaibaClasses.json", "UTF-8");
                writer.print(json);
                writer.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
    
}
