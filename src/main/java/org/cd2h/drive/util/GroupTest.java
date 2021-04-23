package org.cd2h.drive.util;
// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// [START admin_sdk_directory_quickstart]
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GroupTest {
    private static final String APPLICATION_NAME = "Google Admin SDK Directory API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "/Users/eichmann/Documents/Components/google_credentials";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/Users/eichmann/Documents/Components/google_credentials/key.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
//    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
//        // Load client secrets.
//        InputStream in = GroupTest.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
//        if (in == null) {
//            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
//        }
//        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
//
//        // Build flow and trigger user authorization request.
//        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
//                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
//                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
//                .setAccessType("offline")
//                .build();
//        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
//        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
//    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
//    	GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/key.json")).createScoped("https://www.googleapis.com/auth/cloud-identity.groups","https://www.googleapis.com/auth/admin.directory.user");
//    	GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/key.json")).createScoped(Collections.singleton("https://www.googleapis.com/auth/admin.directory.group"));
//    	GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/key.json")).createScoped(Collections.singleton("https://www.googleapis.com/auth/admin.directory.user"));
    	List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/admin.directory.group","https://www.googleapis.com/auth/admin.directory.group.member");
    	GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/key.json")).createScoped(scopes);
    	credential.refreshToken();
    	System.out.println("access token: " + credential.getAccessToken());
    			// Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Directory service = new Directory.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("n3c-groups")
                .build();

//        // Print the first 10 users in the domain.
//        Users result = service.users().list()
//                .setCustomer("my_customer")
//                .setMaxResults(10)
//                .setOrderBy("email")
//                .execute();

//        Directory.Groups.List res = service.groups().list();
//        Groups groups = res.execute();
//        for (com.google.api.services.admin.directory.model.Group member : groups.getGroups()) {
//            System.out.println(member.toString());
//        }
       
        Directory.Members.List result = service.members().list("n3c-admin@ctsa.io");
        Members members = result.execute();
        System.out.println("Members of n3c-groups");
        for (Member member : members.getMembers()) {
            System.out.println(member.getEmail());
        }

//        List<User> users = result.getUsers();
//        if (users == null || users.size() == 0) {
//            System.out.println("No users found.");
//        } else {
//            System.out.println("Users:");
//            for (User user : users) {
//                System.out.println(user.getName().getFullName());
//            }
//        }
    }
}
// [END admin_sdk_directory_quickstart]