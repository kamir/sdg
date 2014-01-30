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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.json.simple.*;

/**
 *
 * @author kamir
 */
public class IndexDataLoader {

    static final String[] seriesTypes = {
        "Adj_Close", "Close", "High", "Low", "Open", "Volume", "logReturn"
    };

    private void initLocalCache() throws FileNotFoundException, IOException {
        _loadListe();
        loadDataFromFile();
    }
    // namens liste ...
    Vector<String> liste = new Vector<String>();
    Hashtable<String, String> _hash = new Hashtable<String, String>();
    Hashtable<String, Messreihe> hashMR = new Hashtable<String, Messreihe>();
    /**
     * Here we select, what list should be prepared ...
     *
     */
    // static String label = "UK.csv"; // "dowjonesmap.dat";//"daxmap.dat";//
    static String label = "global_indexes_v2.csv"; // "dowjonesmap.dat";//"daxmap.dat";//
    String stock = "MSFT";
    String startDate = "2007-01-01";
    String endDate = "2013-12-31";
    String column = "Adj_Close";

    public static IndexDataLoader getLocalLoader(String from, String to, String label) throws FileNotFoundException, IOException {
        IndexDataLoader l = new IndexDataLoader();
        l.startDate = from;
        l.endDate = to;
        l.label = label;
        l.initLocalCache();
        return l;
    }

    public static IndexDataLoader getOnlineLoader(String column, String from, String to, String label) throws FileNotFoundException, IOException {
        IndexDataLoader l = new IndexDataLoader();
        l.startDate = from;
        l.endDate = to;
        l.label = label;
        l.initCacheFromWeb(column);
        return l;
    }
    
    static String year = "2009";

    public static void main(String[] arg) throws IOException {
        
        /**
         * 
         * Modify:     callURL in loadForSymbol !!!!         
         * 
         * 
         * 
         */
         year = "2013";

        vmr = new Vector<Messreihe>();
        String column1 = "Close";
//        String column = "logReturn";
        IndexDataLoader sdl0 = IndexDataLoader.getOnlineLoader(column1, year + "-01-01", year +"-12-31", label);
        sdl0.showCharts();
        sdl0.showChartsNormalized();

       
        
        System.out.println("Bad data ... ");
        int c = 0;
        for (String s : _bad) {
            System.out.println("#(" + c + ") " + s);
        }       
        
        for (String s : _log) {
            System.out.println("*(" + c + ") " + s);
        }  
        
        System.exit( 0 );
        
        
        vmr = new Vector<Messreihe>();
        String column2 = "Volume";
//        String column = "logReturn";
        IndexDataLoader sdl1 = IndexDataLoader.getOnlineLoader(column2,  year + "-01-01",  year + "-12-31", label);
        sdl1.showCharts();
        sdl1.showChartsNormalized();

        System.out.println("Bad data ... ");
        for (String s : _bad) {
            System.out.println("(" + c + ") " + s);
        }

//   

//        IndexDataLoader sdl = IndexDataLoader.getOnlineLoader(column, "2012-01-01", "2012-12-31", label);
//        sdl.initColumn("logReturn");
//        sdl.showCharts();
//        sdl.showChartsNormalized();

        IndexDataLoader sdl2 = IndexDataLoader.getLocalLoader("2007-01-01", "2013-12-31", label);
        sdl2.initColumn( "logReturn" );
        sdl2.showCharts();
        sdl1.showChartsNormalized();
//        
//        IndexDataLoader sdl3 = IndexDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
//        sdl3.initColumn( "High" );
//        sdl3.showCharts();
//        
//        IndexDataLoader sdl4 = IndexDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
//        sdl4.initColumn( "Low" );
//        sdl4.showCharts();
//        
//        IndexDataLoader sdl5 = IndexDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
//        sdl5.initColumn( "Open" );
//        sdl5.showCharts();
//        
//        IndexDataLoader sdl6 = IndexDataLoader.getLocalLoader("2012-01-01", "2012-12-31", label);
//        sdl6.initColumn( "Volume" );
//        sdl6.showCharts();
    }
    static Vector<Messreihe> vmr = new Vector<Messreihe>();

    public void loadForSymbol(String key, String selectedColumn, BufferedWriter bw) throws IOException {

        // String callUrl = "http://query.yahooapis.com/v1/public/yql?q=select * from yahoo.finance.historicaldata where symbol in (" + key + "\") and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"&diagnostics=true&env=http://datatables.org/alltables.env&format=json";

        String callUrl = "http://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20yahoo.finance.historicaldata%20where%20symbol%20in%20%28%22" + key + "%22%29%20and%20startDate=%22"+year+"-01-01%22%20and%20endDate=%22"+year+"-12-31%22&diagnostics=true&env=http://datatables.org/alltables.env&format=json";

        System.out.println(callUrl);


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
            String key1 = key.replace("%5E", "^");
            mr.setLabel(key1 + "_" + selectedColumn);

            Hashtable<Long, Double> data = new Hashtable<Long, Double>();

            while (i < qt.size()) {
                JSONObject val = (JSONObject) qt.get(i);
                String b = (String) val.get(selectedColumn);
                Date date = null;
                try {
                    date = parseDate("Date", val);
                } catch (Exception ex) {
                    try {
                        date = parseDate("date", val);
                    } catch (Exception ex2) {
                        System.err.println(ex2.getMessage());
                    }
                }

                data.put(date.getTime(), Double.parseDouble(b));

                System.out.println(key1 + " : " + date.getTime() + " # " + " : " + b);
                System.out.println("{" + qt.get(i) + "}");
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
            
            _log.add("> " + symbol + " # " + mr.yValues.size() );

        } catch (Exception pe) {
//            Logger.getLogger(IndexDataLoader.class.getName()).log(Level.SEVERE, null, pe);

            System.out.println(pe.getMessage());

            _bad.add("> " + symbol + " can not be loaded.");
        }
    }
    
    static Vector<String> _bad = new Vector<String>();
    static Vector<String> _log = new Vector<String>();
    /**
     * In directory ./DATA/ we assume to find a file called ${label} in which
     * all Index codes are listed.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void _loadListe() throws FileNotFoundException, IOException {
        String file = "./DATA/" + label;
        FileReader fr = new FileReader(new File(file));
        BufferedReader br = new BufferedReader(fr);
        int c = 0;
        while (br.ready()) {
            String line = br.readLine();

            if (line.startsWith("#")) {
            } else {

                StringTokenizer st = new StringTokenizer(line, ",");
                System.out.println(st.countTokens() + ":" + line);
                String tok1 = st.nextToken();
                String tok2 = st.nextToken();
                System.out.println("(" + tok1 + ") [" + tok2 + "]");

                _hash.put(tok1, tok2);

                if (!liste.contains(tok2)) {
                    liste.add(tok2);
                }
            }
        }
//        hash.put("Visa_Inc.","V");
//        hash.put("Walmart","WMT");
//        hash.put("The_Walt_Disney_Company","DIS");


        System.out.println("> list loaded ... " + _hash.size() + " from: " + file);
//        javax.swing.JOptionPane.showMessageDialog(new JFrame(), "indices: " + liste.size());
    }

    private void loadDataFromWeb() throws IOException {
        loadDataFromWeb("Adj_Close");
    }
    Hashtable<String, String> temp = null;

    private void loadDataFromWeb(String column) throws IOException {
        temp = new Hashtable<String, String>();

        int i = 1;

        FileWriter fw = new FileWriter(getFilename());

        BufferedWriter bw = new BufferedWriter(fw);


        for (String s : _hash.values()) {
            try {

                symbol = s;
                System.out.println(i + ") SYMBOL: " + s);

                loadForSymbol(s, column, bw);

//            javax.swing.JOptionPane.showInputDialog( _hash.size() + " done ... ("+s+")");
                i++;
            } catch (Exception ex) {
                System.err.println(">>> failed to load " + s);
            }
        }

        bw.flush();
        bw.close();

    }
    String symbol = "";

    private void initCacheFromWeb(String col) throws FileNotFoundException, IOException {
        column = col;
        _loadListe();
        loadDataFromWeb(column);
    }

    private void loadDataFromFile() throws FileNotFoundException, IOException {

        temp = new Hashtable<String, String>();
        vmr = new Vector<Messreihe>();
        BufferedReader br = new BufferedReader(new FileReader(getFilename()));
        while (br.ready()) {
            String line = br.readLine();
            StringTokenizer st = new StringTokenizer(line, "\t");
            int s = st.countTokens();
            String k = st.nextToken();
            String req = st.nextToken();
            String resp = st.nextToken();
            System.out.println(s + " :" + k + "\n\t" + req + "\n\t\t" + resp);
            temp.put(k, resp);
        }
        System.out.println("> responses reloaded ...");
    }

    private void showChartsNormalized() {
        Vector<Messreihe> v = new Vector<Messreihe>();
        for (Messreihe m : vmr) {
            v.add(m.normalizeToStdevIsOne());
        }
        MultiChart.open(v, label + " (norm) " + "[" + startDate + " ... " + endDate + "]", "t", column, true);
    }

    /**
     * final Vector<Messreihe> mrs,
            final String string, final String x, final String y,
            final boolean b, final String folder, final String filename,
            String comment)
     * 
     */
    private void showCharts() {
        String badData = "?";
        MultiChart.open(vmr, label + "[" + startDate + " ... " + endDate + "]", "t", column, true);
        MultiChart.openAndStore(vmr, stock, "t", column, true, "." , label + "_" + startDate + "___" + endDate + "_" + column, badData );
        
    }

    public String getFilename() {
        return "index.data.collection." + this.startDate + "_" + this.endDate + "_" + label;
    }

    private void initColumn(String col) {
        column = col;
        boolean doPostprocessToLogReturn = false;
        if (column == "logReturn") {
            doPostprocessToLogReturn = true;
            column = "Close";
            System.out.println(">>> need some processing ... ");
        }


        for (String key : temp.keySet()) {
            Messreihe mr = new Messreihe();
            try {
                String s = temp.get(key);

                Object obj = JSONValue.parse(s);
                JSONObject jsonObject = (JSONObject) obj;
                JSONObject query = (JSONObject) jsonObject.get("query");
                JSONObject res = (JSONObject) query.get("results");
                JSONArray qt = (JSONArray) res.get("quote");

                // System.out.println("*** [" + qt + "]");

                String s2 = res.toJSONString();
                int i = 0;


                String key1 = key.replace("%5E", "^");
                mr.setLabel(key1 + "_" + column);

                Hashtable<Long, Double> data = new Hashtable<Long, Double>();

                while (i < qt.size()) {
                    try {
                        JSONObject val = (JSONObject) qt.get(i);


                        String b = (String) val.get(column);
                        String a = (String) val.get("Date");

                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");


                        Date date = parseDate("Date", val);
                        System.out.println(">>>>> " + column + " : " + date.getTime() + " # " + a + " : " + b);
                        System.out.println(">>>>>" + "{" + qt.get(i) + "}");

                        data.put(date.getTime(), Double.parseDouble(b));



                    } catch (java.text.ParseException ex) {
                        Logger.getLogger(IndexDataLoader.class.getName()).log(Level.SEVERE, null, ex);
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


                if (doPostprocessToLogReturn) {
                    mr = calcLogReturn(mr);
                }

                vmr.add(mr);


            } catch (Exception ex) {
                System.err.println("Failed: " + mr.label);
                ex.printStackTrace();
            }
        }

    }

    public Date parseDate(String date, JSONObject val) throws java.text.ParseException {
        String a = (String) val.get(date);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date d = df.parse(a);
        return d;
    }

    private Messreihe calcLogReturn(Messreihe mr) {

        int i = 0;

        Messreihe logReturn = new Messreihe();

        logReturn.setLabel(mr.getLabel() + "_LR");
        logReturn.addValuePair(1, 0);

        Vector<Double> close = mr.xValues;
        Enumeration<Double> d = close.elements();
        double last = d.nextElement();
        double lnLast = 0.0;

        try {
            lnLast = Math.log10(last);
        } catch (Exception ex) {
            lnLast = 0.0;
        }

        while (d.hasMoreElements()) {

            i++;

            double ln = 0.0;

            try {
                ln = Math.log10(d.nextElement());
            } catch (Exception ex) {
                ln = 0.0;
            }

            double logR = ln - lnLast;

            logReturn.addValuePair(i, logR);

            lnLast = ln;
        }

        return logReturn;
    }
}
