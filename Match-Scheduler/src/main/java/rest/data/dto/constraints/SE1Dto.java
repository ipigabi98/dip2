package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class SE1Dto {
    private String id;
    private Integer min;
    private Integer penalty;
    private List<Integer> teams;
    private String type;
}
