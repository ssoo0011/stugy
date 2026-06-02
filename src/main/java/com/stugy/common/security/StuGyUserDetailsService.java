package com.stugy.common.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.stugy.user.repository.UserRepository;

@Service
public class StuGyUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public StuGyUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		com.stugy.user.domain.User user = userRepository.findByLoginId(loginId)
				.orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."));

		return User.withUsername(user.getLoginId())
				.password(user.getPasswordHash())
				.roles("USER")
				.build();
	}
}
