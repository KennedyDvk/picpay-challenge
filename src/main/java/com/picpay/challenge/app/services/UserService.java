package com.picpay.challenge.app.services;

import com.picpay.challenge.app.domain.user.User;
import com.picpay.challenge.app.domain.user.UserType;
import com.picpay.challenge.app.dtos.UserDTO;
import com.picpay.challenge.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public void validateTransaction(User sender, BigDecimal amount) throws Exception {

        if(sender.getUserType() == UserType.MERCHANT) {
            throw new Exception("Merchant type users are not authorized to make transactions.");
        }

        if(sender.getBalance().compareTo(amount) < 0) {
            throw new Exception("User does not have sufficient balance.");
        }
    }

    public User findUserById(Long id) throws Exception {
        return this.repository.findUserById(id).orElseThrow(() -> new Exception("User not found."));
    }

    public User createUser(UserDTO data) {
        User newUser = new User(data);
        this.saveUser(newUser);
        return  newUser;
    }

    public List<User> getAllUsers() {
        return this.repository.findAll();
    }

    public void saveUser(User user) {
        this.repository.save(user);
    }

}
