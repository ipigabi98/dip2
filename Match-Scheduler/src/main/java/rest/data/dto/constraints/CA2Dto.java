package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class CA2Dto {
    private String id;
    private Integer max;
    private Integer min;
    private String mode1;
    private Integer penalty;
    private List<Integer> slots;
    private Integer teams1;
    private List<Integer> teams2;
    private String type;
}
