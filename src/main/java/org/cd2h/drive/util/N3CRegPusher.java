package org.cd2h.drive.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.PropertyConfigurator;

import com.google.api.services.sheets.v4.model.UpdateValuesResponse;

import edu.uiowa.extraction.PropertyLoader;

public class N3CRegPusher extends Sheet {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException, GeneralSecurityException {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("gsuite_registration");
	conn = getConnection();

	List<List<Object>> values = buildValueList();
	UpdateValuesResponse result = updateValues(prop_file.getProperty("sheets.spreadsheetId"), generateRange(values), "USER_ENTERED", values);
	logger.info(result.getUpdatedCells() + " cells updated.");
    }
    
    static List<List<Object>> buildValueList() throws SQLException {
	List<List<Object>> values = new ArrayList<List<Object>>();
	int rowCount = 0;
	
	PreparedStatement stmt = conn.prepareStatement("select registration.email,official_first_name,official_last_name,first_name,last_name,coalesce(ror_id,''),ror_name,'',orcid_id,gsuite_email,slack_id,github_id,twitter_id,expertise,therapeutic_area,assistant_email,enclave,workstreams,created,updated,official_full_name,official_institution,emailed from n3c_admin.registration left outer join n3c_admin.user_org_map on registration.email=user_org_map.email order by last_name,first_name");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    List<Object> resultRow = new ArrayList<Object>();
	    resultRow.add(rs.getString(1));
	    resultRow.add(rs.getString(2));
	    resultRow.add(rs.getString(3));
	    resultRow.add(rs.getString(4));
	    resultRow.add(rs.getString(5));
	    resultRow.add(rs.getString(6));
	    resultRow.add(rs.getString(7));
	    resultRow.add(rs.getString(8));
	    resultRow.add(rs.getString(9));
	    resultRow.add(rs.getString(10));
	    resultRow.add(rs.getString(11));
	    resultRow.add(rs.getString(12));
	    resultRow.add(rs.getString(13));
	    resultRow.add(rs.getString(14));
	    resultRow.add(rs.getString(15));
	    resultRow.add(rs.getString(16));
	    resultRow.add(rs.getString(17));
	    resultRow.add(rs.getString(18));
	    resultRow.add(rs.getString(19));
	    resultRow.add(rs.getString(20));
	    resultRow.add(rs.getString(21));
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
						+ ":AA" + getEndRow(values));
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
