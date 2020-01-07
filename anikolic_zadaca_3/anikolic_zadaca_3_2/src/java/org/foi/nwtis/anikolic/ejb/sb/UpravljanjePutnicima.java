package org.foi.nwtis.anikolic.ejb.sb;

import org.foi.nwtis.anikolic.podaci.Aerodrom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.LocalBean;
import javax.faces.context.FacesContext;
import org.foi.nwtis.anikolic.ejb.eb.Airplanes;
import org.foi.nwtis.anikolic.ejb.eb.Flights;
import org.foi.nwtis.anikolic.ejb.eb.Myairports;
import org.foi.nwtis.anikolic.ejb.eb.Passangers;
import org.foi.nwtis.anikolic.podaci.PodaciLeta;
import org.foi.nwtis.anikolic.podaci.PodaciPutnika;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa za upravljanje podataka o putnicima: kreira, briše zapise o letovima
 * putnika, dohvaća podatke o putnicima i aerodromima
 *
 * @author Ana
 */
@Stateless
@LocalBean
public class UpravljanjePutnicima {

    @EJB
    private PassangersFacade passangersFacade;
    @EJB
    private MyairportsFacade myairportsFacade;
    @EJB
    private AirplanesFacade airplanesFacade;
    @EJB
    private FlightsFacade flightsFacade;

    String username;
    String password;

    /**
     * Metoda učitava parametre za open sky
     */
    @PostConstruct
    public void init() {
        username = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_korisnik");
        password = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("os_lozinka");
    }

    /**
     * Dohvaća sve putnike iz baze podataka
     *
     * @return lista podataka o putnicima
     */
    public List<PodaciPutnika> dajeSvePutnike() {
        List<Passangers> putnici = passangersFacade.findAll();
        List<PodaciPutnika> listaPutnika = new ArrayList<>();
        for (Passangers p : putnici) {
            PodaciPutnika pp = new PodaciPutnika();
            pp.setId(p.getId());
            pp.setFirstname(p.getFirstname());
            pp.setLastname(p.getLastname());
            pp.setUsername(p.getUsername());
            listaPutnika.add(pp);
        }
        return listaPutnika;
    }

    /**
     * Metoda dohvaća listu aerodroma iz MyAirports
     *
     * @return lista aerodroma
     */
    public List<Aerodrom> dajeSveAerodrome() {
        List<Myairports> aerodromi = myairportsFacade.findAll();
        List<Aerodrom> listaAerodroma = new ArrayList<>();
        for (Myairports a : aerodromi) {
            Aerodrom aerodrom = new Aerodrom();
            aerodrom.setIcao(a.getIdent());
            aerodrom.setDrzava(a.getIsoCountry());
            aerodrom.setNaziv(a.getName());

            String strKoordinate = a.getCoordinates();
            String[] koordinate = strKoordinate.split(",");
            Lokacija lokacija = new Lokacija();
            lokacija.setLatitude(koordinate[0]);
            lokacija.setLongitude(koordinate[1]);
            aerodrom.setLokacija(lokacija);

            listaAerodroma.add(aerodrom);
        }
        return listaAerodroma;
    }

    /**
     * Poziva preuzimanje podataka o letovima iz baze podataka za zadani
     * aerodrom u nekom intervalu
     *
     * @param icao zadani aerodrom
     * @param odVremena
     * @param doVremena
     * @return lista letova
     */
    public List<PodaciLeta> preuzmiLetoveBazePodataka(String icao, int odVremena, int doVremena) {
        return airplanesFacade.preuzmiLetove(icao, odVremena, doVremena);
    }

    /**
     * Preuzima podatke o letovima za zadani aerodrom polijetanja u nekom
     * intervalu, koristeći i OpenSky
     *
     * @param icao zadani aerodrom
     * @param odVremena
     * @param doVremena
     * @return listu podataka o letovima
     */
    public List<PodaciLeta> preuzmiLetoveOpenSky(String icao, int odVremena, int doVremena) {
        OSKlijent oSKlijent = new OSKlijent(username, password);
        List<AvionLeti> avioniPolazak = oSKlijent.getDepartures(icao, odVremena, doVremena);
        for (AvionLeti al : avioniPolazak) {
            Airplanes letAviona = new Airplanes();
            letAviona.setIcao24(al.getIcao24());
            letAviona.setCallsign(al.getCallsign());
            letAviona.setEstdepartureairport(al.getEstDepartureAirport());
            letAviona.setEstarrivalairport(al.getEstArrivalAirport());
            letAviona.setFirstseen(al.getFirstSeen());
            letAviona.setLastseen(al.getLastSeen());
            letAviona.setFlightsList(new ArrayList<Flights>());
            letAviona.setDepartureairportcandidatescount(al.getDepartureAirportCandidatesCount());
            letAviona.setArrivalairportcandidatescount(al.getArrivalAirportCandidatesCount());
            airplanesFacade.create(letAviona);
        }
        return preuzmiLetoveBazePodataka(icao, odVremena, doVremena);
    }

    /**
     * Dodaje zapis o letu putnika zadanim avionom, ako nema preklapanja
     *
     * @param putnik koji putnik
     * @param letAvion kojim letom aviona
     * @return uspješnost dodavanja zapisa
     */
    public boolean dodajLet(int putnik, int letAvion) {
        Airplanes letAviona = airplanesFacade.find(letAvion);
        if (letAviona != null) {
            if (!flightsFacade.provjeriPreklapanjeLetova(putnik, letAviona.getFirstseen(), letAviona.getLastseen())) {
                Passangers p = passangersFacade.find(putnik);
                if (p != null) {
                    Flights flights = new Flights();
                    flights.setAirplane(letAviona);
                    flights.setPassanger(p);
                    flights.setStored(new Date());
                    flightsFacade.create(flights);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Metoda poziva brisanje zapisa leta za putnika
     *
     * @param putnik za kojega se briše let
     * @param letAviona za kojeg se briše let putnika
     * @return uspješnost brisanja
     */
    public boolean brisiLet(int putnik, int letAviona) {
        return flightsFacade.obrisiLetPutnika(putnik, letAviona);
    }

    /**
     * Metoda koja preuzima letove putnika u zadanom intervalu
     *
     * @param putnik identificira putnika
     * @param odVremena
     * @param doVremena
     * @return listu podataka o letu
     */
    public List<PodaciLeta> preuzmiLetovePutnika(int putnik, int odVremena, int doVremena) {
        return flightsFacade.preuzmiLetovePutnika(putnik, odVremena, doVremena);
    }
}
