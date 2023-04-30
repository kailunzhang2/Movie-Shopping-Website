import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/CartServlet")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 8L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }


    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        JsonObject responseJsonObject = new JsonObject();

        ArrayList<JsonObject> previousItems = (ArrayList<JsonObject>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new ArrayList<>();
        }
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");
        JsonArray previousItemsJsonArray = new JsonArray();
        previousItems.forEach(previousItemsJsonArray::add);
        responseJsonObject.add("previousItems", previousItemsJsonArray);

        // write all the data into the jsonObject
        request.getServletContext().log(responseJsonObject.toString());
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movieId = request.getParameter("id");
        String action = request.getParameter("action");

        request.getServletContext().log("id:" + movieId + "items:" + action);

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        try (Connection conn = dataSource.getConnection()){
            // get the previous items in a ArrayList
            String query = "select m.id, m.title\n" +
                    "from movies m\n" +
                    "where m.id = ?";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, movieId);
            ResultSet rs = statement.executeQuery();
            rs.next();
            String title = rs.getString("m.title");
            Random rand = new Random();
            int price = rand.nextInt(100) + 1;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("movieId", movieId);
            jsonObject.addProperty("movieTitle", title);
            jsonObject.addProperty("price", price);

            ArrayList<JsonObject> previousItems = (ArrayList<JsonObject>) session.getAttribute("previousItems");
            if(Objects.equals(action, "add")){
                if (previousItems == null) {
                    previousItems = new ArrayList<JsonObject>();
                    jsonObject.addProperty("quantity", "1");
                    previousItems.add(jsonObject);
                    session.setAttribute("previousItems", previousItems);
                } else {
                    // prevent corrupted states through sharing under multi-threads
                    // will only be executed by one thread at a time
                    boolean add = false;
                    for(JsonObject j : previousItems){
                        if(Objects.equals(j.get("movieId").getAsString(), jsonObject.get("movieId").getAsString())){
                            int newQuan = j.get("quantity").getAsInt() + 1;
                            add = true;
                            j.addProperty("quantity", newQuan);
                            break;
                        }
                    }
                    synchronized (previousItems) {
                        if(!add){
                            jsonObject.addProperty("quantity", "1");
                            previousItems.add(jsonObject);
                        }
                    }
                }
            }
            else if(Objects.equals(action, "sub")){
                if (previousItems == null) {
                }
                else{
                    for(JsonObject j : previousItems){
                        if(Objects.equals(j.get("movieId").getAsString(), jsonObject.get("movieId").getAsString())){
                            int newQuan = j.get("quantity").getAsInt() - 1;
                            if(newQuan > 0){
                                j.addProperty("quantity", newQuan);
                            }
                            else{
                                previousItems.remove(j);
                            }
                        }
                    }
                }
            }
            else{
                if (previousItems == null) {
                }
                else{
                    for(JsonObject j : previousItems){
                        if(Objects.equals(j.get("movieId").getAsString(), jsonObject.get("movieId").getAsString())){
                                previousItems.remove(j);
                        }
                    }
                }
            }
            if(previousItems != null){
                int totalPrice = 0;
                for(JsonObject j : previousItems){
                    totalPrice += j.get("price").getAsInt()*j.get("quantity").getAsInt();
                }
                for(JsonObject j : previousItems){
                    j.addProperty("totalPrice", totalPrice);
                }
            }

            session.setAttribute("previousItems", previousItems);
            JsonObject responseJsonObject = new JsonObject();

            JsonArray previousItemsJsonArray = new JsonArray();
            previousItems.forEach(previousItemsJsonArray::add);
            responseJsonObject.add("previousItems", previousItemsJsonArray);
            response.getWriter().write(responseJsonObject.toString());
        }
        catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}