package org.foi.nwtis.anikolic.zadaca_1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Izvršna klasa za korisnika aerodroma
 * @author Ana
 */
public class KorisnikAerodroma {

    private String korisnik;
    private String lozinka;
    private String server;
    private Integer brojPorta;
    private String datotekaAerodroma;
    private String odabranaOpcija;
    private int spavajVal;
    private String parAerodroma;
    private String komanda;
    private Aerodrom aerodrom;

    /**
     * Metoda koja vraća broj porta
     * @return 
     */
    public Integer getBrojPorta() {
        return brojPorta;
    }

    /**
     * Metoda koja postavlja broj porta
     * @param brojPorta 
     */
    public void setBrojPorta(Integer brojPorta) {
        this.brojPorta = brojPorta;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Metoda koja vraća komandu zahtjeva korisnika
     * @return string komanda
     */
    public String getKomanda() {
        return komanda;
    }

    /**
     * Konstruktor klase
     * @param args the command line arguments
     */
    public KorisnikAerodroma() {
    }

    /**
     * Izvršava obradu parametara, generiranje i slanje zahtjeva korisnika aerodroma
     * serveru aerodrma, prima i obrađuje odgovor servera aerodroma
     * @param args parametri ulazne linije
     */
    public static void main(String[] args) {
        KorisnikAerodroma ka = new KorisnikAerodroma();
        if (ka.kontrolaParametara(args) == false) {
            System.out.println("Pogrešnni parametri!");
            return;
        }
        saljiKomanduPrimiOdgovor(ka);
    }

    /**
     * Komunikacija korisnika sa serverom, šalje komandu i prima odgovor te poziva funkciju za obradu odgovora
     * @param ka 
     */
    private static void saljiKomanduPrimiOdgovor(KorisnikAerodroma ka) {
        try {
            Socket socket = new Socket(ka.getServer(), ka.getBrojPorta());
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(ka.getKomanda().getBytes());
            outputStream.flush();
            socket.shutdownOutput();
            int znak;
            StringBuilder stringBuilder = new StringBuilder();
            while ((znak = inputStream.read()) != -1) {
                stringBuilder.append((char) znak);
            }
            ka.obradiOdgovor(stringBuilder.toString());
            socket.shutdownInput();
            socket.close();
        } catch (IOException ex) {
            System.out.println("Greška u odgovoru!");
        }
    }

    /**
     * Izvršava odgovarajuću akciju za odgovor primljen od servera aerodroma
     * (ispisuje potvrdu, grešku ili pohranjuje aerodroe u datoteku, ovisno o poslanoj opciji)
     * @param odgovor - odgovor na poslanu komandu kojeg je korisnik primio od servera
     */
    private void obradiOdgovor(String odgovor) {
        if (odgovor.startsWith("ERROR") || odgovor.equals("OK;")) {
            System.out.println(odgovor);
            return;
        }
        String[] linijeOdgovora = odgovor.split("\n");
        System.out.println(linijeOdgovora[0]);
        try (PrintWriter printWriter = new PrintWriter(datotekaAerodroma, "UTF-8")) {
            for (int i = 1; i < linijeOdgovora.length; i++) {
                printWriter.println(linijeOdgovora[i]);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(KorisnikAerodroma.class.getName()).log(Level.SEVERE, null, ex); //dat ne postoji
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(KorisnikAerodroma.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Metoda koja provjerava, pomoću regexa i .equals() metoda, ispravnost
     * unešenih argumenata pri pokretanju korisnika aerodroma. Parametri moraju
     * biti upisani navedenim redosljedom kako bi vrijedili. KorisnikAerodroma
     * -k korisnik -l lozinka -s [ipadresa | adresa] -p port [--kraj | --stanje
     * | --spavaj n | --aerodromi datoteka | --aerodrom
     * “icao;iata;naziv;grad;država;gš;gd“]
     *
     * @param args
     * @return false ako ijedan parametar nije ispravan
     */
    private boolean kontrolaParametara(String[] args) {
        if (args.length < 9) {
            return false;
        }
        if (!provjeriKorisnickePodatke(args)) {
            return false;
        }
        String regAdresa = "[^\\s]+";
        String regIpAdresa = "\\b(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.\n"
                + "  (25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.\n"
                + "  (25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.\n"
                + "  (25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\b";
        if (!"-s".equals(args[4]) || !(provjeriSintaksu(args[5], regAdresa) || provjeriSintaksu(args[5], regIpAdresa))) {
            return false;
        }
        setServer(args[5]);
        String regPort = "^(8|9)[0-9]{3}$";
        if (!"-p".equals(args[6]) || !provjeriSintaksu(args[7], regPort)) {
            return false;
        }
        setBrojPorta(Integer.parseInt(args[7]));
        String opcionalno = args.length == 10 ? args[9] : "";
        opcijeKomande(args[8], opcionalno);
        return true;
    }

    /**
     * Dio kontrole parametara za korisnicko ime i lozinku
     * @param args
     * @return 
     */
    private boolean provjeriKorisnickePodatke(String[] args) {
        String regKorisnik = "^[a-zA-Z0-9_-]+$";
        if (!"-k".equals(args[0]) || !provjeriSintaksu(args[1], regKorisnik)) {
            return false;
        }
        korisnik = args[1];
        String regLozinka = "^[a-zA-Z0-9!#_-]+$";
        if (!"-l".equals(args[2]) || !provjeriSintaksu(args[3], regLozinka)) {
            return false;
        }
        lozinka = args[3];
        return true;
    }
    
    /**
     * Metoda koja uspoređuje da li upisani parametar zadovoljava regularni izraz za taj parametar
     * @param parametri - string parametra
     * @param sintaksa - regulrni izraz kojeg parametar treba zadovoljiti
     * @return ispravnost parametra
     */
    public boolean provjeriSintaksu(String parametri, String sintaksa) {
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(parametri);
        return m.matches();
    }
    
    /**
     * Dio kontrole parametara za opciju parametara, 
     * stvara komandu koju šalje serveru aerodroma
     * @param opcija
     * @param opcionalanParametar
     * @return true ako je sintaksa opcije ispravna
     */
    private boolean opcijeKomande(String opcija, String opcionalanParametar) {
        komanda = "KORISNIK " + korisnik + "; LOZINKA " + lozinka + ";";
        switch (opcija) {
            case "--kraj":
                odabranaOpcija = "kraj";
                komanda += " KRAJ;";
                return true;
            case "--stanje":
                odabranaOpcija = "stanje";
                komanda += " STANJE;";
                return true;
            case "--spavaj":
                odabranaOpcija = "spavaj";
                return opcijaSpavaj(opcionalanParametar);
            case "--aerodromi":
                odabranaOpcija = "aerodromi";
                return opcijaAerodromi(opcionalanParametar);
            case "--aerodrom":
                odabranaOpcija = "aerodrom";
                return opcijaAerodrom(opcionalanParametar);
            default:
                return false;
        }
    }

    /**
     * Posljednja provjera parametara, za opciju --spavaj
     * @param n
     * @return 
     */
    private boolean opcijaSpavaj(String n) {
        Integer spavaj = Integer.parseInt(n);
        if (spavaj > 1 && spavaj < 300) {
            spavajVal = spavaj;
            komanda += " CEKAJ " + spavaj + ";";
            return true;
        }
        return false;
    }

    /**
     * Posljednja provjera parametara, za opciju --aerodromi
     * @param dat
     * @return 
     */
    private boolean opcijaAerodromi(String dat) {
        String sintaksa = "^([^\\s]+\\.(?i)txt|xml|bin|json)$";
        if (provjeriSintaksu(dat, sintaksa)) {
            datotekaAerodroma = dat;
            komanda += " AERODROMI;";
            return true;
        }
        return false;
    }

    /**
     * Posljednja provjera parametara, za opciju --aerodrom
     * @param aerodromPar
     * @return 
     */
    private boolean opcijaAerodrom(String aerodromPar) {
        String regICAO = "[A-Z]{4}";
        String regIATA = "[A-Z]{3}";
        String[] polje = aerodromPar.split(";");
        Float sirina = Float.parseFloat(polje[5]);
        Float duzina = Float.valueOf(polje[6]);
        if (provjeriSintaksu(polje[0], regICAO) && provjeriSintaksu(polje[1], regIATA)
                && provjeriSintaksu(polje[2], "[^;]+") && provjeriSintaksu(polje[3], "[^;]+")
                && provjeriSintaksu(polje[4], "[^;]+") && Math.abs(sirina) < 90
                && Math.abs(duzina) < 90) {
            aerodrom = new Aerodrom(polje[0], polje[1], polje[2], polje[3], polje[4], sirina, duzina);
            komanda += " ICAO " + polje[0] + "; IATA " + polje[1] + "; NAZIV " + polje[2]
                    + "; GRAD " + polje[3] + "; DRZAVA " + polje[4] + "; GS " + sirina
                    + "; GD " + duzina + ";";
            return true;
        }
        return false;
    }

}
