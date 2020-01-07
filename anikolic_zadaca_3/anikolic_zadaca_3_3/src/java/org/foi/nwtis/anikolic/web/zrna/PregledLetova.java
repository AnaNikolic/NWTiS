package org.foi.nwtis.anikolic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import org.foi.nwtis.anikolic.ejb.sb.UpravljanjePutnicima;
import org.foi.nwtis.anikolic.podaci.PodaciLeta;
import org.foi.nwtis.anikolic.podaci.PodaciPutnika;

/**
 * Pregled i brisanje letova za odabranog korisnika
 * @author Ana
 */
@Named(value = "pregledLetova")
@SessionScoped
public class PregledLetova implements Serializable {

    @EJB
    private UpravljanjePutnicima upravljanjePutnicima;

    private String odVremena;
    private String doVremena;
    /**
     * lista svih putnika
     */
    private List<PodaciPutnika> listaPutnika;
    private Integer odabraniPutnik;
    /**
     * svi letovi za putnika
     */
    private List<PodaciLeta> listaLetova;
    /**
     * dio liste letova koji je prikazan
     */
    private List<PodaciLeta> letoviPrikaz;
    private Integer odabraniLet;

    
    /**
     * varijable za prikaz tblice letova
     */
    private int tablicaPutnika = 7;
    private int stranicenje = 10;
    private int ukupno;
    private int prikazLetovaPocetak = 0;
    private int prikazLetovaKraj = 0;
    /**
     * prikazuje poruku za korisnika
     */
    private String poruka = "";

    public int getTablicaPutnika() {
        return tablicaPutnika;
    }

    public List<PodaciLeta> getLetoviPrikaz() {
        return letoviPrikaz;
    }

    /**
     * Creates a new instance of PregledLetova
     */
    public PregledLetova() {
    }

    @PostConstruct
    public void init() {
        listaPutnika = upravljanjePutnicima.dajeSvePutnike();
    }

    public Integer getOdabraniPutnik() {
        return odabraniPutnik;
    }

    public void setOdabraniPutnik(Integer odabraniPutnik) {
        this.odabraniPutnik = odabraniPutnik;
    }

    public Integer getOdabraniLet() {
        return odabraniLet;
    }

    public void setOdabraniLet(Integer odabraniLet) {
        this.odabraniLet = odabraniLet;
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

    public List<PodaciPutnika> getListaPutnika() {
        return listaPutnika;
    }
    /**
     * ukupno letova za korisnika
     * @return 
     */
    public int getUkupno() {
        return ukupno;
    }

    /**
     * Dohvaća podatke u listu letova za odabranog putnika i vrijeme
     */
    public void preuzmiLetove() {
        poruka = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        try {
            Integer odVremenaInt = (int) (sdf.parse(odVremena).getTime() / 1000);
            Integer doVremenaInt = (int) (sdf.parse(doVremena).getTime() / 1000);
            listaLetova = upravljanjePutnicima.preuzmiLetovePutnika(odabraniPutnik, odVremenaInt, doVremenaInt);
        } catch (ParseException ex) {
            poruka = "Greška formata vremena!";
        }
    }
    
    /**
     * Učitava podatke u tablicu letova za odabranog putnika i vrijeme,
     * uz početno postavljanje straničenja
     */
    public String ucitajLetove() {
        poruka = "";
        preuzmiLetove();
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
        poruka = "";
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
        poruka = "";
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

    /**
     * Poziva brisanje podataka o letu putnika iz tablice letova
     * @param id za odabrani let
     * @return 
     */
    public String brisiLet(int id) {
        if (odabraniPutnik == 0) {
            poruka = "Nije odabran putnik!";
        }
        if (upravljanjePutnicima.brisiLet(odabraniPutnik, id)) {
            poruka = "Let uspješno obrisan!";
            InformatorPutnika.saljiPoruku(String.valueOf(odabraniPutnik));
        } else {
            poruka = "Pogreška kod brisanja leta!";
        }
        return "";
    }

    /**
     * prikaz poruke za korisnika
     * @return 
     */
    public String getPoruka() {
        return poruka;
    }

}
