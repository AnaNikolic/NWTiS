package org.foi.nwtis.anikolic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.foi.nwtis.anikolic.podaci.Aerodrom;
import org.foi.nwtis.anikolic.ejb.sb.UpravljanjePutnicima;
import org.foi.nwtis.anikolic.podaci.PodaciLeta;
import org.foi.nwtis.anikolic.podaci.PodaciPutnika;

/**
 * Dodavanje i učitavanje letova za korisnike
 * @author Ana
 */
@Named(value = "dodavanjeLetova")
@SessionScoped
public class DodavanjeLetova implements Serializable {

    @EJB
    private UpravljanjePutnicima upravljanjePutnicima1;

    @EJB
    private UpravljanjePutnicima upravljanjePutnicima;

    private String odVremena;
    private String doVremena;
    private List<PodaciPutnika> listaPutnika;
    private int odabraniPutnik;
    private List<Aerodrom> listaAerodroma;
    private String odabraniAerodrom = "";
    private List<PodaciLeta> listaLetova;
    private List<PodaciLeta> letoviPrikaz;
    private int odabraniLet;
    
    /**
     * da li se podaci o avionima preuzimaju sa open sky networka
     */
    private boolean preuzetiPodatke;
    
    /**
     * varijable za tablicu letova
     */
    private int tablicaOpcija = 7;
    private int stranicenje = 10;
    private int prikazLetovaPocetak = 0;
    private int prikazLetovaKraj;
    int ukupno = 1; 
    /**
     * poruka za korisnika
     */
    private String poruka = "";

    public int getTablicaOpcija() {
        return tablicaOpcija;
    }

    public List<PodaciLeta> getPrikazLetova() {
        return letoviPrikaz;
    }

    public int getUkupno() {
        return ukupno;
    }
    /**
     * Creates a new instance of DodavanjeLetova
     */
    public DodavanjeLetova() {
        listaAerodroma = new ArrayList<>();
        listaLetova = new ArrayList<>();
        listaPutnika = new ArrayList<>();
        letoviPrikaz = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        listaAerodroma = upravljanjePutnicima.dajeSveAerodrome();
        listaPutnika = upravljanjePutnicima.dajeSvePutnike();
    }

    public String getOdVremena() {
        return odVremena;
    }

    public void setOdVremena(String odVremena) {
        this.odVremena = odVremena;
    }

    public String getDoVremena() {
        return doVremena;
    }

    public void setDoVremena(String doVremena) {
        this.doVremena = doVremena;
    }

    public int getOdabraniPutnik() {
        return odabraniPutnik;
    }

    public void setOdabraniPutnik(int odabraniPutnik) {
        this.odabraniPutnik = odabraniPutnik;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public int getOdabraniLet() {
        return odabraniLet;
    }

    public void setOdabraniLet(int odabraniLet) {
        this.odabraniLet = odabraniLet;
    }

    public boolean isPreuzetiPodatke() {
        return preuzetiPodatke;
    }

    public void setPreuzetiPodatke(boolean preuzetiPodatke) {
        this.preuzetiPodatke = preuzetiPodatke;
    }

    /**
     * dohvaća listu svih putnika
     * @return 
     */
    public List<PodaciPutnika> getListaPutnika() {
        listaPutnika = upravljanjePutnicima.dajeSvePutnike();
        return listaPutnika;
    }

    /**
     * dohvaća listu aerodroma
     * @return 
     */
    public List<Aerodrom> getListaAerodroma() {
        listaAerodroma = upravljanjePutnicima.dajeSveAerodrome();
        return listaAerodroma;
    }

    public List<PodaciLeta> getListaLetova() {
        return listaLetova;
    }
    
    /**
     * dohvaća poruku korisniku
     * @return String
     */
    public String getPoruka() {
        return poruka;
    }

    /**
     * Obzirom na dane parametre: odabrani aerodrom i vremenski interval,
     * dohvaća listu letova
     */
    public void preuzmiLetove() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        if (preuzetiPodatke) {
            try {
                Integer odVremenaInt = (int) (sdf.parse(odVremena).getTime() / 1000);
                Integer doVremenaInt = (int) (sdf.parse(doVremena).getTime() / 1000);
                listaLetova = upravljanjePutnicima.preuzmiLetoveOpenSky(odabraniAerodrom, odVremenaInt, doVremenaInt);
            } catch (ParseException ex) {
                System.out.println("Greška formata vremena!");
            }
        } else {
            try {
                Integer odVremenaInt = (int) (sdf.parse(odVremena).getTime() / 1000);
                Integer doVremenaInt = (int) (sdf.parse(doVremena).getTime() / 1000);
                listaLetova = upravljanjePutnicima.preuzmiLetoveBazePodataka(odabraniAerodrom, odVremenaInt, doVremenaInt);
            } catch (ParseException ex) {
                poruka = "Greška formata vremena!";
            }
        }
    }

    /**
     * Ako je odabran putnik, dodaje let putnika za dani let aviona
     * @param id let aviona
     * @return ponovno učitava
     */
    public String dodajLet(int id) {
        if (odabraniPutnik == 0) {
            poruka = "Nije odabran putnik!";
        }
        if (upravljanjePutnicima.dodajLet(odabraniPutnik, id)) {
            poruka = "Let uspješno dodan!";
            InformatorPutnika.saljiPoruku(String.valueOf(odabraniPutnik));
        } else {
            poruka = "Pogreška kod dodavanja leta!";
        }
        return "";
    }
    
    /**
     * Učitava podatke u tablicu letova za odabranog putnika i vrijeme,
     * uz početno postavljanje straničenja
     */
    public String ucitajLetove() {
        preuzmiLetove();
        if (odabraniAerodrom.equals("")){
            poruka = "Nije odabran aerodrom!";
            return "";
            
        }
        ukupno = listaLetova.size();
        prikazLetovaPocetak = 0;
        if (listaLetova.isEmpty()) {
            prikazLetovaKraj = 0;
        } else if (listaLetova.size() < 10) {
            prikazLetovaKraj = listaLetova.size();
        } else {
            prikazLetovaKraj = stranicenje;
        }
        letoviPrikaz = listaLetova.subList(prikazLetovaPocetak, prikazLetovaKraj);
        return "";
    }

    /**
     * Učitava prethodnu stranicu podataka o letovima
     * @return
     */
    public String prethodna() {
        prikazLetovaKraj = prikazLetovaPocetak;
        if ((prikazLetovaKraj + stranicenje) > listaLetova.size()) {
            prikazLetovaKraj = listaLetova.size();
        }
        if (prikazLetovaPocetak - stranicenje >= 0) {
            prikazLetovaPocetak -= stranicenje;
        } else {
            prikazLetovaPocetak = 0;
        }
        letoviPrikaz = listaLetova.subList(prikazLetovaPocetak, prikazLetovaKraj);
        return "";
    }

    /**
     * Učitava sljedeću stranicu letova
     * @return
     */
    public String sljedeca() {
        if (prikazLetovaKraj < listaLetova.size()) {
            prikazLetovaPocetak = prikazLetovaKraj;
        }
        if ((prikazLetovaKraj + stranicenje) >= listaLetova.size()) {
            prikazLetovaKraj = listaLetova.size();
        } else {
            prikazLetovaKraj += stranicenje;
        }
        letoviPrikaz = listaLetova.subList(prikazLetovaPocetak, prikazLetovaKraj);
        return "";
    }

}
