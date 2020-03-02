/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.util.i18n;

import java.util.Locale;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 * A wrapper to fetch the internationalization keys using a short syntax 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class I18N {

    private static final String BUNDLE_PATH="LANG";
    
    public static String gm(String key){
        try {
            Context context = new InitialContext();
            String locale = (String)context.lookup("java:comp/env/locale");
            String[] localArray = locale.split("-");
            
            return java.util.ResourceBundle.getBundle(BUNDLE_PATH, new Locale(localArray[0], localArray[1])).getString(key);
        } catch (Exception ex) {
            ex.printStackTrace();
            return java.util.ResourceBundle.getBundle(BUNDLE_PATH, Locale.getDefault()).getString(key);
        }
    }
}
