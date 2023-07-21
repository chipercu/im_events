package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@Service
public class UsersService {

    private UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    private Optional<User> getUserFromDB(Long userId){
        return usersRepository.getUserById(userId);
    }
    private Optional<User> getUserFromDB(String email){
        return usersRepository.getUserByEmail(email);
    }

    public String registry(User user){
        if (getUserFromDB(user.getId()).isPresent()){
            return String.format("Пользователь %s уже существует", user.getName());
        }else {
            usersRepository.saveAndFlush(user);
            return String.format("Пользователь %s успешно зарегистрирован", user.getName());
        }
    }

    public User getUserByName(String name){
       return usersRepository.getUserByName(name).get();
    }

}
