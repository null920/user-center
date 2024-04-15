package com.ycr.usercenter.service;

import com.ycr.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 用户服务测试
 *
 * @author null&&
 * @version 1.0
 * @date 2024/4/10 18:04
 */
@SpringBootTest
class UserServiceTest {
	@Resource
	UserService userService;

	@Test
	void testAddUser() {
		User user = new User();
		user.setUsername("test");
		user.setUserAccount("123");
		user.setAvatarUrl("");
		user.setGender(0);
		user.setUserPassword("xxx");
		user.setPhone("123");
		user.setEmail("456");
		boolean result = userService.save(user);
		System.out.println(user.getId());
		assertTrue(result);
	}

	/**
	 * 用户注册测试
	 */
	@Test
	void userRegister() {
		// 测试账户为空情况
		String userAccount = "";
		String userPassword = "";
		String checkPassword = "";
		long result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试密码为空情况
		userAccount = "test";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试验证密码为空情况
		userPassword = "123456789";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试账户不足4位情况
		checkPassword = "123456789";
		userAccount = "te";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试密码不足8位情况
		userAccount = "test";
		userPassword = "123456";
		checkPassword = "123456";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试账户含非法字符情况
		userAccount = "!te st";
		userPassword = "123456789";
		checkPassword = "123456789";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试验证密码不等于密码情况
		checkPassword = "12345678";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试账户重复情况
		userAccount = "12345";
		checkPassword = "123456789";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertEquals(-1, result);
		// 测试正确数据情况
		userAccount = "test";
		result = userService.userRegister(userAccount, userPassword, checkPassword);
		Assertions.assertTrue(result > 0);
	}
}