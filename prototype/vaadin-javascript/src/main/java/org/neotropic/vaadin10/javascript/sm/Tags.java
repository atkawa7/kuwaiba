/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the MIT and GPLv3 Licenses, Version 1.0 (the "Licenses");
 *  you may not use this file except in compliance with the Licenses.
 *  You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/MIT
 *       https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licenses is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the Licenses.
 */

package org.neotropic.vaadin10.javascript.sm;

import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import java.util.Arrays;
import java.util.Objects;

/**
 * An utility class that contains the general purpose HTML tags to be used by the component.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Tags {
    /**
     * This tag will be used as the functional root of the component.
     */
    @Tag(Tag.NAV)
    public static class MenuNavigationTag extends HtmlContainer {
        public MenuNavigationTag(String className) {
            getElement().getClassList().add(className);
            getElement().setAttribute("role", "navigation");
        }
    }
    
    /**
     * This tag represents a row in the menu. This implementation uses only one row. 
     * This tag is contained inside the <code>nav</code> tag. 
     */
    @Tag(Tag.UL)
    public static class MenuRowTag extends HtmlContainer {
        public MenuRowTag(String id, String... classNames) {
            getElement().getClassList().addAll(Arrays.asList(classNames));
            getElement().setProperty("id", id);
        }
    }
    
    /**
     * A simple HTML <code>a</code> tag. It will be contained inside MenuItemTag instances.
     */
    @Tag(Tag.A)
    public static class MenuLinkTag extends HtmlContainer { 
        /**
         * The item's display label.
         */
        private String label;
        /**
         * The URL this menu entry redirects to. Use "#" for no URL.
         */
        private String url;
        
        /**
         * A real link with a target. 
         * @param label The display label.
         * @param url The target URL. Use "#" for no URL, or simply use {@link #MenuLinkTag(java.lang.String) }.
         */
        public MenuLinkTag(String label, String url) {
            this.label = label;
            this.url = url;
            
            getElement().setAttribute("href", Objects.requireNonNull(url));
            add(new Text(label));
        }

        /**
         * A fake link with just a label.
         * @param label The display label.
         */
        public MenuLinkTag(String label) {
            this.label = label;
            add(new Text(label));
        }

        public String getLabel() {
            return label;
        }

        public String getUrl() {
            return url;
        }
    }
    
    /**
     * A generic UL HTML tag used to encapsulate submenus.
     */
    @Tag(Tag.UL)
    public static class MenuSubMenuTag extends HtmlContainer { }
}
