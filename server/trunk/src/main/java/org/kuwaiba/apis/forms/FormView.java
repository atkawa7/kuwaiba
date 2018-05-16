/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.apis.forms;

/**
 *
 * @author johnyortega
 * @param <T> The local representation of the form
 */
public interface FormView<T> {
    void display(T form);
}
