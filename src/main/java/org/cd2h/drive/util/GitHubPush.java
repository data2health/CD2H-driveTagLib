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
    static String repo_list = "viewer {"
	    	+ "	repositories(first:100) {"
	    	+ "		nodes {"
	    	+ "			name,"
	    	+ "			url,"
	    	+ "			id,"
	    	+ "			milestones(first:100) {"
	    	+ "				nodes {"
	    	+ "					id,"
	    	+ "					title,"
	    	+ "					description,"
	    	+ "					dueOn,"
	    	+ "					creator{login}"
	    	+ "				}"
	    	+ "			},"
	    	+ "			repositoryTopics(first:100) {"
	    	+ "				nodes {"
	    	+ "					topic{name}"
	    	+ "				}"
	    	+ "			}"
//	    	+ "			collaborators(first:100) {"
//	    	+ "				nodes {"
//	    	+ "					login,name"
//	    	+ "				}"
//	    	+ "			}"
	    	+ "		}"
	    	+ "	}"
	    	+ "}";
    static String repo = "repository(owner:eichmann, name:SPARQLTagLib) {"
    	+ "	id,"
    	+ "	url"
    	+ "			repositoryTopics(first:100) {"
    	+ "				nodes {"
    	+ "					topic{name}"
    	+ "				}"
    	+ "			}"
    	+ "}";
    static String repo_mutate = " updateTopics ("
    				+ "	input: {"
    				+ "		repositoryId: \"MDEwOlJlcG9zaXRvcnkxMzQzMTU4NDk=\","
    				+ "		topicNames:[\"data2health\", \"pea\"]"
    				+ "	}"
    				+ ") {"
    				+ "	clientMutationId"
    				+ "}";
    static String members = "organization(login:data2health) {"
    	+ "	id,"
    	+ "	login,"
    	+ "	repositories(first:100) {"
    	+ "		nodes {"
    	+ "			name,"
    	+ "			description,"
    	+ "			url,"
    	+ "			milestones(first:100) {"
    	+ "				nodes {"
    	+ "					id,"
    	+ "					title,"
    	+ "					description,"
    	+ "					dueOn,"
    	+ "					creator{login}"
    	+ "				}"
    	+ "			},"
    	+ "			repositoryTopics(first:100) {"
    	+ "				nodes {"
    	+ "					topic{name}"
    	+ "				}"
    	+ "			}"
//    	+ "			collaborators(first:100) {"
//    	+ "				nodes {"
//    	+ "					login,name"
//    	+ "				}"
//    	+ "			}"
    	+ "		}"
    	+ "	}"
    	+ "	members(first:100) {"
    	+ "		nodes {"
    	+ "			id,name,bio,email,login,avatarUrl"
    	+ "		}"
    	+ "	}"
    	+ "}";
    static String repoByTopic = "search(query: \"topic:data2health\", type: REPOSITORY, first:100) {"
    				+ "	repositoryCount"
    				+ "	edges {"
    				+ "		node {"
    				+ "		... on Repository {"
    				+ "			owner {"
    				+ "				id"
    				+ "				login"
    				+ "			}"
    				+ "			name"
    				+ "			description"
    				+ "			stargazers {"
    				+ "				totalCount"
    				+ "			}"
    				+ "			forks {"
    				+ "				totalCount"
    				+ "			}"
    				+ "			updatedAt"
    				+ "			}"
    				+ "		}"
    				+ "	}"
    				+ "}";

    public static void main(String[] args) throws IOException, InterruptedException {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("google");
	github_props = PropertyLoader.loadProperties("github");
	
	GitHubAPI theAPI = new GitHubAPI(github_props);
	
	JSONObject results = null;
	
//	results = theAPI.submitMutation(repo_mutate);
//	logger.info("results:\n" + results.toString(3));
//	
//	results = theAPI.submitQuery(repo);
//	logger.info("results:\n" + results.toString(3));
	
	results = theAPI.submitQuery(members);
	logger.info("results:\n" + results.toString(3));
    }
    
    public static String getQuery(String name) {
	switch(name) {
	case "me":
	    return me;
	case "data2health_tagged_repos":
	    return repoByTopic;
	case "data2health_org":
	    return members;
	default:
	    return null;
	}
    }
    
    public static String getQueryType(String name) {
	switch(name) {
	case "me":
	case "data2health_org":
	    return "query";
	case "data2health_tagged_repos":
	    return "search";
	default:
	    return null;
	}
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
        
}
