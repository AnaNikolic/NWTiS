/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.anikolic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anikolic.web.podaci.Aerodrom;
import org.foi.nwtis.anikolic.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.podaci.AvionLeti;
import org.foi.nwtis.rest.podaci.Lokacija;

/**
 * REST Web Service
 * Servis koji vraća podatke o aerodromima i avionima, te vrši CRUD nad tablicama u bazi podataka
 * @author Ana Nikolić
 */
@Path("aerodromi")
public class AIRP2REST {

    /**
     * Kontekst servleta
     */
    @Context
    private UriInfo context;

    BP_Konfiguracija bpk;
    String korisnik;
    String lozinka;
    String url;

    /**
     * Učiatava podatke iz datoteke konfiguracije
     */
    private void inicijaliziraj() {
        bpk = (BP_Konfiguracija) SlusacAplikacije.getSc().getAttribute("BP_Konfig");
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
    }

    /**
     * Pomoćna metoda koja generira odgovor servisa, 
     * ovisno o parametrima
     * @param ok - uspjeh izvrsavanja zeljenog poziva
     * @param par - poruka sa informacijom o neuspjehu ili traženi podaci
     * @return String odgovor
     */
    private String generirajOdg(Boolean ok, Object par) {
        HashMap<String, Object> odg = new HashMap<>();
        if (ok) {
            odg.put("status", "OK");
            odg.put("odgovor", par);
        } else {
            odg.put("status", "ERR");
            odg.put("poruka", par);
        }
        Gson gson = new Gson();
        String odgovor = gson.toJson(odg);
        return odgovor;
    }

    /**
     * Konstruktor AIRP2REST
     */
    public AIRP2REST() {
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.anikolic.rest.serveri.AIRP2REST
     *
     * vraća popis svih aerodroma, a za svaki aerodrom icao, naziv, državu i geo
     * lokaciju
     *
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson() {
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
            System.out.println("Ne mogu dohvatiti aerodrome!");
            return generirajOdg(false, "greskaUDohvacanjuAerodroma");
        }
        return generirajOdg(true, aerodromi);
    }

    /**
     * POST method
     * Dodaje aerodrom u MYAIRPORTS za dani ICAO kod
     * @param icao
     * @return status i prazna lista ili poruka o neuspjehu
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String postJson(String icao) {
        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(icao).getAsJsonObject();
        JsonElement jsonIcao = json.get("icao");
        icao = jsonIcao.getAsString();
        inicijaliziraj();
        if (postojiMojAerodrom(icao).equals("nepostoji")) {
            String upit = "SELECT * FROM AIRPORTS WHERE ident = '" + icao + "'";
            Aerodrom aerodrom = new Aerodrom();
            try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(upit);
                if (rs.next()) {
                    aerodrom.setIcao(rs.getString("ident"));
                    aerodrom.setNaziv(rs.getString("name"));
                    aerodrom.setDrzava(rs.getString("iso_country"));
                    LIQKlijent lIQKlijent = new LIQKlijent(bpk.getLocationIQToken());
                    Lokacija lokacija = lIQKlijent.getGeoLocation(aerodrom.getNaziv());
                    aerodrom.setLokacija(lokacija);
                    String upisi = "INSERT INTO MYAIRPORTS(ident, name, iso_country, coordinates, stored) VALUES ('" + aerodrom.getIcao()
                            + "', '" + aerodrom.getNaziv() + "', '" + aerodrom.getDrzava() + "', '" + aerodrom.getLokacija().getLatitude()
                            + ", " + aerodrom.getLokacija().getLongitude() + "', CURRENT_TIMESTAMP)";
                    return generirajOdg(true, new ArrayList<>());
                }
            } catch (SQLException ex) {
                return generirajOdg(false, "greskaUnosaAerodroma");
            }
        }
        return generirajOdg(false, "greskaVecUnesenIliNePostoji");
    }

    /**
     * Metoda proverava postoji li aerodrom sa danim ICAO već u tablici MYAIRPORTS
     * @param icao aerodroma za koji provjerava
     * @return String postoji/ne poostoji/greška
     */
    private String postojiMojAerodrom(String icao) {
        String upit = "SELECT * FROM MYAIRPORTS WHERE IDENT = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(upit);
            if (rs.next()) {
                return "postoji";
            } else {
                return "nepostoji";
            }
        } catch (SQLException ex) {
            return "greska";
        }
    }
    
    /**
     * Vraća aerodrom za dani ICAO kod
     * @param icao identificira zadani aerodrom
     * @return status i Aerodrom podatke ili poruku neuspjeha
     */
    @Path("{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJsonId(@PathParam("id") String icao) {
        if (icao == null || icao.isEmpty()) {
            return generirajOdg(false, "pogresniParametri");
        }
        inicijaliziraj();
        String upit = "SELECT * FROM MYAIRPORTS WHERE ident = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(upit);
            if (rs.next()) {
                Aerodrom aerodrom = new Aerodrom();
                aerodrom.setIcao(icao);
                aerodrom.setDrzava(rs.getString("iso_country"));
                aerodrom.setNaziv(rs.getString("name"));
                String[] lokacija = rs.getString("coordinates").split(", ");
                Lokacija lok = new Lokacija(lokacija[0], lokacija[1]);
                aerodrom.setLokacija(lok);
                return generirajOdg(true, aerodrom);
            }
            rs.close();
        } catch (SQLException ex) {
            return generirajOdg(false, "greska");
        }
        return generirajOdg(false, "greska");
    }

    /**
     * PUT method
     * Ažurira podatke zadanog aerodroma koristeći LocationIQ
     * @param icao identificira zadani aerodrom
     * @param content - naziv i adresa, JSON format
     * @return status i prazna lista ili porukas greške, JSON format
     */
    @Path("{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String putJson(@PathParam("id") String icao, String content) {
        if (icao == null || icao.isEmpty() || content == null || content.isEmpty()) {

        }
        JsonParser jsonParser = new JsonParser();
        JsonObject json = jsonParser.parse(content).getAsJsonObject();
        JsonElement jsonNaziv = json.get("naziv");
        String naziv = jsonNaziv.getAsString();
        JsonElement jsonAdresa = json.get("adresa");
        String adresa = jsonAdresa.getAsString();
        inicijaliziraj();
        LIQKlijent lIQKlijent = new LIQKlijent(bpk.getLocationIQToken());
        Lokacija lokacija = lIQKlijent.getGeoLocation(naziv);
        if (lokacija == null) {
            return generirajOdg(false, "greskaUnosaAerodroma");
        }
        if (postojiMojAerodrom(icao).equals("postoji")) {
            String updateAerodrom = "UPDATE MYAIRPORTS SET NAME = '" + naziv + "', ISO_COUNTRY = '" + adresa + "', COORDINATES = '"
                    + lokacija.getLatitude() + ", " + lokacija.getLongitude() + "' WHERE IDENT = '" + icao + "'";
            try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
                Statement s = con.createStatement();
                s.executeQuery(updateAerodrom);
                s.close();
                return generirajOdg(true, new ArrayList<>());
            } catch (SQLException ex) {
                return generirajOdg(false, "greskaPriUpisu");
            }
        }
        return generirajOdg(false, "greskaNePostoji");
    }

    /**
     * Briše aerodrom iz tablice MYAIRPORTS, provjerava prije da li postoji
     * @param icao kod aerodroma
     * @return status i prazna lista ili porukas greške, JSON format
     */
    @Path("{id}")
        @DELETE
        public String deleteJsonId(@PathParam("id") String icao) {
        if (icao == null || icao.isEmpty()) {
            return generirajOdg(false, "greskaParametara");
        }
        inicijaliziraj();
        String postoji = "SELECT * FROM MYAIRPORTS WHERE ident = '" + icao + "'";
        String naredba = "DELETE FROM MYAIRPORTS WHERE ident = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(postoji);
            if (rs.next()) {
                s.executeUpdate(naredba);
                s.close();
                return generirajOdg(true, new ArrayList<>());
            }
            s.close();
            return generirajOdg(false, "greskaBrisanjaAerodroma");
        } catch (SQLException ex) {
            return generirajOdg(false, "greskaBrisanjaAerodroma");
        }
    }

    /**
     * Vraća sve avione poletjele se zadanog aerodroma u vremenskom intervalu
     * @param icao kod aerodroma
     * @param odVremena
     * @param doVremena
     * @return status i listu aviona ili poruku o neuspjehu, JSON format
     */
    @Path("{id}/avioni")
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public String getJsonIdAvioni(@PathParam("id") String icao,
            @QueryParam("odVremena") int odVremena,
            @QueryParam("doVremena") int doVremena) {
        if (icao == null || icao.isEmpty() || odVremena <= 0 || doVremena <= 0) {
            return generirajOdg(false, "greskaParametara");
        }
        inicijaliziraj();
        String upit = "SELECT * FROM AIRPLANES WHERE estDepartureAirport = '" + icao
                + "' AND lastSeen BETWEEN " + odVremena + " AND " + doVremena;//+ " ORDER BY stored DESC";
        List<AvionLeti> avioni = new ArrayList<>();
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
            return generirajOdg(true, avioni);
        } catch (SQLException ex) {
            return generirajOdg(false, "greskaCitanja");
        }
    }
}
