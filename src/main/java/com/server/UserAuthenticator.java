package com.server;

import java.sql.SQLException;

import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;


public class UserAuthenticator extends BasicAuthenticator {

    private MessageDatabase db = null;

    // The String is the realm that the authentication is applied to
    public UserAuthenticator(){
        super("info");
        db = MessageDatabase.getInstance();
    }
    
    @Override
    public boolean checkCredentials(String username, String password) {
        
        try {
            return db.authenticateUser(username, password);
        } 
        catch (SQLException e){
            System.out.println("User authentication failed");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
    * Attempts to add a new user with given parameters.
    * @param username The name used for registering/login
    * @param password
    * @param email
    * @param nickname The name shown publicly
    * @return True if the user could be added. False if the user could not be added.
    */
    public boolean addUser(String username, String password, String email, String nickname) {

        JSONObject potentialUser = new User(username, password, email, nickname).toJsonObject();
        try {
            return db.insertUser(potentialUser);
        } 
        catch(SQLException e) {
            System.out.println("adding user to database failed");
            e.printStackTrace();
            return false;
        }
    }
}
