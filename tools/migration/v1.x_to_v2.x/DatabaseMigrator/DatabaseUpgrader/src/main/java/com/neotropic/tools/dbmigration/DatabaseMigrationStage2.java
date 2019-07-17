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

import com.neotropic.tools.dbmigration.views.EndToEndAndTopologyViewMigrator;
import com.neotropic.tools.dbmigration.views.GeneralViewsMigrator;
import com.neotropic.tools.dbmigration.views.LayoutMigrator;
import com.neotropic.tools.dbmigration.views.ObjectViewMigrator;
import com.neotropic.tools.dbmigration.views.SyncDataSourceMigrator;
import java.io.File;
import java.util.Calendar;

/**
 * Upgrades the database to use the version 3.3.3 of Neo4j creating labels and 
 * schema index, removing the deprecated index and delete unused schema index. This stage 
 * also can be used to migrate a database version 2 that uses long ids to one using string ids.
 * See {@link #displayHelp() } for details on what parameters are allowed.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DatabaseMigrationStage2 {
    
    /**
     * Application entry point
     * @param args The list of command line arguments. Two are expected: The first one 
     * is either --fullMigration (migrate label and indexes from older versions, and update old long ids),
     * --onlyUpgradeIds (migrate a version 2 database using long ids to one using string ids) or 
     * --toDeprecatedVersion2 (takes a database version 1 and migrates it to version 2, but keeps the deprecated long type ids). 
     * The second argument is the database path.
     */
    public static void main(String[] args) {                    
        if (args.length == 2) {
            String migrationType = args[0];
            String dbPath = args[1];
            File dbPathReference = new File(dbPath);
            
            if (!dbPathReference.exists()) {
                System.out.println(String.format("The specified database path (%s) does not exist", dbPathReference.getAbsolutePath()));
                return;
            }
                        
            try {
                if (!dbPathReference.exists())
                    throw new RuntimeException(String.format("Database path %s not found", args[1]));
                
                boolean doMigrateFromV1ToV2;
                boolean doMigrateFromV2WithLongIdsToV2WithStringIds;
                switch(migrationType) {
                    case "--fullMigration":
                        doMigrateFromV1ToV2 = true;
                        doMigrateFromV2WithLongIdsToV2WithStringIds = true;
                    break;
                    case "--onlyUpgradeIds":
                        doMigrateFromV1ToV2 = false;
                        doMigrateFromV2WithLongIdsToV2WithStringIds = true;
                    break;
                    case "--toDeprecatedVersion2":
                        doMigrateFromV1ToV2 = true;
                        doMigrateFromV2WithLongIdsToV2WithStringIds = false;
                    break;
                    default:
                        System.out.println("Invalid migration type.");
                        displayHelp();
                        return;
                }
                
                System.out.println(String.format("[%s] Starting database upgrade stage 2...", Calendar.getInstance().getTime()));
                if (doMigrateFromV1ToV2) {
                    Upgrader.getInstance().upgrade(dbPathReference);
                    LabelUpgrader.getInstance().createLabels(dbPathReference);
                    LabelUpgrader.getInstance().deleteIndexes(dbPathReference);
                    LabelUpgrader.getInstance().deleteUnusedLabels(dbPathReference);
                }
                if (doMigrateFromV2WithLongIdsToV2WithStringIds) {
                    LabelUpgrader.getInstance().replaceLabel(dbPathReference, "attribute", "attributes");
                    LabelUpgrader.getInstance().replaceLabel(dbPathReference, "inventory_objects", "inventoryObjects");
                    LabelUpgrader.getInstance().setUUIDAttributeToInventoryObjects(dbPathReference);
                    LabelUpgrader.getInstance().setUUIDAttributeToListTypeItems(dbPathReference);
                    LabelUpgrader.getInstance().setUUIDAttributeToTemplates(dbPathReference);
                    LabelUpgrader.getInstance().setUUIDAttributeToPools(dbPathReference);
                    ObjectViewMigrator.migrate(dbPathReference);
                    EndToEndAndTopologyViewMigrator.migrate(dbPathReference);
                    GeneralViewsMigrator.migrate(dbPathReference);
                    LayoutMigrator.migrate(dbPathReference);
                    LayoutMigrator.updateCustomShapeIcons(dbPathReference);
                    SyncDataSourceMigrator.migrate(dbPathReference);
                    ClassesUpdater.getInstance().setLabelAttributes(dbPathReference);
                    ClassesUpdater.getInstance().updateClasses(dbPathReference);
                }

                System.out.println(String.format("[%s] Database upgrade stage 2 ended successfully...", Calendar.getInstance().getTime()));
            } catch (Exception ex) {
                System.out.println(String.format("An unexpected error was found: %s", ex.getMessage()));
                //ex.printStackTrace();
            }
        } else 
            displayHelp();
    }
    
    /**
     * Displays a help message.
     */
    private static void displayHelp() {
        System.out.println("This application expects two parameters:\n " + 
                    "1. Migration type. Use --fullMigration to migrate label and indexes from older versions, and update old long ids. \n" +
                    "Use --onlyUpgradeIds to migrate a version 2 database using long ids to one using string ids.\n" + 
                    "Use --toDeprecatedVersion2 to migrate a database version 1 to version 2, but keeping the deprecated long type ids" +
                    "2. Database path");
        System.out.println("Example: java -jar DatabaseMigrationStage2 --fullMigration /data/db/kuwaiba.db");
    }
}
