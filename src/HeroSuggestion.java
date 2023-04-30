import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Objects;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet("/hero-suggestion")
public class HeroSuggestion extends HttpServlet {
    private static final long serialVersionUID = 16L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public HeroSuggestion() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try(Connection conn = dataSource.getConnection())  {
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String title = request.getParameter("query");

            if (Objects.equals(title, "null") || title.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

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
            for(String token : title.strip().split(" ")){
                temp2 = temp2 +  "+" + token + "*,";
            }
            temp2 = temp2.substring(0, temp2.length() - 1);
            title = temp2;

            // return the empty json array if query is null or empty

            String query = "select id, title from movies where match (title) against (? In boolean mode) or (edth(?, lower(title), "+ fuzzyLength + ") AND length(title) = length(?))";

            PreparedStatement statement = conn.prepareStatement(query);

            statement.setString(1, title);
            statement.setString(2, temp3.toLowerCase());
            statement.setString(3, temp3);

            ResultSet rs = statement.executeQuery();

            // search on superheroes and add the results to JSON Array
            // this example only does a substring match
            // TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
            int count = 0;
            while (rs.next()) {
                if(count == 10){
                    break;
                }
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");

                jsonArray.add(generateJsonObject(movieId, movieTitle));
                count += 1;
            }


            response.getWriter().write(jsonArray.toString());
            rs.close();
            statement.close();
            response.setStatus(200);
            return;
        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }

    private static JsonObject generateJsonObject(String heroID, String heroName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", heroName);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("heroID", heroID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }

}

