/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.servmanager.views;

import com.vaadin.ui.Component;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adrian
 */
public class FormStructure {
    private List<Component> physicalPartA;
    private Component odfsA;
    private List<Component> physicalPartB;
    private Component odfsB;
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

    public Component getOdfsA() {
        return odfsA;
    }

    public void setOdfsA(Component odfsA) {
        this.odfsA = odfsA;
    }

    public Component getOdfsB() {
        return odfsB;
    }

    public void setOdfsB(Component odfsB) {
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
