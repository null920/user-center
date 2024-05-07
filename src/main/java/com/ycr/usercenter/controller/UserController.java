package com.ycr.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ycr.usercenter.common.BaseResponse;
import com.ycr.usercenter.common.ErrorCode;
import com.ycr.usercenter.exception.BusinessException;
import com.ycr.usercenter.model.domain.User;
import com.ycr.usercenter.model.request.UserLoginRequest;
import com.ycr.usercenter.model.request.UserRegisterRequest;
import com.ycr.usercenter.model.vo.IndexUserVO;
import com.ycr.usercenter.service.UserService;
import com.ycr.usercenter.utils.ReturnResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.ycr.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.ycr.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author null&&
 * @version 1.0
 * @date 2024/4/11 11:27
 */
@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = {"https://user.null920.top", "http://user.null920.top", "http://localhost:3000", "http://127.0.0.1:3000",
        "https://match.null920.top", "http://match.null920.top"},
        allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;

    // 用户注册
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return ReturnResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return ReturnResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ReturnResultUtils.success(result);
    }

    // 用户登录
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User result = userService.userLogin(userAccount, userPassword, request);
        return ReturnResultUtils.success(result);
    }

    // 用户注销
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ReturnResultUtils.success(result);
    }

    // 获取当前登录用户
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 这样做的话如果用户的信息有更新，同时也要更新session
        String userJson = (String) request.getSession().getAttribute(USER_LOGIN_STATE);
        Gson gson = new Gson();
        User currentUser = gson.fromJson(userJson, new TypeToken<User>() {
        }.getType());
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "请先登录");
        }
        return ReturnResultUtils.success(currentUser);
    }

    // 根据用户名查询用户
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        // 鉴权，仅管理员可查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "请先登录");
        }
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            wrapper.like("username", username);
        }
        List<User> userList = userService.list(wrapper);
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ReturnResultUtils.success(result);
    }

    // 根据标签查询用户
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签为空");
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ReturnResultUtils.success(userList);
    }

    // 伙伴匹配首页推荐
    @GetMapping("/recommend")
    public BaseResponse<IndexUserVO> recommendUsers(Long pageSize, Long pageNum, HttpServletRequest request) {
        if (pageSize == null || pageNum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        IndexUserVO indexUserVO = userService.recommendUsers(pageSize, pageNum, request);
        return ReturnResultUtils.success(indexUserVO);
    }

    // 获取最匹配的用户
    @GetMapping("/match")
    public BaseResponse<IndexUserVO> matchUsers(Long num, HttpServletRequest request) {
        if (num == null || num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空或不符合要求");
        }
        IndexUserVO userList = userService.matchUsers(num, request);
        return ReturnResultUtils.success(userList);
    }

    // 更新用户
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(user, loginUser);
        if (result != null) {
            // 更新Session
            User safetyUser = userService.getSafetyUser(userService.selectUserById(user.getId()));
            Gson gson = new Gson();
            String safetyUserJson = gson.toJson(safetyUser);
            request.getSession().setAttribute(USER_LOGIN_STATE, safetyUserJson);
            return ReturnResultUtils.success(result);
        }
        return ReturnResultUtils.error(ErrorCode.PARAMS_ERROR, "更新失败");
    }

    // 删除用户
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 鉴权，仅管理员可查询
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(id);
        return ReturnResultUtils.success(result);
    }


    /**
     * 是否为管理员
     *
     * @param request http请求
     * @return 是管理员返回true，不是返回false
     */
    private boolean isAdmin(HttpServletRequest request) {
        String userJson = (String) request.getSession().getAttribute(USER_LOGIN_STATE);
        Gson gson = new Gson();
        User currentUser = gson.fromJson(userJson, new TypeToken<User>() {
        }.getType());
        return currentUser != null && currentUser.getUserRole().equals(ADMIN_ROLE);
    }
}
