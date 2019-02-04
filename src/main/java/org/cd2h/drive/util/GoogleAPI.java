package org.cd2h.drive.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

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
    static JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    protected static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT, List<String> SCOPES, String credentials, String tokens) throws IOException {
	// Load client secrets.
	InputStream in = new FileInputStream(credentials);
	GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

	// Build flow and trigger user authorization request.
	GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
		.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(tokens))).setAccessType("offline").build();
	LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
	return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }


}
