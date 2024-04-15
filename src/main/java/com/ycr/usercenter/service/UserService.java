package com.ycr.usercenter.service;

import com.ycr.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 *
 * @author null&&
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-04-10 17:49:34
 */
public interface UserService extends IService<User> {
	/**
	 * 用户注册
	 *
	 * @param userAccount   用户账户
	 * @param userPassword  密码
	 * @param checkPassword 校验密码
	 * @return 新用户 id
	 */
	long userRegister(String userAccount, String userPassword, String checkPassword);

	/**
	 * 用户登录
	 *
	 * @param userAccount  用户账户
	 * @param userPassword 密码
	 * @param request      http请求
	 * @return 用户信息（脱敏后）
	 */
	User userLogin(String userAccount, String userPassword, HttpServletRequest request);

	/**
	 * 用户信息脱敏
	 *
	 * @param originUser 原始用户数据
	 * @return 返回脱敏后的用户数据
	 */
	User getSafetyUser(User originUser);

	/**
	 * 退出登录
	 *
	 * @param request http请求
	 */
	int userLogout(HttpServletRequest request);
}
