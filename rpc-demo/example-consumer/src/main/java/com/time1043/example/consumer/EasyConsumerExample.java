package com.time1043.example.consumer;

import com.time1043.example.common.model.User;
import com.time1043.example.common.service.UserService;

public class EasyConsumerExample {
    public static void main(String[] args) {
        // TODO 需要获取 UserService 的实现类对象
        UserService userService = null;
        User user = new User();
        user.setName("oswin");

        // 调用
        User newUser = userService.getUser(user);
        if (newUser!= null){
            System.out.println(newUser.getName());
        } else {
            System.out.println("user == null");
        }
    }
}
