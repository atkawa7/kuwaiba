/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.tools.kadmin;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.kuwaiba.tools.kadmin.migration.ExportProviderImpl021;

/**
 * This program is intended to be used to perform the main administrative tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Main {
    private static final String appVersion = "0.3";
    /**
     * Allowed flags:
     * -e Performs an export operation based on the parameters specified below
     * -u DB user
     * -h DB host
     * -r DB port
     * -d DB name (default: kuwaibadb)
     * -p DB password
     * -o Output file name
     * --all Exports all elements in the database (default)
     * --types Exports the list types (EquipmentProvider, LocationOwner, etc)
     * --meta Exports the metadata information
     * --application Exports
     * --business Exports the business objects
     * --server specifies the current server version ["legacy" for 0.2.x, "0.3" for 0.3.x]
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String dbUser = null;
        String dbName = null;
        String dbHost = null;
        String dbPort = null;
        String dbPassword = null;
        String outputFileName =null;

        if (args.length == 0){
            System.out.println("Kuwaiba command line administration tool v"+appVersion);
            System.out.println("Allowed flags:\n"+
                  "export Performs an export operation based on the parameters specified below\n" +
                  "\t-u DB user (default: kuwaibadbuser)\n" +
                  "\t-h DB host (default: localhost)\n" +
                  "\t-d DB name (default: kuwaibadb)\n" +
                  "\t-r DB port (default: 5432)\n" +
                  "\t-p DB password (default: kuwaiba)\n" +
                  "\t-o Output file name\n" +
                  "\t-s Specifies the current server version [\"legacy\" for 0.2.x, \"0.3\" for 0.3.x]" +
                  "\t--all Exports all elements in the database (default)\n" +
                  "\t--types Exports the list types (EquipmentProvider, LocationOwner, etc)\n" +
                  "\t--meta Exports the metadata information\n" +
                  "\t--application Exports \n" +
                  "\t--business Exports the business objects\n");
            System.exit(0);
        }

        int scope = 0;
        if (args[0].equals("export")){
            for (int i = 1; i < args.length ; i++){
                if (args[i].startsWith("--")){ //a simple flag
                    if (args[i].equals("--types"))
                        scope |= ExportProvider.TYPE_LISTTYPES;
                    else
                        if (args[i].equals("--meta"))
                            scope |= ExportProvider.TYPE_METADATA;
                        else
                            if (args[i].equals("--application"))
                                scope |= ExportProvider.TYPE_OTHER_APPLICATION_OBJECTS;
                            else
                                if(args[i].equals("--business"))
                                    scope |= ExportProvider.TYPE_BUSINESS;
                                else{
                                    System.out.println("Unknown parameter "+args[i]);
                                    return;
                                }

                }else{
                    if (args[i].startsWith("-")){ //a parameter
                        if (i < args.length -1){
                            if (args[i].equals("-u"))
                                dbUser = args[i+1];
                            else
                                if (args[i].equals("-h"))
                                    dbHost = args[i+1];
                                else
                                    if (args[i].equals("-d"))
                                        dbName = args[i+1];
                                    else
                                        if (args[i].equals("-r"))
                                            dbPort = args[i+1];
                                        else
                                            if (args[i].equals("-p"))
                                                dbPassword = args[i+1];
                                            else
                                                if (args[i].equals("-o"))
                                                    outputFileName = args[i+1];
                                                else
                                                    if (args[i].equals("-s"))
                                                    outputFileName = args[i+1];
                        }else{
                            System.out.println("Value missing for parameter "+args[i]);
                            return;
                        }
                    }
                }
            }
            EntityManager em = Persistence.createEntityManagerFactory("KuwaibaToolsPersistenceUnit").createEntityManager();
            if (dbPassword != null)
                em.setProperty("javax.persistence.jdbc.password", dbPassword);
            if (dbUser != null)
                em.setProperty("javax.persistence.jdbc.user", dbUser);

            em.setProperty("javax.persistence.jdbc.url", "jdbc:postgresql://"+(dbHost == null ? "localhost" : dbHost) +
                    ":" + (dbPort == null ? "5432" : dbPort) +"/"+(dbName == null ? "kuwaibadb" : dbName));
            if (em != null){
                System.out.println("Starting export...");
                ByteArrayOutputStream bas = new ByteArrayOutputStream();
                ExportProvider bp = new ExportProviderImpl021();
                bp.startTextBackup(em, bas, "legacy", ExportProvider.TYPE_ALL);
                try{
                    FileOutputStream fos = new FileOutputStream(outputFileName == null ? "kuwaiba_export_"+Calendar.getInstance().getTimeInMillis()+".xml" : outputFileName);
                    fos.write(bas.toByteArray());
                    fos.flush();
                    fos.close();
                    bas.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
                System.out.println("Export finished");
            }
            else
                System.out.println("The EntityManager couldn't be created. Please check you database connection setings");
        }
    }
}
