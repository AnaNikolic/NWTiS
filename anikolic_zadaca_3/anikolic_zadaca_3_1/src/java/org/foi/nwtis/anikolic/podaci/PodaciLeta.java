/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.podaci;

import org.foi.nwtis.rest.podaci.AvionLeti;

/**
 *
 * @author Dragutin Kermek
 */
public class PodaciLeta extends AvionLeti {
    private int id;
    private String nazivPolazisnogAerodroma;
    private String nazivOdredisnogAerodroma;
    private String vrijemePoletanja;
    private String vrijemeSletanja;

    public String getNazivPolazisnogAerodroma() {
        return nazivPolazisnogAerodroma;
    }

    public void setNazivPolazisnogAerodroma(String nazivPolazisnogAerodroma) {
        this.nazivPolazisnogAerodroma = nazivPolazisnogAerodroma;
    }

    public String getNazivOdredisnogAerodroma() {
        return nazivOdredisnogAerodroma;
    }

    public void setNazivOdredisnogAerodroma(String nazivOdredisnogAerodroma) {
        this.nazivOdredisnogAerodroma = nazivOdredisnogAerodroma;
    }

    public String getVrijemePoletanja() {
        return vrijemePoletanja;
    }

    public void setVrijemePoletanja(String vrijemePoletanja) {
        this.vrijemePoletanja = vrijemePoletanja;
    }

    public String getVrijemeSletanja() {
        return vrijemeSletanja;
    }

    public void setVrijemeSletanja(String vrijemeSletanja) {
        this.vrijemeSletanja = vrijemeSletanja;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
}
