package org.cd2h.drive.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.services.sheets.v4.model.UpdateValuesResponse;

import edu.uiowa.extraction.PropertyLoader;

public class N3CCohortPusher extends Sheet {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, GeneralSecurityException {
	prop_file = PropertyLoader.loadProperties("gsuite_registration");
	conn = getConnection();

	PreparedStatement fetchStmt = conn.prepareStatement("select table_name,file_id from enclave_cohort.file_mapping");
	ResultSet fetchRS = fetchStmt.executeQuery();
	while (fetchRS.next()) {
		String tableName = fetchRS.getString(1);
		String fileID = fetchRS.getString(2);
		logger.info("table name: " + tableName + "\tfile ID: " + fileID);
		
		List<List<Object>> values = buildValueList(tableName);
		UpdateValuesResponse result = updateValues(prop_file.getProperty("sheets.spreadsheetId"), generateRange(tableName, values), "USER_ENTERED", values);
		logger.info(result.getUpdatedCells() + " cells updated.");
	}
	fetchStmt.close();

    }
    
    static List<List<Object>> buildValueList(String tableName) throws SQLException {
	List<List<Object>> values = new ArrayList<List<Object>>();
	int rowCount = 0;
	
	PreparedStatement stmt = conn.prepareStatement("select * from enclave_cohort." + tableName);
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    List<Object> resultRow = new ArrayList<Object>();
	    for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
		String value = rs.getString(i);
		if (value == null)
		    resultRow.add("");
		else
		    resultRow.add(value);
	    }
	    values.add(resultRow);
	    rowCount++;
	}
	logger.info("returned: " + rowCount + "\tlist: " + values.size());
	
	return values;
    }
    
    static String generateRange(String tableName, List<List<Object>> values) {
	logger.info("skip count: " + prop_file.getProperty("sheets.skipcount"));
	StringBuffer buffer = new StringBuffer(tableName
						+ "!A" + getStartRow(values)
						+ ":" + getEndColumn(values) + getEndRow(values));
	logger.info("range: " + buffer.toString());
	return buffer.toString();
    }
    
    static int getStartRow(List<List<Object>> values) {
	return Integer.parseInt(prop_file.getProperty("sheets.skipcount")) + 1;
    }

    static int getEndRow(List<List<Object>> values) {
	return Integer.parseInt(prop_file.getProperty("sheets.skipcount")) + values.size();
    }

    static String getEndColumn(List<List<Object>> values) {
    	String endColumn = "";
    	int index = values.get(0).size();
    	endColumn += (char)(index - 1 + 'A');
    	logger.trace("column index: " + index + " : " + endColumn);
    	return endColumn;
    }

}
