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
        String dbPath;
        
        if (args.length == 0)
            dbPath = "/data/db/kuwaiba.db";
        else {
            if (args.length != 1) {
                System.out.println("Wrong parameter set. Usage: DatabaseMigrationTool <dbPath>");
                return;
            } else
                dbPath = args[0];
        }
        
        File dbPathReference = new File(dbPath);
        
        try {
            System.out.println(String.format("[%s] Starting database upgrade stage 2...", Calendar.getInstance().getTime()));
            if (true) {
                Upgrader.getInstance().upgrade(dbPathReference);
                LabelUpgrader.getInstance().createLabels(dbPathReference);
                IndexUpgrader.getInstance().upgrade(dbPathReference);
                LabelUpgrader.getInstance().deleteIndexes(dbPathReference);
                LabelUpgrader.getInstance().deleteUnusedLabels(dbPathReference);
            }
            if (false) {
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
    }
    
}
