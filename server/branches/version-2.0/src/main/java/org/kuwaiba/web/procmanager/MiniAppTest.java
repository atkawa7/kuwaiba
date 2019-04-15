/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.procmanager;

import com.vaadin.ui.Label;
import java.util.Properties;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;

/**
 *
 * @author johnyortega
 */
public class MiniAppTest extends AbstractMiniApplication<Label, Label> {

    public MiniAppTest(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Label launchDetached() {
        return new Label("MiniAppTest Detached");
    }

    @Override
    public Label launchEmbedded() {
        return new Label("MiniAppTest Embedded");
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
