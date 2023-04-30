import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

@WebServlet(name = "MovieSearchServlet", urlPatterns = "/api/movies-search")
public class MovieSearchServlet extends HttpServlet{
    private static final long serialVersionUID = 7L;
    private DataSource dataSource;
    private String contextPath;

    public void init(ServletConfig config) {
        try{
            super.init(config);
        }catch(Exception e){}
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            contextPath = getServletContext().getRealPath("/");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        long startTimeTS = System.nanoTime();
        long elapsedTimeTJ = -1;
        long elapsedTimeTS = -1;

        HttpSession session = request.getSession();

        String jump = request.getParameter("jump");
        String title;
        String year;
        String Searchdirector;
        String star;
        String sortingOrder;
        String titleOrdering;
        String ratingOrdering;
        String limit;
        String offset;
        if(jump != null){
            title = request.getParameter("title");
            year = request.getParameter("year");
            Searchdirector = request.getParameter("director");
            star = request.getParameter("star");
            sortingOrder = request.getParameter("sorting");
            titleOrdering = request.getParameter("order1");
            ratingOrdering = request.getParameter("order2");
            limit = request.getParameter("limit");
            offset = request.getParameter("offset");
            session.setAttribute("title", title);
            session.setAttribute("year", year);
            session.setAttribute("director", Searchdirector);
            session.setAttribute("star", star);
            session.setAttribute("sorting", sortingOrder);
            session.setAttribute("order1", titleOrdering);
            session.setAttribute("order2", ratingOrdering);
            session.setAttribute("limit", limit);
            session.setAttribute("offset", offset);
        }
        else {
            title = (String)session.getAttribute("title");
            year = (String)session.getAttribute("year");
            Searchdirector = (String)session.getAttribute("director");
            star = (String)session.getAttribute("star");
            sortingOrder = (String)session.getAttribute("sorting");
            titleOrdering = (String)session.getAttribute("order1");
            ratingOrdering= (String)session.getAttribute("order2");
            limit = (String)session.getAttribute("limit");
            offset = (String)session.getAttribute("offset");
        }
        JsonObject temp = new JsonObject();
        temp.addProperty("sorting", sortingOrder);
        temp.addProperty("order1", titleOrdering);
        temp.addProperty("order2", ratingOrdering);
        temp.addProperty("limit", limit);
        temp.addProperty("offset", offset);
        temp.addProperty("moviesearch", "yes");


//        title = request.getParameter("title");
        int fuzzyLength;
        if(title.length() >=4){
            fuzzyLength = 2;
        }
        else if(title.length() == 3){
            fuzzyLength = 1;
        }
        else if(title.length() == 2){
            fuzzyLength = 0;
        }
        else{
            fuzzyLength = 0;
        }
        String temp2 = "";
        String temp3 = title;
        if(title == ""){
            title = "%";
        }
        else{
            for(String token : title.strip().split(" ")){
                temp2 = temp2 +  "+" + token + "*,";
            }
            temp2 = temp2.substring(0, temp2.length() - 1);
        }
        title = temp2;

//        year = request.getParameter("year");

//        Searchdirector = request.getParameter("director");
        if(Searchdirector == ""){
            Searchdirector = "%";
        }
        else{
            Searchdirector = "%" + Searchdirector + "%";
        }

//        star = request.getParameter("star");
        if(star == ""){
            star = "%";
        }
        else{
            star = "%" + star + "%";
        }

//        sortingOrder = request.getParameter("sorting");

//        orderingDirection = request.getParameter("order");
        String finalOrdering;
        if(Objects.equals(sortingOrder, "titleRating")){
            finalOrdering = "M.title "+titleOrdering+", M.rating "+ ratingOrdering;
        }
        else{
            finalOrdering = "M.rating "+ratingOrdering+", M.title "+ titleOrdering;
        }
//        limit = request.getParameter("LIMIT");
        if(limit == null || limit == "NaN"){
            limit = "10";
        }
//        offset = request.getParameter("OFFSET");
        if(offset == null || offset == "NaN"){
            offset = "0";
        }

        request.getServletContext().log("getting title: " + title + "getting genre" + Searchdirector + "getting year" + year + "getting star" + star);
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            long startTimeTJ = System.nanoTime();
            String query;
            String countQuery;
            PreparedStatement statement;
            PreparedStatement statement2;
            if(year == ""){
                query = "with selectedStars as (SELECT x.id, x.name, total\n" +
                        "FROM stars x \n" +
                        "LEFT JOIN (SELECT starId, COUNT(*) total FROM stars_in_movies GROUP BY starId) y ON y.starId = x.id\n" +
                        "ORDER BY total DESC, id), \n" +
                        "SelectedTitleANDRating AS (\n" +
                        "SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m left join ratings r ON m.id = r.movieId\n" +
                        "where match (m.title) against (? In boolean mode) or (edth(?, lower(m.title), "+ fuzzyLength + ") AND length(m.title) = length(?))\n"+
                        "),\n" +
                        "movie_genres as(\n" +
                        "SELECT gm.movieId, GROUP_CONCAT(g.name order by g.name asc) as genres \n" +
                        "FROM genres_in_movies gm\n" +
                        "INNER JOIN genres g ON g.id = gm.genreId\n" +
                        "GROUP BY movieId\n" +
                        ")\n" +
                        "SELECT M.title,M.year,M.director,M.id as movieId,M.rating,GM.genres as genres,star_list.starId as starId, star_list.starName as starName\n" +
                        "FROM SelectedTitleANDRating as M inner join movie_genres as GM on GM.movieId = M.id \n" +
                        "inner join (select sm.movieId, GROUP_CONCAT(s.id order by s.total desc, s.name) as starId,\n" +
                        "GROUP_CONCAT(s.name order by s.total desc, s.name) as starName from stars_in_movies sm \n" +
                        "INNER JOIN selectedStars as s ON s.id = sm.starId where s.name like ? GROUP BY \n" +
                        "movieId) as star_list on star_list.movieId = M.id\n" +
                        "WHERE director like ?\n" +
                        "order by " + finalOrdering + "\n" +
                        "LIMIT " + limit + "\n" +
                        "OFFSET " + offset;
                statement = conn.prepareStatement(query);
                statement.setString(1, title);
                statement.setString(2, temp3.toLowerCase());
                statement.setString(3, temp3.toLowerCase());
                statement.setString(4, star);
                statement.setString(5, Searchdirector);

                countQuery = "with selectedStars as (SELECT x.id, x.name, total\n" +
                        "FROM stars x\n" +
                        "LEFT JOIN (SELECT starId, COUNT(*) total FROM stars_in_movies GROUP BY starId) y ON y.starId = x.id\n" +
                        "ORDER BY total DESC, id), \n" +
                        "SelectedTitleANDRating AS (\n" +
                        "SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m left join ratings r ON m.id = r.movieId\n" +
                        "where match (m.title) against (? In boolean mode) or (edth(?, lower(m.title), "+ fuzzyLength + ") AND length(m.title) = length(?))\n"+
                        "),  final as(\n" +
                        "SELECT M.title\n" +
                        "FROM SelectedTitleANDRating as M\n" +
                        "inner join (select sm.movieId from stars_in_movies sm\n" +
                        "INNER JOIN selectedStars as s ON s.id = sm.starId where s.name like ? GROUP BY \n" +
                        "movieId) as star_list on star_list.movieId = M.id WHERE director like ?)\n" +
                        "select count(*) as total\n" +
                        "from final";
                statement2 = conn.prepareStatement(countQuery);
                statement2.setString(1, title);
                statement2.setString(2, temp3.toLowerCase());
                statement2.setString(3, temp3.toLowerCase());
                statement2.setString(4, star);
                statement2.setString(5, Searchdirector);
            }
            else{
                int year2 = Integer.parseInt(year);
                query = "with selectedStars as (SELECT x.id, x.name, total\n" +
                        "FROM stars x \n" +
                        "LEFT JOIN (SELECT starId, COUNT(*) total FROM stars_in_movies GROUP BY starId) y ON y.starId = x.id\n" +
                        "ORDER BY total DESC, id), \n" +
                        "SelectedTitleANDRating AS (\n" +
                        "SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m left join ratings r ON m.id = r.movieId\n" +
                        "where match (m.title) against (? In boolean mode) or (edth(?, lower(m.title), "+ fuzzyLength + ") AND length(m.title) = length(?))\n"+
                        "),\n" +
                        "movie_genres as(\n" +
                        "SELECT gm.movieId, GROUP_CONCAT(g.name order by g.name asc) as genres \n" +
                        "FROM genres_in_movies gm\n" +
                        "INNER JOIN genres g ON g.id = gm.genreId\n" +
                        "GROUP BY movieId\n" +
                        ")\n" +
                        "SELECT M.title,M.year,M.director,M.id as movieId,M.rating,GM.genres as genres,star_list.starId as starId, star_list.starName as starName\n" +
                        "FROM SelectedTitleANDRating as M inner join movie_genres as GM on GM.movieId = M.id \n" +
                        "inner join (select sm.movieId, GROUP_CONCAT(s.id order by s.total desc, s.name) as starId,\n" +
                        "GROUP_CONCAT(s.name order by s.total desc, s.name) as starName from stars_in_movies sm \n" +
                        "INNER JOIN selectedStars as s ON s.id = sm.starId where s.name like ? GROUP BY \n" +
                        "movieId) as star_list on star_list.movieId = M.id\n" +
                        "WHERE year = ? AND director like ?\n" +
                        "order by " + finalOrdering + "\n" +
                        "LIMIT " + limit + "\n" +
                        "OFFSET " + offset;
                statement = conn.prepareStatement(query);
                statement.setString(1, title);
                statement.setString(2, temp3.toLowerCase());
                statement.setString(3, temp3.toLowerCase());
                statement.setString(4, star);
                statement.setInt(5, year2);
                statement.setString(6, Searchdirector);

                countQuery = "with selectedStars as (SELECT x.id, x.name, total\n" +
                        "FROM stars x\n" +
                        "LEFT JOIN (SELECT starId, COUNT(*) total FROM stars_in_movies GROUP BY starId) y ON y.starId = x.id\n" +
                        "ORDER BY total DESC, id), \n" +
                        "SelectedTitleANDRating AS (\n" +
                        "SELECT m.id, m.title, m.year, m.director, r.rating\n" +
                        "FROM movies m left join ratings r ON m.id = r.movieId\n" +
                        "where match (m.title) against (? In boolean mode) or (edth(?, lower(m.title), "+ fuzzyLength + ") AND length(m.title) = length(?))\n"+
                        "),  final as(\n" +
                        "SELECT M.title\n" +
                        "FROM SelectedTitleANDRating as M\n" +
                        "inner join (select sm.movieId from stars_in_movies sm\n" +
                        "INNER JOIN selectedStars as s ON s.id = sm.starId where s.name like ? GROUP BY \n" +
                        "movieId) as star_list on star_list.movieId = M.id WHERE year = ? AND director like ?)\n" +
                        "select count(*) as total\n" +
                        "from final";
                statement2 = conn.prepareStatement(countQuery);
                statement2.setString(1, title);
                statement2.setString(2, temp3.toLowerCase());
                statement2.setString(3, temp3.toLowerCase());
                statement2.setString(4, star);
                statement2.setInt(5, year2);
                statement2.setString(6, Searchdirector);
            }


            ResultSet rs = statement.executeQuery();
            ResultSet rs2 = statement2.executeQuery();
            JsonArray jsonArray = new JsonArray();

            jsonArray.add(temp);

            int count = 0;
            while(rs2.next()){
                count = rs2.getInt("total");
            }

            while (rs.next()) {
                String movie_id = rs.getString("movieId");
                String movie_name = rs.getString("M.title");
                String movie_year = rs.getString("M.year");
                String director = rs.getString("M.director");
                String starsId = rs.getString("starId");
                String starsName = rs.getString("starName");
                String rating;
                if(rs.getString("M.rating") != null){
                    rating = rs.getString("M.rating");
                }
                else{
                    rating = "N/A";
                }
                String movieGenres = rs.getString("genres");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_name", movie_name);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("director", director);
                jsonObject.addProperty("genres", movieGenres);
                jsonObject.addProperty("starID", starsId);
                jsonObject.addProperty("starsName", starsName);
                jsonObject.addProperty("rating", rating);
                jsonObject.addProperty("count", count);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);


            long endTimeTJ = System.nanoTime();
            elapsedTimeTJ = endTimeTJ - startTimeTJ;

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }

        long endTimeTS = System.nanoTime();
        elapsedTimeTS = endTimeTS - startTimeTS;

        try {
            FileWriter myWriter = new FileWriter("log.txt", true);
            myWriter.write("TS : " + elapsedTimeTS + ", TJ : " + elapsedTimeTJ + "\n");
            myWriter.close();
        }
        catch(Exception e){
            System.out.println("error");
            e.printStackTrace();
        }


        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }

}