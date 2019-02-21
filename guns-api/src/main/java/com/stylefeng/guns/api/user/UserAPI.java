package com.stylefeng.guns.api.user;

public interface UserAPI {
     int login(String username,String password);

     //注册
     boolean register(UserModel userModel);

     //用户名接口
     boolean checkUserName(String userName);

     //查询用户信息
     UserInfoModel getUserInfo(int uuid);

     //修改用户信息
     UserInfoModel updateUserInfo(UserInfoModel userInfoModel);
}
