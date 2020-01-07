/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.ejb.sb;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.foi.nwtis.anikolic.ejb.eb.Passangers;

/**
 *
 * @author nwtis_1
 */
@javax.ejb.Stateless
public class PassangersFacade extends AbstractFacade<Passangers> {

    @PersistenceContext(unitName = "NWTiS_DZ3_PU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public PassangersFacade() {
        super(Passangers.class);
    }
    
}
