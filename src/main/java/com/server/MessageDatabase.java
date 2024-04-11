package com.server;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

import org.apache.commons.codec.digest.Crypt;
import org.json.JSONArray;
import org.json.JSONObject;

public class MessageDatabase {
    
    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private ThreadLocal<String> currentUser = new ThreadLocal<>(); // The current user's username stored during authentication
    private SecureRandom secureRandom = new SecureRandom();

    private String preparedInsertUser = "INSERT INTO users VALUES (?, ?, ?, ?)";
    private String preparedInsertMessage = "INSERT INTO messages VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private MessageDatabase() {
        try {
            open("MessageDB");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    /**
    * Gets the database instance. If one does not exist, a new instance is created.
    * @return the database instance.
    */
    public static synchronized MessageDatabase getInstance() {
        if(dbInstance == null){
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    /**
    * Opens the database file. New file is created and initialized if one does not exists.
    * @param dbName The name of the database file.
    * @throws SQLException when database file initialization fails.
    */
    public void open(String dbName) throws SQLException {

        File dbFile = new File(dbName); // Check if the file exists and is not a directory
        boolean fileExists = dbFile.exists() && !dbFile.isDirectory();
        
        String database = "jdbc:sqlite:" + dbName; // SQLite will automatically create a file is one did not exist
        dbConnection = DriverManager.getConnection(database);

        // Initialize the automatically created file
        if(!fileExists) {
            initializeDatabase();
        }
    }

    /**
    * Initializes the database by creating USERS and MESSAGES tables.
    * @throws SQLException
    */
    private void initializeDatabase() throws SQLException {

        if(dbConnection != null) {
            
            String createUserTable = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), userNickname varchar(50) NOT NULL, primary key(username))";
            String createMessageTable = "create table messages (locationName TEXT, locationDescription TEXT, locationCity TEXT, locationCountry TEXT, locationStreetAddress TEXT, " 
            + "originalPoster TEXT, originalPostingTime INTEGER, latitude REAL, longitude REAL, weather TEXT)";
            
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.executeUpdate(createMessageTable);
            createStatement.close();
        }
    }

    /**
    * Closes the connection to the database and sets it to null.
    * @throws SQLException if a database access error occurs
    */
    public void close() throws SQLException {
        if(dbConnection != null){
            dbConnection.close();
            dbConnection = null;
        }
    }

    /**
    * Creates a new user in the database if the provided user does not already exist.
    * @param user
    * @throws SQLException
    * @return True if the user could be added, False if not.
    */
    public boolean insertUser(JSONObject user) throws SQLException {

        if(checkIfUserExists(user.getString("username"))){
            return false;
        }

        PreparedStatement prepUser = dbConnection.prepareStatement(preparedInsertUser);
        prepUser.setString(1, user.getString("username"));
        prepUser.setString(2, encryptPassword(user.getString("password")));
        prepUser.setString(3, user.getString("email"));
        prepUser.setString(4, user.getString("userNickname"));
        prepUser.executeUpdate();
        prepUser.close();

        return true;
    }

    /**
    * Takes a plaintext password and hashes it.
    * @param password in plaintext.
    * @return The hashed password.
    */
    private String encryptPassword(String password) {
        
        byte bytes[] = new byte[13];
        secureRandom.nextBytes(bytes);

        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;

        String hashedPassword = Crypt.crypt(password, salt);
        return hashedPassword;
    }

    /**
    * Checks if the provided username is already in the database.
    * @param username
    * @throws SQLException
    * @return True if the username is taken, false if the username is not taken.
    */
    private boolean checkIfUserExists(String username) throws SQLException {
        
        String preparedUserSelect = "SELECT username FROM users WHERE username = ?";
        PreparedStatement prep = dbConnection.prepareStatement(preparedUserSelect);
        prep.setString(1, username);
        ResultSet result = prep.executeQuery();

        return result.next();
    }

    /**
    * Checks that the provided credentials are correct. If they are, also sets the current user.
    * @param username
    * @param password
    * @throws SQLException
    * @return True if the authentication is successful, false if credentials are wrong.
    */
    public boolean authenticateUser(String username, String password) throws SQLException {

        PreparedStatement prep = null;
        ResultSet result = null;
        boolean status = false; // Whether or not the authentication is successful

        String prepSelection = "SELECT username, password FROM users WHERE username = ?";
        prep = dbConnection.prepareStatement(prepSelection);
        prep.setString(1, username);
        result = prep.executeQuery();

        if(result.next()){ 
            String storedHashedPassword = result.getString("password"); 
            if(storedHashedPassword.equals(Crypt.crypt(password, storedHashedPassword))){ // Compare the stored password and the given password
                currentUser.set(username); // Store the current user's username since it is later used for getting the nickname
                status = true; // Passwords match, authentication OK
            }
        }

        prep.close();
        return status;
    }
    
    /**
    * Adds the message to the database and attaches the sender's nickname to it.
    * @param msg the message to be added
    * @throws SQLException if the message could not be inserted (UserMessage was faulty).
    */
    public void insertMessage(UserMessage msg) throws SQLException {
        PreparedStatement prep = dbConnection.prepareStatement(preparedInsertMessage);
        prep.setString(1, msg.getLocationName());
        prep.setString(2, msg.getDescription());
        prep.setString(3, msg.getCity());
        prep.setString(4, msg.getLocationCountry());
        prep.setString(5, msg.getLocationStreetAddress());
        prep.setString(6, getUserNickname());
        prep.setLong(7, msg.getPostDateAsLong());
        prep.setDouble(8, msg.getLatitude());
        prep.setDouble(9, msg.getLongitude());
        prep.setString(10, msg.getWeather());
        prep.executeUpdate();
        prep.close();
    }

    /**
    * Gets the current user's nickname from the database.
    * @return the nickname of the current user
    */
    private String getUserNickname() throws SQLException {

        String prepSelection = "SELECT userNickname FROM users WHERE username = ?";
        PreparedStatement prep = dbConnection.prepareStatement(prepSelection);
        prep.setString(1, currentUser.get());
        ResultSet result = prep.executeQuery();
        currentUser.remove();

        return result.getString("userNickname");
    }

    /**
    * Gets all the messages stored in the database and places them in a JSONArray.
    * @return the messages in a JSONArray, if the array is empty there are no messages
    */
    public JSONArray getMessages() throws SQLException {

        String prepSelection = "SELECT * FROM messages";
        PreparedStatement prep = dbConnection.prepareStatement(prepSelection);
        ResultSet result = prep.executeQuery();
        JSONArray array = new JSONArray();

        while (result.next()) {
            JSONObject json = new JSONObject();
            json.put("locationName", result.getString("locationName"));
            json.put("locationDescription", result.getString("locationDescription"));
            json.put("locationCity", result.getString("locationCity"));
            json.put("locationCountry", result.getString("locationCountry"));
            json.put("locationStreetAddress", result.getString("locationStreetAddress"));
            json.put("originalPoster", result.getString("originalPoster"));

            String timestamp = TimestampConverter.convertToString(result.getLong("originalPostingTime"));
            json.put("originalPostingTime", timestamp);

            // Since coordinates are optional, only add them if they are available
            if(result.getDouble("latitude") != 0 && result.getDouble("longitude") != 0){
                json.put("latitude", result.getDouble("latitude"));
                json.put("longitude", result.getDouble("longitude"));

                // Check if weather should be attached
                if(result.getString("weather") != null) {
                    // Get latest weather information
                    String weather = WeatherLookup.getWeatherInformation(result.getDouble("latitude"), result.getDouble("longitude"));
                    
                    // Getting weather has failed if weather is null
                    if(weather != null){
                        json.put("weather", weather);
                    }
                }
            }
            array.put(json);
        }
        prep.close();
        return array;
    }
}
