package Parser;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

public class MovieParser extends DefaultHandler {

    List<Movie> myMovies;

    private String tempDr;

    private String tempVal;

    //to maintain context
    private Movie tempMv;

    private Genre tempGe;


    private int startGenreId;

    List<String> movieCheckingList;

    Map<String,String> movieMap =  new HashMap<String, String>();
    Map<String,Integer> genreMap =  new HashMap<String, Integer>();

    List<String> genreCheckingList;

    FileWriter fileWriter;


    public MovieParser() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, FileNotFoundException {
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb", "mytestuser", "My6$Password");


        String query = "select max(id) as startId from genres";
        PreparedStatement statement = conn.prepareStatement(query);
        ResultSet rs = statement.executeQuery(query);
        rs.next();
        startGenreId = Integer.parseInt(rs.getString("startId"));

        myMovies = new ArrayList<Movie>();
        movieCheckingList = new ArrayList<String>();
        genreCheckingList = new ArrayList<String>();
        try{
            fileWriter = new FileWriter("MysqlFiles/inconsistent.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void runExample() throws  IOException{
        parseDocument();
        writeMovie();
        writeGenre();
        writeGenreInMovie();
        fileWriter.close();
//        printData();
    }

    private void writeMovie() throws IOException{
        PrintWriter writer = new PrintWriter("movies.txt");
        for(Movie m : myMovies){
            String movie = String.format("%s,%d,%s\n",m.getName(),m.getYear(),m.getDirector());
            if(m.getDirector() == null){
                System.out.println("Inconsistent movie: no director named");
                String message = String.format("Inconsistent movie: no director named in Movie: %s\n", m.getName());
                try {
                    fileWriter.write(message);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                continue;
            }
            if(!movieCheckingList.contains(movie)){
                writer.printf("%s,%s,%d,%s\n", m.getId(),m.getName(),m.getYear(),m.getDirector());
                movieCheckingList.add(movie);
                movieMap.put(movie, m.getId());
            }
        }
        writer.close();
    }
    private void writeGenre() throws IOException{
        PrintWriter writer = new PrintWriter("genres.txt");
        for(Movie m : myMovies){
            if(m.getDirector() == null){
                continue;
            }
            for(Genre g : m.getGenreList()){
                String genre = String.format("%s\n", g.getName());
                if(!genreCheckingList.contains(genre)){
                    startGenreId += 1;
                    g.setId(startGenreId);
                    writer.printf("%s,%s\n", g.getId(), g.getName());
                    genreCheckingList.add(genre);
                    genreMap.put(g.getName(), g.getId());
                }
            }
        }
        writer.close();
    }

    private void writeGenreInMovie() throws IOException{
        PrintWriter writer = new PrintWriter("genres_in_movies.txt");
        for(Movie m : myMovies){
            if(m.getDirector() == null){
                continue;
            }
            String movie = String.format("%s,%d,%s\n",m.getName(),m.getYear(),m.getDirector());
            for(Genre g : m.getGenreList()){
                writer.printf("%d,%s\n",genreMap.get(g.getName()),movieMap.get(movie));
            }
        }
        writer.close();
    }
    private void parseDocument() {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            SAXParser sp = spf.newSAXParser();

            // change to mains243.xml !!
            sp.parse("src/Parser/mains243.xml", this);

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

        System.out.println("No of Movie '" + myMovies.size() + "'.");

        for (Movie myMovie : myMovies) {
            System.out.println(myMovie.toString());
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
        if (qName.equalsIgnoreCase("film")) {
            tempMv = new Movie();
        }
        if (qName.equalsIgnoreCase("cat")) {
            tempGe = new Genre();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("directorfilms")) {
            tempDr = null;
        }
        if (qName.equalsIgnoreCase("dirn")) {
            tempDr = tempVal;
        }
        if (qName.equalsIgnoreCase("cat")) {
            for(String temp : tempVal.split(" ")){
                boolean hasNonAlpha = temp.matches("^.*[^a-zA-Z0-9 ].*$");
                if(!hasNonAlpha && !temp.equals("")){
                    tempGe.setName(temp.toLowerCase());
                    tempMv.addGenre(tempGe);
                    tempGe = new Genre();
                }
                else{
                    System.out.printf("Inconsistent Genre: %s\n", temp);
                    String message = String.format("Inconsistent Genre: %s\n", temp);
                    try {
                        fileWriter.write(message);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
        if (qName.equalsIgnoreCase("film")) {
            tempMv.setDirector(tempDr);
            myMovies.add(tempMv);
        }
        if(qName.equalsIgnoreCase("fid")){
            tempMv.setId(tempVal);
        }
        if (qName.equalsIgnoreCase("t")) {
            tempMv.setName(tempVal);
        }
        if (qName.equalsIgnoreCase("year")) {
            try{
                tempMv.setYear(Integer.parseInt(tempVal));
            }
            catch (Exception e){
                System.out.printf("Inconsistent Year: %s\n", tempVal);
                String message = String.format("Inconsistent Year: %s\n", tempVal);
                try {
                    fileWriter.write(message);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                tempMv.setYear(0000);
            }
        }
    }
}
