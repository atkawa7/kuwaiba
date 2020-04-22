/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin.component.demo.services;

import com.neotropic.vaadin.component.demo.model.Source;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class StateService {
    @Autowired
    private UtilService utilService;
    
    public List<Source> getStates() {
        List<Source> states = new ArrayList();
        try {
            JsonObject jsonObject = utilService.getResourceAsJsonObject(
                "json/states.json");
            JsonArray jsonAccounts = jsonObject.getArray("states");
            for (int i = 0; i < jsonAccounts.length(); i++) {
                JsonObject jsonAccount = jsonAccounts.getObject(i);
                states.add(new Source(
                    jsonAccount.getString("text"),
                    jsonAccount.getString("value")
                ));
            }
        } catch (IOException ex) {
            Logger.getLogger(AccountService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return states;
    }
}
