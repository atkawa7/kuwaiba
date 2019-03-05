/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.webapplicationmodule.presentation.windows;

import com.neotropic.databasemodule.entity.User;
import com.neotropic.databasemodule.service.UserService;
import com.neotropic.generalservices.CrudAction;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;

/**
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@SpringComponent
@UIScope
@ComponentScan("com.neotropic")
public class UserScreenWindow extends Dialog {

    private FormLayout contentFormLayout;
    private TextField txtFirstName;
    private TextField txtSecondName;
    private TextField txtFirstLastName;
    private TextField txtSecondLastName;
    private TextField txtIdentification;
    private TextField txtEmail;
    private TextField txtPhoneNumber;
    private PasswordField psfUserPassword;
    private NativeButton btnCRUD;
    private NativeButton btnCerrar;
    private CrudAction action;
    private User user;
    private Binder<User> binder;
    private final UserService userService;

    @Autowired
    public UserScreenWindow(UserService userService, User userIncomming, CrudAction action) {
        this.userService = userService;
        this.action = action;
        this.user = userIncomming;
        initComponents();
    }

    private void initComponents() {
        this.setCloseOnOutsideClick(false);
        //inicializate binder        
        setBinder(new BeanValidationBinder<>(User.class));

        //define content
        setContentFormLayout(new FormLayout());

        // Create the fields
        setTxtFirstName(new TextField());
        setTxtFirstName(new TextField());
        setTxtSecondName(new TextField());
        setTxtFirstLastName(new TextField());
        setTxtSecondLastName(new TextField());
        setTxtIdentification(new TextField());
        setTxtPhoneNumber(new TextField());
        setTxtEmail(new TextField());
        setPsfUserPassword(new PasswordField());
        setBtnCRUD(new NativeButton());
        setBtnCerrar(new NativeButton("Close"));

        //set fields properties
        getTxtFirstName().setRequired(true);
        getTxtFirstName().setRequiredIndicatorVisible(true);
        getTxtFirstLastName().setRequired(true);
        getTxtFirstLastName().setRequiredIndicatorVisible(true);
        getTxtPhoneNumber().setRequired(true);
        getTxtPhoneNumber().setRequiredIndicatorVisible(true);
        getTxtEmail().setRequired(true);
        getTxtEmail().setRequiredIndicatorVisible(true);
        getPsfUserPassword().setRequired(true);
        getPsfUserPassword().setRequiredIndicatorVisible(true);
        getPsfUserPassword().addValueChangeListener(event -> passwordChangedReview(event));
        getBtnCRUD().getElement().getThemeList().add("primary");
        getBtnCRUD().setWidth("-1px");
        getBtnCRUD().setHeight("-1px");
        getBtnCerrar().setWidth("-1px");
        getBtnCerrar().setHeight("-1px");

        //init binding
        getBinder().forField(getTxtFirstName())
                .asRequired("Name is required")
                .bind(User::getFirstName, User::setFirstName);
        getBinder().forField(getTxtFirstLastName())
                .asRequired("Last Name is required")
                .bind(User::getFirstLastname, User::setFirstLastname);
        getBinder().forField(getTxtEmail())
                .asRequired("Email is required")
                .withValidator(new EmailValidator("Incorrect email address"))
                .bind(User::getEmail, User::setEmail);
        getBinder().forField(getTxtPhoneNumber())
                .asRequired("Phone number is required")
                .bind(User::getPhoneNumber, User::setPhoneNumber);
        getBinder().forField(getPsfUserPassword())
                .asRequired("Password is required")
                .bind(User::getPassword, User::setPassword);

        // enable/disable save button 
        getBinder().addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            getBtnCRUD().setEnabled(isValid);

        });

        //set userModel
        getBinder().setBean(getUser());

        //define view
        getContentFormLayout().addFormItem(getTxtFirstName(), "First Name");
        getContentFormLayout().addFormItem(getTxtSecondName(), "Second Name");
        getContentFormLayout().addFormItem(getTxtFirstLastName(), "First Last Name");
        getContentFormLayout().addFormItem(getTxtSecondLastName(), "Second Last Name");
        getContentFormLayout().addFormItem(getTxtEmail(), "E-mail");
        getContentFormLayout().addFormItem(getTxtPhoneNumber(), "Phone Number");
        getContentFormLayout().addFormItem(getPsfUserPassword(), "Password");

        //set button and behaivor
        switch (this.getAction()) {
            case CREATE:
                getBtnCRUD().setText("Save");
                getBtnCRUD().addClickListener(event -> createElement());
                getBtnCerrar().addClickListener(event -> close());

                // enable/disable save button while it not valid
                getBtnCRUD().setEnabled(false);
                break;
            case UPDATE:
                getBtnCRUD().setText("Update");
                getBtnCRUD().addClickListener(event -> updateElement());
                getBtnCerrar().addClickListener(event -> close());

                // enable/disable save button while editing
                getBtnCRUD().setEnabled(false);
                break;
            case DELETE:
                this.getBtnCRUD().setText("Delete");
                this.getBtnCRUD().addClickListener(event -> deleteElement());
                this.getBtnCerrar().addClickListener(event -> close());
                getContentFormLayout().addFormItem(new Label(getUser().getFirstName() + " " + getUser().getFirstLastname() + " ?"), "Deleted ");
                break;
        }

        super.add(getContentFormLayout(), getBtnCRUD(), getBtnCerrar());
    }

    /**
     * created object
     */
    private void createElement() {
        if (getBinder().validate().isOk()) {

            System.out.println("user: " + getUser());
            if (userService.saveUser(getUser())) {
                this.close();
                showNotification("User created successfully.");
            } else {
                showNotification("Error: User cannot be created.");
            }

        } else {
            showNotification("The form has mistakes.");
        }
    }

    /**
     * updated object
     */
    private void updateElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * deleted object
     */
    private void deleteElement() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * change password date, insert if password was changed
     *
     * @param event
     */
    private void passwordChangedReview(AbstractField.ComponentValueChangeEvent<PasswordField, String> event) {
        System.out.println("passold " + event.getOldValue());
        System.out.println("passnew " + event.getValue());
        if (event.getOldValue() != null && !event.getOldValue().equals(event.getValue())) {
            getUser().setPasswordDate(new Date()); //if password change 
        }
    }

    /**
     * display notifications
     *
     * @param notification;Notification
     */
    private void showNotification(String notificationMessage) {
        // keep the notification visible a little while after moving the
        // mouse, or until clicked
        Notification notification = new Notification(
                notificationMessage, 2000,
                Position.BOTTOM_END);
        notification.open();
    }

    //getters and setters
    /**
     * @return the txtFirstName
     */
    public TextField getTxtFirstName() {
        return txtFirstName;
    }

    /**
     * @param txtFirstName the txtFirstName to set
     */
    public void setTxtFirstName(TextField txtFirstName) {
        this.txtFirstName = txtFirstName;
    }

    /**
     * @return the txtSecondName
     */
    public TextField getTxtSecondName() {
        return txtSecondName;
    }

    /**
     * @param txtSecondName the txtSecondName to set
     */
    public void setTxtSecondName(TextField txtSecondName) {
        this.txtSecondName = txtSecondName;
    }

    /**
     * @return the txtFirstLastName
     */
    public TextField getTxtFirstLastName() {
        return txtFirstLastName;
    }

    /**
     * @param txtFirstLastName the txtFirstLastName to set
     */
    public void setTxtFirstLastName(TextField txtFirstLastName) {
        this.txtFirstLastName = txtFirstLastName;
    }

    /**
     * @return the txtSecondLastName
     */
    public TextField getTxtSecondLastName() {
        return txtSecondLastName;
    }

    /**
     * @param txtSecondLastName the txtSecondLastName to set
     */
    public void setTxtSecondLastName(TextField txtSecondLastName) {
        this.txtSecondLastName = txtSecondLastName;
    }

    /**
     * @return the txtIdentification
     */
    public TextField getTxtIdentification() {
        return txtIdentification;
    }

    /**
     * @param txtIdentification the txtIdentification to set
     */
    public void setTxtIdentification(TextField txtIdentification) {
        this.txtIdentification = txtIdentification;
    }

    /**
     * @return the txtEmail
     */
    public TextField getTxtEmail() {
        return txtEmail;
    }

    /**
     * @param txtEmail the txtEmail to set
     */
    public void setTxtEmail(TextField txtEmail) {
        this.txtEmail = txtEmail;
    }

    /**
     * @return the txtPhoneNumber
     */
    public TextField getTxtPhoneNumber() {
        return txtPhoneNumber;
    }

    /**
     * @param txtPhoneNumber the txtPhoneNumber to set
     */
    public void setTxtPhoneNumber(TextField txtPhoneNumber) {
        this.txtPhoneNumber = txtPhoneNumber;
    }

    /**
     * @return the psfUserPassword
     */
    public PasswordField getPsfUserPassword() {
        return psfUserPassword;
    }

    /**
     * @param psfUserPassword the psfUserPassword to set
     */
    public void setPsfUserPassword(PasswordField psfUserPassword) {
        this.psfUserPassword = psfUserPassword;
    }

    /**
     * @return the btnCRUD
     */
    public NativeButton getBtnCRUD() {
        return btnCRUD;
    }

    /**
     * @param btnCRUD the btnCRUD to set
     */
    public void setBtnCRUD(NativeButton btnCRUD) {
        this.btnCRUD = btnCRUD;
    }

    /**
     * @return the btnCerrar
     */
    public NativeButton getBtnCerrar() {
        return btnCerrar;
    }

    /**
     * @param btnCerrar the btnCerrar to set
     */
    public void setBtnCerrar(NativeButton btnCerrar) {
        this.btnCerrar = btnCerrar;
    }

    /**
     * @return the action
     */
    public CrudAction getAction() {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(CrudAction action) {
        this.action = action;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return the binder
     */
    public Binder<User> getBinder() {
        return binder;
    }

    /**
     * @param binder the binder to set
     */
    public void setBinder(Binder<User> binder) {
        this.binder = binder;
    }

    /**
     * @return the contentFormLayout
     */
    public FormLayout getContentFormLayout() {
        return contentFormLayout;
    }

    /**
     * @param contentFormLayout the contentFormLayout to set
     */
    public void setContentFormLayout(FormLayout contentFormLayout) {
        this.contentFormLayout = contentFormLayout;
    }

}
