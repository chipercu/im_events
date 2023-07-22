package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.EventsRepository;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public EventsService(UsersRepository usersRepository, EventsRepository eventsRepository) {
        this.usersRepository = usersRepository;
        this.eventsRepository = eventsRepository;
    }

    private Optional<Event> getEventFromDB(Long id){
        return eventsRepository.getEventById(id);
    }
    private Optional<Event> getEventFromDB(String name){
        return eventsRepository.getEventByName(name);
    }

    public List<Event> getEventsByType(EVENT_TYPE type){
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

    public Event addEvent(String name, User initiator, Date start_date, EVENT_TYPE eventType, Boolean isRepeatable, Integer coin, String description){
        final Optional<Event> eventByName = eventsRepository.getEventByName(name);
        if (eventByName.isPresent()){
            return eventByName.get();
        }else {
            final Event event = new Event(name, initiator, start_date, eventType, isRepeatable, description);
            if (coin > 0){
                event.addCoins(coin);
            }
            eventsRepository.saveAndFlush(event);
            return event;
        }
    }

    public List<User> getEventUsers(String event) {
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);
        if (eventByName.isPresent()){
            return eventByName.get().getParticipants();
        }
        return new ArrayList<User>();
    }

    public String addUserToEvent(String user, String event, String userName) {
        final Optional<User> redactor = usersRepository.getUserByName(user);
        if (redactor.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);
        final Optional<User> userByName = usersRepository.getUserByName(userName);
        if (eventByName.isEmpty()){
            return String.format("Мероприятие с именем %s не существует", event);
        }
        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", userName);
        }
        eventByName.get().addParticipant(userByName.get());
        eventsRepository.flush();
        return String.format("%s был успешно добавлен на мероприятие %s", userName, event);
    }

    public String deleteEvent(String user, String event) {
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }else {
            eventsRepository.delete(eventByName.get());
            return String.format("Событье %s удалена", event);
        }
    }

    public String deleteEvent(String user, Long id) {
        final Optional<Event> eventByName = eventsRepository.getEventById(id);
        if (eventByName.isEmpty()){
            return String.format("Событье с ID %s не существует", id);
        }else {
            eventsRepository.delete(eventByName.get());
            return String.format("Событье с ID %s удалена", id);
        }

    }
}
