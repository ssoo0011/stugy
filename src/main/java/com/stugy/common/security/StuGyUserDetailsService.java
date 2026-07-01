package com.stugy.common.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.stugy.user.mapper.UserMapper;

@Service
public class StuGyUserDetailsService implements UserDetailsService {

	private final UserMapper userMapper;

	public StuGyUserDetailsService(UserMapper userMapper) {
		this.userMapper = userMapper;
	}

	@Override
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		com.stugy.user.domain.User user = userMapper.selectByLoginId(loginId)
				.orElseThrow(() -> new UsernameNotFoundException("아이디 또는 비밀번호가 올바르지 않습니다."));

		return User.withUsername(user.getLoginId())
				.password(user.getPasswordHash())
				.roles("USER")
				.build();
	}
}
