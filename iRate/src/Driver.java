import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class Driver {
    public static int CURRENT_USERID = 7012;
    public static int scanFlag = 1;

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
                    ex.printStackTrace();
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    static void buildTableFromFile() {
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


            System.out.println("\n-----------Start building Database From text files-------------\n");

            parseData(insertRow_Customer, "customer_data.txt", 3);
            System.out.println("finish inserting into table customer");
            parseData(insertRow_Movie, "movie_data.txt", 1);
            System.out.println("finish inserting into table Movie");
            parseData(insertRow_Attendance, "attendance_data.txt", 3);
            System.out.println("finish inserting into table attendance");
            parseData(insertRow_Review, "review_data.txt", 5);
            System.out.println("finish inserting into table review");
            parseData(insertRow_Endorsement, "endorsement_data.txt", 3);
            System.out.println("finish inserting into table endorsement\n");


            // print number of rows in tables
            for (String tbl : dbTables) {
                rs = stmt.executeQuery("select count(*) from " + tbl);
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.printf("Table %s : count: %d\n", tbl, count);
                }
            }
            rs.close();


        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        buildTableFromFile();
        String protocol = "jdbc:derby:";
        String dbName = "iRate";
        String connStr = protocol + dbName + ";create=true";
        Properties props = new Properties(); // connection properties
        // providing a user name and password is optional in the embedded
        // and derbyclient frameworks
        props.put("user", "user1");
        props.put("password", "user1");
        try (
                Connection conn = DriverManager.getConnection(connStr, props);
        ) {

//            printTable.printEndorsement(conn);
//            Helper.voteReview(conn);
//
//            printTable.printEndorsement(conn);
//            Helper.voteReview(conn);
//            printTable.printEndorsement(conn);
//            Helper.voteReview(conn);
//            printTable.printEndorsement(conn);
//            Helper.voteReview(conn);
//            printTable.printEndorsement(conn);

            String emoji = "░▀░ █▀▀█ █▀▀█ ▀▀█▀▀ █▀▀\n" +
                    "▀█▀ █▄▄▀ █▄▄█ ░░█░░ █▀▀\n" +
                    "▀▀▀ ▀░▀▀ ▀░░▀ ░░▀░░ ▀▀▀";


            String[] functionListNoParam = {"logout", "quit"};
            String[] functionListConn = {"freeGift", "movieRating", "topReview", "freeTicket", "topBoxOfficeMovie",
                    "topContributor", "deleteMovie", "addMovie", "registerUser", "login", "voteReview", "buyTicket", "reviewMovie", "displayReview"};
            // Start user input below
            System.out.println("\n\n\n" + emoji + "\n\nHi there, welcome to iRate! I am your iRate assistant.\nMy name is Amazon - Microsoft - Intel - Google - Oracle.\nYou can just call me AMIGO :-)\n");

            System.out.println("We provide some APIs for you to call.\nYou can just type the name of the APi in console When prompted.\nHere is the List of API names:\n");

            for (String func : functionListConn) {
                System.out.println("   " + func);
            }
            for (String func : functionListNoParam) {
                System.out.println("   " + func);
            }

            Object funcCall = new Helper();
            while (scanFlag == 1) {
                System.out.println("\nAmigo >>> Enter your New command here : ");
                Scanner scannerName = new Scanner(System.in);
                String command = scannerName.nextLine();
                if (Arrays.asList(functionListNoParam).contains(command)) {
                    java.lang.reflect.Method method = null;
                    try {
                        method = Helper.class.getMethod(command);
                    } catch (SecurityException e) {
                        // oops
                    } catch (NoSuchMethodException e) {
                        // oops
                    }

                    try {
                        method.invoke(funcCall);
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    } catch (java.lang.reflect.InvocationTargetException e) {
                    }

                } else if (Arrays.asList(functionListConn).contains(command)) {
                    java.lang.reflect.Method method = null;
                    try {
                        method = Helper.class.getMethod(command, Connection.class);
                    } catch (SecurityException e) {
                        // oops
                    } catch (NoSuchMethodException e) {
                        // oops
                    }

                    try {
                        method.invoke(funcCall, conn);
                    } catch (IllegalArgumentException e) {
                    } catch (IllegalAccessException e) {
                    } catch (java.lang.reflect.InvocationTargetException e) {
                    }
                } else {
                    System.out.println("Amigo >>> No command for :  " + command);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}