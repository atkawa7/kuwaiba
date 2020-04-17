package com.neotropic.vaadin.component;

import com.neotropic.vaadin.component.CountryService.Country;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and
 * use @Route annotation to announce it in a URL as a Spring managed
 * bean.
 * Use the @PWA annotation make the application installable on phones,
 * tablets and some desktop browsers.
 * <p>
 * A new instance of this class is created for every new user and every
 * browser tab/window.
 */
@Route
@PWA(name = "Vaadin Application",
        shortName = "Vaadin App",
        description = "This is an example Vaadin application.",
        enableInstallPrompt = true)
@CssImport(value = "./styles/vaadin-paper-autocomplete-styles.css")
public class MainView extends VerticalLayout {
    final String LOGIN = "login";
    final String ID = "id";
    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param countryService The country service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired CountryService countryService) {
        // Select State
        PaperAutocomplete<Country> inputLocal = new PaperAutocomplete();
        inputLocal.setId("input-local"); //NOI18N
        inputLocal.setLabel("Select country");
        inputLocal.setNoLabelFloat(true);
        inputLocal.setSource(Country::getName, Country::getAbbreviation, countryService.getCountries());
        add(inputLocal);
        // With a placeholder
        PaperAutocomplete<Country> inputLocalPlaceholder = new PaperAutocomplete();
        inputLocalPlaceholder.setId("input-local-placeholder"); //NOI18N
        inputLocalPlaceholder.setLabel("Country");
        inputLocalPlaceholder.setPlaceholder("With a placeholder");
        inputLocalPlaceholder.setAlwaysFloatLabel(true);
        inputLocalPlaceholder.setSource(Country::getName, Country::getAbbreviation, countryService.getCountries());
        add(inputLocalPlaceholder);
        // State (custom styled)
        PaperAutocomplete<Country> styled = new PaperAutocomplete();
        styled.setId("styled"); //NOI18N
        styled.setLabel("Country (custom styled)");
        styled.setSource(Country::getName, Country::getAbbreviation, countryService.getCountries());
        add(styled);
        
        String ICON_SEARCH = "search"; //NOI18N
        // Using suffix
//        PaperAutocomplete suffix = new PaperAutocomplete();
//        suffix.setId("suffix"); //NOI18N
//        suffix.setLabel("Using suffix");
//        suffix.setSource(getSource());
//        
//        PaperIconButton btnSuffix = new PaperIconButton();
//        btnSuffix.setSlot(suffix.getId().get());
//        btnSuffix.setSuffix(true);
//        btnSuffix.setIcon(ICON_SEARCH);
//        
//        suffix.getElement().appendChild(btnSuffix.getElement());
//        add(suffix);
        // Using prefix
//        PaperAutocomplete preffix = new PaperAutocomplete();
//        preffix.setId("prefix"); //NOI18N
//        preffix.setLabel("Using prefix");
//        preffix.setSource(getSource());
//        
//        PaperIconButton btnPrefix = new PaperIconButton();
//        btnPrefix.setSlot(preffix.getId().get());
//        btnPrefix.setPrefix(true);
//        btnPrefix.setIcon(ICON_SEARCH);
//        
//        preffix.getElement().appendChild(btnPrefix.getElement());
//        add(preffix);
        // Auto highlight first option
        PaperAutocomplete<Country> highlightFirst = new PaperAutocomplete();
        highlightFirst.setId("highlightFirst");
        highlightFirst.setLabel("Auto highlight first option");
        highlightFirst.setHighlightFirst(true);
        highlightFirst.setSource(Country::getName, Country::getAbbreviation, countryService.getCountries());
        add(highlightFirst);
        // Show results on focus
        PaperAutocomplete<Country> show = new PaperAutocomplete();
        show.setId("show");
        show.setLabel("Show results on focus");
        show.setShowResultsOnFocus(true);
        show.setSource(Country::getName, Country::getAbbreviation, countryService.getCountries());
        show.addAutocompleteSelectedListener(event -> {
            Notification.show(event.getText());
        });
        add(show);
        //input-remote-users
        PaperAutocomplete<Country> inputRemoteUsers = new PaperAutocomplete();
        inputRemoteUsers.setTextProvider(Country::getName);
        inputRemoteUsers.setValueProvider(Country::getAbbreviation);
        inputRemoteUsers.setId("input-remote-users");
        inputRemoteUsers.setLabel("Select country");
        inputRemoteUsers.setMinLength(2);
        inputRemoteUsers.setRemoteSource(true);
                
        inputRemoteUsers.addAutocompleteSelectedListener(event -> {
        });
        inputRemoteUsers.addAutocompleteChange(event -> {
            List<Country> suggestions = countryService.getCountries()
                .stream()
                .filter(country -> country.getName() != null && country.getName().toLowerCase().startsWith(event.getOptionText().toLowerCase()))
                .collect(Collectors.toList());
            inputRemoteUsers.suggestions(suggestions);
        });
        add(inputRemoteUsers);
        
        PaperAutocomplete<Country> inputRemoteCustomProperties = new PaperAutocomplete();
        inputRemoteCustomProperties.setTextProvider(Country::getName);
        inputRemoteCustomProperties.setValueProvider(Country::getAbbreviation);
        inputRemoteCustomProperties.setId("input-remote-custom-properties");
        inputRemoteCustomProperties.setLabel("Select country");
        inputRemoteCustomProperties.setMinLength(2);
        inputRemoteCustomProperties.setRemoteSource(true);
        inputRemoteCustomProperties.setTextProperty("name");
        inputRemoteCustomProperties.setValueProperty("abbreviation");
        
        inputRemoteCustomProperties.addAutocompleteSelectedListener(event -> {
        });
        inputRemoteCustomProperties.addAutocompleteChange(event -> {
            List<Country> suggestions = countryService.getCountries()
                .stream()
                .filter(country -> country.getName() != null && country.getName().toLowerCase().startsWith(event.getOptionText().toLowerCase()))
                .collect(Collectors.toList());
            inputRemoteCustomProperties.suggestions(suggestions);
        });
        add(inputRemoteCustomProperties);
    }
}
