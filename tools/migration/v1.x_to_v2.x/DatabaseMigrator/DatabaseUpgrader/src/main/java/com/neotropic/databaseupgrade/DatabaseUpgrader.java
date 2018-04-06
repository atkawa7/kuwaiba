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
package com.neotropic.databaseupgrade;

import java.io.File;

/**
 * Upgrades the database to use the version 3.3.3 of Neo4j creating labels and 
 * schema index, removing the deprecated index and delete unused schema index
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DatabaseUpgrader {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Set the parameter dbPath");
            return;
        }
        File storDir = new File(args[0]);
        
        try {
            System.out.println("Starting database upgrade...");
            Upgrader.getInstance().upgrade(storDir);
            LabelUpgrader.getInstance().createLabels(storDir);
            IndexUpgrader.getInstance().upgrade(storDir);
            LabelUpgrader.getInstance().deleteIndexes(storDir);
            LabelUpgrader.getInstance().deleteUnusedLabels(storDir);
            System.out.println("...Database upgraded successfully");            
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }
    
}
