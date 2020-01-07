package org.foi.nwtis.anikolic.zadaca_1;

/**
 *
 * @author Ana Nikolić
 * 
 * Klasa za podatke o korisniku
 */
public class Korisnik {
    
    private String korisnickoIme;
    private String lozinka;

    /**
     * Konstruktor klase
     * @param korisnickoIme - oznaka korisnika
     * @param lozinka - lozinka za korisnika
     */
    public Korisnik(String korisnickoIme, String lozinka) {
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
    }

    /**
     * Metoda korisnika koja vraća lozinku
     * @return String lozinka
     */
    public String getLozinka() {
        return lozinka;
    }

    /**
     * Metoda korisnika koja postavlja lozinku
     * @param lozinka 
     */
    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    /**
     * Metoda korisnika koja vraća korisničko ime za korisnika
     * @return String korisnickoIme
     */
    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    /**
     * Metoda korisnika koja postavlja korisničko ime za korisnika
     * @param korisnickoIme 
     */
    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }
    
    
}
