import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Objects;



@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet
{
    private static final long serialVersionUID = 9L;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession();
        String saleDate = java.time.LocalDate.now().toString();
        response.setContentType("application/json");
        String cid = request.getParameter("cardId");
        String first = request.getParameter("firstName");
        String last = request.getParameter("lastName");
        String tempExp = request.getParameter("exp");
        SimpleDateFormat format =new SimpleDateFormat("yyyy-MM-dd");
        Date exp = Date.valueOf(tempExp);

        int userId = ((User) session.getAttribute("user")).get();
        ArrayList<JsonObject> previousItems = (ArrayList<JsonObject>) session.getAttribute("previousItems");

        PrintWriter out = response.getWriter();
        try(Connection conn = dataSource.getConnection())
        {
            String query = "SELECT COUNT(*) AS card \n"
                        + "FROM creditcards C \n"
                        + "WHERE C.id = ? AND C.firstName = ? AND C.lastName = ? AND C.expiration = ?;";
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1,cid);
            statement.setString(2,first);
            statement.setString(3,last);
            statement.setDate(4, (java.sql.Date) exp);
            request.getServletContext().log(statement.toString());
            ResultSet results = statement.executeQuery();
            results.next();
            if (results.getInt("card") > 0 && previousItems != null)
            {
                for (JsonObject j : previousItems) {
                    String postResult = "INSERT INTO sales(customerId, movieId, saleDate) VALUES (" + userId + ", '" + j.get("movieId").getAsString() + "', '" + saleDate + "');";
                    statement.executeUpdate(postResult);
                }
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status","success");
                jsonObject.addProperty("message","success");
                out.write(jsonObject.toString());
            }
            else
            {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status","failure");
                if (previousItems == null) {
                    jsonObject.addProperty("message","Empty Card.");
                }
                else {
                    jsonObject.addProperty("message","Wrong Card Information.");
                }
                out.write(jsonObject.toString());
            }
            response.setStatus(200);
            results.close();
            statement.close();
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            request.getServletContext().log("Error:", e);
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
