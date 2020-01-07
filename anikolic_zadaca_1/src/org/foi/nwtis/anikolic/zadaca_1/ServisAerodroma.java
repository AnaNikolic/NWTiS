package org.foi.nwtis.anikolic.zadaca_1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.anikolic.konfiguracije.Konfiguracija;

/**
 * Klasa dretve koja obavlja serijalizaciju i deserijalizaciju podataka 
 * o aerodromima te dodaje nove podatke o aerodromima
 * @author Ana Nikolić
 */
public class ServisAerodroma extends Thread {

    boolean kraj = false;
    private int intervalSerijalizacije;
    private String datotekaAerodroma;
    private List<Aerodrom> aerodromi;

    /**
     * Metoda koja dohvaća listu aerodroma
     * @return lista aerodroma
     */
    public List<Aerodrom> getAerodromi() {
        return aerodromi;
    }
    
    /**
     * Konstruktor klase
     * @param threadGroup - grupa kojoj dretva pripada
     * @param name - naszivDretve
     * @param konf - podaci zadane konfiguracije
     */
    ServisAerodroma(ThreadGroup threadGroup, String name, Konfiguracija konf) {
        super(threadGroup, name);
        aerodromi = new ArrayList<>();
        intervalSerijalizacije = Integer.parseInt(konf.dajPostavku("interval.serijalizacije")) * 1000;
        datotekaAerodroma = konf.dajPostavku("datoteka.aerodroma");
    }

    @Override
    public void interrupt() {
        kraj = true;
        super.interrupt();
    }

    @Override
    public void run() {
        while (!kraj) {
            System.out.println(this.getName() + " run");
            String serijaliziracija = serijaliziraj();
            if ("OK;".equals(serijaliziracija)) {
                try {
                    Thread.sleep(intervalSerijalizacije);
                } catch (InterruptedException ex) {
                    Logger.getLogger(this.getName() + " prekid");
                    kraj = true;
                }
            } else {
                System.out.println("greška u serijalizaciji");
                interrupt();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Dodaje novi aerodrom u listu aerodroma, ako već ne postoji
     * @param aerodrom - podaci o novom aerodromu
     * @return uspješno dodan
     */
    public synchronized boolean dodajAerodrom(Aerodrom aerodrom) {
        if (aerodrom == null || aerodrom.getIata() == null || aerodrom.getIata() == null) {
            return false;
        }
        for (Aerodrom a : this.aerodromi) {
            System.out.println(a.getIata());
            if (a.getIata().equals(aerodrom.getIata()) || a.getIcao().equals(aerodrom.getIcao())) {
                return false;
            }
        }
        aerodromi.add(aerodrom);
        return true;
    }

    /**
     * Serijalizira podatke iz liste aerodroma te ih zapisuje u zadanu datoteku
     * @return uspjesnost serijalizacije
     */
    public synchronized String serijaliziraj() {
        if (datotekaAerodroma == null || "".equals(datotekaAerodroma)) {
            return "ERROR 11; nešto nije u redu s prekidom rada ili serijalizacijom.";
        }
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(datotekaAerodroma);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(aerodromi);
            return "OK;";
        } catch (IOException ex) {
            File dat = new File(datotekaAerodroma);
            if (dat.exists()) {
                dat.delete();
            }
            return "ERROR 11; nešto nije u redu s prekidom rada ili serijalizacijom.";
        } finally {
            try {
                objectOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ServisAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                fileOutputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(ServisAerodroma.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Deserijalizira podatke o aerodromima iz datoteke navedene u konfiguraciji
     * @return poruku o uspješnosti
     */
    public synchronized String deserijaliziraj() {
        File f = new File(datotekaAerodroma);
        if (!f.exists() || f.length()==0) {
            return "ERROR; datoteka ne postoji";
        }
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            fileInputStream = new FileInputStream(datotekaAerodroma);
            objectInputStream = new ObjectInputStream(fileInputStream);
            aerodromi = (List<Aerodrom>) objectInputStream.readObject();
            return "OK;";
        } catch (Exception ex) {
            return "ERROR 12; Greška u deserijalizaciji aerodroma";
        } finally {
            try {
                objectInputStream.close();
            } catch (IOException ex) {
                return "ERROR 12; Greška u deserijalizaciji aerodroma";
            }
            try {
                fileInputStream.close();
            } catch (IOException ex) {
                return "ERROR 12; Greška u deserijalizaciji aerodroma";
            }
        }
    }

    /**
     * Vraca velicinu datoteke aerodroma
     * @return velicina datoteke u bajtovima
     */
    public synchronized long nadiVelicinuDatoteke() {
        File dat = new File(datotekaAerodroma);
        if (dat.exists()) {
            return dat.length();
        }
        return 0L;
    }
}
