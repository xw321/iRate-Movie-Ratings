import java.sql.*;
import java.util.Scanner;

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


    public static java.sql.Timestamp getCurrentTimestamp() {
        java.util.Date date = new java.util.Date();
        return new java.sql.Timestamp(date.getTime());
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

    // Show the top voted review of a movie
    public static void topReview(Connection conn, String movie) {

        try {
            // This query gets the movie title of the top review
            String titleQuery = "select movie_id from Movie where movie_title = (?)";
            PreparedStatement invoke_getMovieId = conn.prepareStatement(titleQuery);
            invoke_getMovieId.setString(1, movie);
            ResultSet rs2 = invoke_getMovieId.executeQuery();
            int movieId = 0;
            if (rs2.next()) {
                movieId = rs2.getInt("movie_id");
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
            }

            // This query will  select the author of the top voted review, which we obtained in query0, and all other info about that review
            String query1 = "select * from Customer LEFT JOIN Review ON Customer.customer_id = Review.customer_id WHERE Review.review_id = (?)";
            PreparedStatement invoke_getAuthor = conn.prepareStatement(query1);
            invoke_getAuthor.setInt(1, topReviewId);
            ResultSet rs1 = invoke_getAuthor.executeQuery();

            if (rs1.next()) {
                System.out.println("Top review of movie " + movie + " is by user " + rs1.getString("customer_Name") + ". It has " + reviewVote + " votes : \n");
                System.out.println("`" + rs1.getString("review") + "`\n");

            }

        } catch (SQLException ex) {
            System.out.printf("No top review for such movie");
        }

    }


    // Select the author of  top voted review written 3 days ago
    public static void freeTicket(Connection conn) {
        try {
            // This query will select the review_id and its count that satisfies the requirement
            String query0 = "select Endorsement.review_id, count(*) AS nor from Endorsement LEFT JOIN Review ON Endorsement.review_id = Review.review_id" +
                    " WHERE Review.review_date BETWEEN timestamp({fn TIMESTAMPADD(SQL_TSI_DAY, -3, CURRENT_TIMESTAMP)}) AND CURRENT_TIMESTAMP " +
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
                System.out.println(">>      for movie `" + rs2.getString("movie_title") + "` has " + reviewVote + " votes, which is the top rated review within the past 3 days.");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("There is no winner of free ticket for the past 3 days\n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }

    // procedure to find the movie with most reviews before a certain date
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

    //TODO: select popular movies sort by number of reviews, high to low
    public static void popularMovie(Connection conn) {

    }

    //TODO: select movies sort by average ratings, high to low
    public static void topRatedMovie(Connection conn) {

    }


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
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("dasdas\n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }

    //TODO: select user who voted most
    public static void topVoter(Connection conn) {

    }

    //TODO: select user who attended most
    public static void topViewer(Connection conn) {

    }

    public static void deleteMovie(Connection conn, String title) {
        try {
            String query = "delete from Movie where movie_title = (?)";
            PreparedStatement invoke_deleteMovie = conn.prepareStatement(query);
            invoke_deleteMovie.setString(1, title);
            int rs0 = invoke_deleteMovie.executeUpdate();
            if (rs0 != 0) {
                System.out.println("The movie " + title + " has been deleted");
            }
        } catch (SQLException ex) {
            System.out.println("Can not find the movie" + title + ".");
        }
    }

    public static void addMovie(Connection conn, String title) {
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
    
    //this function can let user write a review and rating of a movie
    public static void reviewMovie(Connection conn) {
        // check where is a customer or admin
    	int currentUser = Driver.CURRENT_USERID;
    	
    	// if admin, then do nothing.
    	if (currentUser == 0) {
    		return;
    	}
    	
    	// Ask customer to enter information
    	System.out.print ("Enter the movie title that you would like to review: ");
    	Scanner scannerMovieTitle = new Scanner(System.in);
        String movieTitle = scannerMovieTitle.nextLine();
        
        System.out.print ("Enter the your rating of this movie: ");
    	Scanner scannerRate = new Scanner(System.in);
        int rating = scannerRate.nextInt();
    	
    	System.out.print ("Enter the your review: ");
     	Scanner scannerReview = new Scanner(System.in);
        String review= scannerReview.nextLine();
        
        int movieID = 0;
        try {
        	
        	// Get movie_id from movie_title that customer entered
        	String query = "SELECT movie_id from Movie where Movie.movie_title = (?)";
            PreparedStatement invoke_getMovieId = conn.prepareStatement(query);
            invoke_getMovieId.setString(1, movieTitle);
            ResultSet rs0 = invoke_getMovieId.executeQuery();
            
            if (rs0.next()) {
            	movieID = rs0.getInt("movie_id");
            }
        	 
        	//insert the review information
            PreparedStatement insertRow_Review = conn.prepareStatement(
                    "insert into Review ( customer_id, movie_id, review_date, rating, review) values(?, ?, ?, ?, ?)");
            insertRow_Review.setInt(1, currentUser);
            insertRow_Review.setInt(2, movieID);
            insertRow_Review.setTimestamp(3, getCurrentTimestamp());
            insertRow_Review.setInt(4, rating);
            insertRow_Review.setString(5, review);

            // USE executeUpdate() when insert into table
            int rs1 = insertRow_Review.executeUpdate();

            if (rs1 == 1) {
                System.out.println("Successfully reviewed the movie:  " + movieTitle);
            } else {
                System.out.println("something wrong, might not insert successfully");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to create new customer. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
    }
    

    /*TODO: display given user's number of different actions:
     how many reviews he/she wrote;
     how many votes he/she gave;
     how many movies he/she attend;
     */
    
    public static void registerUser(Connection conn) {
        System.out.print("Enter your Name : ");
        Scanner scannerName = new Scanner(System.in);
        String userName = scannerName.nextLine();


        while (userName.equals("admin")) {
            System.out.print("Invalid Name! Enter your name again : ");
            scannerName = new Scanner(System.in);
            userName = scannerName.nextLine();
        }

        System.out.print("Enter your Email : ");
        Scanner scannerEmail = new Scanner(System.in);
        String email = scannerEmail.nextLine();

        while (!isEmail(email)) {
            System.out.print("Invalid Email! Enter your Email again : ");
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

    public static int login(Connection conn) {
        int customerId = 0;
        System.out.print("Enter your Name for login : ");
        Scanner scannerName = new Scanner(System.in);
        String userName = scannerName.nextLine();

        System.out.print("Enter your Email for login : ");
        Scanner scannerEmail = new Scanner(System.in);
        String email = scannerEmail.nextLine();

        while (!isEmail(email)) {
            System.out.print("Invalid Email! Enter your Email again for login : ");
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
                customerId = rs0.getInt("customer_id");
            }


        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to create new customer. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
        return customerId;
    }
    
    public static void buyTicket(Connection conn) {
    	int id = Driver.CURRENT_USERID;
    	int movieId =0;
    	System.out.println("Enter the Name of movie you want to watch: ");
    	Scanner scannerName = new Scanner(System.in);
    	String movieName = scannerName.nextLine();
    	
    	try {
    		String query0 = "select movie_id from Movie WHERE movie_title = (?)";
    		PreparedStatement invoke_findTitle = conn.prepareStatement(query0);
    		invoke_findTitle.setString(1,movieName);
    		ResultSet rs0 = invoke_findTitle.executeQuery();
    		if(rs0.next()) {
    			movieId = rs0.getInt("movie_id");
    		}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		System.out.println("Can not find the movie. \n");
    	}
    	
    	try {
    		PreparedStatement insertRow_Attendance = conn.prepareStatement(
    				"insert into Attendance(movie_id, customer_id, attendance_date) values(?, ?, ?)");
    		insertRow_Attendance.setInt(1, movieId);
    		insertRow_Attendance.setInt(2, id);
    		insertRow_Attendance.setTimestamp(3, getCurrentTimestamp());
    		
    		int rs2 = insertRow_Attendance.executeUpdate();
    		if(rs2 == 1) {
    			System.out.println("Successfully buy the ticket. ");
    		} else {
    			System.out.println("Did not buy the ticket successfully. Please try again!");
    		}
    	} catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to buy the ticket. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
        
        public static void voteReview(Connection conn) {
    	int id = Driver.CURRENT_USERID;
    	int movieId =0;
    	System.out.println("Enter the Name of movie which review you want to vote: ");
    	Scanner scannerName = new Scanner(System.in);
    	String movieName = scannerName.nextLine();
    	
    	try {
    		String query0 = "select movie_id from Movie WHERE movie_title = (?)";
    		PreparedStatement invoke_findTitle = conn.prepareStatement(query0);
    		invoke_findTitle.setString(1,movieName);
    		ResultSet rs0 = invoke_findTitle.executeQuery();
    		if(rs0.next()) {
    			movieId = rs0.getInt("movie_id");
    		}
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		System.out.println("Can not find the movie. \n");
    	}
    	
    	try {
    		String query3 = "select * from Review WHERE movie_id = (?)";
    		PreparedStatement invoke_findReview = conn.prepareStatement(query3);
    		invoke_findReview.setInt(1,movieId);
    		ResultSet rs4 = invoke_findReview.executeQuery();
    		while(rs4.next()) {
    			System.out.print(rs4.getInt("review_id")+ "   ");
    			System.out.println(rs4.getString("review"));
    		}
    		rs4.close();
    	} catch (SQLException ex) {
    		ex.printStackTrace();
    		System.out.println("Can not find the review_id. \n");
    	}
    	
    	System.out.println("Enter the Review_id of review you want to vote: ");
    	Scanner scannerId = new Scanner(System.in);
    	String choosenid = scannerId.nextLine();
    	int choosenId = Integer.parseInt(choosenid);
    	System.out.println(choosenId);
    	System.out.println(id);
    	System.out.println(getCurrentTimestamp());
	
    	
    	try {
    		PreparedStatement insertRow_Endorsement = conn.prepareStatement(
    				"insert into Endorsement(review_id, endorser_id, endorse_date) values(?, ?, ?)");
    		insertRow_Endorsement.setInt(1, choosenId);
    		insertRow_Endorsement.setInt(2, id);
    		insertRow_Endorsement.setTimestamp(3, Timestamp.valueOf("2019-06-26 12:03:20"));
    		
    		int rs2 = insertRow_Endorsement.executeUpdate();
    		if(rs2 == 1) {
    			System.out.println("Successfully vote for the review. ");
    		} else {
    			System.out.println("Did not vote the review successfully. Please try again!");
    		}
    	} catch (SQLException ex) {
            ex.printStackTrace();
            System.out.printf("Failed to vote review. \n");
            System.out.println("Error message: " + ex.getMessage() + "\n");

        }
 
    }
 
    }


}
