package com.infomaximum.im_event.Controllers;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.EventsService;
import com.infomaximum.im_event.Service.UsersService;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@RestController
@RequestMapping("/im_events")
public class EventsController {

    private final EventsService eventsService;
    private final UsersService usersService;

    public EventsController(EventsService eventsService, UsersService usersService) {
        this.eventsService = eventsService;
        this.usersService = usersService;
    }

    @GetMapping("/getEventById")
    public Event getEventById(@RequestParam(required = false) Long id){
        return eventsService.getEventById(id);
    }

    @GetMapping("/getEvent")
    public Event getEventById(@RequestParam(required = false) String name){
        return eventsService.getEventByName(name);
    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents(){
        return eventsService.getAllEvents();
    }
    @GetMapping("/getEventUsers")
    public List<User> getEventUsers(@RequestParam String event){
        return eventsService.getEventUsers(event);
    }
    @PostMapping("/addUserToEvent")
    public String addUserToEvent(String event, String userName){
        return eventsService.addUserToEvent(event, userName);
    }


    @PostMapping("/addEvent")
    public Event addEvent(String name, String initiator, EVENT_TYPE eventType, Boolean isRepeatable, @RequestParam(required = false) Integer coins){
        final User user = usersService.getUserByName(initiator);
        Integer c = 0;
        if (coins != null){
            c = coins;
        }
        return eventsService.addEvent(name, user, new Date(), eventType, isRepeatable, c);
    }










}
