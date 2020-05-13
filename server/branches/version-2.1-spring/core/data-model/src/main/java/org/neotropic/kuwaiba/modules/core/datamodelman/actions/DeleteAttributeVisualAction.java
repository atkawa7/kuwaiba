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
package org.neotropic.kuwaiba.modules.core.datamodelman.actions;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import org.neotropic.kuwaiba.core.apis.integration.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.AbstractVisualAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionException;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Visual wrapper of delete attribute action
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DeleteAttributeVisualAction extends AbstractVisualAction<Dialog> {

    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the underlying action.
     */
    @Autowired
    private DeleteAttributeAction deleteAttributeAction;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    @Override
    public Dialog getVisualComponent(ModuleActionParameterSet parameters) {
        ClassMetadataLight seletedClass;
        AttributeMetadata attribute;

        if (parameters.containsKey("class") && parameters.containsKey("attribute")) {
            
            seletedClass = (ClassMetadataLight) parameters.get("class");  
            attribute = (AttributeMetadata) parameters.get("attribute");   

        ConfirmDialog wdwDeleteClass = new ConfirmDialog(ts.getTranslatedString("module.general.labels.confirmcaption"),
                ts.getTranslatedString("module.general.labels.confirmdeletemessage"),
                ts.getTranslatedString("module.general.labels.delete"));
        wdwDeleteClass.getBtnConfirm().addClickListener((ev) -> {
            try {

                deleteAttributeAction.getCallback().execute(new ModuleActionParameterSet(
                        new ModuleActionParameter<>("className", seletedClass.getName()),
                        new ModuleActionParameter<>("attributeName", attribute.getName())));

                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS,
                        ts.getTranslatedString("module.datamodelman.actions.attribute-deleted-success"), DeleteAttributeAction.class));
                wdwDeleteClass.close();
            } catch (ModuleActionException ex) {
                fireActionCompletedEvent(new ActionCompletedListener.ActionCompletedEvent(ActionCompletedListener.ActionCompletedEvent.STATUS_ERROR,
                        ex.getMessage(), DeleteAttributeAction.class));
                wdwDeleteClass.close();
            }
        });
        return wdwDeleteClass;
        } else 
            return new Dialog(new Label(ts.getTranslatedString("module.general.messages.object-not-found")));
    }

    @Override
    public AbstractAction getModuleAction() {
        return deleteAttributeAction;
    }
}