import java.sql.Timestamp;

public class Helper {

    /**
     * Determines whether 'email' is a valid email address.
     *
     * @param email the email address
     * @return true if 'email' is a valid email address
     */

    public static boolean isEmail(String email) {
        return email.matches(
                "^[\\p{L}\\p{N}\\._%+-]+@[\\p{L}\\p{N}\\.\\-]+\\.[\\p{L}]{2,}$");
    }
    
    public static String freeGift(Timestamp date) {
        return "SELECT Customer.customer_Name FROM Customer "
                + "JOIN Endorsement ON Customer.customer_id = Endorsement.customer_id WHERE endorse_date = " + date;
    }
    
    public static String freeTicket(Timestamp date) {
        return "SELECT Customer.customer_Name, COUNT(*) AS COUNT"
                + " FROM (Review JOIN Endorsement ON Review.review_id = Endorsement.review_id "
                + "JOIN Customer ON Review.customer_id = Customer.customer_id) "
                + "WHERE review_date <= " + date
                + "review_date >= select timestamp({fn TIMESTAMPADD(SQL_TSI_DAY, -3, " + date
                + ")}) from sysibm.sysdummy1"
                + "GROUP BY Endorsement.review_id ORDER BY COUNT DESC limit(1)";
    }
}


