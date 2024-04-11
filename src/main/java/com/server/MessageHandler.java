package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.*;

public class MessageHandler implements HttpHandler {
   
    final MessageDatabase db = MessageDatabase.getInstance();

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String responseString = null;

        // Handle POST requests here (users send this for sending messages)
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            if(checkContentType(exchange)){ // Accept only JSON format

                // Read incoming message
                InputStreamReader inputReader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                BufferedReader buffer = new BufferedReader(inputReader);
                String message = buffer.lines().collect(Collectors.joining("\n"));
                inputReader.close();

                if(message != null && message.length() != 0){

                    JSONObject json = null;
                    try { 
                        json = new JSONObject(message); // Create a json object (or try at least)
                        UserMessage userMessage = new UserMessage(json);
                        db.insertMessage(userMessage); // Add message to database

                    } catch(Exception e) {
                        responseString = "Message could not be posted - JSON was faulty";
                        handleResponse(exchange, responseString, 400);
                    }
                }
                else {
                    responseString = "No message provided";
                    handleResponse(exchange, responseString, 400);
                }

                // All good, message has been posted
                responseString = "Message was posted";
                handleResponse(exchange, responseString, 200);
            }
        }

        // Handle GET requests here (users use this to get messages)
        else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            try {
                JSONArray messages = db.getMessages();
                if(!messages.isEmpty()) {
                    responseString = messages.toString();
                    handleResponse(exchange, responseString, 200);
                }
                else { // No stored messages
                    exchange.sendResponseHeaders(204, -1);
                }
            } 
            catch(SQLException e) {
                responseString = "Messages could not be fetched";
                handleResponse(exchange, responseString, 500);
            }
        } 
        
        // Only POST and GET functions are supported
        else { 
            responseString = "Not supported";
            handleResponse(exchange, responseString, 400); // error code 400 with a message “Not supported”
        }
    }

    /**
    * Checks that the request header has a correct content type. Handles the response if header is faulty.
    * @param exchange The HTTP request
    * @throws IOException if the response headers have already been sent or an I/O error occurs
    * @return True if header contains a content type and the type is "application/json". Otherwise returns false.
    */
    private boolean checkContentType(HttpExchange exchange) throws IOException {

        String responseString = null;
        Headers requestHeader = exchange.getRequestHeaders();
        boolean contentIsValid = false;

        // See if Content-Type is attached
        if (requestHeader.containsKey("Content-Type")) {

            String contentType = requestHeader.getFirst("Content-Type");
            if (contentType.equalsIgnoreCase("application/json")) {
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
    * Constructs and sends a response to the request with given parameters.
    * @param exchange The HTTP request
    * @param response Message to inform user of the outcome
    * @param code The status code
    * @throws IOException if the response headers have already been sent or an I/O error occurs
    */
    private void handleResponse(HttpExchange exchange, String response, int code) throws IOException {

        exchange.sendResponseHeaders(code, response.getBytes("UTF-8").length);
        OutputStream output = exchange.getResponseBody();
        output.write(response.getBytes());
        output.flush();
        output.close();
    }
}
