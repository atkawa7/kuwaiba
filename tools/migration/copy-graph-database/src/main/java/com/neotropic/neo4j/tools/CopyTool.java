/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.neo4j.tools;

import java.io.File;

/**
 * Copies a Neo4j Graph Database
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CopyTool {
    /**
     * @param args sourceStoreDir targetStoreDir
     */
    public static void main(final String[] args) {
        if (args.length == 2) {
            System.out.println("Starting...");
            System.out.println(String.format("args: sourceStoreDir=%s targetStoreDir=%s", args[0], args[1]));
            final File sourceStoreDir = new File(args[0]);
            if (!sourceStoreDir.exists()) {
                System.err.println(String.format("Finished with error %s not exists", args[0]));
                return;
            }
            final File targetStoreDir = new File(args[1]);
            if (targetStoreDir.exists()) {
                System.err.println(String.format("Finished with error %s exists", args[1]));
                return;
            }
            final CopyGraphDb neoCopyDb = new CopyGraphDb(sourceStoreDir, targetStoreDir);
            neoCopyDb.copyDatabase();
            System.out.println("...Copy end");
        }
        else {
            System.err.println("Finished with error use: java -jar copy-graph-database-3.3.3.jar sourceStoreDir targetStoreDir");
        }
    }
}