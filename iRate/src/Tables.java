import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Tables {

    static void createTables() {
        // the default framework is embedded
        String protocol = "jdbc:derby:";
        String dbName = "iRate";
        String connStr = protocol + dbName + ";create=true";

        // tables created by this program
        String dbTables[] = {
                "Customer", "Movie", "Review",        // entities
                "Attendance", "Endorsement"       // relations
        };

        String dbTriggers[] = {};

        Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user1");
        props.put("password", "user1");

        try (
                // connect to the database using URL
                Connection conn = DriverManager.getConnection(connStr, props);

                // statement is channel for sending commands thru connection
                Statement stmt = conn.createStatement();
        ) {
            System.out.println("Connected to and created database " + dbName);

            // drop the database triggers and recreate them below
            for (String tgr : dbTriggers) {
                try {
                    stmt.executeUpdate("drop trigger " + tgr);
                    System.out.println("Dropped trigger " + tgr);
                } catch (SQLException ex) {
                    System.out.println("Did not drop trigger " + tgr);
                }
            }

            // drop the database tables and recreate them below
            for (String tbl : dbTables) {
                try {
                    stmt.executeUpdate("drop table " + tbl);
                    System.out.println("Dropped table " + tbl);
                } catch (SQLException ex) {
                    System.out.println("Did not drop table " + tbl);
                }
            }


            // create the Customer table
            String createTable_Customer = 
                      "create table Customer(" 
                    + "  ID int NOT NULL AUTO_INCREMENT,"
                    + "  customer_Name varchar(64) NOT NULL," 
                    + "  Email varchar(64) NOT NULL," 
                    + "  address varchar(64) NOT NULL,"
                    + "  join_date date NOT NULL," 
                    + "  PRIMARY KEY (ID),"
                    + " check isValidEmail(Email)"
                    + ")";                     
            stmt.executeUpdate(createTable_Customer);
            System.out.println("Created table Customer");



            // create the Movie table
            String createTable_Movie = 
                      "create table Movie("
                    + "  movie_title varchar(64) NOT NULL,"
                    + "  id int NOT NULL AUTO_INCREMENRT,"
                    + "  PRIMARY KEY (id)"
                    + ")";
            stmt.executeUpdate(createTable_Movie);
            System.out.println("Created table Movie");


            // create the Review table
            String createTable_Review = 
                      "create table Review(" 
                    + "  reviewID int NOT NULL AUTO_INCREMENT,"
                    + "  customer_id int NOT NULL," 
                    + "  movie_id int NOT NULL," 
                    + "  review_date DATE NOT NULL,"
                    + "  rating INT NOT NULL," 
                    + "  check (rating between 0 and 5),"
                    + "  short_review varchar(1000) NOT NULL,"
                    + "  PRIMARY KEY (reviewID)," 
                    + "  UNIQUE (movie_id, customer_id),"  // make sure to have one movie review per customer
                    + "  FOREIGN KEY (customer_id) references Customer(ID) ON DELETE CASCADE,"  // if a customer is deleted, all of his or her reviews and endorsement are deleted.
                    + "  FOREIGH KEY (movie_id) references Movie(id) ON DELETE CASCADE"   // if a movie is deleted, all of its reviews are also deleted.
                    + ")";    

            stmt.executeUpdate(createTable_Review);
            System.out.println("Created table Review");

            // create the Attendance table
            String createTable_Attendance =
                    "create table Attendance("
                            + "movie_id INT NOT NULL,"
                            + "customer_id INT NOT NULL,"
                            + "attendance_date DATE NOT NULL,"
                            + "primary key (movie_id, customer_id, attendance_date),"
                            + "foreign key (movie_id) REFERENCES Movie (id) on delete cascade ,"
                            + "foreign key (customer_id) REFERENCES Customer (id) on delete cascade "
                            + ")";
            ;
            stmt.executeUpdate(createTable_Attendance);
            System.out.println("Created table Attendance");

            // create the Endorsement table
            String createTable_Endorsement =
                    "create table Endorsement("
                            + "review_id INT NOT NULL,"
                            + "endorser_id INT NOT NULL,"
                            + "endorse_date DATE NOT NULL,"
                            + "PRIMARY KEY (review_id, endorser_id,endorse_date),"
                            + "FOREIGN KEY (review_id) REFERENCES Review (id)"
                            + ")";
            stmt.executeUpdate(createTable_Endorsement);
            System.out.println("Created table Endorsement");
            
            
            
            
             String create_isValidEmail = "create function isValidEmail("
                    + "s varchar(64)"
                    + ") returns boolean "
                    + "PARAMETER STYLE JAVA "
                    + "LANGUAGE JAVA "
                    + "DETERMINISTIC "
                    + "NO SQL "
                    + "EXTERNAL NAME "
                    + "'Helper.isValidEmail'";
            stmt.executeUpdate(create_isValidEmail);
            System.out.println("Created function isValidEmail()");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        System.out.println("Start");
        createTables();
    }
}
