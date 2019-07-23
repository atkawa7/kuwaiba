package org.neotropic.menujs.wrapperJS;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    private String title;
    private String url;
    private List<MenuItem> subItems;

    public MenuItem() {
    }

    public MenuItem(String title, String url, List<MenuItem> subItems) {
        this.title = title;
        this.url = url;
        this.subItems = subItems;
    }


    public void addSubItem(MenuItem subItem){
        if(this.subItems == null){
            this.subItems = new ArrayList<>();
        }
        this.subItems.add(subItem);
    }

    public String build(){
        String menu = "";
        menu += " <li>";
        if(getUrl() != null){
            menu += " <a href=\""+getUrl()+"\">";
            menu += getTitle()+"</a>";
        }else{
            menu += getTitle();
        }
        if(getSubItems() !=null && !getSubItems().isEmpty()){
            menu += "   <ul>\n";
            for(MenuItem item : getSubItems()){
                menu += "   "+item.build();
            }
            menu += "   </ul>\n";
        }
        menu +=" </li>\n";
        return menu;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<MenuItem> getSubItems() {
        return subItems;
    }

    public void setSubItems(List<MenuItem> subItems) {
        this.subItems = subItems;
    }
}
