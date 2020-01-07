/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.ejb.sb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.anikolic.ejb.eb.Airplanes;
import org.foi.nwtis.anikolic.ejb.eb.Airplanes_;
import org.foi.nwtis.anikolic.ejb.eb.Airports;
import org.foi.nwtis.anikolic.ejb.eb.Airports_;
import org.foi.nwtis.anikolic.ejb.eb.Flights;
import org.foi.nwtis.anikolic.ejb.eb.Flights_;
import org.foi.nwtis.anikolic.ejb.eb.Myairports;
import org.foi.nwtis.anikolic.ejb.eb.Passangers_;
import org.foi.nwtis.anikolic.podaci.PodaciLeta;

/**
 *
 * @author nwtis_1
 */
@javax.ejb.Stateless
public class FlightsFacade extends AbstractFacade<Flights> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public FlightsFacade() {
        super(Flights.class);
    }
    
    /**
     * Provjerava postoji li vremensko preklapanje letova za zadanog putnika, u određenom intervalu
     * @param id odabrani putnik
     * @param odVremena početak intervala
     * @param doVremena kraj intervala
     * @return true - postoji preklapanje / false - nema preklapanja letova
     */
    public boolean  provjeriPreklapanjeLetova(int id, int odVremena, int doVremena){
        List<PodaciLeta> letoviPutnika = preuzmiLetovePutnika(id, odVremena, doVremena);
        for (PodaciLeta let : letoviPutnika) {
            if (let.getFirstSeen() >= odVremena && let.getLastSeen() <= doVremena) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     *  Metoda vraća sve letove određenog putnika u određenom intervalu.
     * @param id određuje za kojeg putnika
     * @param odVremena početak intervala
     * @param doVremena kraj intervala
     * @return lista podataka letova
     */
    public List<PodaciLeta>  preuzmiLetovePutnika (int id, int odVremena, int doVremena){
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        List<PodaciLeta> letoviPutnika = new ArrayList<>();
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Flights> letovi = cq.from(Flights.class);  
        Root<Airports> aerodromi = cq.from(Airports.class);
        cq.multiselect(letovi.get(Flights_.airplane), aerodromi);
        List<Predicate> uvjeti = new ArrayList<>();
        uvjeti.add(cb.equal(letovi.get(Flights_.passanger).get(Passangers_.id), id));
        uvjeti.add(cb.equal(letovi.get(Flights_.airplane).get(Airplanes_.estarrivalairport), aerodromi.get(Airports_.ident)));
        uvjeti.add(cb.greaterThanOrEqualTo(letovi.get(Flights_.airplane).get(Airplanes_.firstseen), odVremena));
        uvjeti.add(cb.lessThanOrEqualTo(letovi.get(Flights_.airplane).get(Airplanes_.lastseen), doVremena));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        Query q = em.createQuery(cq);
        List<Object[]> rezultat = q.getResultList();
        for (Object[] o : rezultat) {
            Airplanes a = (Airplanes) o[0];
            Airports md = (Airports) o[1];
            PodaciLeta pl = new PodaciLeta();
            pl.setId(a.getId());
            pl.setNazivOdredisnogAerodroma(md.getName());
            pl.setVrijemePoletanja(sdf.format((long) a.getFirstseen() * 1000));
            pl.setFirstSeen(a.getFirstseen());
            pl.setVrijemeSletanja(sdf.format((long) a.getLastseen() * 1000));
            pl.setLastSeen(a.getLastseen());
            pl.setIcao24(a.getIcao24());
            pl.setCallsign(a.getCallsign());
            pl.setEstDepartureAirport(a.getEstdepartureairport());
            pl.setEstArrivalAirport(a.getEstarrivalairport());
            letoviPutnika.add(pl);
        }
        return letoviPutnika;    
    }    
    
    /**
     * Metoda briše zapis o putovanju za zadane let i putnika
     * @param putnik koji putnik
     * @param avion let
     * @return true - uspješno obrisan let
     */
    public boolean obrisiLetPutnika(int putnik, int avion) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Flights> letovi = cq.from(Flights.class);
        List<Predicate> uvjeti = new ArrayList<>();
        uvjeti.add(cb.equal(letovi.get(Flights_.passanger).get(Passangers_.id), putnik));
        uvjeti.add(cb.equal(letovi.get(Flights_.airplane).get(Airplanes_.id), avion));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        Query q = em.createQuery(cq);
        List<Object> rezultat = q.getResultList();
        if (!rezultat.isEmpty()){
            remove((Flights) rezultat.get(0));
            return true;
        }
        return false;
    }
}
