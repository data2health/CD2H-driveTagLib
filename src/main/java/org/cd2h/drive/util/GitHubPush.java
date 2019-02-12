package org.cd2h.drive.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONObject;

import edu.uiowa.extraction.LocalProperties;
import edu.uiowa.extraction.PropertyLoader;

public class GitHubPush extends GoogleAPI {
    static Logger logger = Logger.getLogger(GitHubPush.class);
    static LocalProperties github_props = null;

    static String me = "viewer { login,name,id }";
    static String repo_list =
	"viewer { repositories(first:50) {nodes {name,url}   } }";
    public static void main(String[] args) throws IOException, InterruptedException {
//	Thread.sleep(30000);
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("google");
	github_props = PropertyLoader.loadProperties("github");
	
	GitHubAPI theAPI = new GitHubAPI(github_props);
	JSONObject results = theAPI.submit("{ \"query\": \"query {" + repo_list + "}\" } ");
	logger.info("results:\n" + results.toString(3));
    }
    
    static void writeJSON() throws IOException {
	FileWriter fileWriter = new FileWriter("/tmp/repo/sample/roster.json");
	BufferedWriter writer = new BufferedWriter(fileWriter);
    }

    static String buildFileJSON() throws SQLException {
	StringBuffer buffer = new StringBuffer();

	buffer.append("{\n");
	buffer.append("  persons: [\n");

	PreparedStatement stmt = conn.prepareStatement("select * from drive.person order by 2");
	ResultSet rs = stmt.executeQuery();
	while (rs.next()) {
	    buffer.append("    {\n");

	    buffer.append("      \"a\": \"" + rs.getString(1).replaceAll("\\:", "\\\\:") + "\",\n");
	    buffer.append("      \"b\": \"" + rs.getString(2) + "\",\n");
	    buffer.append("      \"c\": \"" + rs.getString(3) + "\",\n");

	    buffer.append("    }");
	    if (!rs.isLast())
		buffer.append(",");
	    buffer.append("\n");
	}

	buffer.append("  ]\n");
	buffer.append("}\n");

	return buffer.toString();
    }
    
    static String quotify(String theString) {
	return "\"" + theString.replace("\"", "\\\"") + "\"";
    }
    
}
