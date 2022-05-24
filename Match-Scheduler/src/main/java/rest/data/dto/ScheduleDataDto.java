package rest.data.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScheduleDataDto {
    private Long Id;
    private LocalDateTime uploadDate;
    private String inputFileName;
    private String resultFileName;
    private String status;
    private LocalDateTime finishedDate;
}
