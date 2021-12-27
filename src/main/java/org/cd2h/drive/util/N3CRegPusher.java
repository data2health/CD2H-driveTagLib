package org.cd2h.drive.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.sheets.v4.model.UpdateValuesResponse;

import edu.uiowa.extraction.PropertyLoader;

public class N3CRegPusher extends Sheet {
	static Logger logger = LogManager.getLogger(N3CRegPusher.class);

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, GeneralSecurityException {
	prop_file = PropertyLoader.loadProperties("gsuite_registration");
	conn = getConnection();

	List<List<Object>> values = buildValueList();
	UpdateValuesResponse result = updateValues(prop_file.getProperty("sheets.spreadsheetId"), generateRange(values), "USER_ENTERED", values);
	logger.info(result.getUpdatedCells() + " cells updated.");
    }
    
    static List<List<Object>> buildValueList() throws SQLException {
	List<List<Object>> values = new ArrayList<List<Object>>();
	int rowCount = 0;
	
	PreparedStatement stmt = conn.prepareStatement("select * from n3c_admin.gsuite_view");
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
    
    static String generateRange(List<List<Object>> values) {
	logger.info("skip count: " + prop_file.getProperty("sheets.skipcount"));
	StringBuffer buffer = new StringBuffer(prop_file.getProperty("sheets.sheets")
						+ "!A" + getStartRow(values)
						+ ":AF" + getEndRow(values));
	logger.info("range: " + buffer.toString());
	return buffer.toString();
    }
    
    static int getStartRow(List<List<Object>> values) {
	return Integer.parseInt(prop_file.getProperty("sheets.skipcount")) + 1;
    }

    static int getEndRow(List<List<Object>> values) {
	return Integer.parseInt(prop_file.getProperty("sheets.skipcount")) + values.size();
    }

}
