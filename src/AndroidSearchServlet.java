import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

@WebServlet(name = "AndroidSearchServlet", urlPatterns = "/api/android-search")
public class AndroidSearchServlet extends HttpServlet{
    private static final long serialVersionUID = 15L;
    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        String title = request.getParameter("title");
        String offset = request.getParameter("offset");
        String temp2 = "";
        String temp3 = title;
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
        System.out.println(title + ":" + offset);

        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            String query = "with selectedStars as (SELECT x.id, x.name, total\n" +
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
                    "INNER JOIN selectedStars as s ON s.id = sm.starId GROUP BY \n" +
                    "movieId) as star_list on star_list.movieId = M.id\n" +
                    "LIMIT 20\n" +
                    "OFFSET " + offset;

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, title);
            statement.setString(2, temp3.toLowerCase());
            statement.setString(3, temp3);
            ResultSet rs = statement.executeQuery();
            String query2 = "SELECT count(*) as count\n" +
                    "FROM movies m left join ratings r ON m.id = r.movieId\n" +
                    "where match (m.title) against (? In boolean mode) or (edth(?, lower(m.title), "+ fuzzyLength + ") AND length(m.title) = length(?))";
            statement = conn.prepareStatement(query2);
            statement.setString(1, title);
            statement.setString(2, temp3.toLowerCase());
            statement.setString(3, temp3);
            ResultSet rs2 = statement.executeQuery();


            JsonArray jsonArray = new JsonArray();
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
                jsonArray.add(jsonObject);
            }
            rs2.next();
            JsonObject tempObj = new JsonObject();
            tempObj.addProperty("count", rs2.getInt("count"));
            jsonArray.add(tempObj);
            rs.close();
            rs2.close();
            statement.close();

            out.write(jsonArray.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources
    }

}

