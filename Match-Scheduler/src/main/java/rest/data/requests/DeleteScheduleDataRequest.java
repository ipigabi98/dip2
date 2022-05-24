package rest.data.requests;

import lombok.Data;

@Data
public class DeleteScheduleDataRequest {
    private Long userId;
    private Long fileId;
}
