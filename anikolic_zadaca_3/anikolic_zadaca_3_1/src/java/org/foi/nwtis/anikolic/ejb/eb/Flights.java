/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.ejb.eb;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author nwtis_1
 */
@Entity
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Flights.findAll", query = "SELECT f FROM Flights f")})
public class Flights implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    private Date stored;
    @JoinColumn(name = "AIRPLANE", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Airplanes airplane;
    @JoinColumn(name = "PASSANGER", referencedColumnName = "ID")
    @ManyToOne(optional = false)
    private Passangers passanger;

    public Flights() {
    }

    public Flights(Integer id) {
        this.id = id;
    }

    public Flights(Integer id, Date stored) {
        this.id = id;
        this.stored = stored;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getStored() {
        return stored;
    }

    public void setStored(Date stored) {
        this.stored = stored;
    }

    public Airplanes getAirplane() {
        return airplane;
    }

    public void setAirplane(Airplanes airplane) {
        this.airplane = airplane;
    }

    public Passangers getPassanger() {
        return passanger;
    }

    public void setPassanger(Passangers passanger) {
        this.passanger = passanger;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Flights)) {
            return false;
        }
        Flights other = (Flights) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.foi.nwtis.dkermek.ejb.eb.Flights[ id=" + id + " ]";
    }

}
