package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.EventType;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.EventTypeRepository;
import com.infomaximum.im_event.Repository.EventsRepository;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@Service
public class EventsService {

    private UsersRepository usersRepository;
    private EventsRepository eventsRepository;
    private EventTypeRepository eventTypeRepository;

    public EventsService(UsersRepository usersRepository, EventsRepository eventsRepository, EventTypeRepository eventType) {
        this.usersRepository = usersRepository;
        this.eventsRepository = eventsRepository;
        this.eventTypeRepository = eventType;
    }

    private Optional<Event> getEventFromDB(Long id){
        return eventsRepository.getEventById(id);
    }
    private Optional<Event> getEventFromDB(String name){
        return eventsRepository.getEventByName(name);
    }

    public List<Event> getEventsByType(EventType type){
        return eventsRepository.getEventsByEventType(type);
    }
    public List<Event> getAllEvents(){
        return eventsRepository.findAll();
    }
    public Event getEventById(Long id){
        return getEventFromDB(id).get();
    }
    public Event getEventByName(String name){
        return getEventFromDB(name).get();
    }

    public EventType getEventType(EVENT_TYPE event_type){
        return eventTypeRepository.getEventTypeByEventType(event_type);
    }

    public Event addEvent(String name, User initiator, Date start_date, EventType eventType){
        final Optional<Event> eventByName = eventsRepository.getEventByName(name);
        if (eventByName.isPresent()){
            return eventByName.get();
        }else {
            final Event event = new Event(name, initiator, start_date, eventType);
            eventsRepository.saveAndFlush(event);
            return event;
        }
    }




}
