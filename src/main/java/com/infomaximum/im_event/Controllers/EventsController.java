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

//    @GetMapping("/getEventById")
//    public Event getEventById(@RequestParam(required = false) Long id){
//        return eventsService.getEventById(id);
//    }
//
//    @GetMapping("/getEventByName")
//    public Event getEventByName(@RequestParam(required = false) String name){
//        return eventsService.getEventByName(name);
//    }

    @GetMapping("/getAllEvents")
    public List<Event> getAllEvents(){
        return eventsService.getAllEvents();
    }

    @GetMapping("/getEventUsers")
    public List<User> getEventUsers(@RequestParam String event){
        return eventsService.getEventUsers(event);
    }

    @PostMapping("/addUserToEvent")
    public String addUserToEvent(String user, String event, String userName){
//        return eventsService.addUserToEvent(user, event, userName);
        return "премено отключена";
    }

    @PostMapping("/deleteUserFromEvent")
    public String deleteUserFromEvent(String user, String event, String deletingUser){
        return eventsService.deleteUserFromEvent(user, event, deletingUser);
    }

    @PostMapping("/addCoins")
    private String addCoins(String user, String event, Double coins){
        return eventsService.addCoinsToEvent(user, event, coins);
    }

    @DeleteMapping("/deleteEventByName")
    public String deleteEvent(String user, String event){
        return eventsService.deleteEvent(user, event);
    }

    @DeleteMapping("/deleteEventById")
    public String deleteEvent(Long id){
        return eventsService.deleteEvent(id);
    }
    @PostMapping("/restartEvent")
    public String restartEvent(String user, String event){
        return eventsService.restartEvent(user, event);
    }

    @PostMapping("/finishEvent")
    public String finishEvent(String user, String event){
        return eventsService.finishEvent(user, event);
    }

    @PostMapping("/addEvent")
    public Event addEvent(String name,
                          String initiator,
                          String start_date,
                          EVENT_TYPE eventType,
                          Boolean isRepeatable,
                          @RequestParam(required = false) Double coins,
                          String description){
        final User user = usersService.getUserByName(initiator);
        Double c = 0.0;
        if (coins != null){
            c = coins;
        }
        return eventsService.addEvent(name, user, start_date, eventType, isRepeatable, c, description);
    }
}
