/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.anikolic.zadaca_1;



import org.foi.nwtis.anikolic.konfiguracije.Konfiguracija;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author nwtis_1
 */
public class ServerAerodromaTest {
    
    public ServerAerodromaTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
   

    /**
     * Test of main method, of class ServerAerodroma.
     */
    // @Ignore
    @Test
    public void testMain() {
        System.out.println("main");
       // String[] args = {"konfiguracija.txt"};
        String[] args = null;
        ServerAerodroma.main(args);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");

    }
    
    /**
     * Test of pripremiParametre method, of class ServerAerodroma.
     */
    @Test
    public void testPripremiParametre() {
        System.out.println("pripremiParametre");
        String[] args1 = { "konfiguracija.txt" };
        ServerAerodroma instance = new ServerAerodroma();
        String expResult1 = "konfiguracija.txt";
        String result = instance.pripremiParametre(args1);
        assertEquals(expResult1, result);
        String[] args2 = { "konfiguracija.txt", "pero" };
        ServerAerodroma instance2 = new ServerAerodroma();
        String expResult2 = "konfiguracija.txt pero";
        String result2 = instance2.pripremiParametre(args2);
        assertEquals(expResult2, result2);
        
    }

    /**
     * Test of provjeriParametare method, of class ServerAerodroma.
     */
    @Test
    public void testProvjeriParametare() {
        System.out.println("provjeriParametare");
        String parametri1 = "konfiguracija.txt";
        ServerAerodroma instance1 = new ServerAerodroma();
        boolean expResult1 = true;
        boolean result1 = instance1.provjeriParametre(parametri1);
        assertEquals(expResult1, result1);
        String parametri2 = "konfiguracija.txt";
        ServerAerodroma instance2 = new ServerAerodroma();
        boolean expResult2 = false;
        boolean result2 = instance2.provjeriParametre(parametri2);
        assertEquals(expResult2, result2);
    }

    /**
     * Test of ucitajKonfiguraciju method, of class ServerAerodroma.
     */
    @Test
    public void testUcitajKonfiguraciju() throws Exception {
        System.out.println("ucitajKonfiguraciju");
        String nazivDatoteke = "konfiguracija.txt";
        ServerAerodroma instance = new ServerAerodroma();
        Konfiguracija expResult = null;
        Konfiguracija result = instance.ucitajKonfiguraciju(nazivDatoteke);
        assertEquals(expResult, result);
    }

    
}
