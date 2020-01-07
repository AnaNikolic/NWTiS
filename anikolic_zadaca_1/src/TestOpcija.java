/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 * @author anik
 */
public class TestOpcija {

    /**
     * @param args the command line arguments
     */
	 
    public static void main(String[] args) {
	
	// -server -konf datoteka(.txt | .xml) [-load]
        String sintaksa = "^([^\\s]+\\.(?i)txt|xml|bin|json)$";
                                                                   
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]).append(" ");
        }
        String p = sb.toString().trim();
        Pattern pattern = Pattern.compile(sintaksa);
        Matcher m = pattern.matcher(p);
        boolean status = m.matches();
        if (status) {
            int poc = 0;
            int kraj = m.groupCount();
            for (int i = poc; i <= kraj; i++) {
                System.out.println(i + ". " + m.group(i));
            }
        } else {
			System.out.println("Ne odgovara!");
		}
    }
}
