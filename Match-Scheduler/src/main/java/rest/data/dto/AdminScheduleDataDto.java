package rest.data.dto;

import lombok.Data;
import rest.data.dto.ScheduleDataDto;

import java.time.LocalDateTime;

@Data
public class AdminScheduleDataDto {
    private Long userId;
    private Long Id;
    private LocalDateTime uploadDate;
    private String inputFileName;
    private String resultFileName;
    private String status;
    private LocalDateTime finishedDate;
}
