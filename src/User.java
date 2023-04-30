/**
 * This User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 */
public class User {

    private final int customerID;

    public User(int customerID) {
        this.customerID = customerID;
    }

    public int get(){
        return this.customerID;
    }

}
