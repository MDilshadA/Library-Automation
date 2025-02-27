package org.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class LibraryLatestVersion {

    private static final String MAVEN_API_URL =
            "https://search.maven.org/solrsearch/select?q=g:%s+AND+a:%s&core=gav&rows=1&wt=json";

    private static final Map<String, String> KNOWN_GROUP_IDS = new HashMap<>();

    static {
//        KNOWN_GROUP_IDS.put("poi", "org.apache.poi");
        KNOWN_GROUP_IDS.put("poi-ooxml", "org.apache.poi");
        KNOWN_GROUP_IDS.put("xmlbeans", "org.apache.xmlbeans");
        KNOWN_GROUP_IDS.put("commons-lang3", "org.apache.commons");
    }

    public static Map<String, String> getLatestVersions(File[] jarFiles) {
        Map<String, String> latestVersions = new HashMap<>();

        for (File file : jarFiles) {
            String[] metadata = extractMavenMetadata(file);

            // Fallback: Try extracting from filename if metadata is missing
            if (metadata == null) {
                metadata = extractFromJarName(file);
            }

            if (metadata != null) {
                String groupId = metadata[0];
                String artifactId = metadata[1];
                String latestVersion = fetchLatestVersionFromMaven(groupId, artifactId);
                latestVersions.put(file.getName(), latestVersion);
            } else {
                latestVersions.put(file.getName(), "Check Manually (pom.properties missing)");
            }
        }

        return latestVersions;
    }

    private static String fetchLatestVersionFromMaven(String groupId, String artifactId) {
        String apiUrl = String.format(MAVEN_API_URL, groupId, artifactId);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray docs = jsonResponse.getAsJsonObject("response").getAsJsonArray("docs");

            if (!docs.isEmpty()) {
                return docs.get(0).getAsJsonObject().get("v").getAsString();
            }
        } catch (Exception e) {
            System.err.println("Error fetching latest version for " + artifactId + ": " + e.getMessage());
        }
        return "NOT_FOUND";
    }

    private static String[] extractMavenMetadata(File jarFile) {
        try (ZipFile zipFile = new ZipFile(jarFile)) {
            ZipEntry pomEntry = zipFile.stream()
                    .filter(entry -> entry.getName().endsWith("pom.properties"))
                    .findFirst()
                    .orElse(null);

            if (pomEntry != null) {
                try (InputStream input = zipFile.getInputStream(pomEntry)) {
                    Properties properties = new Properties();
                    properties.load(input);
                    return new String[]{
                            properties.getProperty("groupId"),
                            properties.getProperty("artifactId")
                    };
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading JAR metadata from " + jarFile.getName() + ": " + e.getMessage());
        }
        return null;
    }

    private static String[] extractFromJarName(File jarFile) {
        String fileName = jarFile.getName();
//        System.out.println("Jar Name : "+fileName);
        if (fileName.matches("(.+)-\\d+.*\\.jar")) {
            String artifactId = fileName.replaceAll("-(\\d+.*)\\.jar", "");
//            System.out.println("artifactId of current jars : "+artifactId);
            String groupId = KNOWN_GROUP_IDS.get(artifactId);

            if (groupId != null) {
                return new String[]{groupId, artifactId};
            } else {
                System.err.println("Unknown groupId for artifactId: " + artifactId);
            }
        }
        return null;
    }
}
