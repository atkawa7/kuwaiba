package org.neotropic.menujs.wrapperJS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SmartMenuWrapper {
    private final String className;
    private final String menuId;
    private final List<MenuItem> items;


    public SmartMenuWrapper(String className, String menuId) {
        this.className = className;
        this.menuId = menuId;
        this.items = new ArrayList<>();
    }

    public SmartMenuWrapper(String className, String menuId, List<MenuItem> items) {
        this.className = className;
        this.menuId = menuId;
        this.items = items;
    }

    public String build() {
        String divContent = "";

        divContent += "<nav class=\"" + getClassName() + "\" role=\"navigation\">\n";
        if (getItems() != null && !getItems().isEmpty()) {
            String menuString = "<ul id=\""+getMenuId()+"\" class=\"sm sm-blue\">\n";
            for (MenuItem item : getItems()) {
                menuString += item.build();
            }
            menuString += "</ul>\n";
            divContent += menuString;
        }
        divContent += "</nav>";
        return divContent;
    }

    public String buildWithScipt() {
        String divContent = "";
        divContent += "<script type=\"text/javascript\">"+ createJsLogic()+ "</script>";
        divContent += "<nav class=\"" + getClassName() + "\" role=\"navigation\">\n";
        if (getItems() != null && !getItems().isEmpty()) {
            String menuString = "<ul id=\""+getMenuId()+"\" class=\"sm sm-blue\">\n";
            for (MenuItem item : getItems()) {
                menuString += item.build();
            }
            menuString += "</ul>\n";
            divContent += menuString;
        }
        divContent += "</nav>";
        return divContent;
    }

    public String createJsLogic(){
        String jsString =
                "$(function() {\n" +
                "  $('#"+getMenuId()+"').smartmenus({\n" +
                "    subMenusSubOffsetX: 1,\n" +
                "    subMenusSubOffsetY: -8\n" +
                "  });\n" +
                "});\n" +
                "\n";
        return jsString;
    }

    /**
     * SmartMenus menu toggle button
     */
    public String getClassName() {
        return className;
    }

    /**
     * id of menu to be taked by css
     */
    public String getMenuId() {
        return menuId;
    }

    public List<MenuItem> getItems() {
        return items;
    }
}
