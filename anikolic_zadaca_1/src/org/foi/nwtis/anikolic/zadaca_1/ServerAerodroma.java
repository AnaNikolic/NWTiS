package org.foi.nwtis.anikolic.zadaca_1;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.foi.nwtis.anikolic.konfiguracije.Konfiguracija;
import org.foi.nwtis.anikolic.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.anikolic.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.anikolic.konfiguracije.NemaKonfiguracije;

/**
 * Izvršna klasa koja definira server aerodroma, prvo se pokreće ona pa tek onda korisnik aerodroma.
 * Prema datoteci konfiguracije definira svoje podatke.
 * Server stvara dretve potrebne za rad tj. obradu zahtjeva korisnika.
 * 
 * @author Ana Nikolić
 */
public class ServerAerodroma {

    private static boolean kraj;

    List<DretvaZahtjeva> radneDretve = null;
    ThreadGroup threadGroupServisneDretve = null;
    ThreadGroup threadGroupKorisnickeDretve = null;

    List<Korisnik> korisnici = null;
  //  List<Aerodrom> aerodromi = null;
    private static Konfiguracija konf;
    ServerSocket serverSocket;
    ServisAerodroma servisAerodroma;

    public ServerAerodroma() {
        korisnici = Collections.synchronizedList(new ArrayList<>());
        radneDretve = Collections.synchronizedList(new ArrayList<>());
        serverSocket = null;
    }

    /**
     * main metoda provjerava ispravnost unešenih argumenata
     * Zatim stvara objekt ServerAerodroma, te tako započinje njegov rad
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        ServerAerodroma sa = new ServerAerodroma();
        String parametri = sa.pripremiParametre(args);
        if (!sa.provjeriParametre(parametri)) {
            System.out.println(parametri);
            System.out.println("Parametri ne odgovaraju!");
            return;
        }
        String nazivDatoteke = parametri;
        try {
            konf = sa.ucitajKonfiguraciju(nazivDatoteke);
        } catch (Exception ex) {
            System.out.println("Zadana datoteka konfiguracije ne postoji!");
            return;
        }
        
        if (!sa.provjeriPort()) {
            return;
        }
        
        sa.ucitavanjePodatakaKorisnika();
        
        sa.pokreniDretve();
        String deserijalizacija = sa.servisAerodroma.deserijaliziraj();
        if (!deserijalizacija.equals("OK;")) {
            System.out.println(deserijalizacija);
        }
        sa.preuzimanjeZahtjevaKorisnika();
        
    }

    /**
     * Kreira grupe dretvi i dretve, ovisno o postavkma, priprema i pokreće dretve
     */
    public void pokreniDretve() {
        this.threadGroupKorisnickeDretve = new ThreadGroup("anikolic_KD");
        this.threadGroupServisneDretve = new ThreadGroup("anikolic_SD");
        
        servisAerodroma = new ServisAerodroma(threadGroupServisneDretve,
                threadGroupServisneDretve.getName() + "_1", konf);
        servisAerodroma.start();
        int brojDretvi = Integer.parseInt(konf.dajPostavku("maks.dretvi"));
        for (int i = 0; i <= brojDretvi; i++) {
            DretvaZahtjeva dretvaZahtjeva = new DretvaZahtjeva(this.threadGroupKorisnickeDretve,
                    this.threadGroupKorisnickeDretve.getName() + "-" + i, servisAerodroma, korisnici, this);
            radneDretve.add(dretvaZahtjeva);
        }
        
        ServisDretvi servisDretvi = new ServisDretvi(threadGroupServisneDretve,
                threadGroupServisneDretve.getName() + "_2", konf);
        servisDretvi.pripremiServisDretvi(threadGroupServisneDretve, threadGroupKorisnickeDretve);
        servisDretvi.start();
    }

    /**
     * Čeka komandu korisnika, poziva obradu zahtjeva i šalje odgovor korisniku
     */
    public void preuzimanjeZahtjevaKorisnika() {
        try {
            while (!kraj) {
                Socket socket = serverSocket.accept(); //ceka kor
                DretvaZahtjeva dretvaZahtjeva = pronadiSlobodnuDretvu();
                if (dretvaZahtjeva == null) {
                    odgovoriNemaDretve(socket);
                    continue;
                }
                dretvaZahtjeva.setSocket(socket);
                synchronized (dretvaZahtjeva) {
                    if (dretvaZahtjeva.getState() == Thread.State.WAITING) {
                        dretvaZahtjeva.notify();
                    } else {
                        dretvaZahtjeva.start();
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ex.getMessage());
            return;
        }
    }
    
    
    /**
     * Šalje odgovor korisniku da su sve dretve u trenutku primanja komande zauzete.
     * @param socket - socket na koji šalje odgovor
     */
    public void odgovoriNemaDretve(Socket socket){
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("ERROR 01: Nema raspoložive dretve!".getBytes());
            outputStream.flush();
            socket.shutdownOutput();
            outputStream.close();
        } catch (Exception e){
            //error
        }
    }
    
    /**
     * Puni listu korisnika podacima o korisnicima iz konfiguracijske datoteke.
     */
    public void ucitavanjePodatakaKorisnika(){
        korisnici = new ArrayList<Korisnik>();
        Properties postavke = konf.dajSvePostavke();
        for (Object postavka : postavke.keySet()){
            if (postavka.toString().startsWith("korisnik.")){
                String korisnickoIme = postavka.toString().split("\\.")[1];
                Korisnik korisnik = new Korisnik(korisnickoIme, konf.dajPostavku(postavka.toString()));
                korisnici.add(korisnik);
            }
        }
    }

    /**
     * Provjerava da li je port iz postavki slobodan te kreira server socket
     * @return 
     */
    public boolean provjeriPort() {
        int brojPorta = Integer.parseInt(konf.dajPostavku("port"));
        if (brojPorta < 8000 || brojPorta > 9999) return false;
        int brojCekaca = Integer.parseInt(konf.dajPostavku("maks.cekaca"));
        
        try {
            serverSocket = new ServerSocket(brojPorta, brojCekaca);
            return true;
        } catch (Exception e) {
            
            System.out.println("Port " + brojPorta + " je zauzet!");
            return false;
        }
    }

    
    /**
     * oblikuje naziv datoteke parametara iz unešenih argumenata
     * @param args
     * @return oblikovani parametri
     */
    public String pripremiParametre(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        return p;
    }

    /**
     *Provjerava ispravnost unešenog naziva datoteke konfiguracije
     * @param parametri naziv datoteke
     * @return odgovara formatu
     */
    public boolean provjeriParametre(String parametri) {
        String sintaksa = "^([^\\s]+)\\.(txt|xml|json|bin)$";
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(parametri);
        return m.matches();
    }

    /**
     *Učitava podatke konfiguracije 
     * @param nazivDatoteke datoteka postavki
     * @return konfiguracija
     */
    public Konfiguracija ucitajKonfiguraciju(String nazivDatoteke) throws Exception {
        try {
            Konfiguracija konfiguracija = KonfiguracijaApstraktna.preuzmiKonfiguraciju(nazivDatoteke);
            return konfiguracija;
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            throw new Exception(ex.getMessage());
        }
    }

    /**
     * Pronalazi slobodnu radnu dretvu
     * @return dretva - ako je slobodna
     * @return null - ako nijedna dretva nije slobodna
     */
    public DretvaZahtjeva pronadiSlobodnuDretvu() {
        for (DretvaZahtjeva dretva : radneDretve) {
            if (dretva.getState() == Thread.State.WAITING || !dretva.isAlive()) {
                return dretva;
            }
        }
        return null;
    }
    
    /**
     * Završava rad svih dretvi
     */
    public void pogasiSve(){
        kraj = true;
        threadGroupServisneDretve.interrupt();
        threadGroupKorisnickeDretve.interrupt();
        if (serverSocket != null){
            try {
                serverSocket.close();
            } catch (IOException ex) {
                System.out.println("greška u gašenju");
                System.exit(0);
            }
        }
    }
}
