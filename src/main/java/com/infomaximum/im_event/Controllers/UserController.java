package com.infomaximum.im_event.Controllers;

import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.EventsService;
import com.infomaximum.im_event.Service.UsersService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by a.kiperku
 * Date: 22.07.2023
 */
@RestController
@RequestMapping("/im_events/user")
public class UserController {

    private final UsersService usersService;
    private final EventsService eventsService;

    public UserController(UsersService usersService, EventsService eventsService) {
        this.usersService = usersService;
        this.eventsService = eventsService;
    }
    @PostMapping("/registry")
    public String registryUser(User user){
       return usersService.registry(user);
    }

    @PostMapping("/loginUser")
    public User loginUser(String email){
        return usersService.loginUser(email);
    }

    @GetMapping("/getUser")
    public User getUserByName(@RequestParam String name){
        return usersService.getUserByName(name);
    }

    @GetMapping("/getAllUsers")
    public List<User> getAllUsers(){
        return usersService.getAllUsers();
    }

    @DeleteMapping("/deleteUserByName")
    public String deleteUserByName(String deletingUser){
        return usersService.deleteUser(deletingUser);
    }





}
