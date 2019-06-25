# iRate Database Managing Project
## Project Overview
This is the capstone project for Database Management System course in Northeastern University - Silicon Valley Campus. The iRate project is a portion of an application that enables registered movie theater customers to rate a movie that they saw at the theater, and for other registered customers to vote for reviews.


This project is made by [Xun Wang](https://xw321.github.io/), [Yan Zhao](https://yzhao430.github.io/), and [Fang Hong](https://sososummer88.github.io/).

## Technology
During the development, we utilized following technologies:
* Java
* Derby

## Features of The Project
* iRate is a social media application that encourages theater customers to rate a movie that they saw at the theater in the past week and write a short review.
* Other customers can vote one review of a particular movie as "helpful" each day.
* The writer of the top rated review of a movie written three days earlier receives a free movie ticket, and voting is closed for all reviews of the movie written three days ago. 
* Someone who voted one or more movie reviews as "helpful" on a given day will be chosen to receive a free concession item. 

## Project Details
This project is to develop and document a data model for representing entities and relationships in this promotional social media application, provide DDL for creating the tables, DML for editing entries in the tables, and DQL for making commonly used queries to retrieve information about the status of reviews and votes from the database. 

### 1. Entities
* Movie
* Customer
* Review

#### Movie table contains two attributes:
1) movie_title: title of the movie as varchar and not null;
2) movie_id (primary key): self-generated identity of the movie as int (start with 1, increment by 1);

#### Customer table contains five attributes:
1) customer_id (primary key): self-generated id of customer as int (start with 1, increment by 1);
2) customer_Name: name of customers as varchar and not null;
3) email: customer's email as varchar and not null;
4) address: customer's address as varchar and not null;
5) join_date: date customer registered as date and not null;

#### Review table contains six attributes
1) review_id (primary key): self-generated id of review as int (start with 1, increment by 1);
2) customer_id: a foreign key references customer_id in the Customer table. If a customer is deleted, all of his/her reviews and endorsement are deleted;
3) movie_id: a foreign key references movie_id in the Movie table. If a movie is deleted, all of its reviews are also deleted;
4) review_date: date the review is created as date and not null;
5) rating (check between 0 and 5): rating of the movie that is given by customer as int and not null;
6) review (text): review that is writen by customer as varchar and not nulll;


### 2. Relationship
* Attendance
* Endorsement

#### Attendance table contains three attributes:
1) movie_id: a foreign key references movie_id in the Movie table;
2) customer_id: a foreign key references customer_id in the Customer table;
3) attendance_date: date the customer attended a specific movie as date;
movie_id, customer_id, and attendance_date together are primary keys.

#### Endorsement table contains three attributes:
1) review_id: a foreign key references review_id in the review table;
2) endorse_id: id of an endorsement;
3) endorse_date: date a review is endorsed as date;
review_id, endorse_id, and endorse_date together are primary keys. 

### 3. Constraints 
* If a customer does not provide a valid email address, he/she cannot be added to the table.
* If a customer did not attend a movie, he/she cannot review the movie.
* The date of the review cannot be provided before the attendance date and must be within 7 days of the most recent attendance of the movie
* If a customer is the one who wrote the review, he/she cannot endorse it.
* If the date of the endorse is after 3 days when the review was written, customer cannot endorse it.
* If the customer has endorsed one review of a particular movie on a given day, he/she cannot endorse second time on that day.

![Data Model](https://github.com/xw321/iRate-Movie-Ratings/blob/master/iRate%20data%20model.png)

### 4. Triggers
1) review_limit_by_attendance: this trigger is created to make sure a customer has to attend a movie before he/she can review it.
2) review_limit_by_date: this trigger is created to prevents any invalid review such that it has a review_date earlier than its actual attendance date.
3) review_limit_by_date2: this trigger is created to delete the inserted review if the current date is longer than 7 days than reviewee's attendance date.
4) endorse_limit_by_date: this trigger is created to make sure review from customers has to have earlier time than its endorsements.
5) endorse_limit_by_customer: this trigger is created to delete the inserted endorsement if the customer is the one who wrote the review.
6) endorse_limit_by_oneDay: this trigger is created to make sure that customers can only endorse once everyday.

### 5. Stored Functions
- isEmail(): function to check the validation of the email.
- freeGift(): function to select the customers who get the concession.
- freeTicket(): function to select the author of the top voted review written 3 days ago.

### 6. Functions
### Admin Functions
- freeGift(): function to select the customers who get the concession.
- freeTicket(): function to select the author of the top voted review written 3 days ago.
- addMovie(): administrator could use this function to add a movie to the database.
- deleteMovie(): administrator could use this function to delete a movie from the database.

### Client Functions
- registerUser(): customers/administrator are able to use this to register for an account by entering their name and email address. Once registered, customers and administor will be able to use different functions that are available to them.
- login(): function for registered customer to login.
- logout(): function for userer to logout.
- quit(): function to quit the application.
- buyTicket(): function to let customers to buy a movie.
- reviewMovie(): function that let customers to enter review of a movie.
- voteReview(): function that let custoers to vote a movie.
- topReview(): function to show the top voted review of a movie.
- mostReview(): function to find the movie with most reviews before a certain date.
- topBoxOfficeMovie(): function to find the movie with most attendance.
- topContributor(): function to find customer who write most reviews.
- movieRating(): function to find the average rating of the selected movie.

## Future Improvement
- To provide a web-based application and implement the front-end to the database. 
- To implement index in the tables to support large dataset and fast query.
- To provide more analytical queries: peak times that customers will attend a movie


## Reference to the Class with Link:
For more information about the class, please go to this page:
http://www.ccis.northeastern.edu/home/pgust/classes/cs5200/2019/Summer1/index.html
