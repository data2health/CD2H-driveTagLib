package org.cd2h.drive.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.api.services.admin.directory.model.Groups;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;
import com.google.api.services.admin.directory.model.User;
import com.google.api.services.admin.directory.model.Users;

import edu.uiowa.extraction.PropertyLoader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class GroupManager extends GoogleAPI {
    static Logger logger = Logger.getLogger(GroupManager.class);
	private static final String APPLICATION_NAME = "N3C Google Group Manager";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static String TOKENS_DIRECTORY_PATH = null;
	private static String CREDENTIALS = null;

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
	static List<String> SCOPES = Arrays.asList(
			DirectoryScopes.ADMIN_DIRECTORY_GROUP,
			DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER,
			DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY
			);

	public static void main(String... args) throws IOException, GeneralSecurityException, ClassNotFoundException, SQLException {
		PropertyConfigurator.configure(args[0]);
		prop_file = PropertyLoader.loadProperties(args[1]);
		conn = getConnection();
		
		TOKENS_DIRECTORY_PATH = prop_file.getProperty("groups.tokens");
		CREDENTIALS = prop_file.getProperty("groups.credentials");

		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Directory service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		switch(args[2]) {
		case "groups":
			groups(service);
			break;
		case "users":
			users(service);
			break;
		case "members":
			members(service, "025b2l0r0sq7ka1");
			break;
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = new FileInputStream(CREDENTIALS);
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	static void groups(Directory service) throws IOException {
		System.out.println("Groups:");
		Groups groups = service.groups().list().setDomain("ctsa.io").execute();
		System.out.println("group: " + groups.toPrettyString());
		// fill list of users by next page
		boolean next = (groups.getNextPageToken() != null);
		while (next) {
			Groups groups2 = service.groups().list().setDomain("ctsa.io").setPageToken(groups.getNextPageToken()).execute();
			System.out.println("group: " + groups2.toPrettyString());
			groups.getGroups().addAll(groups2.getGroups());
			groups.setNextPageToken(groups2.getNextPageToken());
			next = (groups2.getNextPageToken() != null);
		}
	}

	static void users(Directory service) throws IOException {
		System.out.println("Users:");
		Users users = service.users().list()
				.setCustomer("my_customer")
				.setMaxResults(500)
				.setOrderBy("email")
				.execute();
		// fill list of users by next page
		boolean next = (users.getNextPageToken() != null);
		while (next) {
			Users users2 = service.users().list()
					.setCustomer("my_customer")
					.setMaxResults(500)
					.setOrderBy("email")
					.setPageToken(users.getNextPageToken())
					.execute();
			users.getUsers().addAll(users2.getUsers());
			users.setNextPageToken(users2.getNextPageToken());
			next = (users.getNextPageToken() != null);
		}
		for (User user : users.getUsers()) {
//			System.out.println(user.getName().getFullName() + " : " + user.getPrimaryEmail());
			System.out.println(user.getName().getFullName() + " : " + user.getPrimaryEmail() + " : " + user.toPrettyString());
		}
	}

	static void members(Directory service, String groupKey) throws IOException {
        Directory.Members.List result = service.members().list(groupKey);
        Members members = result.execute();
        System.out.println("Members of n3c-admin");
        for (Member member : members.getMembers()) {
            System.out.println(member.toPrettyString());
        }
	}

}
