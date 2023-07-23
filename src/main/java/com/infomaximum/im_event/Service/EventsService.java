package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Repository.EventsRepository;
import com.infomaximum.im_event.Repository.UsersRepository;
import org.springframework.context.ApplicationContext;
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

    private final UsersRepository usersRepository;
    private final EventsRepository eventsRepository;

    private final ApplicationContext applicationContext;


    public EventsService(UsersRepository usersRepository, EventsRepository eventsRepository, ApplicationContext applicationContext) {
        this.usersRepository = usersRepository;
        this.eventsRepository = eventsRepository;
        this.applicationContext = applicationContext;
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

    public Event addEvent(String name, User initiator, Date start_date, EVENT_TYPE eventType, Boolean isRepeatable, Double coin, String description){
        final Optional<Event> eventByName = eventsRepository.getEventByName(name);
        if (eventByName.isPresent()){
            return eventByName.get();
        }else {
            final Event event = new Event(name, initiator, start_date, eventType, isRepeatable, description);
            if (coin > 0){
                event.addCoins(coin);
            }
            eventsRepository.saveAndFlush(event);
            final List<User> all = usersRepository.findAll();
            for (User user: all){
                if (user.getTelegramId() != null){
                    StringBuilder message = new StringBuilder();
                    String[] split = event.getStart_date().split(":");
                    String date = split[0] + ":" + split[1];
                    message.append("Новое мероприятие:\n")
                            .append(event.getName()).append(" (id ").append(event.getId()).append(")").append("\n")
                            .append("Когда: ").append(date).append("\n\n");

                    final TelegramBotService telegramBotService = applicationContext.getBean(TelegramBotService.class);
                    telegramBotService.sendMessage(user.getTelegramId(), message.toString());
                }
            }
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
        eventsRepository.saveAndFlush(eventByName.get());
        return String.format("%s был успешно добавлен на мероприятие %s", userName, event);
    }

    public String deleteEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || eventByName.get().getInitiator().getName().equals(userByName.get().getName())){
            eventsRepository.delete(eventByName.get());
            return String.format("Событье %s удалена", event);
        }else {
            return String.format("Уважаемый %s! Вы не имеете право удалить данное мероприятие", user);
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

    public String addCoinsToEvent(String user, String event, Double coins) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (!userByName.get().getIsAdmin()){
            return String.format("Уважаемый %s! Вы не имеете право на добавление дублонов", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }

        eventByName.get().setCoins(coins);
        eventsRepository.flush();
        return String.format("Для мероприятия %s было добавлено %f дублонов", event, coins);
    }

    public String finishEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getName().equals(eventByName.get().getInitiator().getName()) || !userByName.get().getIsAdmin()){
            return String.format("Уважаемый %s! Вы не имеете право завершить мероприятие", user);
        }
        eventByName.get().setIsActive(false);
        final Double coins = eventByName.get().getCoins();
        if (coins > 0){
            try {
                final List<User> participants = eventByName.get().getParticipants();
                if (!participants.isEmpty()){
                    final double count = coins / participants.size();
                    participants.forEach(p -> p.setCoins(p.getCoins() + count));
                }
            }finally {
                eventByName.get().setCoins(0.0);
            }

        }
        return String.format("Событье %s завершена", event);
    }

    public String deleteUserFromEvent(String user, String event, String deletingUser) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<User> deletingUserByName = usersRepository.getUserByName(deletingUser);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (deletingUserByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", deletingUserByName);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || userByName.get().getName().equals(deletingUserByName.get().getName())){
            eventByName.get().getParticipants().remove(deletingUserByName.get());
            eventsRepository.saveAndFlush(eventByName.get());
            return String.format("Пользователь %s был удален с мероприятия", deletingUserByName);
        }else {
            return String.format("Уважаемый %s! Вы не имеете право удалять участников мероприятия", user);
        }

    }

    public String restartEvent(String user, String event) {
        final Optional<User> userByName = usersRepository.getUserByName(user);
        final Optional<Event> eventByName = eventsRepository.getEventByName(event);

        if (userByName.isEmpty()){
            return String.format("Пользователь с именем %s не существует", user);
        }
        if (eventByName.isEmpty()){
            return String.format("Событье %s не существует", event);
        }
        if (userByName.get().getIsAdmin() || eventByName.get().getInitiator().getName().equals(userByName.get().getName())){
            eventByName.get().setIsActive(true);
            return String.format("Мероприятие %s была успешно перезапущена", event);
        }else {
            return String.format("Уважаемый %s!Вам не удалось перезапустить мероприятие, возможно у вас нет прав", user);
        }


    }
}
