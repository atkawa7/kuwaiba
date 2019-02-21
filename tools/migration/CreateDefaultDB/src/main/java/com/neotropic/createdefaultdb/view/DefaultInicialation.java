/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package com.neotropic.createdefaultdb.view;

import com.neotropic.createdefaultdb.core.DbManagement;
import java.io.File;
import java.io.IOException;

/**
 * Main class, receive parameter from command line to create a basic database to
 * start Kuwaiba or edit password 'admin' user, if already exist.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public class DefaultInicialation {

    private static DbManagement dbManagement;
    public String greeting;

    public static void main(final String[] args) throws IOException {
        if (args.length == 3) {
            try {

                String DB_FULL_PATH = (args[0]);
                String DB_NAME = (args[1]);
                String DB_ADMIN_PASSWORD = (args[2]);

                dbManagement = new DbManagement(DB_FULL_PATH + File.separator + DB_NAME, DB_ADMIN_PASSWORD);
                dbManagement.createdefaultData();
                dbManagement.shutDown();
            } catch (Exception e) {

                System.out.println("ERROR :" + e);

            }
        } else {
            System.out.println("Argument needed are 3 but recived " + args.length);
            System.out.println("1. Argument: database name ");
            System.out.println("2. Argument: path to create database ");
            System.out.println("3. Argument: password forf admin usrr");
            System.out.println("Example :");

            System.out.println("java -jar CreateDefaulDB /home/myuser/data mydbKuwaiba 456p09");
            System.exit(1);
        }

    }

}
