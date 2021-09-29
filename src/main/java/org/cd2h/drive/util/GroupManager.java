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
import com.google.api.services.admin.directory.model.Group;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Hashtable;
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
			members(service, "040ew0vw1p0o54r");
			break;
		case "refresh":
			members(service, "040ew0vw1p0o54r");
			newMembers(service, "040ew0vw1p0o54r");
			break;
		case "insert":
			insertMember(service, "025b2l0r0sq7ka1", "david.eichmann@gmail.com");
			break;
		case "delete":
			deleteMember(service, "025b2l0r0sq7ka1", "david.eichmann@gmail.com");
			break;
		case "admin":
			Hashtable<String,String> cache = currentMembers(service, "040ew0vw1p0o54r");
			populate(cache,service, "040ew0vw1p0o54r");
			break;
		case "purge":
			purge(service, "025b2l0r0sq7ka1");
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

	static void groups(Directory service) throws IOException, SQLException {
		simpleStmt("truncate n3c_groups.google_group_raw");

		System.out.println("Groups:");
		Groups groups = service.groups().list().setDomain("ctsa.io").execute();
		// fill list of users by next page
		boolean next = (groups.getNextPageToken() != null);
		while (next) {
			Groups groups2 = service.groups().list().setDomain("ctsa.io").setPageToken(groups.getNextPageToken()).execute();
			System.out.println("group: " + groups2.toPrettyString());
			groups.getGroups().addAll(groups2.getGroups());
			groups.setNextPageToken(groups2.getNextPageToken());
			next = (groups2.getNextPageToken() != null);
		}
		for (Group group : groups.getGroups()) {
			System.out.println("Group: " + group.toPrettyString());
			
			PreparedStatement stmt = conn.prepareStatement("insert into n3c_groups.google_group_raw values(?::jsonb)");
			stmt.setString(1, group.toPrettyString());
			stmt.execute();
			stmt.close();
		}
	}

	static void users(Directory service) throws IOException, SQLException {
		simpleStmt("truncate n3c_groups.user_raw");
		
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
			
			PreparedStatement stmt = conn.prepareStatement("insert into n3c_groups.user_raw values(?::jsonb)");
			stmt.setString(1, user.toPrettyString());
			stmt.execute();
			stmt.close();
		}
	}

	static void members(Directory service) throws IOException, SQLException {
		simpleStmt("truncate n3c_groups.google_member_raw");
		
		PreparedStatement stmt = conn.prepareStatement("select id from n3c_groups.google_group");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String id = rs.getString(1);
			members(service, id);
		}
	}
	
	static void members(Directory service, String groupKey) throws IOException, SQLException {
		int count = 0;
        Directory.Members.List result = service.members().list(groupKey);
        Members members = result.execute();
        if (members.getMembers() == null)
        	return;	
		boolean next = (members.getNextPageToken() != null);
		while (next) {
			Members members2 = service.members().list(groupKey)
//					.setCustomer("my_customer")
//					.setMaxResults(500)
//					.setOrderBy("email")
					.setPageToken(members.getNextPageToken())
					.execute();
			members.getMembers().addAll(members2.getMembers());
			members.setNextPageToken(members2.getNextPageToken());
			next = (members.getNextPageToken() != null);
		}
        logger.info("Members of " + groupKey);
        for (Member member : members.getMembers()) {
            logger.debug(member.toPrettyString());
			
			PreparedStatement stmt = conn.prepareStatement("insert into n3c_groups.google_member_raw values(?,?::jsonb)");
			stmt.setString(1, groupKey);
			stmt.setString(2, member.toPrettyString());
			stmt.execute();
			stmt.close();
			
			count++;
        }
        logger.info("\tcount: " + count);
	}
	
	static void newMembers(Directory service, String groupKey) throws SQLException, IOException {
		PreparedStatement stmt = conn.prepareStatement("select email,name from n3c_groups.new_member");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String email = rs.getString(1);
			String name = rs.getString(2);
			
			logger.info("adding user: " + email + " : " + name);

			Member newMember = new Member();
			newMember.setEmail(email);
			newMember.setDeliverySettings("ALL_MAIL");
			newMember.setRole("MEMBER");

			try {
				service.members().insert(groupKey, newMember).execute();
			} catch (IOException e) {
				logger.info("\t*** insert error ***");
			}
			
		}
		stmt.close();
	}

	static void listMembers(Directory service, String groupKey) throws IOException, SQLException {
        Directory.Members.List result = service.members().list(groupKey);
        Members members = result.execute();
        if (members.getMembers() == null)
        	return;	
        System.out.println("Members of " + groupKey);
        for (Member member : members.getMembers()) {
            System.out.println(member.toPrettyString());
//            System.out.println(member.getDeliverySettings()); // this always seems to return null...
            /*
             * per Keats - a direct call for a specific member returns the delivery settings:
             * Member m = service.members().get(grpKey, memberAddr).execute();
             * System.out.println(m.getDeliverySettings());
             */
        }
	}
	
	static Hashtable<String,String> currentMembers(Directory service, String groupKey) throws IOException, SQLException {
		Hashtable<String,String> cache = new Hashtable<String,String>();
        Directory.Members.List result = service.members().list(groupKey);
        Members members = result.execute();
        if (members.getMembers() == null)
        	return cache;	
        System.out.println("Members of " + groupKey);
        for (Member member : members.getMembers()) {
        	cache.put(member.getEmail(), member.getEmail());
        }
        return cache;
	}
	
	static void populate(Hashtable<String,String> cache, Directory service, String groupKey) throws SQLException, IOException {
		PreparedStatement stmt = conn.prepareStatement("select email_address,nickname,group_status,email_preference from n3c_groups.n3c_allhands");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String email = rs.getString(1);
			String nickname = rs.getString(2);
			String status = rs.getString(3);
			String preference = rs.getString(4);
			
			if (cache.containsKey(email)) {
				logger.info("existing user: " + email + " : " + nickname);
				continue;
			}
			
			logger.info("adding user: " + email + " : " + nickname);

			Member newMember = new Member();
			newMember.setEmail(email);
			switch (preference) {
			case "email":
				newMember.setDeliverySettings("ALL_MAIL");
				break;
			case "no email":
				newMember.setDeliverySettings("NONE");
				break;
			case "digest":
				newMember.setDeliverySettings("DIGEST");
				break;
			}
			switch (status) {
			case "member":
				newMember.setRole("MEMBER");
				break;
			case "owner":
				newMember.setRole("OWNER");
				break;
			case "manager":
				newMember.setRole("MANAGER");
				break;
			}
			try {
				service.members().insert(groupKey, newMember).execute();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		stmt.close();
	}
	
	static void purge(Directory service, String groupKey) throws SQLException, IOException {
		PreparedStatement stmt = conn.prepareStatement("select email_address from n3c_groups.n3c_allhands");
		ResultSet rs = stmt.executeQuery();
		while (rs.next()) {
			String email = rs.getString(1);
			logger.info("removing user: " + email);
			try {
				service.members().delete(groupKey, email).execute();
			} catch (IOException e) {
				logger.error("error raised: " + e);
			}
		}
		stmt.close();
	}
	
	static void insertMember(Directory service, String groupKey, String email) throws IOException {
		Member newMember = new Member();
		newMember.setEmail(email);
		newMember.setDeliverySettings("DIGEST");
		newMember.setRole("MANAGER");
		service.members().insert(groupKey, newMember).execute();
	}

	static void deleteMember(Directory service, String groupKey, String email) throws IOException {
		service.members().delete(groupKey, email).execute();
	}

	public static void simpleStmt(String queryString) {
		try {
			logger.debug("executing " + queryString + "...");
			PreparedStatement beginStmt = conn.prepareStatement(queryString);
			beginStmt.executeUpdate();
			beginStmt.close();
		} catch (Exception e) {
			logger.error("Error in database initialization: " + e);
			e.printStackTrace();
		}
	}
}
