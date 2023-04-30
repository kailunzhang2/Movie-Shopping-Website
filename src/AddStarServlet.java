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
import java.lang.reflect.Type;
import java.sql.Types;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/api/add_stars")
public class AddStarServlet extends HttpServlet {
    private static final long serialVersionUID = 12L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        String name = request.getParameter("name");
        String year = request.getParameter("year");
        System.out.println("getting data");

        if(name != null){

            // Output stream to STDOUT
            PrintWriter out = response.getWriter();

            // Get a connection from dataSource and let resource manager close the connection after usage.
            try (Connection conn = dataSource.getConnection()) {

                // Declare our statement

                String query = "select id from stars ORDER BY id DESC LIMIT 1";
                PreparedStatement statement = conn.prepareStatement(query);

                // Perform the query
                ResultSet rs = statement.executeQuery(query);
                rs.next();
                String star_id = rs.getString("id");
                System.out.println();
                int newId = Integer.parseInt(star_id.substring(2)) +1;
                String new_id = "nm" + String.valueOf(newId);
                System.out.println("adding star: " + new_id + name + year);

                rs.close();
                statement.close();
                query = "INSERT INTO stars(id, name, birthYear) VALUES (?, ?, ?)";
                PreparedStatement statement2 = conn.prepareStatement(query);

                statement2.setString(1, new_id);
                statement2.setString(2, name);
                if(year == "")
                    statement2.setNull(3, Types.NULL);
                else if(year == null){
                    statement2.setNull(3, Types.NULL);
                }
                else
                    statement2.setInt(3, Integer.parseInt(year));



                System.out.println(statement2.toString());
                statement2.executeUpdate();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status", "success");
                jsonObject.addProperty("successMessage", "Successfully added star with an id: " + new_id);
                out.write(jsonObject.toString());



                response.setStatus(200);

            } catch (Exception e) {

                // Write error message JSON object to output
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status", "failed");
                jsonObject.addProperty("errorMessage", e.getMessage());
                System.out.print(jsonObject.toString());

                // Set response status to 500 (Internal Server Error)
                response.setStatus(500);
            } finally {
                out.close();
            }

            // Always remember to close db connection after usage. Here it's done by try-with-resources
        }

    }
}
