/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation.actions;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.AbstractVisualInventoryAction;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual action to delete a business object
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class DeleteBusinessObjectVisualAction extends AbstractVisualInventoryAction {
    /**
     * New business object visual action parameter business object.
     */
    public static String PARAM_BUSINESS_OBJECT = "businessObject"; //NOI18N
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translation Service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to module action.
     */
    @Autowired
    private DeleteBusinessObjectAction deleteBusinessObjectAction;
    
    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        BusinessObjectLight businessObject = (BusinessObjectLight) parameters.get(PARAM_BUSINESS_OBJECT);
        Dialog wdwDeleteBusinessObject = new Dialog();
        if (businessObject != null) {
            H4 hdnTitle = new H4(deleteBusinessObjectAction.getDisplayName());
            Label lblDeleteConfirmation = new Label(ts.getTranslatedString("module.navigation.actions.delete-business-object.confirmation-message"));
            //Butons
            Button btnOk = new Button(ts.getTranslatedString("module.general.messages.ok"));
            Button btnCancel = new Button(ts.getTranslatedString("module.general.messages.cancel"), event -> wdwDeleteBusinessObject.close());
            ShortcutRegistration btnOkShortcut = btnOk.addClickShortcut(Key.ENTER).listenOn(wdwDeleteBusinessObject);
            btnOk.addClickListener(event -> {
                try {
                    deleteBusinessObjectAction.getCallback().execute(new ModuleActionParameterSet(
                            new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_CLASS_NAME, businessObject.getClassName()),
                            new ModuleActionParameter(NewBusinessObjectAction.PARAM_PARENT_OID, businessObject.getId())
                    ));
                    ActionResponse ar = new ActionResponse();
                                        
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                            ts.getTranslatedString("module.navigation.actions.delete-business-object.ui.success"),
                            DeleteBusinessObjectAction.class
                            )
                    );
                    wdwDeleteBusinessObject.close();
                } catch (ModuleActionException ex) {
                    fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(
                            ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR, ex.getMessage(),
                            DeleteBusinessObjectAction.class));
                }
                btnOkShortcut.remove();
                event.unregisterListener();
            });
            btnOk.setAutofocus(true);
            
            HorizontalLayout lytButtons = new HorizontalLayout(btnCancel,btnOk);
            VerticalLayout lytMain = new VerticalLayout(hdnTitle, lblDeleteConfirmation, lytButtons);
            lytMain.setHorizontalComponentAlignment(FlexComponent.Alignment.END, lytButtons);
            wdwDeleteBusinessObject.add(lytMain);
        }
        return wdwDeleteBusinessObject;
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteBusinessObjectAction;
    }

    @Override
    public String appliesTo() {
        return null;
    }
}
