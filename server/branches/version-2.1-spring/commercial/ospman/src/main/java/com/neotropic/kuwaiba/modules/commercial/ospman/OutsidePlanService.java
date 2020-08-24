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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class OutsidePlanService {
    /**
     * Special relationship to assign a Generic Container with another Generic Container.
     */
    public static final String SPECIAL_RELATIONSHIP_OSPMAN_HAS_PATH = "ospmanHasPath"; //NOI18N
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Translate Service.
     */
    @Autowired
    private TranslationService ts;
    
    @PostConstruct
    public void init() {
        mem.setSpecialRelationshipDisplayName(
            SPECIAL_RELATIONSHIP_OSPMAN_HAS_PATH, 
            ts.getTranslatedString("module.ospman.special-relationship.ospman-has-path.display-name"));
    }
}
