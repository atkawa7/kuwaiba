package com.neotropic.webapplicationmodule.presentation.ListLayouts;

import com.neotropic.databasemodule.entity.User;
import com.neotropic.databasemodule.service.UserService;
import com.neotropic.generalservices.CrudAction;
import com.neotropic.webapplicationmodule.presentation.windows.UserScreenWindow;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 * @version 1.0, 01 march 2019
 */
@PageTitle("Gitge - users")
@Route("userList")
public class UserListLayout extends VerticalLayout {

    //components
    private TextField search;
    private Label lblSearch;
    private Button addNewObject;
    private Grid<User> objectList;
    private User currentUser;
    private VerticalLayout gridLayout;
    private HorizontalLayout topLayout;

    //Allow pagination
    private CallbackDataProvider<User, Void> dataProvider;

    UserService userService;

    public UserListLayout(@Autowired UserService userService) {
        this.userService = userService;
        System.out.println("userService " + userService.userCount());
        initResources();
    }

    protected final void initResources() {

        setTopLayout(new HorizontalLayout());
        setGridLayout(new VerticalLayout());

        //Instanciando el dato provider.
        dataProvider = DataProvider.fromCallbacks(
                //indicando la consulta que retorna la información
                query -> {

                    // Indicando el primer elemento cargado.
                    int offset = query.getOffset();
                    System.out.println("El offset: " + offset);
                    // La cantidad maxima a cargar
                    int limit = query.getLimit();
                    System.out.println("El limit: " + limit);
                    //Enviando el flujo
                    return userService.getAllUsers(offset, limit).stream();
                },
                query -> {
                    //Indicando la cantidad maxima de elementos.
                    return Math.toIntExact(userService.userCount());
                }
        );

        // Apply the filter to grid's data provider. TextField value is never null
        //ResetButtonForTextField.extend(getSearch());
        setLblSearch(new Label("Search:"));
        setSearch(new TextField());
        getSearch().setPlaceholder("Filter by last name");
        getSearch().addValueChangeListener(event -> searchMessageBox(event.getValue()));

        //create new user
        setAddObject(new Button("Add New User"));
        getAddObject().getElement().getThemeList().add("primary");
        getAddObject().setIcon(VaadinIcon.PLUS_CIRCLE.create());
        getAddObject().addClickListener(event -> createPersona());

        // define data from usermodel     
        setListObject(new Grid<>());
        getListObject().setDataProvider(dataProvider);
        getListObject().setSelectionMode(Grid.SelectionMode.NONE);
        //super.getList().addColumn(UserModel::getIdstaff).setCaption("Código");

        getListObject().addColumn(User::getIdentification).setHeader("Identification");
        getListObject().addColumn(User::getFirstName).setHeader("First Name");
        getListObject().addColumn(User::getSecondName).setHeader("Second Name");
        getListObject().addColumn(User::getFirstLastname).setHeader("Firsl Lastname");
        getListObject().addColumn(User::getSecondLastname).setHeader("Second Lastname");

        //this.updateGrid();
        // addNewObject custom buttons
        getListObject().addComponentColumn(userModel -> {
            return addButtonsToGrid(userModel);
        }).setHeader("Opciones");

        //layouts     
        getTopLayout().add(getLblSearch(), getSearch(), getAddObject());
        getGridLayout().add(getListObject());
        super.add(getTopLayout(), getGridLayout());

        //update user objectList
        dataProvider.refreshAll();
    }

    /**
     * addNewObject custom buttons
     *
     * @param userModel;UserModel
     * @return horizontalLayout; HorizontalLayout
     */
    private HorizontalLayout addButtonsToGrid(User userModel) {
        HorizontalLayout buttonLAyout = new HorizontalLayout();

        Button updateButton = new Button(VaadinIcon.EDIT.create());
        updateButton.getElement().getThemeList().add("BUTTON_SMALL");
        updateButton.addClickListener(event -> updatePersona(userModel));
        buttonLAyout.add(updateButton);

        //if (!Objects.equals(userModel.getId(), getUserModel().getId())) {
        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.getElement().getThemeList().add("BUTTON_SMALL");
        deleteButton.addClickListener(event -> removePersona(userModel));
        buttonLAyout.add(deleteButton);

        return buttonLAyout;
    }

    /**
     * delete user
     *
     * @param user
     */
    public void removePersona(User user) {

        /**
         * call user window *
         */
        //UsuarioWindow usuarioWindow = new UsuarioWindow(getUserModel(), user, CrudAction.DELETE);
        //add a listener, which will be executed when the window will be closed
        //usuarioWindow.addCloseListener(closeEvent -> updateGrid());
        //UI.getCurrent().addWindow(usuarioWindow);
    }

    /**
     * edit user
     *
     * @param user
     */
    public void updatePersona(User user) {

        /**
         * call user window *
         */
        //UsuarioWindow usuarioWindow = new UsuarioWindow(getUserModel(), user, CrudAction.UPDATE);
        //UI.getCurrent().addWindow(usuarioWindow);
        //add a listener, which will be executed when the window will be closed
        //usuarioWindow.addCloseListener(closeEvent -> {           //refresh grid to show any changes
        //    updateGrid();
        //}
        //);
    }

    /**
     * create new user
     */
    public void createPersona() {
        User newUser = new User();
        //call user window                 
        //add a listener, which will be executed when the window will be closed
        //usuarioWindow.addCloseListener(closeEvent -> updateGrid());        
        UserScreenWindow userWindow = new UserScreenWindow(userService, newUser, CrudAction.CREATE);
        userWindow.addDialogCloseActionListener(closeEvent -> {
            System.out.println("clse event");
            //refresh grid to show any changes
            updateGrid();
        });
        userWindow.open();

    }

    /**
     * update objectList
     *
     * @return
     */
    public List<User> updateList() {

        List<User> litUser = new ArrayList<>();

        try {
            litUser = userService.getAllUsers();
            if (litUser != null) {
                return litUser;
            }
        } catch (Exception ex) {
            Logger.getLogger(UserListLayout.class.getName()).log(Level.SEVERE, null, ex);
        }

        return litUser;
    }

    /**
     * load all user
     */
    private void updateGrid() {
        //update objectList from data base   
        System.out.println("Refresh dataProvider");
        dataProvider.refreshAll();
    }

    private void searchMessageBox(String value) {
        /*
        ArrayList<User> litUser = null;
        try {

            litUser = new ArrayList<>(repo.findAll(getUserModel(), value));
            if (!value.isEmpty()) {
                if (litUser != null) {
                    getList().setItems(litUser);
                } else {
                    litUser = new ArrayList<>();
                    getList().setItems(litUser);
                }
            } else {
                updateGrid();
            }
        } catch (Exception ex) {
            Logger.getLogger(UserListLayout.class.getName()).log(Level.SEVERE, null, ex);
        }
         */
    }

    //getters and setters
    /**
     * @return the search
     */
    public TextField getSearch() {
        return search;
    }

    /**
     * @param search the search to set
     */
    public void setSearch(TextField search) {
        this.search = search;
    }

    /**
     * @return the addNewObject
     */
    public Button getAddObject() {
        return addNewObject;
    }

    /**
     * @param add the addNewObject to set
     */
    public void setAddObject(Button add) {
        this.addNewObject = add;
    }

    /**
     * @return the objectList
     */
    public Grid<User> getListObject() {
        return objectList;
    }

    /**
     * @param list the objectList to set
     */
    public void setListObject(Grid<User> list) {
        this.objectList = list;
    }

    /**
     * @return the userModel
     */
    public User getUserModel() {
        return currentUser;
    }

    /**
     * @param userModel the userModel to set
     */
    public void setUserModel(User userModel) {
        this.currentUser = userModel;
    }

    /**
     * @return the gridLayout
     */
    public VerticalLayout getGridLayout() {
        return gridLayout;
    }

    /**
     * @param gridLayout the gridLayout to set
     */
    public void setGridLayout(VerticalLayout gridLayout) {
        this.gridLayout = gridLayout;
    }

    /**
     * @return the topLayout
     */
    public HorizontalLayout getTopLayout() {
        return topLayout;
    }

    /**
     * @param topLayout the topLayout to set
     */
    public void setTopLayout(HorizontalLayout topLayout) {
        this.topLayout = topLayout;
    }

    /**
     * @return the lblSearch
     */
    public Label getLblSearch() {
        return lblSearch;
    }

    /**
     * @param lblSearch the lblSearch to set
     */
    public void setLblSearch(Label lblSearch) {
        this.lblSearch = lblSearch;
    }

}
