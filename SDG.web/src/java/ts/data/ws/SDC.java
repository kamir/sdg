/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ts.data.ws;

import java.util.Vector;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.ejb.Stateless;
 

/**
 *
 * @author kamir
 */
@WebService(serviceName = "SDC")
@Stateless()
public class SDC {

    Vector<String> symbols;
    Vector<String> rows;
    
    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "init")
    public String init(@WebParam(name = "name") String txt) {
        symbols = new Vector<String>();
        rows = new Vector<String>();
        return "Hello " + txt + " !";
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "insertNewRow")
    public String insertNewRow(@WebParam(name = "row") String row, @WebParam(name = "symbol") String symbol) {
        symbols.add(symbol);
        rows.add(row);
        
//        
//        String settings = "{\"hello\": \"world\"}";
//        JSONObject obj = (JSONObject)JSONSerializer.toJSON(settings);
//        System.out.println(obj.toString());
//        
        
        
        return "# of symbols:" + symbols.size();
    }
}
