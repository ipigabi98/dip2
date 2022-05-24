package rest.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResultDto {
    private String round;
    private String homeTeam;
    private String awayTeam;
}
