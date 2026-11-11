package com.backend.eventia.service;

import com.backend.eventia.model.User;

public interface UserService {

    void deactivateAccount(String email, String password);

    User reactivateUser(User user);
}
