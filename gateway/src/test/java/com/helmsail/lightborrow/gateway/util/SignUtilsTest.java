package com.helmsail.lightborrow.gateway.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SignUtilsTest {

    @Test
    void hmacSha256ShouldProduceDeterministicResult() {
        String secret = "test_secret_123";
        String timestamp = "1719360000000";

        String result1 = SignUtils.hmacSha256(secret, timestamp);
        String result2 = SignUtils.hmacSha256(secret, timestamp);

        assertThat(result1).isEqualTo(result2).isNotBlank();
    }

    @Test
    void hmacSha256ShouldDifferForDifferentSecrets() {
        String timestamp = "1719360000000";
        String result1 = SignUtils.hmacSha256("secret1", timestamp);
        String result2 = SignUtils.hmacSha256("secret2", timestamp);
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void hmacSha256ShouldDifferForDifferentTimestamps() {
        String secret = "test_secret";
        String result1 = SignUtils.hmacSha256(secret, "1000");
        String result2 = SignUtils.hmacSha256(secret, "2000");
        assertThat(result1).isNotEqualTo(result2);
    }

    @Test
    void hmacSha256ShouldReturnBase64EncodedString() {
        String result = SignUtils.hmacSha256("secret", "timestamp");
        assertThat(result).matches("^[A-Za-z0-9+/=]+$");
    }

    @Test
    void sha1ShouldProduce40CharHexString() {
        String result = SignUtils.sha1("token1", "timestamp", "nonce");
        assertThat(result).hasSize(40).matches("^[0-9a-f]+$");
    }

    @Test
    void sha1ShouldBeDeterministic() {
        String result1 = SignUtils.sha1("a", "b", "c");
        String result2 = SignUtils.sha1("a", "b", "c");
        assertThat(result1).isEqualTo(result2);
    }

    @Test
    void sha1ShouldDifferWhenTokensChange() {
        String result1 = SignUtils.sha1("token1", "nonce");
        String result2 = SignUtils.sha1("token2", "nonce");
        assertThat(result1).isNotEqualTo(result2);
    }
}
