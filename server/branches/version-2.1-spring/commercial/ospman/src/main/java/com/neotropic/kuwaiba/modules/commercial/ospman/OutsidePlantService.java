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
 * Service to manage the persistence operations in the Outside Plant Manager Module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Service
public class OutsidePlantService {
    // A set of constants (mostly default values) used in the Outside Plant module.
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.centerLatitude</code> can not be found or it's not a number.
     */
    public static double DEFAULT_CENTER_LATITUDE = 3.9353255;
    /**
     * Default map center longitude. This value is used when the configuration variable <code>widgets.simplemap.centerLongitude</code> can not be found or it's not a number.
     */
    public static double DEFAULT_CENTER_LONGITUDE = -73.5377146;
    /**
     * Default map center latitude. This value is used when the configuration variable <code>widgets.simplemap.zoom</code> can not be found or it's not a number.
     */
    public static int DEFAULT_ZOOM = 6;
    /**
     * Default map language (English). This language will be used if the configuration variable <code>widgets.simplemap.language</code> could not be found
     */
    public static String DEFAULT_LANGUAGE = "english"; //NOI18N
    /**
     * The version of the XML document generated by the as
     */
    public static String VIEW_VERSION = "1.0";
    /**
     * Special relationship to assign a Generic Physical Container with another Generic Physical Container.
     */
    public static final String SPECIAL_RELATIONSHIP_OSPMAN_HAS_PATH = "ospmanHasPath"; //NOI18N
    /**
     * Special relationship to assign a Generic Physical Link with a Location (manhole, hand hole, ...)
     */
    public static final String SPECIAL_RELATIONSHIP_OSPMAN_HAS_LOCATION = "ospmanHasLocation"; //NOI18N
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
        mem.setSpecialRelationshipDisplayName(
            SPECIAL_RELATIONSHIP_OSPMAN_HAS_LOCATION, 
            ts.getTranslatedString("module.ospman.special-relationship.ospman-has-location.display-name"));
    }
}
