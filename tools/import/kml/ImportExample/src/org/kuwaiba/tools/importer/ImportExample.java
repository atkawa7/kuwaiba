/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 
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
package org.kuwaiba.tools.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transform a .kml file into a file in .csv format
 *
 * @author Daniel Felipe Cepeda <daniel.cepeda@kuwaiba.org>
 */
public class ImportExample {

    public static void main(String[] args) throws IOException {

        File file;
        //FileReader fileReader = null;
        FileWriter fileWriter = null;
        PrintWriter printWriter = null;

        List<Item> listItemsManhole = new ArrayList();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("doc.kml"))) {

            file = new File("doc.kml");
            //fileReader = new FileReader(file);
            boolean addItem = false;
            Item itemManhole = new Item();
            // Read file
            String lineBufferedReader;
            while ((lineBufferedReader = bufferedReader.readLine()) != null) {

                //////////////------MANHOLS--------//////////////////
                boolean lineNameManhol = lineBufferedReader.contains("<name>");
                boolean lineContextNameManhole1 = lineBufferedReader.contains("Manholes1");
                boolean lineContextNameManhole2 = lineBufferedReader.contains("Manholes2");
                boolean lineLongitudeManhole = lineBufferedReader.contains("longitude");
                boolean lineLatitudeManhole = lineBufferedReader.contains("latitude");
                boolean lineEndPlacemarkManhole = lineBufferedReader.contains("</Placemark>");

                boolean folderManhole = lineBufferedReader.contains("<Folder>");

                if (folderManhole) {
                    addItem = false;
                }

                if (lineNameManhol && lineContextNameManhole1) {
                    addItem = true;
                }

                if (lineNameManhol && lineContextNameManhole2) {
                    addItem = true;
                }

                if (lineNameManhol && addItem) {
                    itemManhole = new Item();
                    itemManhole.setName(getContextTag(lineBufferedReader));
                }

                if (lineLongitudeManhole && addItem) {
                    itemManhole.setLon(getContextTag(lineBufferedReader));
                }

                if (lineLatitudeManhole && addItem) {
                    itemManhole.setLat(getContextTag(lineBufferedReader));
                }
                if (lineEndPlacemarkManhole && addItem) {
                    listItemsManhole.add(itemManhole);
                }
                //////////////------MANHOLS--------//////////////////      
            }

            Collections.sort(listItemsManhole, new LongComparator()); //Sort the "list of manholes" with respect to "longitude"

            fileWriter = new FileWriter("prueba.csv");
            printWriter = new PrintWriter(fileWriter);
            int cont = 0;
            int cont2 = 1;

            //Write in the .csv file the data obtained in the list of manholes
            printWriter.println("Country~t~root~t~name~c~root~t~name~c~Angola~t~acronym~c~AO");
            for (int i = 0; i < listItemsManhole.size(); i++) {
                if (i == 0) {
                    printWriter.println("Zone~t~Country~t~name~c~Angola~t~name~c~Zone" + 0);
                }

                if (cont == 100) {
                    printWriter.println("Zone~t~Country~t~name~c~Angola~t~name~c~Zone" + cont2);
                    cont = 0;
                    cont2++;
                } else {
                    printWriter.println("Manhole~t~Zone~t~name~c~Zone" + (cont2 -1) + "~t~name~c~" + listItemsManhole.get(i).getName() + "~t~longitude~c~" + listItemsManhole.get(i).getLon() + "~t~latitude~c~" + listItemsManhole.get(i).getLat() + "");
                }
                cont++;
            }

            System.out.println(listItemsManhole.size());
        } catch (IOException e) {
        }

    }

    public static String getContextTag(String s) {
        return s.substring(s.indexOf(">") + 1, s.lastIndexOf("<"));
    }
}
