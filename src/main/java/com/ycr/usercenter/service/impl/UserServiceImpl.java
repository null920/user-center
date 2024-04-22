package com.ycr.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ycr.usercenter.common.ErrorCode;
import com.ycr.usercenter.exception.BusinessException;
import com.ycr.usercenter.mapper.UserMapper;
import com.ycr.usercenter.model.domain.User;
import com.ycr.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ycr.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.ycr.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author null&&
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-04-10 17:49:34
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
		implements UserService {
	/**
	 * 只包含字母、数字和下划线的正则表达式
	 */
	private static final String USERNAME_PATTERN = "^\\w*$";
	/**
	 * 加盐，混淆密码
	 */
	private static final String SALT = "null";


	@Resource
	private UserMapper userMapper;

	@Override
	public long userRegister(String userAccount, String userPassword, String checkPassword) {
		// 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度少于4位");
		}
		if (userPassword.length() < 8 || checkPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码少于8位");
		}
		// 账户不能包含特殊字符
		boolean matchesResult = userAccount.matches(USERNAME_PATTERN);
		if (!matchesResult) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
		}
		// 密码和校验密码相同
		if (!userPassword.equals(checkPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码相同");
		}
		// 账户不能重复
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.eq("user_account", userAccount);
		long count = userMapper.selectCount(wrapper);
		if (count > 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
		}
		// 对密码进行加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 插入数据
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encryptPassword);
		boolean saveResult = this.save(user);
		if (!saveResult) {
			throw new BusinessException(ErrorCode.DATABASE_ERROR, "插入异常");
		}
		return user.getId();
	}

	@Override
	public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
		// 校验
		if (StringUtils.isAnyBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		if (userAccount.length() < 4) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度少于4位");
		}
		if (userPassword.length() < 8) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度小于8位");
		}
		// 账号不能包含特殊字符
		boolean matchesResult = userAccount.matches(USERNAME_PATTERN);
		if (!matchesResult) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号包含特殊字符");
		}
		// 对密码进行加密
		String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
		// 查询用户是否存在
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.eq("user_account", userAccount);
		User user = userMapper.selectOne(wrapper);
		// 用户不存在
		if (user == null) {
			log.info("user login failed, userAccount does not exist");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
		}
		if (!encryptPassword.equals(user.getUserPassword())) {
			log.info("user login failed, userAccount does not exist");
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
		}
		// 脱敏
		User safetyUser = getSafetyUser(user);
		// 记录用户的登录态
		request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
		return safetyUser;
	}

	@Override
	public int userLogout(HttpServletRequest request) {
		request.getSession().removeAttribute(USER_LOGIN_STATE);
		return 1;
	}

	@Override
	public List<User> searchUsersByTags(List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		List<User> userList = userMapper.selectList(wrapper);
		Gson gson = new Gson();
		return userList.stream().filter(user -> {
			String tagStr = user.getTags();
			Set<String> tagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
			}.getType());
			tagNameSet = Optional.ofNullable(tagNameSet).orElse(new HashSet<>());
			for (String tagName : tagNameList) {
				// 只要有一个标签匹配就返回 true
				if (tagNameSet.contains(tagName)) {
					return true;
				}
			}
			return false;
		}).map(this::getSafetyUser).collect(Collectors.toList());
	}

	@Deprecated
	public List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
		if (CollectionUtils.isEmpty(tagNameList)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		for (String tagName : tagNameList) {
			wrapper = wrapper.like("tags", tagName);
		}
		List<User> userList = userMapper.selectList(wrapper);
		return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
	}

	@Override
	public Integer updateUser(User user, User loginUser) {
		// 校验参数
		if (user.getId() == null || loginUser.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		// 校验权限：仅管理员和自己可修改
		if (!isAdmin(loginUser) && !loginUser.getId().equals(user.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH, "无权限");
		}
		// 如果是管理员允许更新所有人的信息
		if (isAdmin(loginUser)) {
			return userMapper.updateById(user);
		}
		// 如果不是管理员只允许更新自己的信息
		if (loginUser.getId().equals(user.getId())) {
			return userMapper.updateById(user);
		}
		return null;
	}

	@Override
	public User getLoginUser(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
		if (loginUser == null) {
			throw new BusinessException(ErrorCode.NO_AUTH, "未登录");
		}
		return loginUser;
	}

	@Override
	public User selectUserById(Long id) {
		QueryWrapper<User> wrapper = new QueryWrapper<>();
		wrapper.eq("id", id);
		return userMapper.selectOne(wrapper);
	}



	/**
	 * 判断是否为管理员
	 *
	 * @param loginUser 当前登录用户
	 * @return 是否为管理员
	 */
	@Override
	public boolean isAdmin(User loginUser) {
		return loginUser != null && loginUser.getUserRole().equals(ADMIN_ROLE);
	}

	/**
	 * 用户信息脱敏
	 *
	 * @param originUser 原始用户
	 * @return 脱敏后的用户
	 */
	@Override
	public User getSafetyUser(User originUser) {
		if (originUser == null) {
			return null;
		}
		User safetyUser = new User();
		safetyUser.setId(originUser.getId());
		safetyUser.setUsername(originUser.getUsername());
		safetyUser.setUserAccount(originUser.getUserAccount());
		safetyUser.setAvatarUrl(originUser.getAvatarUrl());
		safetyUser.setGender(originUser.getGender());
		safetyUser.setPhone(originUser.getPhone());
		safetyUser.setEmail(originUser.getEmail());
		safetyUser.setUserStatus(originUser.getUserStatus());
		safetyUser.setCreateTime(originUser.getCreateTime());
		safetyUser.setUserRole(originUser.getUserRole());
		safetyUser.setTags(originUser.getTags());
		return safetyUser;
	}

}




