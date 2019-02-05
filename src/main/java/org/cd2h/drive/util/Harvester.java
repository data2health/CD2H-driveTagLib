package org.cd2h.drive.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Revision;
import com.google.api.services.drive.model.RevisionList;
import com.google.api.services.drive.model.User;

import edu.uiowa.extraction.PropertyLoader;

public class Harvester extends GoogleAPI {
    static Logger logger = Logger.getLogger(Harvester.class);
    static String APPLICATION_NAME = "CD2H Drive Monitor";
    static List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    
    public static void main(String[] args) throws GeneralSecurityException, IOException, ClassNotFoundException, SQLException {
	PropertyConfigurator.configure(args[0]);
	prop_file = PropertyLoader.loadProperties("google");
	conn = getConnection();

	walkHierarchy(getDrive(), prop_file.getProperty("drive.cd2h_rootID"), null);
    }

    static Drive getDrive() throws GeneralSecurityException, IOException {
	NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SCOPES, prop_file.getProperty("drive.credentials"), prop_file.getProperty("drive.tokens")))
		.setApplicationName(APPLICATION_NAME)
		.build();
	return service;
    }

    static void walkHierarchy(Drive service, String id, String parent) throws IOException, SQLException {
	FileList result = service.files().list()
		.setQ("'" + id + "' in parents")
		.setFields("nextPageToken, files(id, name, modifiedTime, createdTime, lastModifyingUser, owners, mimeType)")
		.execute();
	logger.info("next page token: " + result.getNextPageToken());
	List<File> files = result.getFiles();
	if (files == null || files.isEmpty()) {
	    logger.info("No files found.");
	} else {
	    logger.info("Files:");
	    for (File file : files) {
		logger.info("name: " + file.getName() + "\tid: " + file.getId());
		logger.info("\tlast modified by: " + (file.getLastModifyingUser() == null ? null : file.getLastModifyingUser().getDisplayName()));
		logger.info("\tcreated: " + file.getCreatedTime());
		logger.info("\tmodified: " + file.getModifiedTime());
		for (User user : file.getOwners()) {
		    logger.info("\towner: " + user.getDisplayName());
		}
		logger.info("\tmime type: " + file.getMimeType());
		switch (file.getMimeType()) {
		case "application/vnd.google-apps.folder":
		    PreparedStatement stmt = conn.prepareStatement("insert into drive.hierarchy values(?,?,?,?,?,?,?)");
		    stmt.setString(1, parent);
		    stmt.setString(2, file.getId());
		    stmt.setString(3, file.getName());
		    stmt.setString(4, file.getOwners().get(0).getEmailAddress());
		    stmt.setString(5, file.getLastModifyingUser().getEmailAddress());
		    stmt.setString(6, file.getCreatedTime().toString());
		    stmt.setString(7, file.getModifiedTime().toString());
		    stmt.execute();
		    stmt.close();
		    walkHierarchy(service, file.getId(), id);
		    break;
		default:
		    PreparedStatement dstmt = conn.prepareStatement("insert into drive.document values(?,?,?,?,?,?,?,?)");
		    dstmt.setString(1, parent);
		    dstmt.setString(2, file.getId());
		    dstmt.setString(3, file.getName());
		    dstmt.setString(4, file.getMimeType());
		    dstmt.setString(5, file.getOwners().get(0).getEmailAddress());
		    dstmt.setString(6, (file.getLastModifyingUser() == null ? null : file.getLastModifyingUser().getEmailAddress()));
		    dstmt.setString(7, file.getCreatedTime().toString());
		    dstmt.setString(8, file.getModifiedTime().toString());
		    dstmt.execute();
		    dstmt.close();
		    revisionDetails(service,file.getId());
		    break;
		}
	    }
	}
    }

    static void revisionDetails(Drive service, String fileId) throws SQLException {
	logger.info("\trevisions:");
	try {
	    RevisionList revisions = service.revisions().list(fileId)
		    .setFields("revisions(id, modifiedTime, lastModifyingUser)")
		    .execute();
	    List<Revision> revisionList = revisions.getRevisions();

	    for (Revision revision : revisionList) {
		logger.info("\t\trevision ID: " + revision.getId());
		logger.info("\t\t\tmodified date: " + revision.getModifiedTime());
		logger.info("\t\t\tuser: " + (revision.getLastModifyingUser() == null ? null : revision.getLastModifyingUser().getDisplayName()));
		PreparedStatement stmt = conn.prepareStatement("insert into drive.revision values(?,?,?,?)");
		stmt.setString(1, fileId);
		stmt.setString(2, revision.getId());
		stmt.setString(3, revision.getModifiedTime().toString());
		stmt.setString(4, (revision.getLastModifyingUser() == null ? null : revision.getLastModifyingUser().getDisplayName()));
		stmt.execute();
		stmt.close();
	    }
	} catch (IOException e) {
	    logger.info("\t\tAn error occured.", e);
	}
    }

}
