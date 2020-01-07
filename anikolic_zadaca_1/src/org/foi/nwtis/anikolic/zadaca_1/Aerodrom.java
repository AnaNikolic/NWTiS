package org.foi.nwtis.anikolic.zadaca_1;

import java.io.Serializable;

/**
 * @author Ana Nikolić
 * 
 * Klasa za podatke o aerodromu
 * 
 */
public class Aerodrom implements Serializable{
    
    private String icao;
    private String iata;
    private String naziv;
    private String grad;
    private String drzava;
    private float geoSirina;
    private float geoDuzina;

    /**
     * Konstruktor klase
     * @param icao - oznaka aerodroma, 4 znaka 
     * @param iata - oznaka aerodroma, 3 znaka 
     * @param naziv - naziv aerodroma
     * @param grad - grad aerodroma
     * @param drzava - država aerodroma
     * @param geoSirina -  geografska širina aerodroma
     * @param geoDuzina - geografska dužina aerodroma
     */
    public Aerodrom(String icao, String iata, String naziv, String grad, String drzava, float geoSirina, float geoDuzina) {
        this.icao = icao;
        this.iata = iata;
        this.naziv = naziv;
        this.grad = grad;
        this.drzava = drzava;
        this.geoSirina = geoSirina;
        this.geoDuzina = geoDuzina;
    }

    /**
     * Metoda koja vraća geografsku dužinu aerodroma
     * @return String geoDuzina
     */
    public float getGeoDuzina() {
        return geoDuzina;
    }

    /**
     * Metoda aerodroma koja postavlja geografsku dužinu aerodroma
     * @param geoDuzina 
     */
    public void setGeoDuzina(float geoDuzina) {
        this.geoDuzina = geoDuzina;
    }

    /**
     * Metoda aerodroma koja vraća ICAO oznaku aerodroma
     * @return String icao
     */
    public String getIcao() {
        return icao;
    }

    /**
     * Metoda aerodroma koja postavlja ICAO oznaku aerodroma
     * @param icao 
     */
    public void setIcao(String icao) {
        this.icao = icao;
    }

    /**
     * Metoda aerodroma koja vraća IATA oznaku aerodroma
     * @return String iata
     */
    public String getIata() {
        return iata;
    }

    /**
     * Metoda aerodroma koja postavlja IATA oznaku aerodroma
     * @param iata 
     */
    public void setIata(String iata) {
        this.iata = iata;
    }

    /**
     * Metoda aerodroma koja vraća naziv aerodroma
     * @return String naziv
     */
    public String getNaziv() {
        return naziv;
    }

    /**
     * Metoda aerodroma koja postavlja naziv aerodroma
     * @param naziv 
     */
    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    /**
     * Metoda aerodroma koja vraća grad u kojem je aerodrom
     * @return 
     */
    public String getGrad() {
        return grad;
    }

    /**
     * Metoda aerodroma koja postavlja grad u kojem je aerodrom
     * @param grad 
     */
    public void setGrad(String grad) {
        this.grad = grad;
    }

    /**
     * Metoda aerodroma koja vraća državu u kojoj je aerodrom
     * @return String drzava
     */
    public String getDrzava() {
        return drzava;
    }

    /**
     * Metoda aerodroma koja postavlja državu u kojoj je aerodrom
     * @param drzava 
     */
    public void setDrzava(String drzava) {
        this.drzava = drzava;
    }

    /**
     * Metoda aerodroma koja vraća geografsku širinu aerodroma
     * @return String geoSirina
     */
    public float getGeoSirina() {
        return geoSirina;
    }

    /**
     * Metoda aerodroma koja postavlja geografsku širinu aerodroma
     * @param geoSirina 
     */
    public void setGeoSirina(float geoSirina) {
        this.geoSirina = geoSirina;
    }
    
}
