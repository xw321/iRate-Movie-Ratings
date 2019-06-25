import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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

    private static java.sql.Timestamp getCurrentTimestamp() {
        java.util.Date date = new java.util.Date();
        return new java.sql.Timestamp(date.getTime());
    }

    // tested
    public static void freeGift(Connection conn) {
        if (Driver.CURRENT_USERID != 7000) {
            System.out.println("You are NOT authorized to do so.");
            return;
        }

        System.out.print("Amigo >>> Enter the date for free gift (format: yyyy-MM-dd) : ");
        Scanner scanner0 = new Scanner(System.in);
        String date = scanner0.nextLine();

        while (!date.matches("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])")) {
            System.out.print("Amigo >>> Invalid date format! Enter date again : ");
            scanner0 = new Scanner(System.in);
            date = scanner0.nextLine();
        }

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

                if (!rs5.next()) {
                    System.out.printf("There is no winner of the free concession items that day !");
                    return;
                } else {
                    do {
                        //System.out.println("The winner of the free concession items are: ");
                        System.out.println("Winners of Free Gift on day " + date + ":       " + rs5.getString("customer_Name"));
                    } while (rs5.next());
                }


                rs5.close();
            } catch (SQLException ex) {
                System.out.printf("There is no winner of the free concession items that day");
                //ex.printStackTrace();
            }
        }
    }

    // tested
    public static void movieRating(Connection conn) {
        System.out.print("Amigo >>> Enter the movie title you're looking for : ");
        Scanner scanner0 = new Scanner(System.in);
        String movie = scanner0.nextLine();

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
            System.out.printf("There is no available rating for movie " + movie);
        }
    }

    // tested
    public static void topReview(Connection conn) {
        System.out.print("Amigo >>> Enter movie title to show top review : ");
        Scanner scannerName = new Scanner(System.in);
        String movie = scannerName.nextLine();

        try {
            // This query gets the movie title of the top review
            String titleQuery = "select movie_id from Movie where movie_title = (?)";
            PreparedStatement invoke_getMovieId = conn.prepareStatement(titleQuery);
            invoke_getMovieId.setString(1, movie);
            ResultSet rs2 = invoke_getMovieId.executeQuery();
            int movieId = 0;
            if (rs2.next()) {
                movieId = rs2.getInt("movie_id");
            } else {
                System.out.println("No such movie");
                return;
            }
            // This query will select the review_id and its count that satisfies the requirement
            String query0 = "select Endorsement.review_id, count(*) AS nor from Endorsement LEFT JOIN Review ON Endorsement.review_id = Review.review_id" +
                    " WHERE Review.movie_id = (?) " +
                    " GROUP BY Endorsement.review_id ORDER BY nor DESC ";

            PreparedStatement invoke_getTopReview = conn.prepareStatement(query0);
            invoke_getTopReview.setInt(1, movieId);
            ResultSet rsReview = invoke_getTopReview.executeQuery();
            int reviewVote = 0;
            int topReviewId = 0;

            if (rsReview.next()) {
                reviewVote = rsReview.getInt("nor");
                topReviewId = rsReview.getInt("review_id");
            } else {
                System.out.println("No top review for such movie");
                return;
            }

            // This query will  select the author of the top voted review, which we obtained in query0, and all other info about that review
            String query1 = "select * from Customer LEFT JOIN Review ON Customer.customer_id = Review.customer_id WHERE Review.review_id = (?)";
            PreparedStatement invoke_getAuthor = conn.prepareStatement(query1);
            invoke_getAuthor.setInt(1, topReviewId);
            ResultSet rs1 = invoke_getAuthor.executeQuery();

            if (rs1.next()) {
                System.out.println("Top review of movie " + movie + " is by user " + rs1.getString("customer_Name") + ". It has " + reviewVote + " votes : \n");
                System.out.println("`" + rs1.getString("review") + "`\n");

            } else {
                System.out.printf("oops, cannot find user");
                return;
            }

        } catch (SQLException ex) {
            System.out.printf("No top review for such movie");
        }

    }


    // Tested. Select the author of  top voted review written 3 days ago
    public static void freeTicket(Connection conn) {
        if (Driver.CURRENT_USERID != 7000) {
            System.out.println("You are NOT authorized to do so.");
            return;
        }
        try {
            // This query will select the review_id and its count that satisfies the requirement
            String query0 = "select Endorsement.review_id, count(*) AS nor from Endorsement LEFT JOIN Review ON Endorsement.review_id = Review.review_id" +
                    " WHERE Review.review_date < timestamp({fn TIMESTAMPADD(SQL_TSI_DAY, -3, CURRENT_TIMESTAMP)}) " +
                    " GROUP BY Endorsement.review_id ORDER BY nor DESC ";

            PreparedStatement invoke_freeTicket = conn.prepareStatement(query0);
            ResultSet rs0 = invoke_freeTicket.executeQuery();
            int topReviewId = 0;
            int reviewVote = 0;
            int movieID = 0;

            if (rs0.next()) {
                topReviewId = rs0.getInt("review_id");
                reviewVote = rs0.getInt("nor");
            }

            // This query will  select the author of the top voted review, which we obtained in query0, and all other info about that review
            String query1 = "select * from Customer LEFT JOIN Review ON Customer.customer_id = Review.customer_id WHERE Review.review_id = (?)";
            PreparedStatement invoke_freeTicket1 = conn.prepareStatement(query1);
            invoke_freeTicket1.setInt(1, topReviewId);
            ResultSet rs1 = invoke_freeTicket1.executeQuery();
            if (rs1.next()) {
                movieID = rs1.getInt("movie_id");
            }

            // This query gets the movie title of the top review
            String titleQuery = "select movie_title from Movie where movie_id = (?)";
            PreparedStatement invoke_freeTicket2 = conn.prepareStatement(titleQuery);
            invoke_freeTicket2.setInt(1, movieID);
            ResultSet rs2 = invoke_freeTicket2.executeQuery();
            if (rs2.next()) {
                System.out.println(">>      The winner of  FREE TICKET is:  " + rs1.getString("customer_Name") + " !!");
                System.out.println(">>      User " + rs1.getString("customer_Name") + "'s review: \n>>      ");
                System.out.println(">>      `" + rs1.getString("review") + "`\n>>      ");
                System.out.println(">>      for movie `" + rs2.getString("movie_title") + "` has " + reviewVote + " votes, which is the top rated review written three days earlier.");
            } else {
                System.out.printf("There is no winner of free ticket for the past 3 days\n");
                return;
            }


        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.printf("There is no winner of free ticket for the past 3 days\n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }

    /**
     * NEED TEST *** procedure to find the movie with most reviews before a certain date
     **/
    public static void mostReview(Connection conn, String date) {

        if (date.matches("[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])")) {
            // Date format: yyyy-MM-dd
            String endTime = " 23:59:59";

            Timestamp endTimeStamp = Timestamp.valueOf(date + endTime);

            // execute query
            try {
                String query4 = "SELECT COUNT(review_id) AS reviewCnt, movie_id FROM Review GROUP BY movie_id ORDER BY reviewCnt DESC WHERE timestamp(Review.review_date) <= (?)";
                PreparedStatement invoke_mostReview = conn.prepareStatement(query4);
                invoke_mostReview.setTimestamp(1, endTimeStamp);

                ResultSet rs5 = invoke_mostReview.executeQuery();
                while (rs5.next()) {
                    //System.out.println("The movie with most reviews before date is: ");
                    System.out.println("The movie with most reviews before " + date + "is :       " + rs5.getString("movie_id"));

                }
                rs5.close();
            } catch (SQLException ex) {
                System.out.printf("Cannot find the movie with the most reviews");
            }
        }
    }

    // tested, but could display number of attendance
    public static void topBoxOfficeMovie(Connection conn) {
        try {
            String query0 = "select Attendance.movie_id, COUNT(*) AS topMovie from Attendance group by movie_id order by topMovie DESC";

            PreparedStatement invoke_topMovie = conn.prepareStatement(query0);
            ResultSet rs0 = invoke_topMovie.executeQuery();
            int movieId = 0;

            if (rs0.next()) {
                movieId = rs0.getInt("movie_id");
            }

            String query1 = "select movie_title from Movie where movie_id = (?) ";
            PreparedStatement invoke_topMovie1 = conn.prepareStatement(query1);
            invoke_topMovie1.setInt(1, movieId);
            ResultSet rs1 = invoke_topMovie1.executeQuery();

            if (rs1.next()) {
                System.out.println("top Box Office movie: " + rs1.getString("movie_title"));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("No top Box Office movie yet. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }


    // tested
    public static void topContributor(Connection conn) {
        try {
            String query0 = "select customer_id, COUNT(*) AS topContributor from Review group by customer_id order by topContributor DESC";
            PreparedStatement invoke_topContributor = conn.prepareStatement(query0);
            ResultSet rs0 = invoke_topContributor.executeQuery();
            int cusomerId = 0;
            int numOfReview = 0;

            if (rs0.next()) {
                cusomerId = rs0.getInt("customer_id");
                numOfReview = rs0.getInt("topContributor");
            } else {
                System.out.println("duh");
                return;
            }

            String query1 = "select customer_Name from Customer where customer_id = (?)";
            PreparedStatement invoke_findUser = conn.prepareStatement(query1);
            invoke_findUser.setInt(1, cusomerId);
            ResultSet rs1 = invoke_findUser.executeQuery();
            String theName = "";
            if (rs1.next()) {
                theName = rs1.getString("customer_Name");
                System.out.println("Top Contributor of iRate to date is User: " + theName);
                System.out.println("He/She contributed " + numOfReview + " reviews on iRate!");
            } else {
                System.out.println("duh");
                return;
            }


        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.printf("No top contributor found\n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }


    // tested
    public static void deleteMovie(Connection conn) {
        // Admin validation check
        if (Driver.CURRENT_USERID != 7000) {
            System.out.println("Amigo >>> You are NOT authorized to do so.");
            return;
        }

        System.out.print("Amigo >>> Enter movie title to delete : ");
        Scanner scannerName = new Scanner(System.in);
        String title = scannerName.nextLine();

        try {
            String query = "delete from Movie where movie_title = (?)";
            PreparedStatement invoke_deleteMovie = conn.prepareStatement(query);
            invoke_deleteMovie.setString(1, title);
            int rs0 = invoke_deleteMovie.executeUpdate();
            if (rs0 != 0) {
                System.out.println("The movie " + title + " has been deleted");
            } else {
                System.out.println("Can not find the movie" + title + ".");
                return;
            }
        } catch (SQLException ex) {
            System.out.println("Can not find the movie" + title + ".");
        }
    }

    // tested
    public static void addMovie(Connection conn) {
        // Admin validation check
        if (Driver.CURRENT_USERID != 7000) {
            System.out.println("Amigo >>> You are NOT authorized to do so.");
            return;
        }

        System.out.print("Amigo >>> Enter movie title to add : ");
        Scanner scannerName = new Scanner(System.in);
        String title = scannerName.nextLine();

        try {
            String query = "insert into Movie(movie_title) values (?)";
            PreparedStatement invoke_addMovie = conn.prepareStatement(query);
            invoke_addMovie.setString(1, title);
            int rs0 = invoke_addMovie.executeUpdate();
            if (rs0 != 0) {
                System.out.println("The movie " + title + " has been added");
            }
        } catch (SQLException ex) {
            System.out.println("Can not add the movie" + title + ".");
        }
    }

    // tested
    public static void registerUser(Connection conn) {
        if (Driver.CURRENT_USERID != 0) {
            logout();
            System.out.println("We logged you off for security reason. ");
        }

        System.out.print("Amigo >>> Enter your Name : ");
        Scanner scannerName = new Scanner(System.in);
        String userName = scannerName.nextLine();


        while (userName.equals("admin")) {
            System.out.print("Amigo >>> Invalid Name! Enter your name again : ");
            scannerName = new Scanner(System.in);
            userName = scannerName.nextLine();
        }

        System.out.print("Enter your Email : ");
        Scanner scannerEmail = new Scanner(System.in);
        String email = scannerEmail.nextLine();

        while (!isEmail(email)) {
            System.out.print("Amigo >>> Invalid Email! Enter your Email again : ");
            scannerEmail = new Scanner(System.in);
            email = scannerEmail.nextLine();
        }

        // prepare to insert new user in Customer table
        try {
            PreparedStatement insertRow_Customer = conn.prepareStatement(
                    "insert into Customer(customer_Name, email, join_date) values(?, ?, ? )");
            insertRow_Customer.setString(1, userName);
            insertRow_Customer.setString(2, email);
            insertRow_Customer.setTimestamp(3, getCurrentTimestamp());

            // USE executeUpdate() when insert into table
            int rs1 = insertRow_Customer.executeUpdate();

            if (rs1 == 1) {
                System.out.println("Successfully created user " + userName);
            } else {
                System.out.println("something wrong, might not insert successfully");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to create new customer. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }

    public static void logout() {
        Driver.CURRENT_USERID = 0;
        System.out.println("Amigo >>> You are now logged out");
    }

    public static void quit() {
        Driver.scanFlag = 0;
        System.out.println("Amigo >>> Bye.");
    }

    // tested
    public static int login(Connection conn) {
        int customerId = 0;
        System.out.print("Amigo >>> Enter your Name for login : ");
        Scanner scannerName = new Scanner(System.in);
        String userName = scannerName.nextLine();

        System.out.print("Amigo >>> Enter your Email for login : ");
        Scanner scannerEmail = new Scanner(System.in);
        String email = scannerEmail.nextLine();

        while (!isEmail(email)) {
            System.out.print("Amigo >>> Invalid Email! Enter your Email again for login : ");
            scannerEmail = new Scanner(System.in);
            email = scannerEmail.nextLine();
        }

        //excute query
        try {
            String query0 = "select * from Customer WHERE customer_Name = (?) AND email = (?)";
            PreparedStatement invoke_findUser = conn.prepareStatement(query0);
            invoke_findUser.setString(1, userName);
            invoke_findUser.setString(2, email);
            ResultSet rs0 = invoke_findUser.executeQuery();

            if (rs0.next()) {
                Driver.CURRENT_USERID = rs0.getInt("customer_id");

                System.out.println("Amigo >>> You are now logged in as : " + userName);
            } else {
                System.out.println("Amigo >>> No matching user found for this name/email. ");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to login. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
        return customerId;
    }

    // tested
    public static void voteReview(Connection conn) {
        int id = Driver.CURRENT_USERID;
        int movieId = 0;
        System.out.println("Enter the Name of movie which review you want to vote: ");
        Scanner scannerName = new Scanner(System.in);
        String movieName = scannerName.nextLine();
        int endorseCountBefore = 0;
        int endorseCountAfter = 0;

        try {
            ResultSet rsBefore = conn.prepareStatement("select count(*) from Endorsement").executeQuery();
            if (rsBefore.next()) {
                endorseCountBefore = rsBefore.getInt(1);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
//            System.out.println("Can not find the review_id. \n");
        }

        try {
            String query0 = "select movie_id from Movie WHERE movie_title = (?)";
            PreparedStatement invoke_findTitle = conn.prepareStatement(query0);
            invoke_findTitle.setString(1, movieName);
            ResultSet rs0 = invoke_findTitle.executeQuery();
            if (rs0.next()) {
                movieId = rs0.getInt("movie_id");
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.println("Can not find the movie. \n");
        }

        Set<Integer> reviewIds = new HashSet<Integer>();
        try {
            String query3 = "select * from Review WHERE movie_id = (?)";
            PreparedStatement invoke_findReview = conn.prepareStatement(query3);
            invoke_findReview.setInt(1, movieId);
            ResultSet rs4 = invoke_findReview.executeQuery();

            while (rs4.next()) {
                int ids = rs4.getInt("review_id");
                reviewIds.add(ids);
                System.out.printf("Review_id : " + ids + "   Content :  ");
                System.out.println(rs4.getString("review"));
            }
            rs4.close();
        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.println("Can not find the review_id. \n");
        }
        if (reviewIds.size() == 0) {
            System.out.println("Can not find review for this movie. \n");
            return;
        }

        System.out.println("Enter the Review_id of review you want to vote: ");
        Scanner scannerId = new Scanner(System.in);
        String choosenid = scannerId.nextLine();


        int choosenId = 0;
        try {
            choosenId = Integer.parseInt(choosenid);
        } catch (NumberFormatException e) {
        }

        while (!reviewIds.contains(choosenId)) {
            System.out.println("Not a valid review id or number format  !! Enter again : ");
            scannerId = new Scanner(System.in);
            choosenid = scannerId.nextLine();
            try {
                choosenId = Integer.parseInt(choosenid);
            } catch (NumberFormatException e) {
            }
        }


        try {
            PreparedStatement insertRow_Endorsement = conn.prepareStatement(
                    "insert into Endorsement(review_id, endorser_id, endorse_date) values(?, ?, ?)");
            insertRow_Endorsement.setInt(1, choosenId);
            insertRow_Endorsement.setInt(2, id);
            insertRow_Endorsement.setTimestamp(3, getCurrentTimestamp());

            int rs2 = insertRow_Endorsement.executeUpdate();
            if (rs2 != 1) {
                System.out.println("Did not vote the review successfully. Please try again!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to vote review. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }

        //after
        try {
            ResultSet rsBefore = conn.prepareStatement("select count(*) from Endorsement").executeQuery();
            if (rsBefore.next()) {
                endorseCountAfter = rsBefore.getInt(1);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
//            System.out.println("Can not find the review_id. \n");
        }

        if (endorseCountAfter > endorseCountBefore) {
            System.out.println("Endorse review successfully !");

        } else {
            System.out.println("Did not vote the review successfully. Please try again!");
        }

    }

    // tested
    public static void buyTicket(Connection conn) {
        int id = Driver.CURRENT_USERID;
        int movieId = 0;


        System.out.println("Enter the Name of movie you want to watch: ");
        Scanner scannerName = new Scanner(System.in);
        String movieName = scannerName.nextLine();

        try {
            String query0 = "select movie_id from Movie WHERE movie_title = (?)";
            PreparedStatement invoke_findTitle = conn.prepareStatement(query0);
            invoke_findTitle.setString(1, movieName);
            ResultSet rs0 = invoke_findTitle.executeQuery();
            if (rs0.next()) {
                movieId = rs0.getInt("movie_id");
            } else {
                System.out.println("Can not find the movie. \n");
            }
        } catch (SQLException ex) {
            System.out.println("Error message: " + ex.getMessage() + "\n");
            System.out.println("Can not find the movie. \n");
        }

        try {
            PreparedStatement insertRow_Attendance = conn.prepareStatement(
                    "insert into Attendance(customer_id, movie_id, attendance_date) values(?, ?, ?)");
            insertRow_Attendance.setInt(1, id);
            insertRow_Attendance.setInt(2, movieId);
            insertRow_Attendance.setTimestamp(3, getCurrentTimestamp());

            int rs2 = insertRow_Attendance.executeUpdate();
            if (rs2 == 1) {
                System.out.println("Successfully buy the ticket. ");
            } else {
                System.out.println("Did not buy the ticket successfully. Please try again!");
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.printf("Failed to buy the ticket. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }

    // tested
    public static void reviewMovie(Connection conn) {
        int id = Driver.CURRENT_USERID;
        int movieId = 0;
        System.out.println("You can only review ONCE for each movie you viewed.\nEnter the Name of movie you want to review: ");
        Scanner scannerName = new Scanner(System.in);
        String movieName = scannerName.nextLine();
        String[] ratingStr = {"0", "1", "2", "3", "4", "5"};

        try {
            String query0 = "select movie_id from Attendance Where customer_id = (?) AND movie_id = (select movie_id from Movie WHERE movie_title = (?))";
            PreparedStatement invoke_findTitle = conn.prepareStatement(query0);

            invoke_findTitle.setInt(1, id);
            invoke_findTitle.setString(2, movieName);

            ResultSet rs0 = invoke_findTitle.executeQuery();
            if (rs0.next()) {
                movieId = rs0.getInt("movie_id");
            } else {
                System.out.println("Cannot find such movie. ");
                return;
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.println("Can not find the movie. \n");
        }

        System.out.println("Rate the movie from 0 to 5: ");
        Scanner scannerRat = new Scanner(System.in);
        String reviewRat = scannerRat.nextLine();

        while (!Arrays.asList(ratingStr).contains(reviewRat)) {
            System.out.println("Not a valid rating! Enter again: ");
            scannerRat = new Scanner(System.in);
            reviewRat = scannerRat.nextLine();
        }

        int rating = Integer.parseInt(reviewRat);


        System.out.println("Feel free to write your review: ");
        Scanner scannerRev = new Scanner(System.in);
        String rev = scannerRev.nextLine();


        try {
            PreparedStatement insertRow_Review = conn.prepareStatement(
                    "insert into Review(customer_id, movie_id, review_date, rating, review) values(?, ?, ?, ?, ?)");
            insertRow_Review.setInt(1, id);
            insertRow_Review.setInt(2, movieId);
            insertRow_Review.setTimestamp(3, getCurrentTimestamp());
            insertRow_Review.setInt(4, rating);
            insertRow_Review.setString(5, rev);


            int rs2 = insertRow_Review.executeUpdate();
            if (rs2 == 1) {
                System.out.println("Successfully make the review. ");
            } else {
                System.out.println("Did not make review successfully!");
            }
        } catch (SQLException ex) {
            //ex.printStackTrace();
            System.out.printf("Failed to make review. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }

    }


    //TODO: select popular movies sort by number of reviews, high to low
    public static void popularMovie(Connection conn) {

    }

    //TODO: select movies sort by average ratings, high to low
    public static void topRatedMovie(Connection conn) {

    }

    //TODO: select user who voted most
    public static void topVoter(Connection conn) {

    }

    //TODO: select user who attended most
    public static void topViewer(Connection conn) {

    }

}
