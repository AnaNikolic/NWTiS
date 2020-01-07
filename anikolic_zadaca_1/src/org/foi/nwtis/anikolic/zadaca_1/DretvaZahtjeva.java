package org.foi.nwtis.anikolic.zadaca_1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa je dretva koja provodi obradu zahtjeva
 * @author Ana Nikolić
 */
public class DretvaZahtjeva extends Thread{

    boolean kraj = false;
    private Socket socket;
    private ServisAerodroma servisAerodroma;
    private ServerAerodroma serverAerodroma;
    private List<Korisnik> korisnici;

    /**
     * Konstruktor klase
     * @param threadGroup grupa kojoj dretva pripada
     * @param name zaziv dretve
     * @param servis servis aerodroma, za rad sa podacima o aerodromima
     * @param kor lista korisnika iz datoteke postavki
     * @param server server aerodroma, služi za poziv kraja rada
     */
    DretvaZahtjeva(ThreadGroup threadGroup, String name, ServisAerodroma servis, 
            List<Korisnik> kor, ServerAerodroma server) {
        super(threadGroup, name);
        servisAerodroma = servis;
        korisnici = kor;
        serverAerodroma = server;
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt(); 
    }

    @Override
    public void run() {
        while(!kraj){
            System.out.println(this.getName() + " run");
            try {
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                int znak;
                StringBuilder stringBuilder = new StringBuilder();
                while (( znak = inputStream.read()) != -1){
                    stringBuilder.append((char) znak);
                }
                System.out.println(this.getName() + " primljeno: " + stringBuilder.toString());
                String odgovor = obradiKomanduKorisnika(stringBuilder.toString());
                outputStream.write(odgovor.getBytes());
                outputStream.flush();
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
                synchronized(this){
                    wait();
                }
                
            } catch (IOException ex) {
                Logger.getLogger(DretvaZahtjeva.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                interrupt();
            }
            
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Postavlja socket na koji šalje odgovor korisniku
     * @param socket 
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    
    
    /**
     * Provjerava podudarnost primljenih korisnickih podataka sa onima koji su zapisani u datoteci konfiguracije.
     * @param korisnickoIme
     * @param lozinka
     * @return true ako su podaci ispravni
     */
    public boolean provjeriPodatkeKorisnika(String[] parametriKomande){
        String[] korisnikPom = parametriKomande[0].split(" ");
        String korisnik = korisnikPom[1];
        String[] lozinkaPom = parametriKomande[1].split(" ");
        String lozinka = lozinkaPom[1];
        for (Korisnik k : korisnici){
            if(k.getKorisnickoIme().equals(korisnik)){
                if (k.getLozinka().equals(lozinka)){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Ovisno o primljenoj komandi poziva obradu zahtjeva korisnika
     * @param komanda koju je zadao korisnik aerodroma
     * @return odgovor za korisnika
     */
    public String obradiKomanduKorisnika(String komanda){ //bulin
        String[] parametriKomande = komanda.split("; ");
        if (!provjeriPodatkeKorisnika(parametriKomande)){
            return "ERROR 10; korisnik ili lozinka ne  odgovaraju.";
        }
        String[] pomOpcija = parametriKomande[2].split(" ");
        String opcija = pomOpcija[0].replace(";","");
        switch (opcija){
            case "KRAJ": 
                return krajIzvrsi();
            case "STANJE":
                return "OK;";
            case "AERODROMI":
                return aerodromiIzvrsi();
            case "CEKAJ":
                return cekajIzvrsi(pomOpcija[1].replace(";", ""));
            case "ICAO": 
                String icao = pomOpcija[1];
                String iata = parametriKomande[3].split(" ")[1];
                String naziv = parametriKomande[4].replace("NAZIV ", "");
                String grad = parametriKomande[5].replace("GRAD ", "");
                String drzava = parametriKomande[6].replace("DRZAVA ", "");
                String gs = parametriKomande[7].split(" ")[1];
                String gd = parametriKomande[8].split(" ")[1].replace(";", "");
                return aerodromIzvrsi(new Aerodrom(icao, iata, naziv, grad, drzava, 
                        Float.parseFloat(gs), Float.parseFloat(gd)));
        }
        return "ERROR greška u komandi";
    }
    
    /**
     * Za korisnikovu komasandu aerodrom, dodaje novi aerodrom u kolekciju aerodroma
     * @param aerodrom - podaci o novom aerodromu
     * @return poruka o rezultatu
     */
    public String aerodromIzvrsi(Aerodrom aerodrom){
        if (servisAerodroma.dodajAerodrom(aerodrom)){
            return "OK;";
        }
        return "ERROR 14; Greška! Aerodrom već postoji u kolekciji.";
    }
    
    /**
     * Izvrsava korisnikovu komandu opcije aerodromi, vraca popis aerodroma ili poruku 
     * greške, ovisno o izvršetku primljene komande
     * @return poruka o rezultatu
     */
    public String aerodromiIzvrsi(){
        if (servisAerodroma.deserijaliziraj().equals("OK;")){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("OK; DUZINA " + servisAerodroma.nadiVelicinuDatoteke() + "\n");
            List<Aerodrom> aerodromi = servisAerodroma.getAerodromi();
            for (Aerodrom a : aerodromi){
                stringBuilder.append(String.format("%s %s %s %s %s %f %f\n", a.getIcao(), a.getIata(), a.getNaziv(), 
                        a.getGrad(), a.getDrzava(), a.getGeoSirina(), a.getGeoDuzina()));
            }
            return stringBuilder.toString();
        }
        return "ERROR 12; greška u podacima aerodroma.";
    }
    
    /**
     * Izvršava komandu čekanja za zadani broj sekundi
     * @param cekaj - vrijeme spavanja
     * @return poruka o uspjehu
     */
    public String cekajIzvrsi(String cekaj){
        int n = Integer.parseInt(cekaj);
        try {
            Thread.sleep(n*1000);
        } catch (InterruptedException ex) {
            interrupt();
            return  "ERROR 13; dretva nije uspjela odraditi čekanje.";
        }
        return "OK;";
    }
    
    /**
     * Izvršava komandu kraj, poziva metodu koja prekida rad svih dretvi
     */
    public synchronized String krajIzvrsi(){
        String s = this.servisAerodroma.deserijaliziraj();
        if (!s.equals("OK;")){
            return "ERROR 11; nešto nije u redu s prekidom rada ili serijalizacijom.";
        }
        serverAerodroma.pogasiSve();
        kraj = true;
        return "OK;";
    }
}
