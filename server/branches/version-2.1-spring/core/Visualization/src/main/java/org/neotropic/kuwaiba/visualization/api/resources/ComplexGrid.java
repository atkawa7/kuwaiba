/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.kuwaiba.visualization.api.resources;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;

/**
 *
 * @author adr
 * @param <T>
 */
public class ComplexGrid<T> extends Grid<T>{
    /**
     * The class name of the objects in the grid
     */
    private String className;
    /**
     * The searched text is added to the filter
     */
    private String searchedText;
    /**
     * filter for the grid only use name to filter 
     */
    private final GridFilter gridFilter;//TODO change string for HashMap like attributes ib order to filter for objs attributes
    /**
     * The grid Filter
     */
    private ConfigurableFilterDataProvider<T, Void, GridFilter> dpConfigurableFilter;
    /**
     * The first column of the grid for filter the name
     */
    private Grid.Column<T> firstColumn;
    
    private TextField txtFilterField;

    private final BusinessEntityManager bem;
    
    private CallbackDataProvider<T, GridFilter> provider;
    
    public ComplexGrid(BusinessEntityManager bem, String className, String searchedText) {
        super();
        this.bem = bem;
        this.className = className;
        this.txtFilterField = new TextField();
        this.setSelectionMode(Grid.SelectionMode.SINGLE);
        this.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        this.addThemeVariants(GridVariant.LUMO_COMPACT);
        this.gridFilter = new GridFilter();
        this.gridFilter.setName(searchedText);
    }
    
    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public void setClassName(String className) {
        this.className = className;
    }

    public ConfigurableFilterDataProvider<T, Void, GridFilter> getDpConfigurableFilter() {
        return dpConfigurableFilter;
    }

    public void setDpConfigurableFilter(ConfigurableFilterDataProvider<T, Void, GridFilter> dpConfigurableFilter) {
        this.dpConfigurableFilter = dpConfigurableFilter;
    }

//    public Column<T> getFirstColumn() {
//        return firstColumn;
//    }
//
    public void setFirstColumn(Column<T> firstColumnsMap) {
        this.firstColumn = firstColumnsMap;
    }

    public TextField getObjNameField() {
        return txtFilterField;
    }

    public void setObjNameField(TextField objNameField) {
        this.txtFilterField = objNameField;
    }
    
     /**
     * Creates the filter for every grid in the header row 
     */
    public void createPaginateGridFilter(){
        HeaderRow filterNameRow = appendHeaderRow();
        // object name filter
        txtFilterField.addValueChangeListener(event -> {
            if(!event.getValue().isEmpty())
                gridFilter.setName(event.getValue());
            else
                gridFilter.setName(null);

           dpConfigurableFilter.refreshAll();
        });

        txtFilterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterNameRow.getCell(firstColumn).setComponent(txtFilterField);
        txtFilterField.setSizeFull();
        txtFilterField.setPlaceholder(className);
    }
        
    //Provider for every Class grid
    public void createDataProviderPaginateGrid(){
        //data provider
        provider = DataProvider.fromFilteringCallbacks(query ->
            {   
                GridFilter filter = query.getFilter().orElse(null);
                HashMap<String, String> valuesToFilter = new HashMap<>();
                valuesToFilter.put("name", filter == null ? null : filter.getName());
                
                List<T> objs = new ArrayList<>();
                try {
                    objs.addAll((Collection<? extends T>) bem.getObjectOfClassLigth(className, valuesToFilter, 
                    query.getOffset(), query.getLimit()));
                    return objs.stream();
                }catch (InvalidArgumentException | MetadataObjectNotFoundException ex){
                    return objs.stream();
                }
            }, query ->{
                try {
                    GridFilter filter = query.getFilter().orElse(null);
                    HashMap<String, String> valuesToFilter = new HashMap<>();
                    valuesToFilter.put("name", filter == null ? null : filter.getName());
                    int count = bem.getObjectOfClassLigth(className, valuesToFilter, 
                            query.getOffset(), query.getLimit()).size();
                    if(count <= 50)
                        setHeightByRows(true);
                    return count;
                }catch (InvalidArgumentException | MetadataObjectNotFoundException ex){
                    return 0;
                }
            });
        
        dpConfigurableFilter = provider.withConfigurableFilter();
        dpConfigurableFilter.setFilter(gridFilter);
        setDataProvider(dpConfigurableFilter);
    }
}
