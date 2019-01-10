/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.servmanager.views;

import com.vaadin.ui.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Allows to represents a node and its connections from an end to end 
 * view as a table 
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class FormStructure {
    private List<Component> physicalPartA;
    private List<Component> odfsA;
    private List<Component> physicalPartB;
    private List<Component> odfsB;
    private Component logicalPartA;
    private Component logicalPartB;
    private List<Component> logicalConnctions;

    public FormStructure() {
        logicalConnctions = new ArrayList<>();
        physicalPartA = new ArrayList<>();
        physicalPartB = new ArrayList<>();
    }

    public List<Component> getPhysicalPartA() {
        return physicalPartA;
    }

    public List<Component> getPhysicalPartB() {
        return physicalPartB;
    }

    public void setPhysicalPartA(List<Component> physicalPartA) {
        this.physicalPartA = physicalPartA;
    }

    public void setPhysicalPartB(List<Component> physicalPartB) {
        this.physicalPartB = physicalPartB;
    }

    public List<Component> getOdfsA() {
        return odfsA;
    }

    public void setOdfsA(List<Component> odfsA) {
        this.odfsA = odfsA;
    }

    public List<Component> getOdfsB() {
        return odfsB;
    }

    public void setOdfsB(List<Component> odfsB) {
        this.odfsB = odfsB;
    }

    public Component getLogicalPartA() {
        return logicalPartA;
    }

    public void setLogicalPartA(Component logicalPartA) {
        this.logicalPartA = logicalPartA;
    }

    public Component getLogicalPartB() {
        return logicalPartB;
    }

    public void setLogicalPartB(Component logicalPartB) {
        this.logicalPartB = logicalPartB;
    }

    public List<Component> getLogicalConnctions() {
        return logicalConnctions;
    }

    public void setLogicalConnctions(List<Component> logicalConnctions) {
        this.logicalConnctions = logicalConnctions;
    }
            
}
