/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.demo.main;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.shared.Registration;
import java.util.List;

/**
 * abstract class that provides preconfigurations for responsive application
 * management
 *
 * @author Orlando Paz
 */
public abstract class AbstractPage extends VerticalLayout {

    protected Registration resizeListener;
    private final int MOBILE_BREAKPOINT = 600;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        if (ui != null) {

            Page page = ui.getPage();

            //get information from the client
            page.retrieveExtendedClientDetails(receiver -> {
                if (receiver.isTouchDevice()) {
                    // touch screen
                } else {
                    // keyboard based device
                }

                //device screen size
                int width = receiver.getScreenWidth();
                int height = receiver.getScreenHeight();

                receiver.getTimeZoneId();
                receiver.getCurrentDate();
                //...
            });
            
            // add listener to screen size changes
            resizeListener = page.addBrowserWindowResizeListener(event -> updateResposiveComponents(event.getWidth(), event.getWidth() < MOBILE_BREAKPOINT));
            page.retrieveExtendedClientDetails(details -> updateResposiveComponents(details.getBodyClientWidth(), details.getBodyClientWidth() < MOBILE_BREAKPOINT));

            // retrieve more client information
            WebBrowser browser = ui.getSession().getBrowser();

            // identify browser
            browser.isChrome();
            browser.isSafari();
            browser.isOpera();
            browser.isFirefox();

            // identify mobile OS
            browser.isAndroid();
            browser.isIOS();
            browser.isWindowsPhone();

            // identify mobile device 
            browser.isIPhone();
            browser.isIPad();

            //desktop OS
            browser.isLinux();
            browser.isWindows();
            browser.isMacOSX();

        }

    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if (resizeListener != null) {
            resizeListener.remove();
        }
        super.onDetach(detachEvent);
    }

    /**
     * proc to update the responsive components when the screen size changes
     *
     * @param widthScreen the new width screen value
     * @param mobile true if is mobile screen
     */
    public abstract void updateResposiveComponents(int widthScreen, boolean mobile);

    /**
     * updates the visibility of the given grid, based on if is mobile or
     * desktop
     * @param grid the grid to update
     * @param pos the mobile col position
     * @param mobile true if is mobile screen
     */
    public void updateVisibleGridColumns(Grid grid, int pos, boolean mobile) {

        List<Grid.Column<? extends Object>> columns = grid.getColumns();

        // "Mobile" column
        columns.get(pos).setVisible(mobile);

        // "Desktop" columns
        for (int i = pos + 1; i < columns.size(); i++) {
            columns.get(i).setVisible(!mobile);
        }
    }
}
