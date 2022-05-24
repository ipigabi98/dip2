package rest.data.requests;

import logic.constraints.breakConstraints.BR1;
import logic.constraints.capacityConstraints.CA1;
import logic.entities.Team;
import lombok.Data;
import rest.data.dto.constraints.*;

import java.util.List;

@Data
public class UploadManualInputsRequest {
    private String roundRobin;
    private List<Team> teams;
    private List<BR1Dto> br1;
    private List<BR2Dto> br2;
    private List<CA1Dto> ca1;
    private List<CA2Dto> ca2;
    private List<CA3Dto> ca3;
    private List<CA4Dto> ca4;
    private List<FA2Dto> fa2;
    private List<GA1Dto> ga1;
    private List<SE1Dto> se1;
}
