package Parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.*;
import java.util.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class StarParser extends DefaultHandler {

    List<Star> myStars;

    private String tempVal;


    private Star tempSt;

    private int startStarId;

    List<String> starCheckingList;

    Map<String,String> starMap =  new HashMap<String, String>();




    public StarParser() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // connect
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");


        String query = "select max(substring(id, 3)) as startId from stars";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery(query);
        rs.next();
        startStarId = Integer.parseInt(rs.getString("startId"));


        myStars = new ArrayList<Star>();
        starCheckingList = new ArrayList<String>();
    }

    public void runExample() throws  IOException{
        parseDocument();
        writeStars();
        writeStarInMovie();
//        printData();
    }

    private void writeStars() throws IOException{
        PrintWriter writer = new PrintWriter("stars.txt", "UTF-8");
        for(Star s : myStars){
            for(String starname : s.getStarNames()){
                if(!starCheckingList.contains(starname)){
                    startStarId += 1;
                    starMap.put(starname, String.format("nm%07d", startStarId));
                    starCheckingList.add(starname);
                    writer.printf("%s,%s, %s\n", String.format("nm%07d", startStarId), starname, "NULL");
                }
            }
        }
        writer.close();
    }

    private void writeStarInMovie() throws IOException{
        PrintWriter writer = new PrintWriter("stars_in_movies.txt", "UTF-8");
        for(Star s : myStars){
            for(String starname : s.getStarNames()){
                writer.printf("%s,%s\n", starMap.get(starname), s.getId());
            }
        }
        writer.close();
    }
    private void parseDocument() {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            SAXParser sp = spf.newSAXParser();

            sp.parse("src/Parser/casts124.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void printData() {

        System.out.println("No of Movie '" + myStars.size() + "'.");

        for (Star myMovie : myStars) {
            System.out.println(myMovie.toString());
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("filmc")) {
            tempSt = new Star();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("f")) {
            tempSt.setId(tempVal);
        }
        if (qName.equalsIgnoreCase("t")) {
            tempSt.setName(tempVal);
        }
        if (qName.equalsIgnoreCase("a")) {
            tempSt.addStar(tempVal.strip());
        }
        if (qName.equalsIgnoreCase("filmc")) {
            myStars.add(tempSt);
        }
    }
}