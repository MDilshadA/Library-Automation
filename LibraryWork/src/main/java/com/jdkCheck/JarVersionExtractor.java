package com.jdkCheck;
import java.io.*;
import java.util.jar.*;
import java.util.*;

public class JarVersionExtractor {
    public static void main(String[] args) {
        String outputFile = "output/jar_version.txt";
        File directory = new File("lib");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jarFiles == null || jarFiles.length == 0) {
                writer.write("No JAR files found.\n");
                return;
            }

            for (File jarFile : jarFiles) {
                writer.write(jarFile.getName() + " : ");

                try (JarFile jar = new JarFile(jarFile)) {
                    Manifest manifest = jar.getManifest();

                    if (manifest != null) {
                        Attributes mainAttributes = manifest.getMainAttributes();
                        String version = extractVersion(mainAttributes);

                        // Debugging: Print all main attributes
                        System.out.println("Main Attributes for " + jarFile.getName() + ":");
                        for (Map.Entry<Object, Object> entry : mainAttributes.entrySet()) {
                            System.out.println(entry.getKey() + ": " + entry.getValue());
                        }

                        if (version.equals("v-missing")) {
                            Map<String, Attributes> entries = manifest.getEntries();
                            for (Map.Entry<String, Attributes> entry : entries.entrySet()) {
                                System.out.println("Named Section [" + entry.getKey() + "]");
                                for (Map.Entry<Object, Object> attrEntry : entry.getValue().entrySet()) {
                                    System.out.println(attrEntry.getKey() + ": " + attrEntry.getValue());
                                }

                                version = extractVersion(entry.getValue());
                                if (!version.equals("v-missing")) {
                                    break; // Stop once we find a valid version
                                }
                            }
                        }

                        if (version.equals("v-missing")) {
                            Attributes xmlBeansAttributes = manifest.getAttributes("org/apache/xmlbeans/");
                            if (xmlBeansAttributes != null) {
                                version = extractVersion(xmlBeansAttributes);
                            }
                        }

                        writer.write(version + "\n");
                    } else {
                        writer.write("No manifest found.\n");
                    }
                } catch (IOException e) {
                    writer.write("Error reading JAR: " + e.getMessage() + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing output file: " + e.getMessage());
        }

        System.out.println("JAR version extraction complete. Check jar_version.txt.");
    }

    private static String extractVersion(Attributes attributes) {
        if (attributes == null) return "v-missing";

        String versionKeys[] = {
                "Implementation-Version",
                "Specification-Version",
                "Bundle-Version",
                "Version"
        };

        for (String key : versionKeys) {
            String value = attributes.getValue(key);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "v-missing";
    }
}
