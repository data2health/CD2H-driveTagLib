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
import java.util.Hashtable;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Sheet extends GoogleAPI {
    static Logger logger = Logger.getLogger(Sheet.class);
    static String APPLICATION_NAME = "CD2H Onboarding Sync";
    static List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    static Hashtable<String,String> attributeHash = new Hashtable<String,String>();
    static Hashtable<String,String> reservedHash = new Hashtable<String,String>();
    
    static String schema = null;
    static String[] sheets = null;
    static int skipCount = 0;

    public static void main(String... args) throws IOException, GeneralSecurityException, SQLException, ClassNotFoundException {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties(args[1]);
	conn = getConnection();
	
	schema = prop_file.getProperty("jdbc.schema");
	String sheetString = prop_file.getProperty("sheets.sheets");
	if (prop_file.getProperty("sheets.skipcount") != null)
	    skipCount = Integer.parseInt(prop_file.getProperty("sheets.skipcount"));
	sheets = sheetString.split(",");
	logger.info("database schema: " + schema);
	logger.info("sheets: " + arrayAsString(sheets));
	
	initializeReserveHash();
	rebuildDriveSheetAsTable();
    }
    
    static void rebuildDriveSheetAsTable() throws SQLException, GeneralSecurityException, IOException {
	for (String sheet : sheets) {
	    attributeHash = new Hashtable<String,String>();
	    rebuildDriveSheetAsTable(sheet, generateSQLName(sheet));
	}
    }
    
    static void rebuildDriveSheetAsTable(String sheet, String table) throws SQLException, GeneralSecurityException, IOException {
	logger.info("loading sheet: " + sheet);
	
	PreparedStatement schemaStmt = conn.prepareStatement("create schema if not exists "+schema);
	schemaStmt.execute();
	schemaStmt.close();

	PreparedStatement dropStmt = conn.prepareStatement("drop table if exists "+schema+"."+table+" cascade");
	dropStmt.execute();
	dropStmt.close();

	// Build a new authorized API client service.
	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	final String spreadsheetId = prop_file.getProperty("sheets.spreadsheetId");
	final String range = sheet;
	Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SCOPES, prop_file.getProperty("sheets.credentials"), prop_file.getProperty("sheets.tokens")))
		.setApplicationName(APPLICATION_NAME)
		.build();
	
	ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
	logger.debug("response: " + response);
	List<List<Object>> values = response.getValues();
	int personCount = 0;
	if (values == null || values.isEmpty()) {
	    logger.error("No data found.");
	} else {
	    int rowNum = 0;
	    int rowCount = 0;
	    StringBuffer insertStatement = new StringBuffer("insert into "+schema+"."+table+" values(");
	    for (List<?> row : values) {
		if (skipCount > rowNum) {
		    rowNum++;
		    continue;
		}
		if (skipCount == rowNum) {
		    StringBuffer createStatement = new StringBuffer("create table "+schema+"."+table+"(");
		    logger.info("\trow size: " + row.size());
		    logger.debug("\tslots: " + row.toString());
		    rowCount = row.size();
		    for (int i = 0; i <row.size(); i++) {
			logger.debug("slot " + i + ": " + row.get(i));
			createStatement.append((i > 0 ? ", " : " ") + generateSQLName((String)row.get(i)) + " text");
			insertStatement.append((i > 0 ? ", ?" : "?"));
		    }
		    createStatement.append(" )");
		    insertStatement.append(" )");
		    logger.debug(createStatement);
		    PreparedStatement createStmt = conn.prepareStatement(createStatement.toString());
		    createStmt.execute();
		    createStmt.close();
		    rowNum++;
		    continue;
		}
//		logger.debug("timestamp: " + row.get(0));
//		logger.debug("\temail: " + row.get(1));
//		logger.debug("\tfirst name: " + row.get(2));
//		logger.debug("\tlast name: " + row.get(3));
//		logger.debug("\tinstitution: " + row.get(4));
		for (int i = 0; i <row.size(); i++)
		    logger.trace("slot " + i + ": " + row.get(i));
		
		PreparedStatement stmt = conn.prepareStatement(insertStatement.toString());
		for (int i = 1; i <= row.size() && i <= rowCount; i++) {
		    String value = row.get(i-1).toString();
		    if (value != null)
			value = value.trim();
		    if (value != null && value.length() == 0)
			value = null;
		    stmt.setString(i, value);
		}
		for (int i = row.size()+1; i <=rowCount; i++) { // the API shorts us sometimes when rightmost cells are empty
		    stmt.setString(i, null);
		}
		stmt.execute();
		stmt.close();
		personCount++;
	    }
	    logger.info(sheet + " loaded: " + personCount);
	}
    }

   static String generateSQLName(String attribute) {
       String attributeBase = null;
       if (attribute == null || attribute.length() == 0)
	    attributeBase = "blank";
       else
	   attributeBase = attribute.trim().replace("\uFEFF", "");
	attributeBase = (Character.isJavaIdentifierStart(attributeBase.charAt(0)) ? "" : "x___") + attributeBase.replaceAll("[^A-Za-z0-9_]", "_");
	if (reservedHash.containsKey(attributeBase.toUpperCase()))
	    attributeBase = "x__"+attributeBase;
	if (attributeBase.length() > 60)
	    attributeBase = attributeBase.substring(0, 60);
	if (!attributeHash.containsKey(attributeBase.toLowerCase())) {
	    attributeHash.put(attributeBase.toLowerCase(), attributeBase);
	    return attributeBase;
	}
	int count = 1;
	while (true) {
	    String newAttribute = attributeBase + "_" + count;
	    if (!attributeHash.containsKey(newAttribute.toLowerCase())) {
		attributeHash.put(newAttribute.toLowerCase(), newAttribute);
		return newAttribute;
	    }
	    count++;
	}
    }

   static void initializeReserveHash() {
	reservedHash.put("ALL", "ALL");
	reservedHash.put("ANALYSE", "ANALYSE");
	reservedHash.put("ANALYZE", "ANALYZE");
	reservedHash.put("AND", "AND");
	reservedHash.put("ANY", "ANY");
	reservedHash.put("ARRAY", "ARRAY");
	reservedHash.put("AS", "AS");
	reservedHash.put("ASC", "ASC");
	reservedHash.put("ASYMMETRIC", "ASYMMETRIC");
	reservedHash.put("AUTHORIZATION", "AUTHORIZATION");
	reservedHash.put("BETWEEN", "BETWEEN");
	reservedHash.put("BINARY", "BINARY");
	reservedHash.put("BOTH", "BOTH");
	reservedHash.put("CASE", "CASE");
	reservedHash.put("CAST", "CAST");
	reservedHash.put("CHECK", "CHECK");
	reservedHash.put("CMAX", "CMAX");
	reservedHash.put("COLLATE", "COLLATE");
	reservedHash.put("COLUMN", "COLUMN");
	reservedHash.put("CONSTRAINT", "CONSTRAINT");
	reservedHash.put("CREATE", "CREATE");
	reservedHash.put("CROSS", "CROSS");
	reservedHash.put("CURRENT_DATE", "CURRENT_DATE");
	reservedHash.put("CURRENT_ROLE", "CURRENT_ROLE");
	reservedHash.put("CURRENT_TIME", "CURRENT_TIME");
	reservedHash.put("CURRENT_TIMESTAMP", "CURRENT_TIMESTAMP");
	reservedHash.put("CURRENT_USER", "CURRENT_USER");
	reservedHash.put("DEFAULT", "DEFAULT");
	reservedHash.put("DEFERRABLE", "DEFERRABLE");
	reservedHash.put("DESC", "DESC");
	reservedHash.put("DISTINCT", "DISTINCT");
	reservedHash.put("DO", "DO");
	reservedHash.put("ELSE", "ELSE");
	reservedHash.put("END", "END");
	reservedHash.put("EXCEPT", "EXCEPT");
	reservedHash.put("FALSE", "FALSE");
	reservedHash.put("FOR", "FOR");
	reservedHash.put("FOREIGN", "FOREIGN");
	reservedHash.put("FREEZE", "FREEZE");
	reservedHash.put("FROM", "FROM");
	reservedHash.put("FULL", "FULL");
	reservedHash.put("GRANT", "GRANT");
	reservedHash.put("GROUP", "GROUP");
	reservedHash.put("HAVING", "HAVING");
	reservedHash.put("ILIKE", "ILIKE");
	reservedHash.put("IN", "IN");
	reservedHash.put("INITIALLY", "INITIALLY");
	reservedHash.put("INNER", "INNER");
	reservedHash.put("INTERSECT", "INTERSECT");
	reservedHash.put("INTO", "INTO");
	reservedHash.put("IS", "IS");
	reservedHash.put("ISNULL", "ISNULL");
	reservedHash.put("JOIN", "JOIN");
	reservedHash.put("LEADING", "LEADING");
	reservedHash.put("LEFT", "LEFT");
	reservedHash.put("LIKE", "LIKE");
	reservedHash.put("LIMIT", "LIMIT");
	reservedHash.put("LOCALTIME", "LOCALTIME");
	reservedHash.put("LOCALTIMESTAMP", "LOCALTIMESTAMP");
	reservedHash.put("NATURAL", "NATURAL");
	reservedHash.put("NEW", "NEW");
	reservedHash.put("NOT", "NOT");
	reservedHash.put("NOTNULL", "NOTNULL");
	reservedHash.put("NULL", "NULL");
	reservedHash.put("OFF", "OFF");
	reservedHash.put("OFFSET", "OFFSET");
	reservedHash.put("OLD", "OLD");
	reservedHash.put("ON", "ON");
	reservedHash.put("ONLY", "ONLY");
	reservedHash.put("OR", "OR");
	reservedHash.put("ORDER", "ORDER");
	reservedHash.put("OUTER", "OUTER");
	reservedHash.put("OVERLAPS", "OVERLAPS");
	reservedHash.put("PLACING", "PLACING");
	reservedHash.put("PRIMARY", "PRIMARY");
	reservedHash.put("REFERENCES", "REFERENCES");
	reservedHash.put("RIGHT", "RIGHT");
	reservedHash.put("SELECT", "SELECT");
	reservedHash.put("SESSION_USER", "SESSION_USER");
	reservedHash.put("SIMILAR", "SIMILAR");
	reservedHash.put("SOME", "SOME");
	reservedHash.put("SYMMETRIC", "SYMMETRIC");
	reservedHash.put("TABLE", "TABLE");
	reservedHash.put("THEN", "THEN");
	reservedHash.put("TO", "TO");
	reservedHash.put("TRAILING", "TRAILING");
	reservedHash.put("TRUE", "TRUE");
	reservedHash.put("UNION", "UNION");
	reservedHash.put("UNIQUE", "UNIQUE");
	reservedHash.put("USER", "USER");
	reservedHash.put("USING", "USING");
	reservedHash.put("VERBOSE", "VERBOSE");
	reservedHash.put("WHEN", "WHEN");
	reservedHash.put("WHERE", "WHERE");
	reservedHash.put("WINDOW", "WINDOW");
	reservedHash.put("WITH", "WITH");
   }

   static String arrayAsString(String[] array) {
	StringBuffer result = new StringBuffer("[");
	for (int i = 0; i < array.length; i++) {
	    if (i>0)
		result.append(", ");
	    result.append(array[i]);
	}
	result.append("]");
	return result.toString();
   }
}
