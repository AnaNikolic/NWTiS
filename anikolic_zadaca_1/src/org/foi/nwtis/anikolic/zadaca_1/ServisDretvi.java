package org.foi.nwtis.anikolic.zadaca_1;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.foi.nwtis.anikolic.konfiguracije.Konfiguracija;

/**
 *
 * @author Ana Nikolić
 *
 * Klasa je dretva koja provodi nadzor svih dretvi
 *
 */
public class ServisDretvi extends Thread {

    boolean kraj = false;
    Konfiguracija konf = null;
    Integer intervalNadzora;
    String datotekaNadzora;
    private ThreadGroup servisneDretve;
    private ThreadGroup korisnickeDretve;

    /**
     * Konstruktor klase
     * @param threadGroup grupa kojoj dretva pripada
     * @param name naziv dretve
     * @param konf postavke iz datoteke parametara
     */
    ServisDretvi(ThreadGroup threadGroup, String name, Konfiguracija konf) {
        super(threadGroup, name);
        intervalNadzora = Integer.parseInt(konf.dajPostavku("interval.nadzora")) * 1000;
        datotekaNadzora = konf.dajPostavku("datoteka.nadzora");
    }

    /**
     * Dohvaća grupe dretvi
     * @param servDretve
     * @param korDretve 
     */
    public void pripremiServisDretvi(ThreadGroup servDretve, ThreadGroup korDretve) {
        servisneDretve = servDretve;
        korisnickeDretve = korDretve;
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
            try {
                if (!izveziPodatke()){
                    interrupt();
                }
                Thread.sleep(intervalNadzora);
            } catch (InterruptedException ex) {
                Logger.getLogger(this.getName() + " prekid");
                interrupt();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * Zapisuje podatke o dretvama u zadanu datotetku nadzora, 
     * poziva metodu koja generira te podatke
     * @return uspjesnost zapisivanja podataka
     */
    public boolean izveziPodatke() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
        String vrijeme = sdf.format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(vrijeme).append("\n");
        try {
            FileWriter fileWriter = new FileWriter(datotekaNadzora, true);
            stringBuilder.append(generirajPodatke(korisnickeDretve));
            stringBuilder.append(generirajPodatke(servisneDretve));
            fileWriter.write(stringBuilder.toString());
            fileWriter.close();
            return true;
        } catch (IOException ex) {
            System.out.println("Upis podataka o dretvama nije uspio.");
            return false;
        }
    }
    
    /**
     * Stvara string podataka o dretvama
     * @param dretve - grupa dretvi za koju generira podatke
     * @return string podataka
     */
    public String generirajPodatke(ThreadGroup dretve){
        StringBuilder stringBuilder = new StringBuilder();
        Thread[] dretveGrupe = new Thread[dretve.activeCount()];
            dretve.enumerate(dretveGrupe);
            for (Thread dretva : dretveGrupe) {
                String pisi = String.format("%s %s %s %s %s\n", dretva.getThreadGroup().getName(), dretva.getId(),
                        dretva.getName(), dretva.getState(), dretva.getPriority());
                stringBuilder.append(pisi);
            }
        return stringBuilder.toString();
    }
}
