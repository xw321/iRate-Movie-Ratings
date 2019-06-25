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
                "Attendance", "Endorsement",       // relations
                "Review", "Customer", "Movie"        // entities
        };

        // triggers created by this program
        String dbTriggers[] = {"review_limit_by_attendance", "review_limit_by_date", "review_limit_by_date2",
                "endorse_limit_by_date", "endorse_limit_by_customer", "endorse_limit_by_oneDay"};

        // procedures created by this program
        String storedFunctions[] = {"isEmail", "freeGift", "freeTicket"};

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

            // drop the storedFunctions and recreate them below
            for (String func : storedFunctions) {
                try {
                    stmt.executeUpdate("drop function " + func);
                    System.out.println("Dropped function " + func);
                } catch (SQLException ex) {
                    System.out.println("Did not drop function " + func);
                }
            }

            // create the isEmail stored procedure
            String create_isEmail = "create function isEmail("
                    + " Email varchar(64)"
                    + " ) RETURNS BOOLEAN "
                    + " PARAMETER STYLE JAVA "
                    + " LANGUAGE JAVA "
                    + " DETERMINISTIC "
                    + " NO SQL "
                    + " EXTERNAL NAME "
                    + " 'Helper.isEmail'";
            stmt.executeUpdate(create_isEmail);
            System.out.println("Created function isEmail()");


            // create the freeGift stored procedure
            String create_freeGift = "create function freeGift("
                    + " date TIMESTAMP"
                    + " ) RETURNS VARCHAR(64) "
                    + " PARAMETER STYLE JAVA "
                    + " LANGUAGE JAVA "
                    + " DETERMINISTIC "
                    + " NO SQL "
                    + " EXTERNAL NAME "
                    + " 'Helper.freeGift'";
            stmt.executeUpdate(create_freeGift);
            System.out.println("Created function freeGift()");

            // create the freeTicket stored procedure
            String create_freeTicket = "create function freeTicket("
                    + " date TIMESTAMP"
                    + " ) RETURNS VARCHAR(64) "
                    + " PARAMETER STYLE JAVA "
                    + " LANGUAGE JAVA "
                    + " DETERMINISTIC "
                    + " NO SQL "
                    + " EXTERNAL NAME "
                    + " 'Helper.freeTicket'";
            stmt.executeUpdate(create_freeTicket);
            System.out.println("Created function freeTicket()");


            // create the Customer table
            String createTable_Customer =
                    "create table Customer("
                            + "  customer_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 7000, INCREMENT BY 1),"
                            + "  customer_Name varchar(64) NOT NULL,"
                            + "  email varchar(64) NOT NULL,"
                            + "  join_date TIMESTAMP NOT NULL,"
                            + "  PRIMARY KEY (customer_id),"
                            + "  check (isEmail(email))"
                            + ")";
            stmt.executeUpdate(createTable_Customer);
            System.out.println("Created table Customer");


            // create the Movie table
            String createTable_Movie =
                    "create table Movie("
                            + "  movie_title varchar(64) NOT NULL,"
                            + "  movie_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 8000, INCREMENT BY 1),"
                            + "  PRIMARY KEY (movie_id)"
                            + ")";
            stmt.executeUpdate(createTable_Movie);
            System.out.println("Created table Movie");


            // create the Review table
            String createTable_Review =
                    "create table Review("
                            + "  review_id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 9000, INCREMENT BY 1),"
                            + "  customer_id int NOT NULL,"
                            + "  movie_id int NOT NULL,"
                            + "  review_date TIMESTAMP NOT NULL,"
                            + "  rating INT NOT NULL,"
                            + "  check (rating between 0 and 5),"
                            + "  review varchar(1000) NOT NULL,"
                            + "  PRIMARY KEY (review_id),"
                            + "  UNIQUE (movie_id, customer_id),"  // make sure to have one movie review per customer
                            + "  FOREIGN KEY (customer_id) references Customer(customer_id) ON DELETE CASCADE,"  // if a customer is deleted, all of his or her reviews and endorsement are deleted.
                            + "  FOREIGN KEY (movie_id) references Movie(movie_id) ON DELETE CASCADE"   // if a movie is deleted, all of its reviews are also deleted.
                            + ")";

            stmt.executeUpdate(createTable_Review);
            System.out.println("Created table Review");

            // create the Attendance table
            String createTable_Attendance =
                    "create table Attendance("
                            + "movie_id INT NOT NULL,"
                            + "customer_id INT NOT NULL,"
                            + "attendance_date TIMESTAMP NOT NULL,"
                            + "primary key (movie_id, customer_id, attendance_date),"
                            + "foreign key (movie_id) REFERENCES Movie (movie_id) on delete cascade ,"
                            + "foreign key (customer_id) REFERENCES Customer (customer_id) on delete cascade "
                            + ")";
            ;
            stmt.executeUpdate(createTable_Attendance);
            System.out.println("Created table Attendance");

            // create the Endorsement table
            String createTable_Endorsement =
                    "create table Endorsement("
                            + "review_id INT NOT NULL,"
                            + "endorser_id INT NOT NULL,"
                            + "endorse_date TIMESTAMP NOT NULL,"
                            + "PRIMARY KEY (review_id, endorser_id, endorse_date),"
                            + "FOREIGN KEY (review_id) REFERENCES Review (review_id) on delete cascade ,"
                            + "FOREIGN KEY (endorser_id) REFERENCES Customer (customer_id) on delete cascade "
                            + ")";
            stmt.executeUpdate(createTable_Endorsement);
            System.out.println("Created table Endorsement");


                 /*
       Create trigger review_limit
       Constraints:
       1) if a customer did not attend a movie, he/she cannot review it.
       2) the date of the review must be within 7 days of the most recent attendance of the movie.
       3) the date of the review must be within 7 days of the most recent attendance of the movie
       */
            String createTrigger_review_limit_by_attendance =
                    "create trigger review_limit_by_attendance"
                            + " after insert ON Review"
                            + " for each statement"
                            + "   delete from Review where customer_id not in"
                            + "     (select customer_id from Attendance)";
            stmt.executeUpdate(createTrigger_review_limit_by_attendance);
            System.out.println("Created review_limit trigger for Review by Attendance");


            // This trigger prevents any invalid review such that it has a review_date earlier than its actual attendance_date
            String createTrigger_review_limit_by_date =
                    "create trigger review_limit_by_date"
                            + " after insert ON Review"
                            + " REFERENCING new as insertedRow"
                            + " for each row MODE DB2SQL"
                            + "   delete from Review where  review_id = insertedRow.review_id AND timestamp(review_date) <"
                            + "     (select timestamp(attendance_date) from Attendance where "
                            + " Attendance.customer_id = insertedRow.customer_id AND Attendance.movie_id = insertedRow.movie_id)";
            stmt.executeUpdate(createTrigger_review_limit_by_date);
            System.out.println("Created review_limit trigger for Review by Date");

            // This trigger will delete the inserted Review if: current_date - 7days > reviewee's attendance date
            String createTrigger_review_limit_by_date2 =
                    "create trigger review_limit_by_date2"
                            + " after insert ON Review"
                            + " REFERENCING new as insertedRow"
                            + " for each row MODE DB2SQL"
                            + "   delete from Review where review_id = insertedRow.review_id AND (select timestamp({fn TIMESTAMPADD(SQL_TSI_DAY, -7, insertedRow.review_date)}) from sysibm.sysdummy1) > "
                            + "     (select timestamp(attendance_date) from Attendance where "
                            + " Attendance.customer_id = insertedRow.customer_id AND Attendance.movie_id = insertedRow.movie_id)";
            stmt.executeUpdate(createTrigger_review_limit_by_date2);
            System.out.println("Created review_limit trigger for Review by Date2");



                /*
           Create trigger endorse_limit
           Constraints:
           1) if a customer is the one who wrote the review, cannot endorse.
           2) if the endorse_date is after 3 days when the review was written, cannot endorse.
           */

            //create Trigger endorse_limit_by_date: review has to have  earlier time than its endorsements
            String createTrigger_endorse_limit_by_date =
                    " create trigger endorse_limit_by_date"
                            + " after insert ON Endorsement"
                            + " REFERENCING new as insertedRow"
                            + " for each row MODE DB2SQL"
                            + "   delete from Endorsement where review_id = insertedRow.review_id AND endorser_id = insertedRow.endorser_id AND endorse_date = insertedRow.endorse_date AND timestamp(insertedRow.endorse_date) <"
                            + "     (select timestamp(review_date) from Review where Review.review_id = insertedRow.review_id)";
            stmt.executeUpdate(createTrigger_endorse_limit_by_date);
            System.out.println("Created endorse_limit trigger for endorse limit by Date");


            // This trigger will delete the inserted Endorsement if the customer is the one who wrote the review
            String createTrigger_endorse_limit_by_customer =
                    "create trigger endorse_limit_by_customer"
                            + " after insert ON Endorsement"
                            + " REFERENCING new as insertedRow"
                            + " for each row MODE DB2SQL"
                            + "   delete from Endorsement where review_id = insertedRow.review_id AND endorser_id = insertedRow.endorser_id AND endorse_date = insertedRow.endorse_date AND review_id = "
                            + "     (select review_id from Review where Review.customer_id = insertedRow.endorser_id AND insertedRow.review_id = Review.review_id)";
            stmt.executeUpdate(createTrigger_endorse_limit_by_customer);
            System.out.println("Created review_limit trigger for Review by Customer");

            String createTrigger_endorse_limit_by_oneDay =
                    "create trigger endorse_limit_by_oneDay"
                            + " after insert ON Endorsement"
                            + " REFERENCING new as insertedRow"
                            + " for each row MODE DB2SQL "
                            + "   delete from Endorsement where review_id = insertedRow.review_id AND endorser_id = insertedRow.endorser_id AND endorse_date = insertedRow.endorse_date " +
                            " AND {fn TIMESTAMPDIFF( SQL_TSI_DAY, timestamp(insertedRow.endorse_date),   "
                            // get this user's most recent endorse_date of a review for the same movie
                            + "     (select max(timestamp(endorse_date)) as mostRecentDate from Endorsement LEFT JOIN Review " +
                            "ON Endorsement.review_id = Review.review_id " +
                            "where timestamp(Endorsement.endorse_date) < timestamp(insertedRow.endorse_date) AND Endorsement.endorser_id = insertedRow.endorser_id AND " +
                            "Review.movie_id = (select movie_id from Review WHERE review_id = insertedRow.review_id))" +
                            "   )} = 0";
            stmt.executeUpdate(createTrigger_endorse_limit_by_oneDay);
            System.out.println("Created review_limit trigger for Review by oneDay");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {

        System.out.println("*********** Start building iRate Db Schema ***********");
        createTables();
        System.out.println("*********** Finish building iRate Db Schema ***********");
    }
}