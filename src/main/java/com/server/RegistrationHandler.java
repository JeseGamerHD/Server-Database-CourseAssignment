package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    final UserAuthenticator authenticator;
    
    public RegistrationHandler(UserAuthenticator auth) {
        authenticator = auth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        
        String responseString = null;
        
        // Handle POST requests
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            if(checkContentType(exchange)){
                
                InputStreamReader inputReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader buffer = new BufferedReader(inputReader);
                String newUser = buffer.lines().collect(Collectors.joining("\n"));
                inputReader.close();

                if(newUser != null && newUser.length() != 0){

                    JSONObject json = null;
                    try { // Create a json object (or try at least)
                        json = new JSONObject(newUser);
                    } catch(JSONException e) {
                        responseString = "JSON was faulty";
                        handleResponse(exchange, responseString, 400);
                    }

                    if(json != null){
                        tryToRegisterUser(exchange, json);
                    }
                    // try - catch already sent a response if json could not be created
                }
                else {
                    responseString = "No user credentials provided";
                    handleResponse(exchange, responseString, 400);
                }
            }
            // response handled inside checkContentType()
        }
        // Other requests are not supported
        else { 
            responseString = "Not supported; Only POST is accepted";
            handleResponse(exchange, responseString, 400);
        }
    }

    /**
    * Checks whether or not the request header contains a Content-Type and that the type is "application/json"
    * @param exchange
    * @return True if Content-Type is "application/json". False if not.
    */
    private boolean checkContentType(HttpExchange exchange) throws IOException {
       
        String responseString = null;
        Headers requestHeader = exchange.getRequestHeaders();
        boolean contentIsValid = false;

        // See if Content-Type is attached
        if(requestHeader.containsKey("Content-Type")){ 
            
            String contentType = requestHeader.getFirst("Content-Type");
            if(contentType.equalsIgnoreCase("application/json")){
                contentIsValid = true;
            }
            else {
                responseString = "Content type must be application/json";
                handleResponse(exchange, responseString, 400);
                contentIsValid = false;
            }
        }
        else {
            responseString = "Content-Type missing";
            handleResponse(exchange, responseString, 400);
            contentIsValid = false;
        }

        return contentIsValid;
    }

    /**
    * Tries to register the user from the given JSONObject. User can only be registered if the username is unique and password is not missing.
    * @param exchange The HTTP request
    * @param json The JSONObject containing user information
    */
    private void tryToRegisterUser(HttpExchange exchange, JSONObject json) throws IOException {
        
        String responseString = null;
        
        try {
            if(json.getString("username").length() != 0 && json.getString("password").length() != 0){
                // Try to register the user
                if(authenticator.addUser(json.getString("username"), json.getString("password"), json.getString("email"), json.getString("userNickname"))){
                    responseString = "Registration was succesful";
                    handleResponse(exchange, responseString, 200);
                }
                else {
                    responseString = "User already exists";
                    handleResponse(exchange, responseString, 409);
                }
            }
            else {
                responseString = "Username or password is missing";
                handleResponse(exchange, responseString, 400);
            }

        } catch(JSONException e) {
            responseString = "JSON is missing a key";
            handleResponse(exchange, responseString, 400);
        }
    }

    /**
    * Constructs and sends a response to the request with given parameters.
    * @param exchange The HTTP request
    * @param responseMessage Message to inform user of the outcome
    * @param responseCode The status code
    * @throws IOException if the response headers have already been sent or an I/O error occurs
    */
    private void handleResponse(HttpExchange exchange, String responseMessage, int responseCode) throws IOException {
        exchange.sendResponseHeaders(responseCode, responseMessage.getBytes("UTF-8").length);
        OutputStream output = exchange.getResponseBody();
        output.write(responseMessage.getBytes());
        output.flush();
        output.close();
    }
}
