package fit.tatakae.infrastructure.web.security;

import fit.tatakae.application.usecase.IssueSessionTokensUseCase;
import fit.tatakae.application.usecase.RefreshSessionTokensUseCase;
import fit.tatakae.application.usecase.RevokeRefreshTokenUseCase;
import fit.tatakae.application.usecase.SessionTokens;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactional facade over session token use cases (application layer stays Spring-free).
 */
@Service
public class SessionAuthService {

    private final IssueSessionTokensUseCase issueSessionTokensUseCase;
    private final RefreshSessionTokensUseCase refreshSessionTokensUseCase;
    private final RevokeRefreshTokenUseCase revokeRefreshTokenUseCase;

    public SessionAuthService(IssueSessionTokensUseCase issueSessionTokensUseCase,
                              RefreshSessionTokensUseCase refreshSessionTokensUseCase,
                              RevokeRefreshTokenUseCase revokeRefreshTokenUseCase) {
        this.issueSessionTokensUseCase = issueSessionTokensUseCase;
        this.refreshSessionTokensUseCase = refreshSessionTokensUseCase;
        this.revokeRefreshTokenUseCase = revokeRefreshTokenUseCase;
    }

    @Transactional
    public SessionTokens issue(String userId) {
        return issueSessionTokensUseCase.execute(userId);
    }

    @Transactional
    public SessionTokens refresh(String refreshToken) {
        return refreshSessionTokensUseCase.execute(refreshToken);
    }

    @Transactional
    public void logout(String userId, String refreshToken) {
        revokeRefreshTokenUseCase.execute(userId, refreshToken);
    }
}
