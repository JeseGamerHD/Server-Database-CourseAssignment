package com.server;

import java.time.ZonedDateTime;

import org.json.JSONException;
import org.json.JSONObject;

public class UserMessage {

    private String locationName;
    private String locationDescription;
    private String locationCity;
    private String locationCountry;
    private String locationStreetAddress;
    private ZonedDateTime postDate;
    private double latitude;
    private double longitude;
    private String weather;
    
    public UserMessage() { }

    public UserMessage(String name, String desc, String city, String date) {
        locationName = name;
        locationDescription = desc;
        locationCity = city;
        setPostDateFromString(date);
    }
    public UserMessage(String name, String desc, String city, long date) {
        locationName = name;
        locationDescription = desc;
        locationCity = city;
        setPostDate(date);
    }

    /**
    * Constructs a UserMessage from a JSONObject. Assumes that all mandatory information is available in the given json. 
    * If optional information such as coordinates and weather are included, the user message will contain them.
    * @param json the user message information in json format.
    * @throws JSONException if a string value for a key is missing.
    */
    public UserMessage(JSONObject json) throws JSONException {

        locationName = json.getString("locationName");
        locationDescription = json.getString("locationDescription");
        locationCity = json.getString("locationCity");
        locationCountry = json.getString("locationCountry");
        locationStreetAddress = json.getString("locationStreetAddress");
        setPostDateFromString(json.getString("originalPostingTime"));

        // Coordinates are optional
        if(json.has("latitude") && json.has("longitude")){
            latitude = json.getDouble("latitude");
            longitude = json.getDouble("longitude");

            // weather is optional, but depends on the coordinates
            if(json.has("weather")){
                weather = json.getString("weather");
            }
        }
    }

    public void setLocationName(String name){
        if(name != null){
            locationName = name;
        }
    }

    public String getLocationName() {
        return locationName;
    }

    public void setDescription(String desc) {
        if(desc != null){
            locationDescription = desc;
        }
    }

    public String getDescription() {
        return locationDescription;
    }

    public void setCity(String city) {
        if(city != null){
            locationCity = city;
        }
    }

    public String getCity() {
        return locationCity;
    }

    public void setLocationCountry(String country) {
        if(country != null){
            locationCountry = country;
        }    
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public void setLocationStreetAddress(String address) {
        if(address != null){
            locationStreetAddress = address;
        }
    }

    public String getLocationStreetAddress() {
        return locationStreetAddress;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWeather() {
        return weather;
    }

    /**
    * Sets the post date of this message to ZonedDateTime from Unix Time.
    * @param dateAsEpoch the posting date in Epoch (long)
    */
    public void setPostDate(long dateAsEpoch) {
        postDate = TimestampConverter.convertToZonedDateTime(dateAsEpoch);
    }

    /**
    * Gets the post date of this message converted to INTEGER as Unix Time.
    * The date should be stored in the database using this format
    * @return postDate as a long
    */
    public long getPostDateAsLong() {
        return TimestampConverter.convertToLong(postDate);
    }

    /**
    * Sets the post date of this UserMessage using a String input.
    * The String is converted to ZonedDateTime by parsing the String with a formatter.
    * @param dateAsString the posting date as a String. It should follow this format: yyyy-MM-dd'T'HH:mm:ss.SSSX
    */
    public void setPostDateFromString(String dateAsString) {
        postDate = TimestampConverter.convertToZonedDateTime(dateAsString);
    }
}
