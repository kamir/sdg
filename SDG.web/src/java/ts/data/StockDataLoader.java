package ts.data;

import chart.simple.MultiChart;
import data.series.Messreihe;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author kamir
 */
public class StockDataLoader {

    private void initLocalCache() throws FileNotFoundException, IOException {
        loadListe();
        loadDataFromFile();    
    }
    // namens liste ...
    Vector<String> liste = new Vector<String>();
    Hashtable<String, String> hash = new Hashtable<String, String>();
    Hashtable<String, Messreihe> hashMR = new Hashtable<String, Messreihe>();
    
    static String label = "UK.csv"; // "dowjonesmap.dat";//"daxmap.dat";//
    
    String stock = "MSFT";
    String startDate = "2009-01-01";
    String endDate = "2009-12-31";
    String column = "Adj_Close";

    public static StockDataLoader getLocalLoader(String from, String to, String label) throws FileNotFoundException, IOException {
        StockDataLoader l = new StockDataLoader();
        l.startDate = from;
        l.endDate = to;
        l.label = label;
        l.initLocalCache();
        return l;
    }

    public static StockDataLoader getOnlineLoader(String column, String from, String to, String label) throws FileNotFoundException, IOException {
        StockDataLoader l = new StockDataLoader();
        l.startDate = from;
        l.endDate = to;
        l.label = label;
        l.initCacheFromWeb( column );
        return l;
    }

    public static void main(String[] arg) throws IOException {

        String column = "Close";
        StockDataLoader sdl0 = StockDataLoader.getOnlineLoader( column, "2012-01-01", "2012-12-31", label );
        sdl0.showCharts();
        
        for( String s : bad ) { 
            System.out.println( s );
        }
//        
//        System.exit( 0 );
        
        StockDataLoader sdl = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl.initColumn( "Close" );
        sdl.showCharts();
        
        StockDataLoader sdl2 = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl2.initColumn( "Adj_Close" );
        sdl2.showCharts();
        
        StockDataLoader sdl3 = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl3.initColumn( "High" );
        sdl3.showCharts();
        
        StockDataLoader sdl4 = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl4.initColumn( "Low" );
        sdl4.showCharts();
        
        StockDataLoader sdl5 = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl5.initColumn( "Open" );
        sdl5.showCharts();
        
        StockDataLoader sdl6 = StockDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
        sdl6.initColumn( "Volume" );
        sdl6.showCharts();
    }
    static Vector<Messreihe> vmr = new Vector<Messreihe>();

    public void loadForSymbol(String key, String selectedColumn, BufferedWriter bw) throws IOException {

        // String callUrl = "http://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.historicaldata where symbol in (" + key + "\") and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"&diagnostics=true&env=http://datatables.org/alltables.env&format=json";
        
        String callUrl = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20in%20%28%22" + key + "%22%29%20and%20startDate=%222009-01-01%22%20and%20endDate=%222009-12-31%22&diagnostics=true&env=http://datatables.org/alltables.env&format=json";

        System.out.println( callUrl ); 
        
        
        bw.write(key + "\t");
        bw.write(callUrl + "\t");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(callUrl);

        HttpResponse response = httpClient.execute(httpget);

        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());

        BufferedReader br = new BufferedReader(
                new InputStreamReader((response.getEntity().getContent())));

        StringBuffer buffer = new StringBuffer();

        String output;
        System.out.println("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            System.out.println(output);
            buffer = buffer.append(output);
        }
        System.out.println("...");

        try {
            String s = buffer.toString();
            temp.put(key, s);

            bw.write(s + "\n");

            Object obj = JSONValue.parse(s);
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject query = (JSONObject) jsonObject.get("query");
            JSONObject res = (JSONObject) query.get("results");
            JSONArray qt = (JSONArray) res.get("quote");

            System.out.println("*** [" + qt + "]");

            String s2 = res.toJSONString();
            int i = 0;

            Messreihe mr = new Messreihe();
            mr.setLabel(key + "_" + selectedColumn);

            Hashtable<Long, Double> data = new Hashtable<Long, Double>();

            while (i < qt.size()) {
                JSONObject val = (JSONObject) qt.get(i);
                String b = (String) val.get( selectedColumn );
                String a = (String) val.get("date");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                Date date = df.parse(a);

                data.put(date.getTime(), Double.parseDouble(b));

                System.out.println( date.getTime() + " # " + a + " : " + b );
                System.out.println( "{"+qt.get(i)+"}" );
                i++;
            }

            Set<Long> k = data.keySet();
            ArrayList<Long> liste = new ArrayList<Long>();
            liste.addAll(k);

            Collections.sort(liste);

            for (Long key2 : k) {
                System.out.println(key2 + " : " + data.get(key2));
                mr.addValuePair(key2, data.get(key2));
            }

            vmr.add(mr);

        } 
        catch (Exception pe) {
            Logger.getLogger(StockDataLoader.class.getName()).log(Level.SEVERE, null, pe);

            System.out.println(pe.getMessage());

            bad.add( symbol );
        }
    }
    
    static Vector<String> bad = new Vector<String>();

    private void loadListe() throws FileNotFoundException, IOException {
        String file = "./DATA/" + label;
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        while (br.ready()) {
            String line = br.readLine();

            if (line.startsWith("#")) {
            } else {
                
                StringTokenizer st = new StringTokenizer(line, "\t");
                System.out.println(st.countTokens() + ":" + line );
                String tok1 = st.nextToken();
                String tok2 = st.nextToken();
                System.out.println("(" + tok1 + ") [" + tok2 + "]");

                hash.put(tok1, tok2);
 
                if (!liste.contains(tok2)) {
                    liste.add(tok2);
                }
            }
        }
//        hash.put("Visa_Inc.","V");
//        hash.put("Walmart","WMT");
//        hash.put("The_Walt_Disney_Company","DIS");
        
       
        System.out.println( "> list loaded ... ");
    }

    private void loadDataFromWeb() throws IOException {
        loadDataFromWeb( "Adj_Close" );
    }
    
    Hashtable<String,String> temp = null;
    
    private void loadDataFromWeb( String column ) throws IOException {
        temp = new Hashtable<String,String>();
    
        int i = 1;

        FileWriter fw = new FileWriter(getFilename());

        BufferedWriter bw = new BufferedWriter(fw);

        
        for (String s : hash.values()) {
            symbol = s;
            System.out.println(i + ") SYMBOL: " + s);
            loadForSymbol(s, column, bw);
        
            //javax.swing.JOptionPane.showInputDialog("go");
            i++;
        }

        bw.flush();
        bw.close();

    }
    
    String symbol = "";

    private void initCacheFromWeb(String col) throws FileNotFoundException, IOException {
        column = col;
        loadListe();
        loadDataFromWeb( column );       
    }

    private void loadDataFromFile() throws FileNotFoundException, IOException {
        
        temp = new Hashtable<String,String>();
        vmr = new Vector<Messreihe>();
        BufferedReader br = new BufferedReader( new FileReader( getFilename() ) );
        while( br.ready() ) { 
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer( line,"\t");
            int s = st.countTokens();
            String k = st.nextToken();
            String req = st.nextToken();
            String resp = st.nextToken();
            System.out.println(s + " :" + k + "\n\t" + req + "\n\t\t" + resp );
            temp.put(k, resp);
        }
        System.out.println("> responses reloaded ...");
    }

    private void showCharts() {
       MultiChart.open(vmr, label + "[" + startDate + " ... " + endDate + "]",  "t", column ,true);
    }

    public String getFilename() {
        return "stockdata.collection." + this.startDate +"_"+ this.endDate + "_" + label;
    }

    private void initColumn(String col) {
        column = col;
        for( String key : temp.keySet() ) {
            String s = temp.get(key);

            Object obj = JSONValue.parse(s);
            JSONObject jsonObject = (JSONObject) obj;
            JSONObject query = (JSONObject) jsonObject.get("query");
            JSONObject res = (JSONObject) query.get("results");
            JSONArray qt = (JSONArray) res.get("quote");

            // System.out.println("*** [" + qt + "]");

            String s2 = res.toJSONString();
            int i = 0;

            Messreihe mr = new Messreihe();
            mr.setLabel(key + "_" + column);

            Hashtable<Long, Double> data = new Hashtable<Long, Double>();

            while (i < qt.size()) {
               try { 
                   JSONObject val = (JSONObject) qt.get(i);
                String b = (String) val.get( column );
                String a = (String) val.get("date");

                DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                Date date;
                
                    date = df.parse(a);
                

                data.put(date.getTime(), Double.parseDouble(b));

                //System.out.println( date.getTime() + " # " + a + " : " + b );
                //System.out.println( "{"+qt.get(i)+"}" );
                
                } catch (java.text.ParseException ex) {
                    Logger.getLogger(StockDataLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
               i++;
            }
                

            Set<Long> k = data.keySet();
            ArrayList<Long> liste = new ArrayList<Long>();
            liste.addAll(k);

            Collections.sort(liste);

            for (Long key2 : k) {
                //System.out.println(key2 + " : " + data.get(key2));
                mr.addValuePair(key2, data.get(key2));
            }

            vmr.add(mr);

        
        }
    }
}
