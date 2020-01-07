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
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.foi.nwtis.anikolic.ejb.eb.Airplanes;
import org.foi.nwtis.anikolic.ejb.eb.Airplanes_;
import org.foi.nwtis.anikolic.ejb.eb.Myairports;
import org.foi.nwtis.anikolic.ejb.eb.Myairports_;
import org.foi.nwtis.anikolic.podaci.PodaciLeta;

/**
 *
 * @author nwtis_1
 */
@javax.ejb.Stateless
public class AirplanesFacade extends AbstractFacade<Airplanes> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }
    
    private SimpleDateFormat simpleDateFormat;

    public AirplanesFacade() {
        super(Airplanes.class);
    }
    
    /**
     * Metoda koja vraća letove za zadani aerodrom za zadani interval
     * @param icao zadani aerodrom polijetanja
     * @param odVremena
     * @param doVremena
     * @return lista podataka leta za zadane parametre
     */
     public List<PodaciLeta> preuzmiLetove(String icao, int odVremena, int doVremena) {
        simpleDateFormat= new SimpleDateFormat("dd.MM.yyyy HH:mm");
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery();
        Root<Airplanes> sviLetovi = cq.from(Airplanes.class);
        Root<Myairports> sviAerodromiDolazak = cq.from(Myairports.class);        
        cq.multiselect(sviLetovi, sviAerodromiDolazak);
        List<Predicate> uvjeti = new ArrayList<>();
        uvjeti.add(cb.equal(sviLetovi.get(Airplanes_.estarrivalairport),
                sviAerodromiDolazak.get(Myairports_.ident)));
        uvjeti.add(cb.equal(sviLetovi.get(Airplanes_.estdepartureairport), icao));
        uvjeti.add(cb.between(sviLetovi.get(Airplanes_.firstseen), odVremena, doVremena));
        cq.where(uvjeti.toArray(new Predicate[]{}));
        cq.orderBy(cb.asc(sviLetovi.get(Airplanes_.firstseen)));
        
        Query q = em.createQuery(cq);
        List<Object[]> rezultat = q.getResultList();
        List<PodaciLeta> podaciLeta = new ArrayList<>();

        for (Object[] o : rezultat) {
            Airplanes a = (Airplanes) o[0];
            Myairports md = (Myairports) o[1];
            PodaciLeta pl = new PodaciLeta();
            pl.setId(a.getId());
            pl.setNazivOdredisnogAerodroma(md.getName());
            pl.setVrijemePoletanja(simpleDateFormat.format((long) a.getFirstseen() * 1000));
            pl.setFirstSeen(a.getFirstseen());
            pl.setVrijemeSletanja(simpleDateFormat.format((long) a.getLastseen() * 1000));
            pl.setLastSeen(a.getLastseen());
            pl.setIcao24(a.getIcao24());
            pl.setCallsign(a.getCallsign());
            pl.setEstDepartureAirport(a.getEstdepartureairport());
            pl.setEstArrivalAirport(a.getEstarrivalairport());
            podaciLeta.add(pl);
        }

        return podaciLeta;
    }
}
