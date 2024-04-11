package com.server;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WeatherLookup {

    /**
    * Gets the current weather information (temperature) at the given coordinates from an external weather service.
    * An XML string is created from the coordinates and sent to the service which responds with another XML string containing weather information.
    * @param latitude
    * @param longitude
    * @return The temperature as a string or null if weather could not be fetched.
    */
    public static String getWeatherInformation(double latitude, double longitude) {

        String coordinates = createXMLString(latitude, longitude);
        String url = "http://localhost:4001/weather";
        String weather = null;
        
        try {
            weather = handleRequest(url, coordinates);
        } catch(Exception e){
            e.printStackTrace();
        }

        // Get the temperature out of the response (which is in XML format)
        try {
            weather = parseWeatherResponse(weather);
        } catch(Exception e){
            e.printStackTrace();
        }

        return weather; // Null if could not get weather
    }

    /**
    * Creates the POST request using a HttpClient and reads the response message from the weather service.
    * @param url The URL of the weather service.
    * @param coordinatesXML The coordinates in XML String format.
    * @return The response from the service as a string (which follows XML format). Example: {@code <weather><longitude>20.1</longitude><latitude>30.1</latitude><temperature>2</temperature><Unit>Celcius</Unit></weather>}
    * @throws ClientProtocolException in case of an http protocol error
    * @throws IOException in case of a problem or the connection was aborted
    */
    private static String handleRequest(String url, String coordinatesXML) throws ClientProtocolException, IOException {

        String weatherXML = null;
        HttpClient httpClient = HttpClientBuilder.create().build();
        // HttpClient example from: https://www.educative.io/answers/how-to-make-post-requests-in-java-using-httpclient

        // Set up and build the request
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/xml");
        StringEntity postRequest = new StringEntity(coordinatesXML);
        httpPost.setEntity(postRequest);
                
        // Send the request and get the response
        HttpResponse httpResponse = httpClient.execute(httpPost);
        HttpEntity responseEntity = httpResponse.getEntity();
        weatherXML = EntityUtils.toString(responseEntity);

        return weatherXML;
    }

    /**
    * Parses the response string into an XML document and gets the temperature element out of it.
    * @param weatherResponse
    * @return The temperature as a string or null if weatherResponse is null.
    */
    private static String parseWeatherResponse(String weatherXML) throws ParserConfigurationException, SAXException, IOException {

        String temperature = null;
        
        if(weatherXML != null){ // If response is null, something went wrong with the post request or the service
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builer = factory.newDocumentBuilder();
            Document doc = builer.parse(new InputSource(new StringReader(weatherXML)));
    
            NodeList nodes = doc.getElementsByTagName("temperature");
            Element element = (Element) nodes.item(0);
            temperature = element.getTextContent();
        }

        return temperature;
    } 

    private static String createXMLString(double latitude, double longitude) {
        
        String coordinates = 
        "<coordinates>" +
        "  <latitude>" + latitude + "</latitude>" +
        "  <longitude>" + longitude + "</longitude>" +
        "</coordinates>";
        
        return coordinates;
    }
}
