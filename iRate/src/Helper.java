import java.sql.*;

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

    public static void freeGift(Connection conn, String date) {
        if (date.matches("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])")) {
            // Date format: yyyy-MM-dd
            String endTime = " 23:59:59";
            String startTime = " 00:00:00";

            Timestamp startTimeStamp = Timestamp.valueOf(date + startTime);
            Timestamp endTimeStamp = Timestamp.valueOf(date + endTime);


            // execute query

            try {
                String query0 = "select customer_Name from Customer INNER JOIN Endorsement ON " +
                        "Customer.customer_id = Endorsement.endorser_id WHERE timestamp(Endorsement.endorse_date) BETWEEN (?) AND (?)";
                PreparedStatement invoke_freeGift = conn.prepareStatement(query0);
                invoke_freeGift.setTimestamp(1, startTimeStamp);
                invoke_freeGift.setTimestamp(2, endTimeStamp);

                ResultSet rs5 = invoke_freeGift.executeQuery();
                while (rs5.next()) {
                    //System.out.println("The winner of the free concession items are: ");
                    System.out.println("Winners of Free Gift on day " + date + ":       " + rs5.getString("customer_Name"));

                }
                rs5.close();
            } catch (SQLException ex) {
                System.out.printf("There is no winner of the free concession items that day");
            }
        }
    }


    public static void movieRating(Connection conn, String movie) {

        try {
            String query1 = "select avg(CAST(rating as FLOAT )) as rat from Review INNER JOIN Movie ON Review.movie_id = Movie.movie_id WHERE Movie.movie_title = (?)";
            PreparedStatement invoke_avgRating = conn.prepareStatement(query1);
            invoke_avgRating.setString(1, movie);
            ResultSet rs5 = invoke_avgRating.executeQuery();

            if (rs5.next()) {
                if (rs5.getString("rat") != null) {
                    System.out.println("Rating of movie " + movie + " is: " + rs5.getString("rat"));
                } else {
                    System.out.println("No rating for movie " + movie);
                }

            }

        } catch (SQLException ex) {
            System.out.printf("There is no winner of the free concession items that day");
        }
    }
}