
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Statement;


public class printTable {
	static void printCustomer(Connection conn) {
		try (
			// statement is channel for sending commands thru connection 
		    Statement stmt = conn.createStatement();
		){
			System.out.printf("Table Customer:\n");
			ResultSet rs = stmt.executeQuery("select * from Customer");
			while (rs.next()) {
				int customer_id = rs.getInt(1);
				String customer_Name = rs.getString(2);
				String email = rs.getString(3);
				Timestamp join_date = rs.getTimestamp(4);
				System.out.printf("Id: %d Name: %s Email: %s Date: %s\n", customer_id, customer_Name, email, join_date);
				System.out.println("");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void printMovie(Connection conn) {
		try (
			// statement is channel for sending commands thru connection 
		    Statement stmt = conn.createStatement();
		){
			System.out.printf("Table Movie:\n");
			ResultSet rs = stmt.executeQuery("select * from Movie");
			while (rs.next()) {
				String movie_title = rs.getString(1);
				int movie_id = rs.getInt(2);
				System.out.printf("Title: %s Id: %s \n", movie_title, movie_id);
				System.out.println("");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void printReview(Connection conn) {
		try (
			// statement is channel for sending commands thru connection 
		    Statement stmt = conn.createStatement();
		){
			System.out.printf("Table Review:\n");
			ResultSet rs = stmt.executeQuery("select * from Review");
			while (rs.next()) {
				int review_id = rs.getInt(1);
				int customer_id = rs.getInt(2);
				int movie_id = rs.getInt(3);
				Timestamp review_date = rs.getTimestamp(4);
				int rating = rs.getInt(5);
				String review = rs.getString(6);
				System.out.printf("Review_id: %d Customer_id: %s Movie_id: %s Date: %s Rating: %s\n", review_id, customer_id, movie_id, review_date, rating);
				System.out.println("Review: " + review);

			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void printAttendance(Connection conn) {
		try (
			// statement is channel for sending commands thru connection 
		    Statement stmt = conn.createStatement();
		){
			System.out.printf("Table Attendance:\n");
			ResultSet rs = stmt.executeQuery("select * from Attendance");
			while (rs.next()) {
				int movie_id = rs.getInt(1);
				int customer_id = rs.getInt(2);
				Timestamp attendance_date = rs.getTimestamp(3);
				System.out.printf("Movie_id: %d Customer_id: %s Date: %s\n", movie_id, customer_id, attendance_date);
				System.out.println("");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	static void printEndorsement(Connection conn) {
		try (
			// statement is channel for sending commands thru connection 
		    Statement stmt = conn.createStatement();
		){
			System.out.printf("Table Endorsement:\n");
			ResultSet rs = stmt.executeQuery("select * from Endorsement");
			while (rs.next()) {
				int review_id = rs.getInt(1);
				int endorser_id = rs.getInt(2);
				Timestamp endorse_date = rs.getTimestamp(3);
				System.out.printf("Review_id: %d Endorser_id: %s Date: %s\n", review_id, endorser_id, endorse_date);
				System.out.println("");
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


}



  
