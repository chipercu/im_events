package com.infomaximum.im_event.Controllers;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.EventType;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.EventsService;
import com.infomaximum.im_event.Service.UsersService;
import jdk.jfr.EventSettings;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@RestController
@RequestMapping("/im_events")
public class EventsController {

    private EventsService eventsService;
    private UsersService usersService;


    @GetMapping("/event")
    public Event getEventById(@RequestParam(required = false) Long id){
        return eventsService.getEventById(id);
    }

    @PostMapping("/addEvent")
    public Event addEvent(String name, String initiator, EVENT_TYPE event_type ){
        final User user = usersService.getUserByName(initiator);
        final EventType eventType = eventsService.getEventType(event_type);

        return eventsService.addEvent(name, user, new Date(), eventType);
    }







}
