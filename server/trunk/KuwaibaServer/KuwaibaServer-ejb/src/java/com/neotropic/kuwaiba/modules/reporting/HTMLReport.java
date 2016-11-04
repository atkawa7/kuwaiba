/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.kuwaiba.modules.reporting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Useful methods to build HTML reports.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class HTMLReport extends InventoryReport {
    /**
     * Text of the embedded style sheet.
     */
    private String embeddedStyleSheet;
    /**
     * List of the URL of the external CSS linked from the report document. Note that the location has to be reachable from whenever the report will be rendered.
     */
    private List<String> linkedStyleSheets;
    /**
     * Text of the embedded Javascript section.
     */
    private String embeddedJavascript;
    /**
     * List of the URL of the external js linked from the report document. Note that the location has to be reachable from whenever the report will be rendered.
     */
    private List<String> linkedJavascriptFiles;
    /**
     * Report components. They will be displayed one after another, so make sure you arrange them properly
     */
    private List<HTMLComponent> components;
  
    public HTMLReport(String title, String copyrightNotice, String author, String version) {
        super(title, copyrightNotice, author, version);
        this.components = new ArrayList<>();
    }

    public String asHTML() {
        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\">"); //NOI18N
        
        if (embeddedStyleSheet != null) {
            builder.append("<style type=\"text/css\">"); //NOI18N
            builder.append(embeddedStyleSheet);
            builder.append("</style>"); //NOI18N
        }
        
        if (linkedStyleSheets != null) {
            for (String linkedStyleSheet : linkedStyleSheets) {
                builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");  //NOI18N
                builder.append(linkedStyleSheet);
                builder.append("\">"); //NOI18N
            }
        }
            
        if (embeddedJavascript != null) {
            builder.append("<script type=\"text/javascript\">"); //NOI18N
            builder.append(embeddedJavascript);
            builder.append("</script>"); //NOI18N
        }
        
        if (linkedJavascriptFiles != null) {
            for (String linkedJavascriptFile : linkedJavascriptFiles) {
                builder.append("<script src=\"");  //NOI18N
                builder.append(linkedJavascriptFile);
                builder.append("\"></script>"); //NOI18N
            }
        }
        
        builder.append("<title>"); //NOI18N
        builder.append(title);
        builder.append(" - Kuwaiba Open Network Inventory</title>"); //NOI18N
        
        builder.append("</head><body>"); //NOI18N
        
        for (HTMLComponent component : components)
            builder.append(component.asHTML());
        
        builder.append("</body></html>"); //NOI18N
        return builder.toString();
    }
    
    @Override
    public byte[] asByteArray() {
        return asHTML().getBytes(StandardCharsets.UTF_8);
    }

    public String getEmbeddedStyleSheet() {
        return embeddedStyleSheet;
    }

    public void setEmbeddedStyleSheet(String embeddedStyleSheet) {
        this.embeddedStyleSheet = embeddedStyleSheet;
    }

    public List<String> getLinkedStyleSheets() {
        return linkedStyleSheets;
    }

    public void setLinkedStyleSheets(List<String> linkedStyleSheets) {
        this.linkedStyleSheets = linkedStyleSheets;
    }

    public String getEmbeddedJavascript() {
        return embeddedJavascript;
    }

    public void setEmbeddedJavascript(String embeddedJavascript) {
        this.embeddedJavascript = embeddedJavascript;
    }

    public List<String> getLinkedJavascriptFiles() {
        return linkedJavascriptFiles;
    }

    public void setLinkedJavascriptFiles(List<String> linkedJavascriptFiles) {
        this.linkedJavascriptFiles = linkedJavascriptFiles;
    }

    public List<HTMLComponent> getComponents() {
        return components;
    }

    public void setComponents(List<HTMLComponent> components) {
        this.components = components;
    }
    
    public static String getDefaultStyleSheet() {
        return      "   body {\n" +
                    "            font-family: Helvetica, Arial, sans-serif;\n" +
                    "            font-size: small;\n" +
                    "            padding: 5px 10px 5px 10px;\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   table {\n" +
                    "            border: hidden;\n" +
                    "            width: 100%;\n" +
                    "          }\n" +
                    "   th {\n" +
                    "            background-color: #94b155;\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "            color: white;\n" +
                    "            font-weight: normal;\n" +
                    "   }\n" +
                    "   td {\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "   }\n" +
                    "   div {\n" +
                    "            padding: 5px 5px 5px 5px;\n" +
                    "   }\n" +
                    "   div.warning {\n" +
                    "            background-color: #FFF3A2;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.error {\n" +
                    "            background-color: #FFD9C7;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.footer {\n" +
                    "            width: 100%;\n" +
                    "            text-align: center;\n" +
                    "            font-style: italic;\n" +
                    "            font-size: x-small;\n" +
                    "            color: #848484;\n" +
                    "   }\n" +
                    "   span.ok {\n" +
                    "            color: green;\n" +
                    "   }\n" +
                    "   span.warning {\n" +
                    "            color: orange;\n" +
                    "   }\n" +
                    "   span.error {\n" +
                    "            color: red;\n" +
                    "   }\n" +
                    "   td.generalInfoLabel {\n" +
                    "            background-color: #c2da8e;\n" +
                    "            width: 20%;\n" +
                    "   }\n" +
                    "   td.generalInfoValue {\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   tr.even {\n" +
                    "            background-color: #f3e270;\n" +
                    "   }\n" +
                    "   tr.odd {\n" +
                    "            background-color: #D1F680;\n" +
                    "   }" +
                    "   hr { \n" +
                    "            display: block; \n"+
                    "            margin-top: 0.5em; \n"+
                    "            margin-bottom: 0.5em; \n"+
                    "            margin-left: auto; \n"+
                    "            margin-right: auto; \n"+
                    "            border-style: inset; \n"+
                    "            border-width: 1px; \n"+
                    "            color: #A5DF00; \n"+
                    "       }  \n";
    }
    
    public static abstract class HTMLComponent {
        /**
         * Component in-line style
         */
        protected String style;
        /**
         * Component's class
         */
        protected String cssClass;

        public HTMLComponent() {}
        
        public HTMLComponent(String style, String cssClass) {
            this.style = style;
            this.cssClass = cssClass;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public String getCssClass() {
            return cssClass;
        }

        public void setCssClass(String cssClass) {
            this.cssClass = cssClass;
        }
        
        public abstract String asHTML();
    }
    
    public static class HTMLTable extends HTMLComponent {
        
        private String[] columnHeaders;
        private List<HTMLRow> rows;

        public HTMLTable(String[] columnHeaders) {
            this.columnHeaders = columnHeaders;
            this.rows = new ArrayList<>();
        }

        public HTMLTable(String style, String cssClass, String[] columnHeaders) {
            super(style, cssClass);
            this.columnHeaders = columnHeaders;
            this.rows = new ArrayList<>();
        }

        public String[] getColumnHeaders() {
            return columnHeaders;
        }

        public void setColumnHeaders(String[] columnHeaders) {
            this.columnHeaders = columnHeaders;
        }

        public List<HTMLRow> getRows() {
            return rows;
        }

        public void setRows(List<HTMLRow> rows) {
            this.rows = rows;
        }
        
        @Override
        public String asHTML() {
            StringBuilder builder = new StringBuilder();
            builder.append("<table").append(style == null ? "" : " style=\"" + style + "\"") //NOI18N
                    .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">"); //NOI18N
            if (columnHeaders != null) {
                builder.append("<tr>"); //NOI18N
                for (String columnHeader : columnHeaders)
                    builder.append("<th>").append(columnHeader).append("</th>"); //NOI18N
                builder.append("</tr>"); //NOI18N
            }
            
            for (HTMLRow row : rows)
                builder.append(row.asHTML());
                
            builder.append("</table>"); //NOI18N
            
            return builder.toString();
        }
    }
    
    public static class HTMLRow extends HTMLComponent {
        private HTMLColumn[] columns;
        
        public HTMLRow(HTMLColumn[] columns) {
            this.columns = columns;
        }
        
        public HTMLRow(String style, String cssClass, HTMLColumn[] columns) {
            super(style, cssClass);
            this.columns = columns;
        }

        public HTMLColumn[] getColumns() {
            return columns;
        }

        public HTMLRow setColumns(HTMLColumn[] columns) {
            this.columns = columns;
            return this;
        }
        
        @Override
        public String asHTML() {
            StringBuilder builder = new StringBuilder();
            builder.append("<tr").append(style == null ? "" : " style=\"" + style + "\"") //NOI18N
                    .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">"); //NOI18N
            for (HTMLColumn column : columns)
                builder.append(column.asHTML());
            builder.append("</tr>"); //NOI18N
            
            return builder.toString();
        }
    }
    
    public static class HTMLColumn extends HTMLComponent {
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
    
    public static class HTMLImage extends HTMLComponent {
        
        private String location;
        
        public HTMLImage(String style, String cssClass, String location) {
            super(style, cssClass);
            this.location = location;
        }

        @Override
        public String asHTML() {
            return new StringBuilder().append("<img").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
                                        .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(" src=\"")  //NOI18N
                                        .append(location).append("\"/>").toString(); //NOI18N
        }
        
    }
}
