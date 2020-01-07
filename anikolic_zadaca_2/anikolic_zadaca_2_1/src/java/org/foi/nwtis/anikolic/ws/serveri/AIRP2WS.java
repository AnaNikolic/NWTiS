/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.foi.nwtis.anikolic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anikolic.web.podaci.Aerodrom;
import org.foi.nwtis.anikolic.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * Klasa sadrži operacije koje čine SOAP web servis.
 *
 * @author Ana Nikolić
 */
@WebService(serviceName = "AIRP2WS")
public class AIRP2WS {

    BP_Konfiguracija bpk;
    String korisnik;
    String lozinka;
    String url;

    /**
     * Metoda čita podatke iz datoteke konfiguracije i sprema konfiguraciju
     * lokalno
     */
    private void inicijaliziraj() {
        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
    }

    /**
     * Web service operation Metoda vraća popis svih spremljenih aerodroma,
     * njihovih naziva, država i geolokacija
     *
     * @return java.util.List<Aerodrom>
     */
    @WebMethod(operationName = "dajSveAerodrome")
    public List<Aerodrom> dajSveAerodrome() {
        inicijaliziraj();
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
            System.out.println("Ne mogu dohvatiti moje aerodrome!");
        }
        return aerodromi;
    }

    /**
     * Web service operation Vraća podatke o aerodromu, naziv, državu i lokaciju
     *
     * @param icao kod aerodroma
     * @return Aerodrom
     */
    @WebMethod(operationName = "dajAerodrom")
    public Aerodrom dajAerodrom(@WebParam(name = "icao") final String icao) {
        inicijaliziraj();
        Aerodrom aerodrom = new Aerodrom();
        String upit = "SELECT * FROM AIRPORTS WHERE ident = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit)) {
            if (rs.next()) {
                aerodrom.setIcao(icao);
                aerodrom.setNaziv(rs.getString("name"));;
                aerodrom.setDrzava(rs.getString("iso_country"));
            }
        } catch (SQLException ex) {
            System.out.println("Problem u konfiguraciji!");
            return null;
        }
        if (aerodrom.getNaziv() != null) {
            LIQKlijent lIQKlijent = new LIQKlijent(bpk.getLocationIQToken());
            aerodrom.setLokacija(lIQKlijent.getGeoLocation(aerodrom.getNaziv()));
        }
        return aerodrom;
    }

    /**
     * Web service operation Dodaje aerodrom u tablicu my airports na temalju
     * poslanog icao koda, provjerava da li već postoji
     *
     * @param icao
     * @return boolean - uspješno dodan
     */
    @WebMethod(operationName = "dodajAerodrom")
    public boolean dodajAerodrom(@WebParam(name = "icao") String icao) {
        inicijaliziraj();
        Aerodrom aerodrom = new Aerodrom();
        String provjeri = "SELECT * FROM MYAIRPORTS WHERE ident = '" + icao + "'";
        String pronadiUpit = "SELECT * FROM AIRPORTS WHERE ident = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka);
                Statement s = con.createStatement()){
            ResultSet rs = s.executeQuery(provjeri);
            if (rs.next()) {
                return false;
            }
            rs = s.executeQuery(pronadiUpit);
            if (rs.next()) {
                aerodrom.setIcao(icao);
                aerodrom.setNaziv(rs.getString("name"));
                aerodrom.setDrzava(rs.getString("iso_country"));
                LIQKlijent lIQKlijent = new LIQKlijent(bpk.getLocationIQToken());
                aerodrom.setLokacija(lIQKlijent.getGeoLocation(aerodrom.getNaziv()));
                String lokacija = aerodrom.getLokacija().getLatitude() + ", " + aerodrom.getLokacija().getLongitude();
                String naredbaDodaj = "INSERT INTO MYAIRPORTS VALUES ('" + aerodrom.getIcao() + "', "
                        + "'" + aerodrom.getNaziv() + "', '" + aerodrom.getDrzava() + "', '" + lokacija + "', CURRENT_TIMESTAMP)";
                s.executeUpdate(naredbaDodaj);
            }
            s.close();
        } catch (SQLException ex) {
            System.out.println("Problem pri dodavanju!");
            return false;
        }
        return true;
    }

    /**
     * Web service operation Daje popis svih aviona koji su polijetali sa
     * zadanog aerodrom u određenom razdoblju
     *
     * @param icao - zadani aerodrom
     * @param odVremena
     * @param doVremena
     * @return java.util.List<Avion> - popis aviona
     */
    @WebMethod(operationName = "dajAvionePoletjeleSAerodroma")
    public List<AvionLeti> dajAvioneZaAerodrom(@WebParam(name = "icao") String icao,
            @WebParam(name = "odVremena") int odVremena, @WebParam(name = "doVremena") int doVremena) {
        if (icao == null || odVremena <= 0 || doVremena <= 0) {
            return null;
        }
        inicijaliziraj();
        List<AvionLeti> avioni = new ArrayList<>();
        String upit = "SELECT * FROM AIRPLANES WHERE estDepartureAirport = '" + icao
                + "' AND lastSeen BETWEEN " + odVremena + " AND " + doVremena;//+ " ORDER BY stored DESC";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit)) {
            while (rs.next()) {
                AvionLeti avion = new AvionLeti();
                avion.setIcao24(rs.getString("icao24"));
                avion.setFirstSeen(rs.getInt("firstSeen"));
                avion.setEstDepartureAirport(rs.getString("estDepartureAirport"));
                avion.setLastSeen(rs.getInt("lastSeen"));
                avion.setEstArrivalAirport(rs.getString("estArrivalAirport"));
                avion.setCallsign(rs.getString("callsign"));
                avion.setEstDepartureAirportHorizDistance(rs.getInt("estDepartureAirportHorizDistance"));
                avion.setEstDepartureAirportVertDistance(rs.getInt("estDepartureAirportVertDistance"));
                avion.setEstArrivalAirportHorizDistance(rs.getInt("estArrivalAirportHorizDistance"));
                avion.setEstArrivalAirportVertDistance(rs.getInt("estArrivalAirportVertDistance"));
                avion.setDepartureAirportCandidatesCount(rs.getInt("departureAirportCandidatesCount"));
                avion.setArrivalAirportCandidatesCount(rs.getInt("arrivalAirportCandidatesCount"));
                avioni.add(avion);
            }
        } catch (SQLException ex) {
            return new ArrayList<AvionLeti>();
        }
        return avioni;
    }

    /**
     * Web service operation Daje informaciju da li polijetio avion sa zadanog
     * aerodroma na određeni aerodrom u određenom razdoblju (od, do)
     *
     * @param icao24
     * @param icao - sa kojeg aerodroma
     * @param odVremena
     * @param doVremena
     * @return boolean
     */
    @WebMethod(operationName = "provjeriAvionPoletioSAerodroma")
    public boolean provjeriAvionPoletioSAerodroma(@WebParam(name = "icao24") String icao24,
            @WebParam(name = "icao") String icao, @WebParam(name = "odVremena") int odVremena,
            @WebParam(name = "doVremena") int doVremena) {
        if (icao == null || icao24 == null || odVremena <= 0 || doVremena <= 0) {
            return false;
        }
        inicijaliziraj();
        String upit = "SELECT COUNT(*) as broj FROM AIRPLANES WHERE icao24 = '" + icao24
                + "' AND estDepartureAirport = '" + icao
                + "' AND lastSeen BETWEEN " + odVremena + " AND " + doVremena;
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka);
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit)) {
            if (rs.next()) {
                if (rs.getInt("broj") >= 1) {
                    return true;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Greska u čitanju podataka aviona");
        }
        return false;
    }

    /**
     * Web service operation vraća broj redaka u padajućem izborniku s
     * aerodromima, učitane iz datoteke konfiguracije
     *
     * @return int
     */
    @WebMethod(operationName = "izbornikBrojRedaka")
    public int dajBrRedakaIzbornika() {
        inicijaliziraj();
        return bpk.getBrojRedakaIzbornika();
    }

    /**
     * Web service operation vraća broj redaka u tablici s avionima, učitane iz
     * datoteke konfiguracije
     *
     * @return int
     */
    @WebMethod(operationName = "tablicabrojRedaka")
    public int dajBrRedakaTablice() {
        inicijaliziraj();
        return bpk.getBrojRedakaTablice();
    }
}
