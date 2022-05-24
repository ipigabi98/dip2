package rest.data.utilities;

import lombok.Data;

@Data
public class JWTData {
    private String email;
    private String[] roles;
}
