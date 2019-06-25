import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;

public class Driver {
    static void parseData(PreparedStatement preparedStatement, String file, int columns) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                String[] data = line.trim().split("\\|");
                if (data.length != columns) {
                    continue;
                }

                String timeStampPattern = "[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]";
                for (int i = 0; i < columns; i++) {
                    if (data[i].matches(timeStampPattern)) {
                        Timestamp ts = Timestamp.valueOf(data[i]);
                        preparedStatement.setTimestamp(i + 1, ts);
                    } else {
                        preparedStatement.setString(i + 1, data[i].trim());
                    }
                }

                try {
                    preparedStatement.executeUpdate();
                } catch (SQLException ex) {
                    System.out.println("~~~~~~OOPS~~~~~~ Invalid Record from " + file + " : " + Arrays.toString(data));
                    System.out.println("Error message: " + ex.getMessage() + "\n");
                    //ex.printStackTrace();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        int CURRENT_USERID = 0;
        // the default framework is embedded
        String protocol = "jdbc:derby:";
        String dbName = "iRate";
        String connStr = protocol + dbName + ";create=true";

        // tables tested by this program
        String dbTables[] = {
                "Attendance", "Endorsement",       // relations
                "Review", "Customer", "Movie"        // entities
        };


        Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user2");
        props.put("password", "user2");

        // result set for queries
        ResultSet rs = null;
        try (
                // connect to database
                Connection conn = DriverManager.getConnection(connStr, props);
                Statement stmt = conn.createStatement();

                // insert prepared statements
                PreparedStatement insertRow_Customer = conn.prepareStatement(
                        "insert into Customer(customer_Name, email, join_date) values(?, ?, ?)");
                PreparedStatement insertRow_Movie = conn.prepareStatement(
                        "insert into Movie(movie_title) values (?)");
                PreparedStatement insertRow_Review = conn.prepareStatement(
                        "insert into Review(customer_id, movie_id, review_date, rating, review) values(?, ?, ?, ?, ?)");
                PreparedStatement insertRow_Attendance = conn.prepareStatement(
                        "insert into Attendance(customer_id, movie_id, attendance_date) values(?, ?, ?)");
                PreparedStatement insertRow_Endorsement = conn.prepareStatement(
                        "insert into Endorsement(review_id, endorser_id, endorse_date) values(?, ?, ?)");

        ) {
            // connect to the database using URL
            System.out.println("Connected to database " + dbName);

            // clear data from tables
            for (String tbl : dbTables) {
                try {
                    stmt.executeUpdate("delete from " + tbl);
                    System.out.println("Truncated table " + tbl);
                } catch (SQLException ex) {
                    System.out.println("Did not truncate table " + tbl);
                }
            }


            System.out.println("-----------Start tests-------------");

            parseData(insertRow_Customer, "customer_data.txt", 3);
            parseData(insertRow_Movie, "movie_data.txt", 1);
            parseData(insertRow_Attendance, "attendance_data.txt", 3);
            parseData(insertRow_Review, "review_data.txt", 5);
            parseData(insertRow_Endorsement, "endorsement_data.txt", 3);


            // print number of rows in tables
            for (String tbl : dbTables) {
                rs = stmt.executeQuery("select count(*) from " + tbl);
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.printf("Table %s : count: %d\n", tbl, count);
                }
            }
            rs.close();


            // freeGift function testing
            System.out.println("\n*****Test for freeGift function*****\n");
            Helper.freeGift(conn, "2019-06-26");

            System.out.println("\n*****Test for movieRating function*****\n");
            Helper.movieRating(conn, "Rush hour");
            
            System.out.println("\n*****Test for freeTicket function*****\n");
            Helper.freeTicket(conn);
            
            // mostReview function testing
            System.out.println("\n*****Test for mostReview function*****\n");
            Helper.mostReview(conn, "2019-06-24");
            
            
            System.out.println("\n*****Test for Register function*****\n");
            Helper.registerUser(conn);

            printTable.printCustomer(conn);
            
            printTable.printMovie(conn);
            
            System.out.println("*****Test for deleteMovie function*****\n");
            Helper.deleteMovie(conn, "Rush hour");
            
            printTable.printMovie(conn);
            
            System.out.println("*****Test for addMovie function*****\n");
            Helper.addMovie(conn, "Rush hour");
            
            printTable.printMovie(conn);
            
            
            CURRENT_USERID = Helper.login(conn);
            System.out.println("\ncurrent login userId is: " + CURRENT_USERID);



        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
