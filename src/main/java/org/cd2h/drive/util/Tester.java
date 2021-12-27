package org.cd2h.drive.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.Revision;
import com.google.api.services.drive.model.RevisionList;
import com.google.api.services.drive.model.User;

import edu.uiowa.extraction.PropertyLoader;

public class Tester extends GoogleAPI {
	static Logger logger = LogManager.getLogger(Tester.class);
    static String APPLICATION_NAME = "CD2H Drive Tester";
    static List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);

    public static void main(String[] args) throws GeneralSecurityException, IOException {
	prop_file = PropertyLoader.loadProperties("google");
	
	// Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT, SCOPES, prop_file.getProperty("drive.credentials"), prop_file.getProperty("drive.tokens")))
                .setApplicationName(APPLICATION_NAME)
                .build();

//        executeDrivePermission(service, "1cDR2wgV-JMmFggG0xgRMk0bwy-SF-Wyl2KkDXSTV2hY", "cook.cd2h@gmail.com");
       recent(service);
//	cd2h(service);
    }

    public static void recent(Drive service) throws GeneralSecurityException, IOException {
        FileList result = service.files().list()
                .setPageSize(5)
                .setFields("nextPageToken, files(id, name, modifiedTime, createdTime, lastModifyingUser, owners, mimeType)")
                .execute();
        logger.debug("next page token: " + result.getNextPageToken());
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            logger.info("No files found.");
        } else {
            logger.info("Recent Files:");
            for (File file : files) {
                logger.info("name: " + file.getName() + "\tid: " + file.getId());
                logger.info("\tlast modified by: " + (file.getLastModifyingUser() == null ? null : file.getLastModifyingUser().getDisplayName()));
                logger.info("\tcreated: " + file.getCreatedTime());
                logger.info("\tmodified: " + file.getModifiedTime());
                for (User user : file.getOwners()) {
                    logger.info("\towner: " + user.getDisplayName());
                }
                logger.info("\tmime type: " + file.getMimeType());
                detailedRevisions(service, file.getId());
            }
        }
        
    }

    public static void cd2h(Drive service) throws GeneralSecurityException, IOException {
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
        	.setQ("'0B1ggMLKFepMxU21oOTk2dXZTcmc' in parents")
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
            }
        }
        
    }

    private static void detailedRevisions(Drive service, String fileId) {
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
	    }
	} catch (IOException e) {
	    logger.info("\t\tAn error occured.");
	}
    }
    
    protected static void executeDrivePermission(Drive drive, String fileId, String email) throws IOException {
	Permission newPermission = new Permission();
	newPermission.setKind("drive#permission");
	newPermission.setRole("owner");
	newPermission.setType("user");
	newPermission.setEmailAddress(email);

	Drive.Permissions.Create createPerm = drive.permissions().create(fileId, newPermission);
	createPerm.setTransferOwnership(true);
	Permission response = createPerm.execute();

	if (response == null) {
	    logger.error("response is null!");
	}
    }
}
