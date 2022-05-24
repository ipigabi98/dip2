package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class GA1Dto {
    private String id;
    private Integer max;
    private List<List<Integer>> meetings;
    private Integer min;
    private Integer penalty;
    private List<Integer> slots;
    private String type;
}
