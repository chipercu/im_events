package com.infomaximum.im_event.Repository;

import com.infomaximum.im_event.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Repository
public interface UsersRepository extends JpaRepository<User, Long> {

    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByName(String name);
    User getUserByTelegramId(long chatId);
}
