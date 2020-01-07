package org.foi.nwtis.anikolic.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.xml.ws.WebServiceRef;
import org.foi.nwtis.anikolic.ws.serveri.AIRP2WS_Service;
import org.foi.nwtis.anikolic.ws.serveri.Aerodrom;
import org.foi.nwtis.anikolic.ws.serveri.AvionLeti;

/**
 *
 * @author Ana
 */
@Named(value = "radsAerodromima")
@SessionScoped
public class RadsAerodromima implements Serializable {

    @WebServiceRef(wsdlLocation = "http://localhost:8084/anikolic_zadaca_2_1/AIRP2WS?WSDL")

    private AIRP2WS_Service service;
    private AIRP2REST_JerseyClient restClient;

    private String icao = "";
    private List<Aerodrom> aerodromi;
    private String odabraniAerodrom;
    private Aerodrom aerodrom;
    /**
     * svi avioni za odabrani aerodrom
     */
    private AvionLeti avion;
    private List<AvionLeti> avioni;
    private List<AvionLeti> avioniDio;
    /**
     * poruka korisniku koja se ispisuje ovisno o jeziku i statusu
     */
    private String poruka = "poruka";

    int brojRedakaIzbornika;
    int brojRedakaTablice;
    private boolean prikaz = false;
    JsonParser jsonParser = new JsonParser();
    JsonObject json;
        
    public int getBrojRedakaIzbornika() {
        return brojRedakaIzbornika;
    }

    public int getBrojRedakaTablice() {
        return brojRedakaTablice;
    }
    
    public boolean isPrikaz() {
        return prikaz;
    }
   
    public void setIcao(String icao) {
        this.icao = icao;
    }

    public String getIcao() {
        return icao;
    }

    public String getOdabraniAerodrom() {
        return odabraniAerodrom;
    }

    public void setOdabraniAerodrom(String odabraniAerodrom) {
        this.odabraniAerodrom = odabraniAerodrom;
    }

    public Aerodrom getAerodrom() {
        return aerodrom;
    }

    public List<Aerodrom> getAerodromi() {
        return aerodromi;
    }

    public void setAerodromi(List<Aerodrom> aerodromi) {
        this.aerodromi = aerodromi;
    }
    
    public List<AvionLeti> getAvioni() {
        return avioni;
    }

    /**
     * Konstruktor kreira klijente servisa i učitava podatke
     */
    public RadsAerodromima() {
        service = new AIRP2WS_Service();
        restClient = new AIRP2REST_JerseyClient();
        brojRedakaIzbornika = izbornikBrojRedaka();
        brojRedakaTablice = tablicabrojRedaka();
        aerodromi = dajSveAerodrome();
    }

    /**
     * Dodaje aerodrom sa upisanim IDENT u tablicu MYAIRPORTS koristeći RESTservis
     * @return 
     */
    public String dodajAerodromREST() {
        HashMap<String, String> odg = new HashMap<>();
        odg.put("icao", icao);
        Gson gson = new Gson();
        String zahtjev = gson.toJson(odg);
        String odgovor = restClient.postJson(zahtjev);
        if (dajParametarOdgovora(odgovor, "status").equals("ERR")) {
            poruka = "poruka." + dajParametarOdgovora(odgovor, "poruka");
        }
        poruka = "poruka.uspjesnoDodan";
        return "";
    }

    /**
     * Dodaje aerodrom sa upisanim IDENT u tablicu MYAIRPORTS
     * @return 
     */
    public String dodajAerodromSOAP() {
        if (dodajAerodrom(icao)) {
            poruka = "poruka.uspjesnoDodan";
        } else {
            poruka = "poruka.greskaVecUnesenIliNePostoji";
        }
        return "";
    }

    /**
     * Preuzima podatke o odabranom aerodromu
     * @return 
     */
    public String preuzmiREST() {
        String odgovor = restClient.getJsonId(odabraniAerodrom);
        if (dajParametarOdgovora(odgovor, "status").equals("ERR")) {
            poruka = "poruka." + dajParametarOdgovora(odgovor, "poruka");
        }
        else {
            aerodrom = (Aerodrom) ucitajAerodrom(odgovor);
            poruka = "poruka";
        }
        return "";
    }

    /**
     * Briše podatke o aerodromu iz MYAIRPORTS
     * @return 
     */
    public String brisiREST() {
        String odgovor = restClient.deleteJsonId(odabraniAerodrom);
        if (dajParametarOdgovora(odgovor, "status").equals("ERR")) {
            poruka = "poruka." + dajParametarOdgovora(odgovor, "poruka");
        }
        else {
            poruka = "poruka";
        }
        aerodromi = dajSveAerodrome();
        icao = "";
        return "";
    }

    /**
     * preuzima Podatke o aerodromu
     * @return refresh, podaci aerodroma
     */
    public String preuzmiSOAP() {
        aerodrom = dajAerodrom(odabraniAerodrom);
        if (aerodrom == null) {
            poruka = "poruka.greškaPreuzimanjaAerodroma";
        }
        this.icao = "";
        poruka = "poruka";
        return "";
    }

    /**
     * preuzima avione za odabrani aerodrom
     * @return 
     */
    public String preuzmiSOAPAvione() {
        int odVremena = (int)(System.currentTimeMillis() / 1000 - TimeUnit.DAYS.toSeconds(30));
        int doVremena = (int)(System.currentTimeMillis() / 1000);
        this.avioni = dajAvionePoletjeleSAerodroma(odabraniAerodrom, odVremena, doVremena);
        if (this.avioni == null) {
            poruka = "poruka.greskaAvioni";
        }
        prikaz = true;
        return "";
    }

    public String getPoruka() {
        return poruka;
    }

    /**
     * Metoda vraća vrijednost parametra
     * @param odgovor
     * @param parametar
     * @return 
     */
    private String dajParametarOdgovora(String odgovor, String parametar) {
        json = jsonParser.parse(odgovor).getAsJsonObject();
        JsonElement jsonPoruka = json.get(parametar);
        return jsonPoruka.getAsString();
    }
    
    /**
     * Generira aerodrom iz odgovora
     * @param odgovor
     * @return 
     */
    private Object ucitajAerodrom(String odgovor){
            json = jsonParser.parse(odgovor).getAsJsonObject();
            Gson gson = new Gson();
            return gson.fromJson(json.get("odgovor"), Aerodrom.class);
    }
    
    static class AIRP2REST_JerseyClient {

        private WebTarget webTarget;
        private Client client;
        private static final String BASE_URI = "http://localhost:8084/anikolic_zadaca_2_1/webresources";

        public AIRP2REST_JerseyClient() {
            client = javax.ws.rs.client.ClientBuilder.newClient();
            webTarget = client.target(BASE_URI).path("aerodromi");
        }

        public String putJson(Object requestEntity, String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request(javax.ws.rs.core.MediaType.APPLICATION_JSON).put(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String postJson(Object requestEntity) throws ClientErrorException {
            return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).post(javax.ws.rs.client.Entity.entity(requestEntity, javax.ws.rs.core.MediaType.APPLICATION_JSON), String.class);
        }

        public String getJsonIdAvioni(String id, String doVremena, String odVremena) throws ClientErrorException {
            WebTarget resource = webTarget;
            if (doVremena != null) {
                resource = resource.queryParam("doVremena", doVremena);
            }
            if (odVremena != null) {
                resource = resource.queryParam("odVremena", odVremena);
            }
            resource = resource.path(java.text.MessageFormat.format("{0}/avioni", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String getJsonId(String id) throws ClientErrorException {
            WebTarget resource = webTarget;
            resource = resource.path(java.text.MessageFormat.format("{0}", new Object[]{id}));
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public String deleteJsonId(String id) throws ClientErrorException {
            return webTarget.path(java.text.MessageFormat.format("{0}", new Object[]{id})).request().delete(String.class);
        }

        public String getJson() throws ClientErrorException {
            WebTarget resource = webTarget;
            return resource.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).get(String.class);
        }

        public void close() {
            client.close();
        }
    }

    private boolean dodajAerodrom(java.lang.String icao) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dodajAerodrom(icao);
    }

    private int tablicabrojRedaka() {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.tablicabrojRedaka();
    }

    private int izbornikBrojRedaka() {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.izbornikBrojRedaka();
    }

    private java.util.List<org.foi.nwtis.anikolic.ws.serveri.Aerodrom> dajSveAerodrome() {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajSveAerodrome();
    }

    private java.util.List<org.foi.nwtis.anikolic.ws.serveri.AvionLeti> dajAvionePoletjeleSAerodroma(java.lang.String icao, int odVremena, int doVremena) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajAvionePoletjeleSAerodroma(icao, odVremena, doVremena);
    }

    private Aerodrom dajAerodrom(java.lang.String icao) {
        // Note that the injected javax.xml.ws.Service reference as well as port objects are not thread safe.
        // If the calling of port operations may lead to race condition some synchronization is required.
        org.foi.nwtis.anikolic.ws.serveri.AIRP2WS port = service.getAIRP2WSPort();
        return port.dajAerodrom(icao);
    }
    
    

}
