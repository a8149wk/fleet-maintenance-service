package com.fms.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public final class FileUploadUtil {

    private FileUploadUtil() {
    }

    public static String save(String uploadDir, String subDir, MultipartFile file) throws IOException {
        Path targetDir = Paths.get(uploadDir, subDir);
        Files.createDirectories(targetDir);

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > -1) {
            ext = original.substring(dot);
        }
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = targetDir.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return subDir + "/" + name;
    }
}
