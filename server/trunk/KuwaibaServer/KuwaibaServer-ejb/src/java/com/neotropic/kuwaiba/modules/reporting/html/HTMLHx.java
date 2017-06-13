/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.html;

/**
 * A simple HTML h1 tag.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class HTMLHx extends HTMLComponent {
    private String content;
    private int x;
    
    public HTMLHx(int x, String content) {
        this.content = content;
        this.x = x;
    }
    
    public HTMLHx(String style, String cssClass, int x, String content) {
        super(style, cssClass);
        this.x = x;
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String asHTML() {
        return new StringBuilder().append("<h").append(x).append("").append(style == null ? "" : " style=\"" + style + "\"")   //NOI18N
                                  .append(cssClass == null ? "" : " class=\"" + cssClass + "\"").append(">")  //NOI18N
                                  .append(content).append("</h1>").toString(); //NOI18N
    }
    
}
