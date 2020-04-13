package com.neotropic.vaadin.component;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
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
//@CssImport("./styles/shared-styles.css")
//@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-app-layout")
@CssImport(value = "./styles/vaadin-paper-autocomplete-styles.css")
public class MainView extends VerticalLayout {

    /**
     * Construct a new Vaadin view.
     * <p>
     * Build the initial UI state for the user accessing the application.
     *
     * @param service The message service. Automatically injected Spring managed bean.
     */
    public MainView(@Autowired GreetService service) {
        // Select State
        PaperAutocomplete inputLocal = new PaperAutocomplete();
        inputLocal.setId("input-local"); //NOI18N
        inputLocal.setLabel("Select State");
        inputLocal.setNoLabelFloat(true);
        inputLocal.setSource(getSource());
        add(inputLocal);
        // With a placeholder
        PaperAutocomplete inputLocalPlaceholder = new PaperAutocomplete();
        inputLocalPlaceholder.setId("input-local-placeholder"); //NOI18N
        inputLocalPlaceholder.setLabel("State");
        inputLocalPlaceholder.setPlaceholder("With a placeholder");
        inputLocalPlaceholder.setAlwaysFloatLabel(true);
        inputLocalPlaceholder.setSource(getSource());
        add(inputLocalPlaceholder);
        // State (custom styled)
        PaperAutocomplete styled = new PaperAutocomplete();
        styled.setId("styled"); //NOI18N
        styled.setLabel("State (custom styled)");
        styled.setSource(getSource());
        add(styled);
        
        String ICON_SEARCH = "search"; //NOI18N
        // Using suffix
        PaperAutocomplete suffix = new PaperAutocomplete();
        suffix.setId("suffix"); //NOI18N
        suffix.setLabel("Using suffix");
        suffix.setSource(getSource());
        
        PaperIconButton btnSuffix = new PaperIconButton();
        btnSuffix.setSlot(suffix.getId().get());
        btnSuffix.setSuffix(true);
        btnSuffix.setIcon(ICON_SEARCH);
        
        suffix.getElement().appendChild(btnSuffix.getElement());
        add(suffix);
        // Using prefix
        PaperAutocomplete preffix = new PaperAutocomplete();
        preffix.setId("prefix"); //NOI18N
        preffix.setLabel("Using prefix");
        preffix.setSource(getSource());
        
        PaperIconButton btnPrefix = new PaperIconButton();
        btnPrefix.setSlot(preffix.getId().get());
        btnPrefix.setPrefix(true);
        btnPrefix.setIcon(ICON_SEARCH);
        
        preffix.getElement().appendChild(btnPrefix.getElement());
        add(preffix);
        // Auto highlight first option
//        PaperAutocomplete highlightFirst = new PaperAutocomplete();
//        highlightFirst.setId("highlightFirst");
//        highlightFirst.setLabel("Auto highlight first option");
//        highlightFirst.setHighlightFirst(true);
//        highlightFirst.setSource(getSource());
//        add(highlightFirst);
        // Show results on focus
//        PaperAutocomplete show = new PaperAutocomplete();
//        show.setId(ICON_SEARCH);
//        show.setLabel("Show results on focus");
//        show.setShowResultsOnFocus(true);
//        show.setSource(getSource());
//        add(show);
    }
    
    private JsonArray getSource() {
        final String TEXT = "text";
        final String VALUE = "value";
        
        JsonArray source = Json.createArray();
        
        JsonObject obj0 = Json.createObject();
        obj0.put(TEXT, "Alabama");
        obj0.put(VALUE, "AL");
        
        JsonObject obj1 = Json.createObject();
        obj1.put(TEXT, "Alaska");
        obj1.put(VALUE, "AK");
        
        source.set(0, obj0);
        source.set(1, obj1);
        return source;
    }
}
