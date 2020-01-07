package org.foi.nwtis.anikolic.web.podaci;

import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa podataka o aerodromu
 * @author Ana
 */
public class Aerodrom {
    /**
     * ICAO kod za identifikaciju aerodroma
     */
    private String icao;
    /**
     * Naziv aerodroma
     */
    private String naziv;
    /**
     * Država aerodroma
     */
    private String drzava;
    /**
     * Geografska lokacija aerodroma
     */
    private Lokacija lokacija;

    /**
     * Kreira objekt za dane parametre
     * @param icao kod aerodroma
     * @param naziv naziv aerodroma
     * @param drzava država aerodroma
     * @param lokacija GPS lokacija
    */
    public Aerodrom(String icao, String naziv, String drzava, Lokacija lokacija) {
        this.icao = icao;
        this.naziv = naziv;
        this.drzava = drzava;
        this.lokacija = lokacija;
    }

    /**
     * prazan konstruktor, za kada podaci nisu poznati pri kreiranju
     */
    public Aerodrom() {
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getDrzava() {
        return drzava;
    }

    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    public Lokacija getLokacija() {
        return lokacija;
    }

    public void setLokacija(Lokacija lokacija) {
        this.lokacija = lokacija;
    }
    
}