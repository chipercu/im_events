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
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 25.07.2023
 */
public class TGUtil {

    private final TelegramBotService telegramBotService;
    private final EventsService eventService;
    private final UsersService usersService;

    public TGUtil() {
        this.telegramBotService = ImEventApplication.getInstance().getBean(TelegramBotService.class);
        this.eventService = ImEventApplication.getInstance().getBean(EventsService.class);
        this.usersService = ImEventApplication.getInstance().getBean(UsersService.class);
    }

    public void regToEvent(CallbackQuery callbackQuery){
        final Message message = callbackQuery.getMessage();
        long chatId = message.getChatId();
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
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(message.getMessageId());
                editMessageText.setText(EventInfo.showEvent(event));
                editMessageText.setReplyMarkup(callbackQuery.getMessage().getReplyMarkup());
                telegramBotService.executeMessage(editMessageText);
            }
        }else {
            telegramBotService.infoMessage(chatId, "Вы уже зарегистрированы на данное мероприятие");
        }
    }
    public void unregToEvent(CallbackQuery callbackQuery){
        final Message message = callbackQuery.getMessage();
        long chatId = message.getChatId();
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
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(message.getMessageId());
                editMessageText.setText(EventInfo.showEvent(event));
                editMessageText.setReplyMarkup(callbackQuery.getMessage().getReplyMarkup());
                telegramBotService.executeMessage(editMessageText);
            }
        }else {
            telegramBotService.infoMessage(chatId, "Вы не являетесь участником мероприятия " + event.getName());
        }
    }


}
