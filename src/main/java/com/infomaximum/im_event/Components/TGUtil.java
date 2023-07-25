package com.infomaximum.im_event.Components;

import com.infomaximum.im_event.ImEventApplication;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.EventsService;
import com.infomaximum.im_event.Service.TelegramBotService;
import com.infomaximum.im_event.Service.UsersService;
import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.*;

/**
 * Created by a.kiperku
 * Date: 25.07.2023
 */
public class TGUtil implements CallBackConst{

    private final TelegramBotService telegramBotService;
    private final EventsService eventService;
    private final UsersService usersService;

    public TGUtil() {
        this.telegramBotService = ImEventApplication.getInstance().getBean(TelegramBotService.class);
        this.eventService = ImEventApplication.getInstance().getBean(EventsService.class);
        this.usersService = ImEventApplication.getInstance().getBean(UsersService.class);
    }

    public void seeAllEvents(CallbackQuery callbackQuery){
        long chatId = callbackQuery.getMessage().getChatId();
        final EventsService eventsService = ImEventApplication.getInstance().getBean(EventsService.class);
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(callbackQuery.getMessage().getMessageId());
        final List<Event> allEvents = eventsService.getAllEvents().stream().filter(Event::getIsActive).toList();
        Map<Long, String> eventsFormat = new HashMap<>();
        for (Event event : allEvents) {
            String[] split = event.getStart_date().split(":");
            String date = event.getStart_date();
            if (split.length > 1){
                date = split[0] + ":" + split[1];
            }
            eventsFormat.put(event.getId(), event.getName() + " | " + date);
        }
        if (eventsFormat.isEmpty()){
            message.setText("На данный момент нет мероприятии");
            final TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
            keyBoard.addButton(new TGInlineButton("MENU", MENU, 1));
            message.setReplyMarkup(keyBoard);
            telegramBotService.executeMessage(callbackQuery, message);
            return;
//            sendMessage(chatId, "На данный момент нет мероприятии");
        }
        final TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(allEvents.size());
        int i = 1;
        for (Map.Entry<Long, String> event: eventsFormat.entrySet()){
            keyBoard.addButton(new TGInlineButton(event.getValue(), SEE_EVENT +":" + event.getKey(), i));
            i++;
        }
        message.setText("Мероприятия");
        message.setReplyMarkup(keyBoard);

        telegramBotService.executeMessage(callbackQuery, message);


    }


    public void regToEvent(CallbackQuery callbackQuery){

        long chatId = callbackQuery.getMessage().getChatId();
        final User user = usersService.getUserByTelegramID(chatId);
        final long eventId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        final Event event = eventService.getEventById(eventId);

        final Optional<User> first = event.getParticipants().stream()
                .filter(u -> Objects.equals(u.getTelegramId(), user.getTelegramId()))
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .findFirst();
        if (first.isEmpty()){
            if (eventService.addUserToEvent(user, event)){
                telegramBotService.infoMessage(chatId, String.format("%s был успешно добавлен на мероприятие %s", user.getName(), event.getName()));
                TGEditMessage message = new TGEditMessage(callbackQuery, EventInfo.showEvent(event));
                message.setReplyMarkup(callbackQuery.getMessage().getReplyMarkup());
                telegramBotService.executeMessage(callbackQuery, message);
            }
        }else {
            telegramBotService.infoMessage(chatId, "Вы уже зарегистрированы на данное мероприятие");
        }
    }
    public void unregToEvent(CallbackQuery callbackQuery){
        long chatId = callbackQuery.getMessage().getChatId();
        final User user = usersService.getUserByTelegramID(chatId);
        final long eventId = Long.parseLong(callbackQuery.getData().split(":")[1]);
        final Event event = eventService.getEventById(eventId);

        final Optional<User> first = event.getParticipants().stream()
                .filter(u -> Objects.equals(u.getTelegramId(), user.getTelegramId()))
                .filter(u -> u.getEmail().equals(user.getEmail()))
                .findFirst();
        if (first.isPresent()){
            if (eventService.removeUserToEvent(user, event)){
                telegramBotService.infoMessage(chatId, "Вы успешно отписались от мероприятия " + event.getName());

                TGEditMessage message = new TGEditMessage(callbackQuery, EventInfo.showEvent(event));
                message.setReplyMarkup(callbackQuery.getMessage().getReplyMarkup());
                telegramBotService.executeMessage(callbackQuery, message);
            }
        }else {
            telegramBotService.infoMessage(chatId, "Вы не являетесь участником мероприятия " + event.getName());
        }
    }


}
