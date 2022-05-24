package rest.data.dto.constraints;

import lombok.Data;

import java.util.List;

@Data
public class BR2Dto {
    private String id;
    private Integer intp;
    private String homeMode;
    private Integer penalty;
    private List<Integer> slots;
    private List<Integer> teams;
    private String type;
}
