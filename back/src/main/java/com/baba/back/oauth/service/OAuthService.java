package com.baba.back.oauth.service;

import com.baba.back.oauth.OAuthClient;
import com.baba.back.oauth.domain.JoinedUser;
import com.baba.back.oauth.dto.OAuthAccessTokenResponse;
import com.baba.back.oauth.dto.TokenResponse;
import com.baba.back.oauth.repository.JoinedUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OAuthService {

    private final OAuthClient oAuthClient;
    private final TokenProvider tokenProvider;
    private final JoinedUserRepository joinedUserRepository;

    public TokenResponse signInKakao(final String code) {
        final OAuthAccessTokenResponse accessTokenResponse = oAuthClient.getOAuthAccessToken(code);
        final String userId = oAuthClient.getUserId(accessTokenResponse.getAccessToken());
        final JoinedUser joinedUser = joinedUserRepository.findById(userId)
                .orElseGet(() -> joinedUserRepository.save(new JoinedUser(userId, false)));
        final String token = tokenProvider.createToken(userId);

        if (joinedUser.getIsSigned()) {
            return new TokenResponse(true, "이미 가입되어 있습니다.", token);
        }
        return new TokenResponse(false, "가입이 필요합니다.", token);
    }
}
