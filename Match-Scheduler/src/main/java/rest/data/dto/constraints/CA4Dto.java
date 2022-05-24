package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class CA4Dto {
    private String id;
    private Integer max;
    private Integer min;
    private String mode1;
    private String mode2;
    private Integer penalty;
    private List<Integer> slots;
    private List<Integer> teams1;
    private List<Integer> teams2;
    private String type;
}
