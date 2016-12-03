/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.prototypes.actions;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ActionsFactory {
    static AddCompanyAction addCompanyAction;
    static AddEmployeeAction addEmployeeAction;
    static DeleteElementAction deleteEmployeeAction;
    static AddEmployeeWithAgeAction addEmployeeWithAgeAction;
    
    public static AbstractAction createAddCompanyAction(){
        if (addCompanyAction == null)
           addCompanyAction = new AddCompanyAction();
        return addCompanyAction;
    }
    
    public static AbstractAction createAddEmployeeAction(){
        if (addEmployeeAction == null)
           addEmployeeAction = new AddEmployeeAction();
        return addEmployeeAction;
    }
    
    public static AbstractAction createAddEmployeeWithAgeAction(){
        if (addEmployeeWithAgeAction == null)
           addEmployeeWithAgeAction = new AddEmployeeWithAgeAction();

        return addEmployeeWithAgeAction;
    }
    
    public static AbstractAction createDeleteAction(){
        if (deleteEmployeeAction == null)
           deleteEmployeeAction = new DeleteElementAction();
        return deleteEmployeeAction;
    }
    
}
