/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.selenium;

import io.github.sukgu.*;
import io.github.sukgu.elements.Element;
import java.util.List;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;



/**
 *
 * @author adrian
 */
public class NewMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        System.setProperty("webdriver.chrome.driver", "/data/downloads/chromedriver/chromedriver");
        WebDriver driver = new ChromeDriver();
//        System.setProperty("webdriver.gecko.driver", "/data/downloads/geckodriver/geckodriver");
//        WebDriver driver = new FirefoxDriver();
        driver.navigate().to("http://localhost:8080");        
        System.out.println("title: " + driver.getTitle());
        Shadow shadow = null; 
        shadow = new Shadow(driver);
//        try {
//            //if you want to use implicit wait
//            shadow.setImplicitWait(10); //will wait for 10 secs.
//            shadow.setExplicitWait(10,5); //will wait for maximum 10 secs and will check after every 5 secs.
//        } catch (Exception e) {
//                e.printStackTrace();
//        }
        WebElement txt  = shadow.findElement("vaadin-text-field#msg>div[class='vaadin-text-field-container']>div[part='input-field']>slot[name='input']>input[part='value']");
        txt.sendKeys(new String[]{"Hello, selenium"});
        
        WebElement cbx = shadow.findElement("vaadin-combo-box#cbxA>div[class='vaadin-text-field-container']>div[part='input-field']>slot[name='input']>input[part='value']");
        cbx.sendKeys(new String[]{"b"});

        WebElement txt2 = shadow.findElement("vaadin-text-field[tabindex='2']>div[class='vaadin-text-field-container']>div[part='input-field']>slot[name='input']>input[part='value']");
        txt2.sendKeys(new String[]{"Hello again"});
                        
        WebElement lyt = shadow.findElement("vaadin-vertical-layout");
        lyt.click();
        
        WebElement btn = shadow.findElement("vaadin-button#btnH");
        btn.click();
    }
}
