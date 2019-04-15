/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting;

/**
 * All report types must inherit from this abstract class. It describes the general behavior of a report. 
 * @author duckman
 */
public abstract class InventoryReport {
    /**
     * Report title.
     */
    protected String title;
    /**
     * Author of the script.
     */
    protected String author;
    /**
     * Version of the report.
     */
    protected String version;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public InventoryReport(String title, String author, String version) {
        this.title = title;
        this.author = author;
        this.version = version;
    }
    
    /**
     * Returns the result of the report as a by array
     * @return 
     */
    public abstract byte[] asByteArray();
}
