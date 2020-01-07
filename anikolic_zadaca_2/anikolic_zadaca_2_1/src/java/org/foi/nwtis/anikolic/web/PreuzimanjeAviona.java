package org.foi.nwtis.anikolic.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.anikolic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anikolic.web.podaci.Aerodrom;
import org.foi.nwtis.rest.klijenti.OSKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa dretve, preuzima podatke o avionima za aerodrome iz tablice MyAirports
 * u zadanim intervalima
 * @author Ana Nikolić
 */
public class PreuzimanjeAviona extends Thread {

    boolean kraj = false;
    String username;
    String password;
    /**
     * inicijalni pocetak intervala preuzimanja aviona, koristi se samo jednom
     */
    int inicijalniPocetakIntervala;
    int pocetakIntervala;
    int krajIntervala;
    int trajanjeIntervala;
    int ciklusDretve;

    String korisnik;
    String lozinka;
    String url;
    /**
    * OpenSky klijent za preuzimanje podataka o avionima
    */
    OSKlijent oSKlijent;

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        while (!kraj) {
            long pocetakCiklusa = System.currentTimeMillis();
            List<Aerodrom> aerodromi = dohvatiAerodrome();
            if (!aerodromi.isEmpty()) {
                spremiAvione(aerodromi);
            }
            pocetakIntervala = krajIntervala;
            krajIntervala = pocetakIntervala + trajanjeIntervala;
            try {
                Thread.sleep((ciklusDretve * 60 * 1000) - (System.currentTimeMillis() - pocetakCiklusa));
            } catch (InterruptedException ex) {
                Logger.getLogger(PreuzimanjeAviona.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        BP_Konfiguracija bpk = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        ciklusDretve = bpk.getCiklusPreuzimanja();
        username = bpk.getOpenSkyNetworkKorisnik();
        password = bpk.getOpenSkyNetworkLozinka();
        pocetakIntervala = (int) (new Date().getTime() / 1000) - (bpk.getPocetakPreuzimanja() * 60 * 60);
        trajanjeIntervala = bpk.getTrajanjePreuzimanja();
        ciklusDretve = bpk.getCiklusPreuzimanja();
        krajIntervala = pocetakIntervala + (trajanjeIntervala * 60);
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        oSKlijent = new OSKlijent(username, password);
    }

    /**
     * Učitava sve aerodrome spremljene u tablici MYAIRPORTS
     * @return listaAerodroma - podaci svih aerodroma koji su spremljeni
     */
    public List<Aerodrom> dohvatiAerodrome(){
        List<Aerodrom> aerodromi = new ArrayList<>();
        String upit = "SELECT * FROM MYAIRPORTS";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(upit);
            while (rs.next()) {
                String[] lokacija = rs.getString("coordinates").split(", ");
                Lokacija lok = new Lokacija(lokacija[0], lokacija[1]);
                aerodromi.add(new Aerodrom(rs.getString("ident"), rs.getString("name"),
                        rs.getString("iso_country"), lok));
            }
            s.close();
        } catch (SQLException ex) {
            System.out.println("Ne mogu dohvatiti aerodrome!");
        }
        return aerodromi;
    }

    /**
     * Pohranjuje avione za dani interval koji su poletjeli sa zadanog aerodroma
     * @param aerodromi lista iz koje čita zadane aerodroe za koje sprema avione
     */
    private void spremiAvione(List<Aerodrom> aerodromi) {
        for (Aerodrom aerodrom : aerodromi) {
            List<AvionLeti> departures = oSKlijent.getDepartures(aerodrom.getIcao(), pocetakIntervala, krajIntervala);
            if (!departures.isEmpty()) {
                for (AvionLeti avion : departures) {
                    String naredba = pripremiAvion(avion);
                    if (!naredba.equals("")) {
                        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
                            Statement s = con.createStatement();
                            s.executeUpdate(naredba);
                            s.close();
                            System.out.println("Spremljen aerodrom!");
                        } catch (SQLException ex) {
                            System.out.println("Greška u spremanju aviona!");
                        }
                    }
                }
            }
        }
    }

    /**
     * Metoda koja generira SQL naredbu za spremanja danog aviona
     * @param avion zadani avion čije podatke treba spremiti
     * @return String naredba;
     */
    private String pripremiAvion(AvionLeti avion) {
        if (avion.getIcao24() == null || avion.getIcao24().isEmpty() || avion.getFirstSeen() == 0 || avion.getLastSeen() == 0
                || avion.getArrivalAirportCandidatesCount() == 0 || avion.getDepartureAirportCandidatesCount() == 0
                || avion.getCallsign() == null || avion.getCallsign().isEmpty() || avion.getEstArrivalAirport() == null
                || avion.getEstArrivalAirport().isEmpty() || avion.getEstDepartureAirport() == null
                || avion.getEstDepartureAirport().isEmpty() || avion.getEstDepartureAirportHorizDistance() == 0
                || avion.getEstDepartureAirportVertDistance() == 0 || avion.getEstArrivalAirportHorizDistance() == 0
                || avion.getEstArrivalAirportVertDistance() == 0) {
            return "";
        } else {
            String naredbaSpremiAvion = "INSERT INTO airplanes(icao24, firstSeen, "
                    + "estDepartureAirport, lastSeen, estArrivalAirport, callsign, "
                    + "estDepartureAirportHorizDistance, estDepartureAirportVertDistance,"
                    + "estArrivalAirportHorizDistance, estArrivalAirportVertDistance,"
                    + "departureAirportCandidatesCount, arrivalAirportCandidatesCount, stored) "
                    + "VALUES('" + avion.getIcao24() + "', " + avion.getFirstSeen() + ", '"
                    + avion.getEstDepartureAirport() + "', " + avion.getLastSeen() + ", '"
                    + avion.getEstArrivalAirport() + "', '" + avion.getCallsign()
                    + "', " + avion.getEstDepartureAirportHorizDistance() + ", "
                    + avion.getEstDepartureAirportVertDistance() + ", "
                    + avion.getEstArrivalAirportHorizDistance() + ", "
                    + avion.getEstArrivalAirportVertDistance() + ", "
                    + avion.getDepartureAirportCandidatesCount() + ", "
                    + avion.getArrivalAirportCandidatesCount() + ", CURRENT_TIMESTAMP)";
            return naredbaSpremiAvion;
        }
    }
}
