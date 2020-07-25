/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stringselenium.demo;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adrian
 */
@Route("")
@PageTitle("vaadin + selenium")
public class View extends VerticalLayout{

    public View() {
        List<String> data = new ArrayList<>();
        data.add("aaa");
        data.add("baa");
        data.add("aac");
        
        TextField txtMsg = new TextField();
        TextField txtSec = new TextField();
        ComboBox cbxA = new ComboBox("A", data);
        ComboBox cbxB = new ComboBox("B", data);
        
        cbxA.setId("cbxA");
        cbxA.setTabIndex(3);
        cbxB.setId("cbxB");
        cbxB.setTabIndex(4);
        
        txtMsg.setId("msg");
        txtSec.setTabIndex(2);
        Button btnHi = new Button(new Icon(VaadinIcon.HAND), e->{
            if(txtMsg.getValue() != null)
                Notification.show("¡click! " + txtMsg.getValue());
        });
        btnHi.setId("btnH");
        add(new HorizontalLayout(txtMsg, txtSec),
            new HorizontalLayout(cbxA, cbxB),
            btnHi);
    }
}
