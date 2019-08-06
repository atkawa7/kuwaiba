/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting.html;

/**
 * A simple HTML table cell (<code>td</code>).
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class HTMLColumn extends HTMLComponent {
    private Object content;

    public HTMLColumn(Object content) {
        this.content = content;
    }

    public HTMLColumn(String style, String cssClass, String content) {
        super(style, cssClass);
        this.content = content;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    @Override
    public String asHTML() {
        return new StringBuilder().append("<td").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
                                    .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">")  //NOI18N
                                    .append(content).append("</td>").toString(); //NOI18N
    }
}