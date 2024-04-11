package com.server;

import org.json.JSONObject;

public class User {
    
    private String username;
    private String password;
    private String email;
    private String nickname;

    public User(String username, String password, String email, String nickname){
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
    }

    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }

    public String getEmail(){
        return email;
    }

    public String getNickname(){
        return nickname;
    }

    /**
    * Creates a JSONObject from the User.
    * If email is not specified for the user it is not added. </p>
    * Example: { "username" : "johndoe", "password" : "password", "email" : "email@thing.com", "userNickname" : "john123"}
    * @return The created JSONObject
    */
    public JSONObject toJsonObject() {
        
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("password", password);

        if(email != null) {
            json.put("email", email);
        }

        json.put("userNickname", nickname);
        
        return json;
    }
}
