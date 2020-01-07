package org.foi.nwtis.anikolic.web.slusaci;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.anikolic.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.anikolic.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.anikolic.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.anikolic.web.PreuzimanjeAviona;

@WebListener
public class SlusacAplikacije implements ServletContextListener {

    /**
     * Kontekst servleta
     */
    private static ServletContext sc;
    /**
     * Dretva koja u pozadini izvršava preuzimanje aviona
     */
    PreuzimanjeAviona preuzimanjeAviona;
     /**
     * @return kontekst servleta
     */
    public static ServletContext getSc() {
        return sc;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sc = sce.getServletContext();
        String putanja = sc.getRealPath("/WEB-INF");
        String datoteka = putanja + File.separator
                + sc.getInitParameter("konfiguracija");

        try {
            BP_Konfiguracija bpk = new BP_Konfiguracija(datoteka);
            sc.setAttribute("BP_Konfig", bpk);
            System.out.println("Učitana konfiguracija");
            preuzimanjeAviona = new PreuzimanjeAviona();
            // TODO maknuti komentar iz linje iza tako da se pokrene dretva
            preuzimanjeAviona.start();
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (preuzimanjeAviona != null) {
            preuzimanjeAviona.interrupt();
        }
        sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
    }
}
