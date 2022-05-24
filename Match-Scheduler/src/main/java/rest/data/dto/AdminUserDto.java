package rest.data.dto;

import lombok.Data;

@Data
public class AdminUserDto {
    private Long id;
    private String email;
    private Boolean hasUserRole;
    private Boolean hasAdminRole;
}
