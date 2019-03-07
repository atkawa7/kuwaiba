/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.tools.dbmigration;

import java.io.File;
import java.util.Calendar;

/**
 * Upgrades the database to use the version 3.3.3 of Neo4j creating labels and 
 * schema index, removing the deprecated index and delete unused schema index
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DatabaseMigrationStage2 {
    
    /**
     * Application entry point
     * @param args The list of command line arguments, currently only one is needed: dbPath. If not specified, 
     * /data/db/kuwaiba.db will be used.
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            String dbPath = args[0];

            File dbPathReference = new File(dbPath);

            try {
                int migrationType = Integer.valueOf(args[1]);
                boolean flag1;
                boolean flag2;
                switch(migrationType) {
                    case 1:
                        flag1 = true;
                        flag2 = true;
                    break;
                    case 2:
                        flag1 = false;
                        flag2 = true;
                    break;
                    case 3:
                        flag1 = true;
                        flag2 = false;
                    break;
                    default:
                        flag1 = true;
                        flag2 = true;
                    break;
                }
                
                System.out.println(String.format("[%s] Starting database upgrade stage 2...", Calendar.getInstance().getTime()));
                if (flag1) {
                    Upgrader.getInstance().upgrade(dbPathReference);
                    LabelUpgrader.getInstance().createLabels(dbPathReference);
                    IndexUpgrader.getInstance().upgrade(dbPathReference);
                    LabelUpgrader.getInstance().deleteIndexes(dbPathReference);
                    LabelUpgrader.getInstance().deleteUnusedLabels(dbPathReference);
                }
                if (flag2) {
                    LabelUpgrader.getInstance().replaceLabel(dbPathReference, "attribute", "attributes");
                    LabelUpgrader.getInstance().replaceLabel(dbPathReference, "inventory_objects", "inventoryObjects");
                    LabelUpgrader.getInstance().setUUIDAttributeToInventoryObjects(dbPathReference);
                    LabelUpgrader.getInstance().setUUIDAttributeToListTypeItems(dbPathReference);
                    LabelUpgrader.getInstance().setUUIDAttributeToPools(dbPathReference);
                }

                System.out.println(String.format("[%s] Database upgrade stage 2 ended successfully...", Calendar.getInstance().getTime()));
            } catch (Exception ex) {
                System.out.println(String.format("An unexpected error was found: %s", ex.getMessage()));
            }
        } else {
            System.out.println("Argument needed are 2 but recived " + args.length);
            System.out.println("1. Argument: database name ");
            System.out.println("2. Argument: Migration Type ");
            System.out.println(" Migration Type = 1 (Databases from stage 1 which apply stage 2 to use UUIDs)");
            System.out.println(" Migration Type = 2 (Databases from stage 2 which apply again stage 2 to use UUID)");
            System.out.println(" Migration Type = 3 (Databases from stage 1 which apply stage 2 and continue using ids)");
            
            System.out.println("Example :");

            System.out.println("java -jar DatabaseMigrationStage2 /data/db/kuwaiba.db 1");
            //System.exit(1);
        }
    }
    
}
