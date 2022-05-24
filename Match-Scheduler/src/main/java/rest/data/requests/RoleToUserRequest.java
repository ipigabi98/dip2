package rest.data.requests;

import lombok.Data;

@Data
public class RoleToUserRequest {
    private Long id;
    private String roleName;
}
