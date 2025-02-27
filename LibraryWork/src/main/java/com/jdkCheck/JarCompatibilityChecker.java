package com.jdkCheck;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class JarCompatibilityChecker {
    private static final String DIRECTORY_PATH = "lib";
    private static final Map<Integer, String> JAVA_VERSIONS = new HashMap<>();

    static {
        JAVA_VERSIONS.put(52, "JDK 8");
        JAVA_VERSIONS.put(53, "JDK 9");
        JAVA_VERSIONS.put(54, "JDK 10");
        JAVA_VERSIONS.put(55, "JDK 11");
        JAVA_VERSIONS.put(56, "JDK 12");
        JAVA_VERSIONS.put(57, "JDK 13");
        JAVA_VERSIONS.put(58, "JDK 14");
        JAVA_VERSIONS.put(59, "JDK 15");
        JAVA_VERSIONS.put(60, "JDK 16");
        JAVA_VERSIONS.put(61, "JDK 17");
        JAVA_VERSIONS.put(62, "JDK 18");
        JAVA_VERSIONS.put(63, "JDK 19");
        JAVA_VERSIONS.put(64, "JDK 20");
        JAVA_VERSIONS.put(65, "JDK 21");
    }

    public static void main(String[] args) {
        File dir = new File(DIRECTORY_PATH);
        File[] jarFiles = dir.listFiles((d, name) -> name.endsWith(".jar"));

        if (jarFiles == null || jarFiles.length == 0) {
            System.out.println("No JAR files found.");
            return;
        }

        for (File jarFile : jarFiles) {
            System.out.print(jarFile.getName() + " -> ");
            checkJarCompatibility(jarFile);
        }
    }

    private static void checkJarCompatibility(File jarFile) {
        try (ZipFile zipFile = new ZipFile(jarFile)) {
            Set<Integer> detectedVersions = new TreeSet<>();

            for (ZipEntry entry : Collections.list(zipFile.entries())) {
                if (entry.getName().endsWith(".class")) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        int majorVersion = readMajorVersion(is);
                        detectedVersions.add(majorVersion);
                    }
                }
            }

            // Find the highest detected version
            if (!detectedVersions.isEmpty()) {
                int highestVersion = Collections.max(detectedVersions);
                String javaVersion = JAVA_VERSIONS.getOrDefault(highestVersion, "UNKNOWN");

                if (JAVA_VERSIONS.containsKey(highestVersion)) {
                    System.out.println("Compatible with " + javaVersion);
                } else {
                    System.out.println("Not compatible with JDK 8, 11, or 21 (Found major version: " + highestVersion + ")");
                }
            } else {
                System.out.println("No .class files found in JAR.");
            }

        } catch (IOException e) {
            System.out.println("Error reading JAR: " + e.getMessage());
        }
    }

    private static int readMajorVersion(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        dis.skipBytes(6); // Skip the first 6 bytes (magic number and minor version)
        return dis.readUnsignedShort(); // Read a major version
    }
}

