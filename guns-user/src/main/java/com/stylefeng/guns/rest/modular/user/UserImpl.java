package com.stylefeng.guns.rest.modular.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.api.user.UserInfoModel;
import com.stylefeng.guns.api.user.UserModel;
import com.stylefeng.guns.core.util.MD5Util;
import com.stylefeng.guns.rest.common.persistence.dao.MoocUserTMapper;
import com.stylefeng.guns.rest.common.persistence.model.MoocUserT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
@Service(interfaceClass = UserAPI.class)
public class UserImpl implements UserAPI {

    @Autowired
    private MoocUserTMapper moocUserTMapper;


    /**
    * 获取注册信息
    * */
    @Override
    public boolean register(UserModel userModel) {
        //将注册信息转化为数据实体
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUserName(userModel.getUsername());
        //moocUserT.setUserPwd(userModel.getPassword());
        moocUserT.setEmail(userModel.getEmail());
        moocUserT.setAddress(userModel.getAddress());
        moocUserT.setUserPhone(userModel.getPhone());


        //md5混淆加密 + 盐值   --->shiro
        String md5pwd = MD5Util.encrypt(userModel.getPassword());
        moocUserT.setUserPwd(md5pwd);
        //
        Integer flag = moocUserTMapper.insert(moocUserT);
        if(flag>0){
            return true;

        }else{
            return false;
        }
     }

    @Override
    public int login(String username, String password) {
        // 根据登陆账号获取数据库信息
        MoocUserT moocUserT = new MoocUserT();
        System.out.println("username = " +username);
        moocUserT.setUserName(username);

        MoocUserT result = moocUserTMapper.selectOne(moocUserT);

        // 获取到的结果，然后与加密以后的密码做匹配
        if(result!=null && result.getUuid()>0){
            String md5Password = MD5Util.encrypt(password);
            if(result.getUserPwd().equals(md5Password)){
                return result.getUuid();
            }
        }
        return 0;
    }



    @Override
    public boolean checkUserName(String userName) {
        EntityWrapper<MoocUserT> entityWrapper = new EntityWrapper <>();
        entityWrapper.eq("user_name",userName);
        Integer result = moocUserTMapper.selectCount(entityWrapper);
        if(result!=null&&result>0){
                return false;
        }else{
            return  true;
        }

    }
    private UserInfoModel do2UserInfo(MoocUserT moocUserT){
        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setHeadAddress(moocUserT.getHeadUrl());
        userInfoModel.setPhone(moocUserT.getUserPhone());
        userInfoModel.setUpdateTime(moocUserT.getUpdateTime().getTime());
        userInfoModel.setEmail(moocUserT.getEmail());
        userInfoModel.setUsername(moocUserT.getUserName());
        userInfoModel.setNickname(moocUserT.getNickName());
        userInfoModel.setLifState(""+moocUserT.getLifeState());
        userInfoModel.setBirthday(moocUserT.getBirthday());
        userInfoModel.setAddress(moocUserT.getAddress());
        userInfoModel.setSex(moocUserT.getUserSex());
        userInfoModel.setBeginTime(moocUserT.getBeginTime().getTime());
        userInfoModel.setBiography(moocUserT.getBiography());
        return  userInfoModel;
    }
    @Override
    public UserInfoModel getUserInfo(int uuid) {
        MoocUserT moocUserT = moocUserTMapper.selectById(uuid);
        if(moocUserT!=null){
          return do2UserInfo(moocUserT);
        }else{
            return new UserInfoModel();
        }
    }
    private Date time2Date(long time){
        Date date = new Date(time);
        return date;
    }

    @Override
    public UserInfoModel updateUserInfo(UserInfoModel userInfoModel) {

        // 将传入的参数转换为DO 【MoocUserT】
        MoocUserT moocUserT = new MoocUserT();
        moocUserT.setUuid(userInfoModel.getUuid());
        moocUserT.setNickName(userInfoModel.getNickname());

        moocUserT.setLifeState(userInfoModel.getLifState()==null?null:Integer.parseInt(userInfoModel.getLifState()));
        moocUserT.setBirthday(userInfoModel.getBirthday());
        moocUserT.setBiography(userInfoModel.getBiography());
        moocUserT.setBeginTime(null);
        moocUserT.setHeadUrl(userInfoModel.getHeadAddress());
        moocUserT.setEmail(userInfoModel.getEmail());
        moocUserT.setAddress(userInfoModel.getAddress());
        moocUserT.setUserPhone(userInfoModel.getPhone());
        moocUserT.setUserSex(userInfoModel.getSex());
        moocUserT.setUpdateTime(null);

        // DO存入数据库
        Integer integer = moocUserTMapper.updateById(moocUserT);
        if(integer>0){
            // 将数据从数据库中读取出来
            UserInfoModel userInfo = getUserInfo(moocUserT.getUuid());
            // 将结果返回给前端
            return userInfo;
        }else{
            return null;
        }
     }
}
