/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.dashboard.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.security.InvalidParameterException;
import java.util.HashMap;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.communications.core.LocalTaskResultMessage;

/**
 * A widget that shows the results of a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskResultWidget extends AbstractWidget {
    private LocalTask task;
        
    @Override
    public String getName() {
        return "Task Results Widget"; //NOI18N
    }
    
    @Override
    public String getTitle() {
        return String.format("%s Results", task.getName()); //NOI18N
    }
    
    @Override
    public String getDescription() {
        return "This widget displays the results of a task";
    }

    @Override
    public String getVersion() {
        return "1.0"; //NOI18N
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS"; //NOI18N
    }
    
    @Override
    public void setup(HashMap<String, Object> parameters) throws InvalidStateException, InvalidParameterException {
        if (state != WIDGET_STATE_CREATED)
            throw new InvalidStateException("Widget state is not CREATED");
        
        task = (LocalTask)parameters.get("task"); //NOI18N
        if (task == null)
            throw new InvalidParameterException("The parameter \"task\" is missing");
        
        state = WIDGET_STATE_SET_UP;
    }
    
    @Override
    public void init() throws InvalidStateException {
        if (state != WIDGET_STATE_SET_UP)
            throw new InvalidStateException("Widget state is not SET_UP");
        
        setLayout(new GridBagLayout());
        
        GridBagConstraints layoutConstraints = new GridBagConstraints();
        
        //The horizontal fill is a general setting
        layoutConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        //Lets configure how the title will be placed
        layoutConstraints.gridx = 0;
        layoutConstraints.gridy = 0;
        
        //Now, we create the actual title component
        JLabel lblTitle = new JLabel("<html><b>" + getTitle() + "</b></html>", SwingConstants.RIGHT);
        lblTitle.setMinimumSize(new Dimension(0, 20));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(Color.LIGHT_GRAY);
        lblTitle.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        add(lblTitle, layoutConstraints);
        
        LocalTaskResult taskResult = CommunicationsStub.getInstance().executeTask(task.getId());
        
        if (taskResult == null) {
            JLabel lblError = new JLabel(String.format("Widget Error: %s", CommunicationsStub.getInstance().getError()));
            lblError.setOpaque(true);
            lblError.setBorder(new EmptyBorder(10, 10, 10, 10));
            lblError.setBackground(Color.PINK);
            
            layoutConstraints.gridy = 1;
            
            add(lblTitle, layoutConstraints);
            
        } else {
            int i = 1;
            for (LocalTaskResultMessage message : taskResult.getMessages()) {
                JLabel lblMessage = new JLabel(message.getMessage());
                lblMessage.setOpaque(true);
                lblMessage.setBorder(new EmptyBorder(10, 10, 10, 10));
                
                switch (message.getMessageType()) {
                    case LocalTaskResultMessage.STATUS_SUCCESS:
                        lblMessage.setBackground(Color.GREEN);
                        break;
                    case LocalTaskResultMessage.STATUS_WARNING:
                        lblMessage.setBackground(Color.ORANGE);
                        break;
                    case LocalTaskResultMessage.STATUS_ERROR:
                        lblMessage.setBackground(Color.PINK);
                        break;
                }
                
                layoutConstraints.gridy = i;
                
                add(lblMessage, layoutConstraints);
                
                i++;
            }
        }
        
        state = WIDGET_STATE_INITIALIZED;
    }
    
    @Override
    public void refresh() throws InvalidStateException {
        if (state != WIDGET_STATE_INITIALIZED)
            throw new InvalidStateException("Widget state is not INITIALIZED");
        removeAll();
        init();
    }
    
    @Override
    public void done() throws InvalidStateException {
        if (state != WIDGET_STATE_INITIALIZED)
            throw new InvalidStateException("Widget state is not INITIALIZED");
        
        removeAll();
        
        state = WIDGET_STATE_DONE;
    }
}
