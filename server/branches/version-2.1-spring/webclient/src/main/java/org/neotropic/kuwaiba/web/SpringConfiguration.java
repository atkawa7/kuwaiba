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

package org.neotropic.kuwaiba.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The Spring basic automated configuration file. 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Configuration
@ComponentScan(basePackages = { "org.neotropic.kuwaiba.core.i18n", // The translation service.
                                "org.neotropic.kuwaiba.core.persistence", //The persistence service.
                                "org.neotropic.kuwaiba.core.apis.integration", // The API necessary to create modules.
                                "org.neotropic.kuwaiba.northbound.ws", // The SOAP-based web service interface implementation.
                                "org.neotropic.kuwaiba.modules.optional.serviceman",
                                "com.neotropic.kuwaiba.commercial", // Commercial modules
                                "org.neotropic.kuwaiba.modules.core.listtypeman.actions",
                                "org.neotropic.util.visual" //visual utilities
                              }) 
public class SpringConfiguration { }
