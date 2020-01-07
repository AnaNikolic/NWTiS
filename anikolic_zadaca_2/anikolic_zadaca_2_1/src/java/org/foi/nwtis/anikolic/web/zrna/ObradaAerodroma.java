package org.foi.nwtis.anikolic.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.foi.nwtis.anikolic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anikolic.web.podaci.Aerodrom;
import org.foi.nwtis.rest.klijenti.LIQKlijent;
import org.foi.nwtis.rest.klijenti.OWMKlijent;
import org.foi.nwtis.rest.podaci.MeteoPodaci;

/**
 * Zrno koje sprema i upravlja podacima o aerodromima te dobavlja
 * informacije o lokaciji i meteo podatke
 * @author Ana
 */
@Named(value = "obradaAerodroma")
@SessionScoped
public class ObradaAerodroma implements Serializable {

    private String icao;
    private Aerodrom aerodrom;
    private MeteoPodaci meteoPodaci;

    private BP_Konfiguracija bpk;

    private String pom;
    String poruka;

    public String getPoruka() {
        return poruka;
    }
    String korisnik;
    String lozinka;
    String url;

    /**
     * Kosstruktor klase, učitava konfiguraciju iz datoteke
     */
    public ObradaAerodroma() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
        bpk = (BP_Konfiguracija) servletContext.getAttribute("BP_Konfig");
        korisnik = bpk.getUserUsername();
        lozinka = bpk.getUserPassword();
        url = bpk.getServerDatabase() + bpk.getUserDatabase();
        aerodrom = new Aerodrom();
    }

    public String getPom() {
        return pom;
    }

    public String getIcao() {
        return icao;
    }

    public void setIcao(String icao) {
        this.icao = icao;
    }

    public Aerodrom getAerodrom() {
        return aerodrom;
    }

    public MeteoPodaci getMeteoPodaci() {
        return meteoPodaci;
    }

    /**
     * Za dani icao dohvaća podatke o aerodromu
     * @return "" osvježava stranicu te je zatim prikazan naziv aerodroma ili poruka
     */
    public String preuzmiNazivAerodroma() {
        List<Aerodrom> aerodromi = new ArrayList<>();
        String upit = "SELECT * FROM AIRPORTS WHERE ident = '" + icao + "'";
        try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
            Statement s = con.createStatement();
            ResultSet rs = s.executeQuery(upit);
            if (rs.next()) {
                aerodrom.setNaziv(rs.getString("name"));
                aerodrom.setDrzava(rs.getString("iso_country"));
                aerodrom.setIcao(icao);
                poruka = null;
            } else {
                aerodrom = new Aerodrom();
                poruka = "poruka.nePostojiIdent";
            }
            s.close();
        } catch (SQLException ex) {
            System.out.println("Greška u čitanju podataka");
        }
        return "";
    }

    /**
     * Za poznati naziv aerodroma, koristeći LocationIQ servis dohvaća podatke o lokaciji
     * ili ispisuje poruku
     * @return "" osvježava stranicu, tako su prikazani GPS podaci
     */
    public String preuzmiGPSLokaciju() {
        try {
            String token = bpk.getLocationIQToken();
            LIQKlijent lIQKlijent = new LIQKlijent(token);
            aerodrom.setLokacija(lIQKlijent.getGeoLocation(aerodrom.getNaziv()));
            poruka = null;
        } catch (Exception ex) {
            poruka = "poruka.nemaNaziva";
        }
        return "";
    }

    /**
     * Metoda sprema podatke o aerodromu u tablicu MYAIRPORTS, ako su svi podaci o
     * aerodromu poznati i ako aerodrom sa tom ident oznakom nije već spremljen
     * @return "" - osvježava i ispisuje poruku
     */
    public String spremiAerodrom() {
        if (aerodrom.getLokacija() != null) {
            String lokacija = aerodrom.getLokacija().getLatitude().toString() + ", " + aerodrom.getLokacija().getLongitude().toString();
            String upit = "INSERT INTO MYAIRPORTS VALUES ('" + aerodrom.getIcao() + "', "
                    + "'" + aerodrom.getNaziv() + "', '" + aerodrom.getDrzava() + "', '" + lokacija + "', CURRENT_TIMESTAMP)";
            try (Connection con = DriverManager.getConnection(url, korisnik, lozinka)) {
                String provjeraUpit = "SELECT * FROM MYAIRPORTS WHERE ident = '" + aerodrom.getIcao() + "'";
                Statement s = con.createStatement();
                ResultSet rs = s.executeQuery(provjeraUpit);
                if (rs.next()) {
                    poruka = "poruka.vecPostoji";
                    s.close();
                    return "";
                }

                PreparedStatement naredba = con.prepareStatement(upit);
                naredba.executeUpdate();
                s.close();
                poruka = "poruka.spremljen";
            } catch (SQLException ex) {
                Logger.getLogger(ObradaAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            poruka = "poruka.nisuPoznatiPodaci";
        }
        return "";
    }

    /**
     * Za poznatu lokaciju aerodroma dobavlja i ispisuje meterološke
     * podatke ili ispisuje poruku
     * @return "" - osvježava i ispisuje podatke
     */
    public String preuzmiMeteoPodatke() {
        try {
            String apikey = bpk.getOpenWeatherMapApiKey();
            OWMKlijent oWMKlijent = new OWMKlijent(apikey);
            meteoPodaci = oWMKlijent.getRealTimeWeather(
                    aerodrom.getLokacija().getLatitude(),
                    aerodrom.getLokacija().getLongitude());
            poruka = null;
        } catch (NullPointerException ex) {
            poruka = "poruka.nepoznataLokacija";
        }
        return "";
    }

}
