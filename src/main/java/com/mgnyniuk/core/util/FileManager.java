package com.mgnyniuk.core.util;

import java.io.File;

/**
 * Created by maksym on 5/26/14.
 */
public class FileManager {

    public static void deleteFilesFromCurrentDir(String namePattern) {

        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);
        File workDir = new File(workingDir);
        File[] filesList = workDir.listFiles((dir, name) -> name.matches(namePattern));

        for (File file : filesList) {

            if (file.delete()) {
                System.out.println(file.getName() + " is deleted!");
            } else {
                System.out.println("Delete operation is failed.");
            }

        }

    }
}
