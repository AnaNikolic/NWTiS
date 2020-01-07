package org.foi.nwtis.anikolic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;

/**
 * Klasa za prijevod
 * @author Ana
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {

    /**
     * jezik (locale), postavljen hrvatski
     */
    private String jezik = "hr";

    /**
     * Konstruktor klase
     */
    public Lokalizacija() {
    }

    /**
     * Vraća trenutni jezik
     * @return jezik
     */
    public String getJezik() {
        return jezik;
    }

    /**
     * Postavlja jezik
     * @param jezik aplikacije
     */
    public void setJezik(String jezik) {
        this.jezik = jezik;
    }

    /**
     * Mijenja jezik
     * @return "" - ponovno učitava stranicu
     */
    public Object odaberiJezik() {
        return "";
    }
    
}
