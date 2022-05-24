package rest.data.dto;

import lombok.Data;

import java.io.File;

@Data
public class UploadFileDto {
    private String fileName;
    private File content;
}
