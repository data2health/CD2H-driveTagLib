package org.cd2h.drive.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Member;
import com.google.api.services.admin.directory.model.Members;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("deprecation")
public class GroupTest {
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public static void main(String... args) throws IOException, GeneralSecurityException {
    	List<String> scopes = Arrays.asList("https://www.googleapis.com/auth/admin.directory.group","https://www.googleapis.com/auth/admin.directory.group.member");
    	GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("/Users/eichmann/Documents/Components/google_credentials/keats.json")).createScoped(scopes);
    	credential.refreshToken();
    	System.out.println("access token: " + credential.getAccessToken());

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
       
        Directory.Members.List result = service.members().list("025b2l0r0sq7ka1");
        Members members = result.execute();
        System.out.println("Members of n3c-admin");
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
