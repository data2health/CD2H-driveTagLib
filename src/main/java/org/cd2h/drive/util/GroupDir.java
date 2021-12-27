package org.cd2h.drive.util;

//Copyright 2018 Google LLC
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

//[START admin_sdk_directory_quickstart]
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

public class GroupDir {
	private static final String APPLICATION_NAME = "Google Admin SDK Directory API Java Quickstart";
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	private static final String TOKENS_DIRECTORY_PATH = "/Users/eichmann/Documents/Components/google_credentials/tokens";

	/**
	 * Global instance of the scopes required by this quickstart. If modifying these
	 * scopes, delete your previously saved tokens/ folder.
	 */
//	private static final List<String> SCOPES = Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
	static List<String> SCOPES = Arrays.asList(
			DirectoryScopes.ADMIN_DIRECTORY_GROUP,
			DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER,
			DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY
			);

	/**
	 * Creates an authorized Credential object.
	 * 
	 * @param HTTP_TRANSPORT The network HTTP Transport.
	 * @return An authorized Credential object.
	 * @throws IOException If the credentials.json file cannot be found.
	 */
	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		// Load client secrets.
		InputStream in = new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/keats.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
						.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
						.setAccessType("offline").build();
		LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
		return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
	}

	public static void main(String... args) throws IOException, GeneralSecurityException {
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		Directory service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
				.setApplicationName(APPLICATION_NAME).build();

		switch(args[0]) {
		case "groups":
			groups(service);
			break;
		case "users":
			users(service);
			break;
		}
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
        Directory.Members.List result = service.members().list("025b2l0r0sq7ka1");
        Members members = result.execute();
        System.out.println("Members of n3c-admin");
        for (Member member : members.getMembers()) {
            System.out.println(member.toPrettyString());
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

//     List<User> users = result.getUsers();
//     if (users == null || users.size() == 0) {
//         System.out.println("No users found.");
//     } else {
//         System.out.println("Users:");
//         for (User user : users) {
//             System.out.println(user.getName().getFullName() +  " : " + user.getPrimaryEmail() + " : " + user.toPrettyString());
//         }
//     }
}
