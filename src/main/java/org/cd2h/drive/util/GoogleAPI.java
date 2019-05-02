package org.cd2h.drive.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import edu.uiowa.extraction.LocalProperties;

public class GoogleAPI {
    static LocalProperties prop_file = null;
    public static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    static Connection conn = null;

    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, List<String> SCOPES, String credentials, String tokens) throws IOException {
	// Load client secrets.
	InputStream in = new FileInputStream(credentials);
	GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	// Build flow and trigger user authorization request.
	GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
		.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokens)))
		.setAccessType("offline")
		.build();
	LocalServerReceiver receiver = new LocalServerReceiver.Builder()
		.setPort(8888)
		.build();
	return new AuthorizationCodeInstalledApp(flow, receiver)
		.authorize("user");
    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Properties props = new Properties();
        props.setProperty("user", prop_file.getProperty("jdbc.user"));
        props.setProperty("password", prop_file.getProperty("jdbc.password"));
        // if (use_ssl.equals("true")) {
        // props.setProperty("sslfactory",
        // "org.postgresql.ssl.NonValidatingFactory");
        // props.setProperty("ssl", "true");
        // }
        Connection conn = DriverManager.getConnection(prop_file.getProperty("jdbc.url"), props);
        // conn.setAutoCommit(false);
        return conn;
    }


}
