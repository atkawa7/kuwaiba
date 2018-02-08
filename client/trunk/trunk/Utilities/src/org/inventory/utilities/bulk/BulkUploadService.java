/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.utilities.bulk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import org.inventory.communications.CommunicationsStub;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This class provides the business logic to the associated component
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class BulkUploadService implements Runnable {

    private String fileName;
    private byte[] logResults;
    private int fileType;
    private int commitSize;
    private byte[] file;
    
    public BulkUploadService(byte[] file, int commitSize, int fileType) {
        this.fileName = "";
        this.file = file;
        this.commitSize = commitSize;
        this.fileType = fileType;
    }

    public byte[] getLogFile() {
        return logResults;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void run() {
        InputOutput io = IOProvider.getDefault().getIO ("Bulk import results " + Calendar.getInstance().getTime(), true);
        CommunicationsStub com = CommunicationsStub.getInstance();
        io.getOut().println(String.format("[%s] Starting importing process... ", Calendar.getInstance().getTime()));
        io.getOut().println(String.format("[%s] Uploading file...", Calendar.getInstance().getTime()));
        fileName =  com.loadDataFromFile(file, commitSize, fileType);
        
        if (fileName != null) {
            logResults = com.downloadLog(fileName);
            if (logResults != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(logResults)));
                try {
                    String line;
                    while ((line = br.readLine()) != null)
                        io.getOut().println (line);

                    br.close();
                }catch (IOException ex){
                    io.getOut().println (String.format("[%s] Error reading log file: %s", Calendar.getInstance().getTime(), ex.getMessage()));
                }
            }else
                io.getOut().println (String.format("[%s] Retrieving log file failed: %s", Calendar.getInstance().getTime(), com.getError()));
        } else
            io.getOut().println(String.format("[%s] Upload failed: %s", Calendar.getInstance().getTime(), com.getError()));
        io.getOut().println(String.format("[%s] Importing process finished", Calendar.getInstance().getTime()));
        io.getOut().close();
    } 
}