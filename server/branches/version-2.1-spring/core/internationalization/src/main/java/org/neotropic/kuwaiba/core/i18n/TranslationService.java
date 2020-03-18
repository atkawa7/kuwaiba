/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.i18n;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import org.springframework.stereotype.Service;

/**
 * This service provides I18N support for the application
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class TranslationService {
    /**
     * The default application language.
     */
    private Locale currentLanguage = Locale.ENGLISH;
    /**
     * The list of languages currently supported and their respective translation bundles. 
     */
    private final HashMap<Locale, ResourceBundle> languages;
    
    
    public Locale getCurrentLanguage() {
        return currentLanguage;
    }
    
    public void setCurrentlanguage(Locale newLanguage) {
        this.currentLanguage = newLanguage;
    }

    public HashMap<Locale, ResourceBundle> getLanguages() {
        return languages;
    }
    
    private ResourceBundle getResourceBundle(Locale language) {
        return languages.get(language);
    }
    
    public String getTranslatedString(String key) {
        return getResourceBundle(currentLanguage).containsKey(key) ?  
                    getResourceBundle(currentLanguage).getString(key) : key;
    }
    
    public TranslationService() {
        this.languages = new HashMap<>();
        // Supported languages 
        Locale enLanguage = new Locale("en");
        languages.put(enLanguage, ResourceBundle.getBundle("i18n.messages", enLanguage));
        Locale esLanguage = new Locale("es");
        languages.put(esLanguage, ResourceBundle.getBundle("i18n.messages", esLanguage));
        Locale ptLanguage = new Locale("pt");
        languages.put(ptLanguage, ResourceBundle.getBundle("i18n.messages", ptLanguage));
        Locale ruLanguage = new Locale("ru");
        languages.put(ruLanguage, ResourceBundle.getBundle("i18n.messages", ruLanguage));
    }
}
