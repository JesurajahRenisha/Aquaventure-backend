package com.example.aquaventure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField(jwtUtil, "secret", "test_only_secret_key_for_unit_tests_do_not_use_in_prod_123456");
        setField(jwtUtil, "expirationMs", 86400000L);
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private User user() {
        User user = new User();
        user.setId(42L);
        user.setEmail("surfer@example.com");
        user.setRole(Role.SURFER);
        return user;
    }

    @Test
    void generateAndParse_roundTripsClaims() {
        String token = jwtUtil.generateToken(user());

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("surfer@example.com");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtUtil.extractRole(token)).isEqualTo(Role.SURFER);
    }

    @Test
    void expiredToken_isInvalid() throws Exception {
        setField(jwtUtil, "expirationMs", -1000L);
        String token = jwtUtil.generateToken(user());

        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    @Test
    void tamperedSignature_isInvalid() {
        String token = jwtUtil.generateToken(user());
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(jwtUtil.isTokenValid(tampered)).isFalse();
    }

    @Test
    void malformedToken_throwsOnParseButIsTokenValidReturnsFalse() {
        assertThat(jwtUtil.isTokenValid("not-a-jwt")).isFalse();
        assertThatThrownBy(() -> jwtUtil.extractEmail("not-a-jwt")).isInstanceOf(RuntimeException.class);
    }
}
