

import java.util.List;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.*;
import org.json.simple.parser.ParseException;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.TwitterException;

public class Book extends connecttodb {
	private int ID;
	private String BookTitle;
	private String BookAuthor;
	private String BookSum;
	private Boolean Availability;
	private int wl; // count of how many users are on the wait list

	public Book(int ID, String BookTitle, String BookAuthor, String BookSum, Boolean Availability, int wl) {
		this.ID = ID;
		this.BookTitle = BookTitle;
		this.BookAuthor = BookAuthor;
		this.BookSum = BookSum;
		this.Availability = false;
		this.wl = wl;
	}

	public Book() {
		ID = 0;
		BookTitle = "";
		BookAuthor = "";
		Availability = false;
		wl = 0;
	}

	public int getID() {
		return ID;
	}

	public String getBookTitle() {
		return BookTitle;
	}

	public String getBookAuthor() {
		return BookAuthor;
	}

	public String getBookSum() {
		return BookSum;
	}

	public int getWL() {
		return wl;
	}

	public void setID(int ID) {
		this.ID = ID;
	}

	public void setBookTitle(String BookTitle) {
		this.BookTitle = BookTitle;
	}

	public void setBookSum(String BookSum) {
		this.BookSum = BookSum;
	}

	public void setBookAuthor(String BookAuthor) {
		this.BookAuthor = BookAuthor;
	}

	public Boolean getAvailability() {
		return Availability;
	}

	public void setAvailability(Boolean Availablility, String bid) throws SQLException, ClassNotFoundException {
		this.Availability = Availablility;
	}

	public String IsBookAvailable() {
		return this.Availability ? "It is available now!           Enter 'B' to rent the book.    "
				: "Currently unavailable.           Enter 'W' to join the waitlist.";
	}

	public void setWL(int wl, String bid) throws SQLException, ClassNotFoundException {
		this.wl = wl;
	}
 
	public boolean changeWL(int wl, String bid) throws SQLException, ClassNotFoundException {
		int id = Integer.parseInt(bid);
		if (wl >= 0 && bookListLength() > id && id > 0 && checkAvailability(bid)==false) { 
			String sql = "UPDATE books SET wl = ? WHERE id ='" + bid + "'";
			PreparedStatement stmt = getConnect().prepareStatement(sql);
			stmt.setInt(1, wl);
			stmt.executeUpdate();// UPDATE DATABASE after joining a waitlist

			String sql2 = "select * from books where id ='" + bid + "'";
			PreparedStatement stmt2 = getConnect().prepareStatement(sql2);
			ResultSet list = stmt2.executeQuery();
			while (list.next()) {
				setWL(list.getInt(6), bid);// update wl that will displayed
			}
			return true; 
		} else {
			return false;
		}
	}
	 // check if this login session's email has RENTED THIS book in the database
		public boolean borrowedBook(String uemail, String bid) throws SQLException, ClassNotFoundException {
			ResultSet r = getResultSet("SELECT email FROM waitlists where id ='"+bid+"' ORDER BY created_at ASC LIMIT 1");//only is they are the first on the list
			// Use while loop to check existence until the end of the email column
			while (r.next()) {
				if (r.getString("email").equals(uemail)) {
					return true;
				}
			}
			return false;
		}
		
		// check if this login session's email has JOINED THIS BOOK'S waitlist in the database
		public boolean inWaitlist(String uemail, String bid) throws SQLException, ClassNotFoundException {
			ResultSet r = getResultSet("select * from waitlists where id = '"+bid+"'");
			// Use while loop to check existence until the end of the email column
			while (r.next()) {
				if (r.getString("email").equals(uemail)) {
					return true;
				}
			}
			return false;
		}

	public int bookListLength() throws ClassNotFoundException, SQLException {
		String sql = "select * from books";
		PreparedStatement stmt = getConnect().prepareStatement(sql);
		ResultSet result = stmt.executeQuery();
		int size = 0;
		while (result.next()) {
			size++;
		}
		return size;
	}
        
        public int requestListLength() throws SQLException, ClassNotFoundException{
            String sql = "select * from request";
		PreparedStatement stmt = getConnect().prepareStatement(sql);
		ResultSet result = stmt.executeQuery();
		int size = 0;
		while (result.next()) {
			size++;
		}
		return size;
        }

	public boolean checkAvailability(String bid) throws ClassNotFoundException, SQLException {
		String sql = "select * from books where id ='" + bid + "'";
		PreparedStatement stmt = getConnect().prepareStatement(sql);
		ResultSet result = stmt.executeQuery();
		while (result.next()) {
			if (result.getInt(1) == Integer.parseInt(bid)) {
				return result.getBoolean(5);
			}
		}
		return false;
	}

	public boolean changeAvailability(Boolean i, String bid) {
		// if availability changed to true, its waitlist should be 0
		if (Boolean.TRUE.equals(i)) {
			try {
				String sql = "UPDATE books SET availability = ?,wl=? WHERE id ='" + bid + "'";
				PreparedStatement stmt = getConnect().prepareStatement(sql);
				stmt.setBoolean(1, i);
				stmt.setInt(2, 0);
				stmt.executeUpdate();// UPDATE DATABASE after joining a waitlist
				return true;
			} catch (Exception e) {
				System.out.println("Execution stoped.");
				return false;
			}
			// if availability is false, just change the availability on db
		} else if (Boolean.FALSE.equals(i)) {
			try {
				String sql = "UPDATE books SET availability = ? WHERE id ='" + bid + "'";
				PreparedStatement stmt = getConnect().prepareStatement(sql);
				stmt.setBoolean(1, i);
				stmt.executeUpdate();// UPDATE DATABASE after joining a waitlist
				return true;
			} catch (Exception e) {
				System.out.println("Execution stoped.");
				return false;
			}
		}
		try { 
			changeWL(0, bid);
			return true;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;

	}

	public void binfo(String bid) throws SQLException, ClassNotFoundException {
		String sql = "select * from books where id ='" + bid + "'";
		PreparedStatement stmt = getConnect().prepareStatement(sql);
		ResultSet list = stmt.executeQuery();
		while (list.next()) {
			setID(list.getInt(1));// set id in class to the one selected
			setBookTitle(list.getString(2));// title
			setBookAuthor(list.getString(3));// author
			setBookSum(list.getString(4));// summary of book
			setAvailability(list.getBoolean(5), bid);// get availablility of book
			setWL(list.getInt(6), bid);// get number of people on the waitlist
		}
	}
        
        public void rbinfo(String bid) throws SQLException, ClassNotFoundException{
            String sql = "select * from request where id ='" + bid + "'";
		PreparedStatement stmt = getConnect().prepareStatement(sql);
		ResultSet list = stmt.executeQuery();
		while (list.next()) {
			setID(list.getInt(1));// set id in class to the one selected
			setBookTitle(list.getString(2));// title
			setBookAuthor(list.getString(3));// author
			setBookSum(list.getString(4));// summary of book
			setAvailability(list.getBoolean(5), bid);// get availablility of book
			setWL(list.getInt(6), bid);// get number of people on the waitlist
		}
        }
        
        //get book information based on the inputed book's ISBN number
    public void BookAPICon(String isbn) throws IOException, ParseException{
        String title;
        String author;
        String summary;
        Availability =false;
        wl = 0;
            try{
                
                URL url = new URL("https://openlibrary.org/api/books?bibkeys=ISBN:"+isbn+"&jscmd=details&format=json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();
                
                int responseCode = conn.getResponseCode();
                
                if(responseCode!=200){
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }else{
                    
                    StringBuilder informationString = new StringBuilder();
                    Scanner scanner = new Scanner(url.openStream());
                    
                    while(scanner.hasNext()){
                        informationString.append(scanner.nextLine());
                    }
                    scanner.close();
                    
                    System.out.println(informationString);
                    
                    
                    JSONObject jobj = new JSONObject(informationString.toString());
                   
                    //get ISBN object which contains main information about the book
                    JSONObject content = jobj.getJSONObject("ISBN:"+isbn);
                    JSONObject details = content.getJSONObject("details");
                    
                    //get title object
                    title =  (String) details.get("title");
                    setBookTitle(title);
                   // System.out.println(title);
                    
                    //get author object
                    JSONArray authors = (JSONArray) details.get("authors");
                    author = authors.getJSONObject(0).get("name").toString();
                    setBookAuthor(author);
                   // System.out.println(author);
                    
                    //get description object
                    try{
                       summary = (String) details.getString("description");
                       setBookSum(summary);
                    }catch(Exception e){
                       summary = null;
                    }
                    //  System.out.println(summary);         
                }                
            } catch (MalformedURLException ex) {
                Logger.getLogger(Book.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
    
    public void TwitterOpinion() throws TwitterException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true).setOAuthConsumerKey("N2E7u9dxNW9BwlVCt5ToAsARK")
                                             .setOAuthConsumerSecret("aoVBcsLrfc9wPTA30SMU5CrFcQ9DC6as0sGF9mAY6gRKHcSXrz")
                                             .setOAuthAccessToken("3259811256-l5fk6jwUGzaA9tT9GOyTmh3jaF4TtJrwoI4wI4v") 
                                             .setOAuthAccessTokenSecret("QqVDOW6vHCPbwhorEUCbYi1CttjglsQc1p38n5qniYl4B");

      TwitterFactory tf = new TwitterFactory(configurationBuilder.build());
      twitter4j.Twitter twitter = tf.getInstance();
      
      Query query = new Query("book");
//      query.setResultType(Query.RECENT);
//      query.setCount(1);
      
      QueryResult result = twitter.search(query);
//      
//      List<Status> status = twitter.getHomeTimeline();
//      for(Status s:status){
//          System.out.println(s.getUser().getName()+ " "+s.getText());
//      }
//      
     System.out.println(result.getTweets());
}
    
}


