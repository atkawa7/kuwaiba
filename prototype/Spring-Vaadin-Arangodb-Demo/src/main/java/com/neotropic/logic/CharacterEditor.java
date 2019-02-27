/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.logic;

import com.arangodb.springframework.core.ArangoOperations;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import com.neotropic.repository.CharacterRepository;
import com.neotropic.entity.Character;

/**
 * As Vaadin UIs are just plain Java code, there is no excuse to not write
 * re-usable code from the beginning. Define an editor component for your
 * Customer entity. You’ll make it a Spring-managed bean so you can directly
 * inject the CustomerRepository to the editor and tackle the C, U, and D parts
 * or our CRUD functionality.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@SpringComponent
@UIScope
@ComponentScan("com.neotropic")
public class CharacterEditor extends VerticalLayout implements KeyNotifier {

    @Autowired
    private ArangoOperations operations;
    @Autowired
    private final CharacterRepository repository;

    /**
     * The currently edited customer
     */
    private Character character;

    /* Fields to edit properties in Customer entity */
    TextField name = new TextField("Name");
    TextField surname = new TextField("Surname");

    /* Action buttons */
    // TODO why more code?
    Button save = new Button("Save", VaadinIcon.CHECK.create());
    Button cancel = new Button("Cancel");
    Button delete = new Button("Delete", VaadinIcon.TRASH.create());
    HorizontalLayout actions = new HorizontalLayout(save, cancel, delete);

    Binder<Character> binder = new Binder<>(Character.class);
    private ChangeHandler changeHandler;

    @Autowired
    public CharacterEditor(CharacterRepository repository) {
        this.repository = repository;

        add(name, surname, actions);

        // bind using naming convention
        binder.bindInstanceFields(this);

        // Configure and style components
        setSpacing(true);

        save.getElement().getThemeList().add("primary");
        delete.getElement().getThemeList().add("error");

        addKeyPressListener(Key.ENTER, e -> save());

        // wire action buttons to save, delete and reset
        save.addClickListener(e -> save());
        delete.addClickListener(e -> delete());
        cancel.addClickListener(e -> editCustomer(character));
        setVisible(false);
    }

    void delete() {
        repository.delete(character);
        changeHandler.onChange();
    }

    void save() {
        repository.save(character);
        changeHandler.onChange();
    }

    public interface ChangeHandler {

        void onChange();
    }

    public final void editCustomer(Character c) {
        if (c == null) {
            setVisible(false);
            return;
        }
        final boolean persisted = c.getId() != null;
        if (persisted) {
            // Find fresh entity for editing
            character = repository.findById(c.getId()).get();
        } else {
            character = c;
        }
        cancel.setVisible(persisted);

        // Bind customer properties to similarly named fields
        // Could also use annotation or "manual binding" or programmatically
        // moving values from fields to entities before saving
        binder.setBean(character);

        setVisible(true);

        // Focus first name initially
        name.focus();
    }

    public void setChangeHandler(ChangeHandler h) {
        // ChangeHandler is notified when either save or delete
        // is clicked
        changeHandler = h;
    }

}
