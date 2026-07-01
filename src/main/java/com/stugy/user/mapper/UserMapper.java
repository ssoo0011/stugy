package com.stugy.user.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.stugy.user.domain.User;

@Mapper
public interface UserMapper {

	int insertUser(User user);

	int updateUserProfile(User user);

	boolean selectExistsByLoginId(String loginId);

	boolean selectExistsByEmail(String email);

	boolean selectExistsByNickname(String nickname);

	boolean selectExistsByPhoneNumber(String phoneNumber);

	boolean selectExistsByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);

	boolean selectExistsByNicknameAndIdNot(@Param("nickname") String nickname, @Param("id") Long id);

	boolean selectExistsByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);

	Optional<User> selectByLoginId(String loginId);
}
