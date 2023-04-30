package Parser;
import java.io.IOException;
import java.sql.SQLException;

public class runner {
    public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InstantiationException, IllegalAccessException {
        MovieParser mv = new MovieParser();
        mv.runExample();
        StarParser str = new StarParser();
        str.runExample();

    }
}
