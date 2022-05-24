package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class BR1Dto {
    private String id;
    private Integer intp;
    private String mode2;
    private Integer penalty;
    private List<Integer> slots;
    private Integer teams;
    private String type;
}
