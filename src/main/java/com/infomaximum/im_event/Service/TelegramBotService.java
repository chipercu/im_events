package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Config.TelegramBotConfig;
import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */
@Component
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;
    private final UsersService usersService;

    private final Map<Long, Event> eventCreateMap = new HashMap<>();


    public TelegramBotService(TelegramBotConfig botConfig, EventsService eventsService, UsersService usersService) {
        this.botConfig = botConfig;
        this.eventsService = eventsService;
        this.usersService = usersService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    private boolean checkReg(User user, long chatId) {
        if (user == null) {
            sendMessage(chatId, "До использования бота вы должны\n быть зарегистрированы на IM.EVENTS\n/reg Имя Фамилия почта пароль");
            return false;
        }
        return true;
    }

    @Override
    public void onUpdateReceived(Update update) {

        String currency = "";

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            final String[] commands = messageText.split(" ");
            final User user = usersService.getUserByTelegramID(chatId);

            if (eventCreateMap.containsKey(chatId)){
                if (messageText.equals("/break")){
                    Event event = eventCreateMap.get(chatId);
                    if (event != null){
                        eventCreateMap.remove(chatId);
                        sendMessage(chatId, "Создание мероприятия отменена");
                    }
                }

                Event event = eventCreateMap.get(chatId);
                if (event != null){
                    if (event.getName() == null || event.getName().isEmpty()){
                        event.setName(messageText);
                        sendMessage(chatId, "Имя события: " + messageText);
                        sendMessage(chatId, "Задайте время начала мероприятия\n " +
                                "формат: dd-MM-yyyy HH:mm\n" +
                                "пример: 23-07-2023 18:30");
                    } else if (event.getStart_date() == null || event.getStart_date().isEmpty()) {
                        event.setStart_date(messageText);
                        sendMessage(chatId, "Имя события: " + messageText + "\n" +
                                "Когда: " + event.getStart_date() + "\n");
                        sendMessage(chatId, "Задайте тип мероприятия\n" +
                                        "типы: SPORT,EDUCATION,CULTURE,GAME,CINEMA,CHILL");
                    } else if(event.getEventType() == null){
                        Optional<EVENT_TYPE> type = Arrays.stream(EVENT_TYPE.values())
                                .filter(eventType -> eventType.name().equals(messageText)).findFirst();
                        if (type.isPresent()){
                            event.setEventType(type.get());
                            sendMessage(chatId, "Имя события: " + messageText + "\n" +
                                    "Когда: " + event.getStart_date() + "\n" +
                                    "Тип: " + event.getEventType());
                            sendMessage(chatId, "Задайте описание мероприятия");
                        }else {
                            sendMessage(chatId, "Задан не правильный типа мероприятия\n " +
                                    "типы: SPORT,EDUCATION,CULTURE,GAME,CINEMA,CHILL");
                        }
                    }else if (event.getDescription() == null || event.getDescription().isEmpty()){
                        event.setDescription(messageText);
                        sendMessage(chatId, "Имя события: " + messageText + "\n" +
                                "Когда: " + event.getStart_date() + "\n" +
                                "Тип: " + event.getEventType() + "\n" +
                                "Описание: " + event.getDescription());
                    }
                }
            }



            switch (commands[0]) {


                case "/delete":
                    deleteEvent(chatId, commands, user);
                    break;
                case "/createEvent":
                    if (checkReg(user, chatId)){
                        Event event = new Event(user);
                        if (!eventCreateMap.containsKey(chatId)){
                            eventCreateMap.put(chatId, event);
                            sendMessage(chatId, "Задайте имя мероприятия");
                        }
                    }
                    break;
                case "/all":
                    getAllEvents(chatId, user);
                    break;
                case "/+":
                    addUserToEvent(chatId, commands, user);
                    break;
                case "/event":
                    getEvent(chatId, commands, user);
                    break;
                case "/-":
                    deleteUserFromEvent(chatId, commands, user);
                    break;
                case "/reg":
                    registryUser(chatId, commands);
                    break;
                case "/help":
                    help(chatId, user);
                    break;
            }
        }
    }

    private void getAllEvents(long chatId, User user) {
        if (checkReg(user, chatId)){
            sendAllEvents(chatId);
        }
    }

    private void help(long chatId, User user) {
        if (checkReg(user, chatId)){
            String commandList = """
                command:
                /all (просмотр списка активных мероприятий)
                /event id (просмотр мероприятия)
                /reg (подписка на IM.EVENT)
                /+ id  (запись на мероприятие)
                /- id  (отписка от мероприятия)
                """;
            sendMessage(chatId, commandList);
        }
    }

    private void registryUser(long chatId, String[] commands) {
        try {
            final String name = commands[1];
            final String surname = commands[2];
            final String mail = commands[3];
            final String pass = commands[4];

            if ((mail.split("@")[1].equals("infomaximum.com") || mail.split("@")[1].equals("infomaximum.biz")) && pass.equals("1111")) {
                final User userByTelegramID = usersService.getUserByTelegramID(chatId);
                if (userByTelegramID != null) {
                    sendMessage(chatId, "Вы уже подписаны на IM.EVENTS");
                } else {
                    final User registryUser = new User(name, surname, pass, mail);
                    registryUser.setTelegramId(chatId);
                    usersService.registry(registryUser);
                    sendMessage(chatId, "Вы успешно подписались на IM.EVENTS");
                }
            }
        } catch (Exception e) {
            sendMessage(chatId, "Не правильный формат данных\n/reg Имя Фамилия почта пароль");
        }
    }

    private void addUserToEvent(long chatId, String[] commands, User user) {
        if (checkReg(user, chatId)){
            final long event_id = Long.parseLong(commands[1]);
            final Event eventById = eventsService.getEventById(event_id);
            if (eventById != null){
//                            eventsService.addUserToEvent(user.getName(), eventById.getName(), user.getName());
                sendMessage(chatId, eventsService.addUserToEvent(user.getName(), eventById.getName(), user.getName()));
            }else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }

    private void deleteUserFromEvent(long chatId, String[] commands, User user) {
        if (checkReg(user, chatId)){
            final long event_id = Long.parseLong(commands[1]);
            final Event eventById = eventsService.getEventById(event_id);
            if (eventById != null){
                List<User> participants = eventById.getParticipants();
                for (User u: participants){
                    if (Objects.equals(u.getId(), user.getId())){
                        String s = eventsService.deleteUserFromEvent(user.getName(), eventById.getName(), user.getName());
                        sendMessage(chatId, "Вы успешно отписались от мероприятия " + eventById.getName());
                    }else {
                        sendMessage(chatId, "Вы не являетесь участником мероприятия " + eventById.getName());
                    }
                }
            }else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }

    private void getEvent(long chatId, String[] commands, User user) {
        if (checkReg(user, chatId)){
            final long event_id = Long.parseLong(commands[1]);
            final Event eventById = eventsService.getEventById(event_id);
            if (eventById != null){
                StringBuilder message = new StringBuilder();
                final List<User> participants = eventById.getParticipants();

                String[] split = eventById.getStart_date().split(":");
                String date = split[0] + ":" + split[1];
                message.append(eventById.getName()).append("\n")
                        .append("Когда: ").append(date).append("\n")
                        .append("Организатор: ").append(eventById.getInitiator().getName()).append(" ").append(eventById.getInitiator().getSurname()).append("\n")
                        .append(eventById.getDescription()).append("\n\n")
                        .append("Участники:\n");
                if (participants.size() > 0){
                    for (int i = 0; i < participants.size(); i++) {
                        message.append(i + 1).append(".").append(participants.get(i).getName()).append(" ").append(participants.get(i).getSurname()).append("\n");
                    }
                }
                sendMessage(chatId, message.toString());
            }else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }

    private void deleteEvent(long chatId, String[] commands, User user) {
        if (checkReg(user, chatId)){
            final Long event_id = Long.parseLong(commands[1]);
            final Event eventById = eventsService.getEventById(event_id);
            if (eventById != null){
                final List<User> allUsers = usersService.getAllUsers();
                for (User u: allUsers){
                    sendMessage(u.getTelegramId(), String.format("Событие %s удалено", eventById.getName()));
                }
            }
            eventsService.deleteEvent("Дина", event_id);
        }
    }


    public void sendAllEvents(Long chatId) {
        StringBuilder answer = new StringBuilder();
        final List<Event> allEvents = eventsService.getAllEvents().stream().filter(Event::getIsActive).toList();
        if (allEvents.isEmpty()) {
            answer = new StringBuilder("На данный момент нет мероприятии");
        }
        answer.append("Список мероприятий:\n\n");
        int i = 1;

        for (Event event : allEvents) {
            String[] split = event.getStart_date().split(":");
            String date = event.getStart_date();
            if (split.length > 1){
                date = split[0] + ":" + split[1];
            }
            answer.append(i++ + ". ").append(event.getName()).append(" (id ").append(event.getId()).append(")").append("\n")
                    .append("   Когда: ").append(date).append("\n\n");
        }
        sendMessage(chatId, answer.toString());
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "";
        sendMessage(chatId, answer);
    }


    public void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }
}
