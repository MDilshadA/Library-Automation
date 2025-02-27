package org.library;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class LibraryCurrentVersion {

    public static Map<String, String> getCurrentVersions(File[] jarFiles) {
        Map<String, String> jarVersionMap = new HashMap<>();
        if (jarFiles == null || jarFiles.length == 0) return jarVersionMap;

        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                Manifest manifest = jar.getManifest();
                if (manifest != null) {
                    String version = extractVersion(manifest.getMainAttributes());
                    if ("v-missing".equals(version)) {
                        version = searchManifestEntries(manifest);
                    }
                    jarVersionMap.put(jarFile.getName(), version);
                }
            } catch (IOException e) {
                System.err.println("Error reading JAR: " + jarFile.getName());
            }
        }
        return jarVersionMap;
    }

    private static String searchManifestEntries(Manifest manifest) {
        for (Map.Entry<String, Attributes> entry : manifest.getEntries().entrySet()) {
            String version = extractVersion(entry.getValue());
            if (!"v-missing".equals(version)) return version;
        }
        return "v-missing";
    }

    private static String extractVersion(Attributes attributes) {
        if (attributes == null) return "v-missing";
        String[] versionKeys = {"Implementation-Version", "Specification-Version", "Bundle-Version", "Version"};

        for (String key : versionKeys) {
            String value = attributes.getValue(key);
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "v-missing";
    }
}
