package com.stugy.common.security;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
		@NotBlank String loginId,
		@NotBlank String password,
		boolean rememberMe
) {
}
