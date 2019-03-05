package com.neotropic;

import com.neotropic.databasemodule.service.UserService;
import com.neotropic.webapplicationmodule.presentation.ListLayouts.HolaMundoComponente;
import com.neotropic.webapplicationmodule.presentation.ListLayouts.UserListLayout;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class BasicApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BasicApplication.class, args);
    }

    /**
     * Ruta por defecto, vista principal
     */
    @PageTitle("Gitge")
    @Route("")
    public static class MiView extends VerticalLayout {

        public MiView() {
            add(new Button("Presionar", new ComponentEventListener<ClickEvent<Button>>() {
                @Override
                public void onComponentEvent(ClickEvent<Button> event) {
                    Notification.show("Presioando el botón....");
                }
            }));

            //creando rutas.
            crearRutas();
        }

        /**
         * Creando la creación de las rutas.
         */
        private void crearRutas() {
            VerticalLayout caja = new VerticalLayout();
            caja.add(new H2("Enlaces a funcionalidades:"));
            //con RouterLink el renderizado no recarga la pagina.
            caja.add(new RouterLink("Users", UserListLayout.class));

            add(caja);
        }
    }

    /**
     * this method inject userService to be used
     */
    @Component
    static class BootStrap {

        @Autowired
        UserService userService;

        @PostConstruct
        public void init() {
            System.out.println("Usuarios guardados:");
            //datos de pruebas
            //for(int i = 0; i<=10000;i++){
            //    userService.crearEstudiante(new Estudiante(i, "Estudiante "+i));
            //}
            System.out.println("users: " + userService.getAllUsers().size());
        }
    }
}
