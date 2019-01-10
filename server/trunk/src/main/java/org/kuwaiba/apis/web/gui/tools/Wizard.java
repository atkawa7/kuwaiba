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

package org.kuwaiba.apis.web.gui.tools;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;

/**
 * A simple, general purpose wizard implementation that can be easily embedded in miniapplications and dashboard widgets
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Wizard extends VerticalLayout {
    /**
     * Main panel
     */
    private Panel pnlMain;
    /**
     * The next or finish action
     */
    private Button btnAction;
    /**
     * The cancel action
     */
    private Button btnCancel;
    /**
     * Reference to the current step
     */
    private Step currentStep;
    /**
     * The event listeners
     */
    private List<WizardEventListener> listeners;

    public Wizard(Step firstStep) {
        this.setSizeFull();
        this.pnlMain = new Panel();
        this.btnAction = new Button();
        this.btnCancel = new Button("Cancel");

        this.btnAction.addClickListener((event) -> {
            try {
                Step nextStep = currentStep.next();
                if (nextStep != null) {
                    currentStep = nextStep;
                    pnlMain.setContent(currentStep);

                    if (currentStep.isFinal())
                        btnAction.setCaption("Finish");
                    else
                        btnAction.setCaption("Next");

                    fireEvent(new WizardEvent(WizardEvent.TYPE_NEXT_STEP, currentStep.getProperties()));
                } else 
                    fireEvent(new WizardEvent(WizardEvent.TYPE_FINAL_STEP,currentStep.getProperties()));
            } catch (InvalidArgumentException ex) {
                fireEvent(new WizardEvent(WizardEvent.TYPE_STEP_REJECTED, currentStep.getProperties()));
                Notifications.showError(ex.getLocalizedMessage());
            }

        });

        btnCancel.addClickListener((event) -> {
            fireEvent(new WizardEvent(WizardEvent.TYPE_CANCEL, currentStep.getProperties()));
        });

        this.pnlMain.setContent(firstStep);
        this.currentStep = firstStep;
        if (currentStep.isFinal())
            this.btnAction.setCaption("Finish");
        else
            this.btnAction.setCaption("Next");

        HorizontalLayout lytButtons = new HorizontalLayout(btnAction, btnCancel);
        
        this.addComponents(pnlMain, lytButtons);
        this.setComponentAlignment(lytButtons, Alignment.MIDDLE_RIGHT);
        this.setExpandRatio(pnlMain, 9);
        this.setExpandRatio(lytButtons, 1);

        this.listeners = new ArrayList<>();
    }

    public void fireEvent(WizardEvent event) {
        listeners.forEach((listener) -> { listener.eventFired(event); });
    }

    public void addEventListener(WizardEventListener listener) {
        listeners.add(listener);
    }

    public void clearListeners() {
        listeners.clear();
    }
    
    public class WizardEvent {
        /**
         * When passing from one step to the next
         */
        public final static int TYPE_NEXT_STEP = 1;
        /**
         * When the wizard ends and the user hits Finish
         */
        public final static int TYPE_FINAL_STEP = 2;
        /**
         * If passing from one step to the next is rejected by the validate() method
         */
        public final static int TYPE_STEP_REJECTED = 3;
        /**
         * The user hit "Cancel"
         */
        public final static int TYPE_CANCEL = 4;
        /**
         * Type of event. See TYPE_XXX for possible values
         */
        private int type;
        /**
         * The information the issuer consider relevant for the listener to receive
         */
        private Properties information;

        public WizardEvent(int type, Properties information) {
            this.type = type;
            this.information = information;
        }

        public int getType() {
            return type;
        }

        public Properties getInformation() {
            return information;
        }
    }
    
    /**
     * Interface to be implemented by all components interested in listening to the events fired by a {@link Wizard}
     */
    public interface WizardEventListener extends EventListener {
        /**
         * What to do when an event is fired
         * @param event The event to be fired
         */
        public void eventFired(WizardEvent event);
    }
    
    /**
     * Interface implemented by all the steps in the wizard
     */
    public interface Step extends Component {
        /**
         * What to do next. This method also validates if the information requested in the step is correct. Should be called upon the user clicking on th "Next/Finish" button
         * @return Null if it's the final step, or the next step otherwise
         * @throws InvalidArgumentException If the information provided by the user in the current step in missing or inconsistent
         */
        public Step next() throws InvalidArgumentException;
        /**
         * Indicates if the step is the last one
         * @return True if the step is the last one, false otherwise
         */
        public boolean isFinal();
        /**
         * Each step saves the information related to it (forms, tree selections, etc) in a Properties object. This method allows to retrieve it. 
         * Also, the last step of a wizard packs the information in the WizardEvent.TYPE_FINAL_STEP step
         * @return A Properties instance with the information collected in that step or anything relevant to the WizardEventListener
         */
        public Properties getProperties();
    }    
}