package com.example.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "UploadRequest", description = "Request body for uploading PDF files")
public class UploadRequest {

    @Schema(
            description = "One or more PDF files to upload (max 3)",
            type = "array",
            format = "binary"
    )
    private MultipartFile[] files;

    public MultipartFile[] getFiles() {
        return files;
    }

    public void setFiles(MultipartFile[] files) {
        this.files = files;
    }
}
