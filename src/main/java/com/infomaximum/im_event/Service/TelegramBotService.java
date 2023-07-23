package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Config.TelegramBotConfig;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */
@Component
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;
    private final UsersService usersService;

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
            sendMessage(chatId, "До использования бота вы должны\n быть зарегистрированы на IM.EVENTS\n/reg имя фамилия почта пароль");
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
            final String[] text = messageText.split(" ");
            final User user = usersService.getUserByTelegramID(chatId);


            switch (text[0]) {
                case "/delete":
                    if (checkReg(user, chatId)){
                        final Long event_id = Long.parseLong(text[1]);
                        sendMessage(chatId, eventsService.deleteEvent("Дина", event_id));
                    }
                    break;
                case "/events":
                    if (checkReg(user,chatId)){
                        sendAllEvents(chatId);
                    }
                    break;
                case "/+":
                    if (checkReg(user, chatId)){
                        final long event_id = Long.parseLong(text[1]);
                        final Event eventById = eventsService.getEventById(event_id);
                        if (eventById != null){
                            eventsService.addUserToEvent(user.getName(), eventById.getName(), user.getName());
                            sendMessage(chatId, "Вы успешно записались на мероприятие " + eventById.getName());
                        }else {
                            sendMessage(chatId, "Проверьте id мероприятия\n /event");
                        }
                    }
                    break;
                case "/-":
                    if (checkReg(user, chatId)){
                        final long event_id = Long.parseLong(text[1]);
                        final Event eventById = eventsService.getEventById(event_id);
                        if (eventById != null){
                            eventsService.deleteUserFromEvent(user.getName(), eventById.getName(), user.getName());
                            sendMessage(chatId, "Вы успешно отписались от мероприятия " + eventById.getName());
                        }else {
                            sendMessage(chatId, "Проверьте id мероприятия\n /event");
                        }
                    }
                    break;
                case "/reg":

                    try {
                        final String name = text[1];
                        final String surname = text[2];
                        final String mail = text[3];
                        final String pass = text[4];

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
                        sendMessage(chatId, "Не правильный формат данных\n/reg имя фамилия почта пароль");
                    }

                    break;
                case "/help":
                    if (checkReg(user,chatId)){
                        String commandList = """
                            command:
                            /events (просмотр списка активных мероприятий)
                            /reg (подписка на IM.EVENT)
                            /+ id  (запись на мероприятие)
                            /- id  (отписка от мероприятия)
                            """;
                        sendMessage(chatId, commandList);
                    }
                    break;
                default:
                    try {
                        sendMessage(chatId, " hello");
                    } catch (ParseException e) {
                        throw new RuntimeException("Unable to parse date");
                    }
                    sendMessage(chatId, currency);
            }
        }
    }


    public void sendAllEvents(Long chatId) {
        StringBuilder answer = new StringBuilder();
        final List<Event> allEvents = eventsService.getAllEvents().stream().filter(Event::getIsActive).sorted((o1, o2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
            try {
                final Date date1 = format.parse(o1.getStart_date());
                final Date date2 = format.parse(o2.getStart_date());
                return (int) (date2.getTime() - date1.getTime());
            } catch (java.text.ParseException e) {
                throw new RuntimeException(e);
            }
        }).toList();
        if (allEvents.isEmpty()) {
            answer = new StringBuilder("На данный момент нет мероприятии");
        }
        answer.append("Список мероприятий:\n\n");
        int i = 1;

        for (Event event : allEvents) {
            String[] split = event.getStart_date().split(":");
            String date = split[0] + ":" + split[1];
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
