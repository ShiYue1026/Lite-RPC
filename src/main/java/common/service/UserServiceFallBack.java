package common.service;

import common.pojo.User;

public class UserServiceFallBack implements UserService {

    @Override
    public User getUserByUserId(Integer id) {
        System.out.println(id);
        System.out.println("getUserByUserId的fallback方法执行...");
        return null;
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println(user.getId());
        System.out.println("insertUserId的fallback方法执行...");
        return null;
    }
}
