package org.cd2h.drive.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

import edu.uiowa.extraction.PropertyLoader;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Sheet extends GoogleAPI {
    static Logger logger = Logger.getLogger(Sheet.class);
    static String APPLICATION_NAME = "CD2H Onboarding Sync";
    static List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    public static void main(String... args) throws IOException, GeneralSecurityException, SQLException, ClassNotFoundException {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("google");
	conn = getConnection();
	
	PreparedStatement truncateStmt = conn.prepareStatement("truncate drive.person");
	truncateStmt.execute();
	truncateStmt.close();

	// Build a new authorized API client service.
	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	final String spreadsheetId = prop_file.getProperty("sheets.spreadsheetId");
	final String range = "profiles!A3:AR";
	Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SCOPES, prop_file.getProperty("sheets.credentials"), prop_file.getProperty("sheets.tokens"))).setApplicationName(APPLICATION_NAME).build();
	
	ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
	logger.debug("response: " + response);
	List<List<Object>> values = response.getValues();
	if (values == null || values.isEmpty()) {
	    logger.error("No data found.");
	} else {
	    for (List<?> row : values) {
		logger.info("timestamp: " + row.get(0));
		logger.info("\temail: " + row.get(1));
		logger.info("\tfirst name: " + row.get(3));
		logger.info("\tlast name: " + row.get(4));
		logger.info("\tinstitution: " + row.get(5));
		logger.debug("\trow size: " + row.size());
		logger.debug("\tlast slot: " + row.toString());
		
		PreparedStatement stmt = conn.prepareStatement("insert into drive.person values(?,?,?::boolean,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?::boolean,?,?,?,?,?,?,?,?,?,?,?,?)");
		for (int i = 1; i <= row.size(); i++) {
		    stmt.setString(i, row.get(i-1).toString());
		}
		for (int i = row.size()+1; i <= 44; i++) { // the API shorts us sometimes when rightmost cells are empty
		    stmt.setString(i, null);
		}
		stmt.execute();
		stmt.close();
	    }
	}
    }
}
