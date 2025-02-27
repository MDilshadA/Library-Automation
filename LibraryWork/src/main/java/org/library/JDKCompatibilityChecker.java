package org.library;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JDKCompatibilityChecker {

    public static Map<String, String> getJDKCompatibility(File[] jarFiles) {
        Map<String, String> jdkSupport = new HashMap<>();

        if (jarFiles == null || jarFiles.length == 0) return jdkSupport;

        for (File jarFile : jarFiles) {
            try (JarFile jar = new JarFile(jarFile)) {
                for (JarEntry entry : jar.stream().toList()) {
                    if (entry.getName().endsWith(".class")) {
                        try (InputStream is = jar.getInputStream(entry)) {
                            byte[] classBytes = new byte[8];
                            if (is.read(classBytes) == 8) {
                                int majorVersion = (classBytes[6] & 0xFF) << 8 | (classBytes[7] & 0xFF);
                                jdkSupport.put(jarFile.getName(), mapToJDKVersion(majorVersion));
                                break;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                jdkSupport.put(jarFile.getName(), "Unknown");
            }
        }
        return jdkSupport;
    }

    private static String mapToJDKVersion(int majorVersion) {
        return switch (majorVersion) {
            case 45 -> "JDK 1.1";
            case 46 -> "JDK 1.2";
            case 47 -> "JDK 1.3";
            case 48 -> "JDK 1.4";
            case 49 -> "JDK 5";
            case 50 -> "JDK 6";
            case 51 -> "JDK 7";
            case 52 -> "JDK 8";
            case 53 -> "JDK 9";
            case 54 -> "JDK 10";
            case 55 -> "JDK 11";
            case 56 -> "JDK 12";
            case 57 -> "JDK 13";
            case 58 -> "JDK 14";
            case 59 -> "JDK 15";
            case 60 -> "JDK 16";
            case 61 -> "JDK 17";
            case 62 -> "JDK 18";
            case 63 -> "JDK 19";
            case 64 -> "JDK 20";
            case 65 -> "JDK 21";
            default -> "Unknown";
        };
    }
}
