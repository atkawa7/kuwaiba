package com.neotropic.view;

import com.neotropic.logic.CharacterEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import org.springframework.util.StringUtils;
import com.neotropic.entity.Character;
import com.neotropic.repository.CharacterRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The MainView class is the entry point for Vaadin’s UI logic. In Spring Boot
 * applications you just need to annotate it with @Route and it will be
 * automatically picked up by Spring and shown at the root of your web app. You
 * can customize the URL where the view is shown by giving a parameter to the
 * Route annotation. A simple "hello world" could look like this:
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@Route
public class MainView extends VerticalLayout {

    private final CharacterRepository repo;
    private final CharacterEditor editor;
    final Grid<Character> grid;
    final TextField filter;
    private final Button addNewBtn;

    public MainView(CharacterRepository repo, CharacterEditor editor) {
        this.repo = repo;
        this.editor = editor;
        this.grid = new Grid<>(Character.class);
        this.filter = new TextField();
        this.addNewBtn = new Button("New Character", VaadinIcon.PLUS.create());

        // build layout
        HorizontalLayout actions = new HorizontalLayout(filter, addNewBtn);
        add(actions, grid, editor);

        grid.setHeight("300px");
        grid.setColumns("name", "surname");
        //grid.getColumnByKey("Name").setWidth("50px").setFlexGrow(0);

        filter.setPlaceholder("Filter by surname");

        // Hook logic to components
        // Replace listing with filtered content when user changes filter
        filter.setValueChangeMode(ValueChangeMode.EAGER);
        filter.addValueChangeListener(e -> listCustomers(e.getValue()));

        // Connect selected Customer to editor or hide if none is selected
        grid.asSingleSelect().addValueChangeListener(e -> {
            editor.editCustomer(e.getValue());
        });

        // Instantiate and edit new Customer the new button is clicked
        addNewBtn.addClickListener(e -> editor.editCustomer(new Character("", "")));

        // Listen changes made by the editor, refresh data from backend
        editor.setChangeHandler(() -> {
            editor.setVisible(false);
            listCustomers(filter.getValue());
        });

        // Initialize listing
        listCustomers(null);
    }

    // tag::listCustomers[]
    void listCustomers(String filterText) {
        if (StringUtils.isEmpty(filterText)) {
            //grid.setItems(repo.findAll());
            Iterable<Character> iteratorToCollection = repo.findAll();
            List<Character> myList = StreamSupport.stream(iteratorToCollection.spliterator(), false)
                    .collect(Collectors.toList());

            grid.setItems(myList);
        } else {
            grid.setItems(repo.findBySurnameStartsWithIgnoreCase(filterText));
        }
    }
    // end::listCustomers[]

}
