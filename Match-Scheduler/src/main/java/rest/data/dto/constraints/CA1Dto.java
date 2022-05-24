package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class CA1Dto {
    private String id;
    private Integer max;
    private Integer min;
    private String mode;
    private Integer penalty;
    private List<Integer> slots;
    private Integer teams;
    private String type;
}
