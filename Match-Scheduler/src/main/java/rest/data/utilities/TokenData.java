package rest.data.utilities;

import lombok.Data;

@Data
public class TokenData {
    Long id;
    String accessToken;
    String refreshToken;
}
