package com.neotropic.demo.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

@Route(layout = MainLayout.class)
@CssImport("./styles/shared-styles.css")
public class MainView extends AbstractPage {

    private FlexLayout lytActions;
    private Button action1;
    private Grid<Product> grdItems;
    private boolean mobile = false;

    public MainView() {

        List<Product> products = new ArrayList<>();

        products.add(new Product("TV Samsung OLED", "Samsung", 2, 3000000d, "2019"));
        products.add(new Product("Washing Machine", "LG", 2, 1200000d, "2020"));
        products.add(new Product("Asus Laptop", "Samsung", 2, 3000000d, "2019"));
        products.add(new Product("Lenovo Laptop", "Lenovo", 3, 2000000d, "2020"));
        products.add(new Product("TV LG 55'", "LG", 10, 2000000d, "2019"));
        products.add(new Product("TV Sony 60'", "Samsung", 2, 3000000d, "2018"));

        grdItems = new Grid();

        grdItems.setItems(products);

        grdItems.addComponentColumn(product -> createMobileRow(product))
                .setHeader("Products").setKey("mobile")
                .setVisible(false);  // set mobile column hidden by default
        grdItems.addColumn(Product::getName).setHeader("Name");
        grdItems.addColumn(Product::getCompany).setHeader("Company");
        grdItems.addColumn(Product::getYear).setHeader("Year");
        grdItems.addColumn(Product::getQuantity).setHeader("Quantity");
        grdItems.addColumn(Product::getPrice).setHeader("Price");

        grdItems.setHeightByRows(true);

        lytActions = new FlexLayout(); // lfexible layout to wrap responsively
        lytActions.setWrapMode(FlexLayout.WrapMode.WRAP);
        lytActions.setClassName("lytAction");

        action1 = new Button("Action 1");
        Button action2 = new Button("Action 2");
        Button action3 = new Button("Action 3");
        Button action4 = new Button("Action 4");

        lytActions.add(action1, action2, action3, action4);

        add(new H1("Products Inventory"));

        add(lytActions);

        add(new TextField());

        add(grdItems);

    }

    /**
     * Creates the row for the mobile version
     *
     * @param product the item to fill the row
     * @return the new row component
     */
    private Component createMobileRow(Product product) {

        // order the data in a single verticalLayout()
        VerticalLayout vlyt = new VerticalLayout();
        vlyt.setSpacing(false);
        vlyt.add(new HorizontalLayout(createSubTitleLabel("Name: "), new Label(product.getName())));
        vlyt.add(new HorizontalLayout(createSubTitleLabel("Company: "), new Label(product.getCompany())));
        vlyt.add(new HorizontalLayout(createSubTitleLabel("Quantity: "), new Label(product.getQuantity() + "")));

        Label lblPrice = new Label(" $" + product.getPrice());
        lblPrice.getStyle().set("color", "green");

        Label lblYear = new Label(product.getYear() + "");
        lblYear.getStyle().set("color", "red");

        HorizontalLayout lyt = new HorizontalLayout(lblPrice, lblYear);
        lyt.setWidthFull();
        lyt.setJustifyContentMode(JustifyContentMode.END);

        vlyt.add(lyt);

        return vlyt;
    }

    private Label createSubTitleLabel(String text) {
        Label lbl = new Label(text);
        lbl.addClassName("subTitle");
        return lbl;
    }

    @Override
    public void updateResposiveComponents(int widthScreen, boolean mobile) {

        // detect when the 
        if (Boolean.logicalXor(this.mobile, mobile)) {
            Notification.show("Screen changed to :" + (mobile ? " Mobile" : "Desktop"), 1000, Notification.Position.BOTTOM_CENTER);
            this.mobile = mobile;
        }

        //update some classes by code
        if (mobile) {
            action1.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            action1.addThemeVariants(ButtonVariant.LUMO_ERROR);
            lytActions.addClassName("bkGray");
        } else {
            action1.removeThemeVariants(ButtonVariant.LUMO_ERROR);
            action1.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
            lytActions.removeClassName("bkGray");
        }

        //update the grid
        updateVisibleGridColumns(grdItems, 0, mobile);
    }

}
