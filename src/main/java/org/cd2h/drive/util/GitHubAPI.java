package org.cd2h.drive.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.uiowa.extraction.LocalProperties;

public class GitHubAPI {
    static Logger logger = Logger.getLogger(GitHubPush.class);
     LocalProperties github_props = null;
    
    public GitHubAPI(LocalProperties github_props) {
	this.github_props = github_props;
    }

    public JSONObject submitSearch(String search) throws IOException {
	if (!search.matches(" *search.*"))
	    throw new IOException("search request doesn't start with search");
	// normalize search string to remove tabs, as GraphQL doesn't like them...
	return submit(("{ \"query\": \"{" + quotify(search) + "}\" } ").replaceAll("[\t ]+", " "));
    }

    public JSONObject submitQuery(String query) throws IOException {
	if (query.matches(" *search.*") || query.matches(" *update.*"))
	    throw new IOException("query request doesn't start with valid object");
	// normalize query string to remove tabs, as GraphQL doesn't like them...
	return submit(("{ \"query\": \"query {" + query + "}\" } ").replaceAll("[\t ]+", " "));
    }

    public JSONObject submitMutation(String mutation) throws IOException {
	if (!mutation.matches(" *update.*"))
	    throw new IOException("mutation request doesn't start with update...");
	// normalize mutation string to remove tabs, as GraphQL doesn't like them...
	return submit(("{ \"query\": \"mutation {" + quotify(mutation) + "}\" } ").replaceAll("[\t ]+", " "));
    }

    private JSONObject submit(String request) throws IOException {
	// configure the connection
	URL uri = new URL("https://api.github.com/graphql");
	HttpURLConnection con = (HttpURLConnection) uri.openConnection();
	con.setRequestMethod("POST"); // type: POST, PUT, DELETE, GET
	con.setRequestProperty("Authorization", "token "+github_props.getProperty("token"));
	con.setRequestProperty("Accept","application/json");
	con.setDoOutput(true);
	con.setDoInput(true);
	
	// submit the GraphQL construct
	logger.info("request: " + request);
	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
	out.write(request);
	out.flush();
	out.close();

	// pull down the response JSON
	con.connect();
	logger.debug("response:" + con.getResponseCode());
	BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	JSONObject results = new JSONObject(new JSONTokener(in));
	logger.debug("results:\n" + results.toString(3));
	in.close();
	return results;
    }

    static String quotify(String theString) {
	return theString.replace("\"", "\\\"");
    }
}
