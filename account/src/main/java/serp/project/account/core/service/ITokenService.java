package serp.project.account.core.service;

import serp.project.account.core.domain.dto.response.TokenResponse;

public interface ITokenService {
    TokenResponse getUserToken(String username, String password);
}
