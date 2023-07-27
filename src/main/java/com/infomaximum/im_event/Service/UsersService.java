package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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
        if (getUserFromDB(user.getName()).isPresent()){
            return String.format("Пользователь %s уже существует", user.getName());
        } else if (getUserFromDB(user.getEmail()).isPresent()) {
            return String.format("Пользователь %s уже существует", user.getName());
        } else {
            usersRepository.saveAndFlush(user);
            return String.format("Пользователь %s успешно зарегистрирован", user.getName());
        }
    }

    public boolean registryFromTG(User user){
        usersRepository.saveAndFlush(user);
        return usersRepository.getUserByTelegramId(user.getTelegramId()) != null;
    }


    public User getUserByName(String name){
       return usersRepository.getUserByName(name).get();
    }

    public List<User> getAllUsers() {
        return usersRepository.findAll();
    }

    public String deleteUser(String deletingUser) {
        final Optional<User> deletingUserByName = usersRepository.getUserByName(deletingUser);
        if (deletingUserByName.isEmpty()){
            return String.format("Пользователь %s не существует", deletingUser);
        }else {
            usersRepository.delete(deletingUserByName.get());
            return String.format("Пользователь %s удален", deletingUser);
        }
    }
    public void deleteUser(long telegramId){
        final User userByTelegramId = usersRepository.getUserByTelegramId(telegramId);
        if (userByTelegramId != null){
            usersRepository.delete(userByTelegramId);
        }
    }


    public User loginUser(String email) {
        final Optional<User> userByEmail = usersRepository.getUserByEmail(email);
        return userByEmail.orElseThrow();
    }

    public User getUserByTelegramID(long chatId) {
        return usersRepository.getUserByTelegramId(chatId);
    }
}
