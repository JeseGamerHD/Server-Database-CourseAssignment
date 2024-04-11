package com.server;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsServer;

public class Main {
    
    public static void main(String[] args) throws Exception {

        // Open the database
        try {
            MessageDatabase database = MessageDatabase.getInstance();
            database.open("MessageDB");
        } catch(SQLException e){
            e.printStackTrace();
        }

        // Open the server
        try {
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = ServerSSLContext(args[0], args[1]);
    
            // Configure the HttpsServer to use the sslContext by adding this call to setHttpsConfigurator
            server.setHttpsConfigurator (new HttpsConfigurator(sslContext) {
                public void configure (HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });

            UserAuthenticator authenticator = new UserAuthenticator();
    
            // Create context that defines path for the resource
            HttpContext httpContext = server.createContext("/info", new MessageHandler());
            httpContext.setAuthenticator(authenticator);
            server.createContext("/registration", new RegistrationHandler(authenticator));
            server.setExecutor(Executors.newCachedThreadPool()); // Use multiple threads
            server.start();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static SSLContext ServerSSLContext(String keystore, String password) throws Exception {
        
        char[] passphrase = password.toCharArray();
        KeyStore keyStorage = KeyStore.getInstance("JKS");
        keyStorage.load(new FileInputStream(keystore), passphrase);
        
        KeyManagerFactory keyManager = KeyManagerFactory.getInstance("SunX509");
        keyManager.init(keyStorage, passphrase);
        
        TrustManagerFactory trustManager = TrustManagerFactory.getInstance("SunX509");
        trustManager.init(keyStorage);
        
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(keyManager.getKeyManagers(), trustManager.getTrustManagers(), null);

        return context;
    }  
}
