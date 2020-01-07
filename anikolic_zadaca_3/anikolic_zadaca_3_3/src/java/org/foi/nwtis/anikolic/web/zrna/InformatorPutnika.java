package org.foi.nwtis.anikolic.web.zrna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket krajnja točka
 * @author Ana
 */
@ServerEndpoint("/infoPutnik")
public class InformatorPutnika {

    /**
     * Statička kolekcija za otvorene sjednice 
     */
    private static List<Session> sjednice = Collections.synchronizedList(new ArrayList<>());

    
    /**
     * Poziva se kada primi poruku
     * @param poruka string
     * @param sjednica 
     */
    @OnMessage
    public void onMessage(String poruka, Session sjednica) {
        synchronized (sjednice) {
            for (Session s : sjednice) {
                if (!s.equals(sjednica) && s.isOpen()) {
                    try {
                        s.getBasicRemote().sendText(poruka);
                    } catch (IOException ex) {
                        System.out.println("greska slanja poruke");
                    }
                }
            }
        }
    }

    /**
     * Poziva se pri stvaranju sjednice
     * @param sjednica otvorena sjednica
     */
    @OnOpen
    public void onOpen(Session sjednica) {
        System.out.println("Dodana sjednica.");
        sjednice.add(sjednica);
    }

    /**
     * Ispisuje da je obrisana sjednica pri zatvaranju.
     * @param sjednica koja se zatvara
     */
    @OnClose
    public void onClose(Session sjednica) {
        System.out.println("Obrisana sjednica.");
        sjednice.remove(sjednica);
    }
 
    /**
     * Šalje poruku svakoj otvorenoj sjednici
     * @param poruka string poruka
     */
    public static void saljiPoruku(String poruka) {
        for (Session s : sjednice) {
            if (s.isOpen()) {
                try {
                    s.getBasicRemote().sendText(poruka);
                } catch (IOException ex) {
                    System.out.println("greska slanja poruke");
                }
            }
        }
    }

}
