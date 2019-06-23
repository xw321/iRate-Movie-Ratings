import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * This program tests the version of the publication database tables for Assignment 5
 * that uses relation tables for the PublishedBy and PublishedIn relations. The sample
 * data is stored in a tab-separated data file The columns of the data file are:
 * pubName, pubCity, jnlName, jnlISSN, artTitle, artDOI, auFamiily, auGiven, auORCID
 *
 * @author philip gust
 */
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

                for (int i = 0; i < columns; i++) {
                    //TODO: change validation to regex
                    if (data[i].length() == 19 && data[i].substring(0, 3).equals("201")) {
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
                    //ex.printStackTrace();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
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
        props.put("user", "user1");
        props.put("password", "user1");

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
            PreparedStatement invoke_freeGift  =
                    conn.prepareStatement("values ( freeGift(?) )");
            System.out.println("");
            System.out.println("Test for freeGift function");
    
            String [] queryDate = {"1960-01-01 23:03:20", "2019-07-01 23:03:20"};
            for (String date : queryDate) {                 
                try {
                    invoke_freeGift.setString(1, date);
                    ResultSet rs5 = invoke_freeGift.executeQuery();
                    if (rs5.next()) {
                        System.out.println("The winner of the free concession items are: ");
                        System.out.println(rs5.getString("customer_Name"));
                
                    }rs5.close();
                } catch (SQLException ex) {
                    System.out.printf("There is no winner of the free concession items that day");
                }
            }
            
            // freeTicket function testing
            PreparedStatement invoke_freeTicket  =
                    conn.prepareStatement("values ( freeTicket(?) )");
            System.out.println("");
            System.out.println("Test for freeTicket function");
    
            String [] queryDate2 = {"1960-01-01 23:03:20", "2019-07-01 23:03:20"};
            for (String date : queryDate2) {                    
                try {
                    invoke_freeGift.setString(1, date);
                    ResultSet rs2 = invoke_freeTicket.executeQuery();
                    if (rs2.next()) {
                        System.out.println("The winner of the free ticket is: ");
                        System.out.println(rs2.getString("customer_Name"));
                
                    }rs2.close();
                } catch (SQLException ex) {
                    System.out.printf("There is no winner of the free ticket that day");
                }
            }


//            // delete article
//            System.out.println("\nDeleting article 10.1145/2838730 from CACM with 3 authors");
//            stmt.execute("delete from Article where doi = '10.1145/2838730'");
//            PubUtil.printArticles(conn);
//            PubUtil.printAuthors(conn);
//
//            // delete publisher ACM
//            System.out.println("\nDeleting publisher ACM");
//            stmt.executeUpdate("delete from Publisher where name = 'ACM'");
//            PubUtil.printPublishers(conn);
//            PubUtil.printJournals(conn);
//            PubUtil.printArticles(conn);
//            PubUtil.printAuthors(conn);
//
//            // delete journal Spectrum (0018-9235)
//            System.out.println("\nDeleting journal Spectrum from IEEE");
//            stmt.executeUpdate("delete from Journal where issn = " + Biblio.parseIssn("0018-9235"));
//            PubUtil.printJournals(conn);
//            PubUtil.printArticles(conn);
//            PubUtil.printAuthors(conn);
//
//
//            // delete journal Computer
//            System.out.println("\nDeleting journal Computer from IEEE");
//            stmt.executeUpdate("delete from Journal where title = 'Computer'");
//            PubUtil.printPublishers(conn);
//            PubUtil.printJournals(conn);
//            PubUtil.printArticles(conn);
//            PubUtil.printAuthors(conn);

        } catch (
                SQLException e) {
            e.printStackTrace();
        }
    }
}
