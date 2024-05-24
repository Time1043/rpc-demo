package com.time1043.example.provider;

import com.time1043.example.common.model.User;
import com.time1043.example.common.service.UserService;

public class UserServiceImpl implements UserService {
    @Override
    public User getUser(User user) {
        System.out.println("username: " + user.getName());
        return user;
    }
}
