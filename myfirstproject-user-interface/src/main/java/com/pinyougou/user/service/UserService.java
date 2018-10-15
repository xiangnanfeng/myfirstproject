package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;
import entity.PageResult;

import java.util.List;

public interface UserService {

    public List<TbUser> findAll();

    public PageResult findPage(int pageNum, int pageSize);

    public void add(TbUser user);

    public void update(TbUser user);

    public TbUser findOne(Long id);

    public void delete(Long[] ids);

    public PageResult findPage(TbUser user, int pageNum, int pageSize);

    /**
     * 发送短信验证码
     * @param phone
     */
    void sendCode(String phone);

    /**
     * 比较验证码
     * @param phone
     * @param code
     * @return
     */
    public boolean checkCode(String phone,String code);
}
