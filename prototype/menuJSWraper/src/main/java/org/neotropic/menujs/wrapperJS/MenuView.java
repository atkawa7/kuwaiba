package org.neotropic.menujs.wrapperJS;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

import java.util.ArrayList;
import java.util.List;

//The necessary CSS and Javascript dependencies
@JavaScript("/js/jquery.js")
@JavaScript("/js/jquery.smartmenus.js")
@StyleSheet("/css/sm-core-css.css")
@StyleSheet("/css/sm-blue/sm-blue.css")
@Route("Menu")
public class MenuView extends VerticalLayout  {
    private SmartMenuWrapper menuWrapper;

    public MenuView() {
        createMenu();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        setSizeFull();
        add(new MenuDiv());
        attachEvent.getUI().getPage().executeJavaScript(createJs());

    }


    @Tag("div")
    public static class MenuDiv extends Component {
        public MenuDiv() {
            getElement().setProperty("id", "menu");
            getElement().setProperty("style", "width:100%");
        }

    }

    /**
     * creare java script to be executed
     * @return
     */
    private String createJs(){
        this.menuWrapper = new SmartMenuWrapper("main-nav","main-menu");
        String menu = menuWrapper.build();
        System.out.println(menu);
        return menuWrapper.createJsLogic();
    }

    /**
     * create display layout with dummy items
     */
    private void createMenu (){
        List<MenuItem> menuItems = new ArrayList<>();
        MenuItem item1 = new MenuItem();
        item1.setTitle("HOME");
        item1.setUrl("#");

        RouterLink lnk = new RouterLink("", HellowWord.class);
        MenuItem item2 = new MenuItem();
        item2.setTitle("Docs");
        item2.setUrl(lnk.getHref());

        MenuItem item3 = new MenuItem();
        item3.setTitle("Cafe");
        item3.setUrl("#");
        MenuItem submenu31 = new MenuItem();
        submenu31.setTitle("Negro");
        submenu31.setUrl("#");
        MenuItem submenu32 = new MenuItem();
        submenu32.setTitle("Con Leche");
        submenu32.setUrl("#");
        item3.addSubItem(submenu31);
        item3.addSubItem(submenu32);
        //add irems to menu
        menuItems.add(item1);
        menuItems.add(item2);
        menuItems.add(item3);

        //add item a new div if is necessary
        SmartMenuWrapper menuWrapper = new SmartMenuWrapper("main-nav","main-menu", menuItems);
        String menu = menuWrapper.build();
        Div divMenu = new Div();
        divMenu.add(new Html(menu));
        super.add(divMenu);
    }


}
