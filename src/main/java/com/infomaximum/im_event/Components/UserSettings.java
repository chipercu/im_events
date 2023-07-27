package com.infomaximum.im_event.Components;

import com.infomaximum.im_event.ImEventApplication;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.EventsService;
import com.infomaximum.im_event.Service.TelegramBotService;
import com.infomaximum.im_event.Service.UsersService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

public class UserSettings implements CallBackConst{

    public static void showUserSettings(CallbackQuery callbackQuery){
        final TGEditMessage message = new TGEditMessage(callbackQuery, "Настройки");
        message.setReplyMarkup(userSettingsButtons());
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
    public static void deleteAccount(CallbackQuery callbackQuery){
        final TGEditMessage message = new TGEditMessage(callbackQuery, "Вы уверены что хотите удалить аккаунт?");
        message.setReplyMarkup(conformDeleteUser());
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
    public static TGInlineKeyBoard userSettingsButtons(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(2);
        keyBoard.addButton(new TGInlineButton("Редактировать", REDACT_USER, 1));
        keyBoard.addButton(new TGInlineButton("Удалить свой профиль", DELETE_USER, 1));
        keyBoard.addButton(new TGInlineButton("Пока не знаю", MAIL, 2));
        keyBoard.addButton(new TGInlineButton("MENU", MENU, 2));
        return keyBoard;
    }
    public static TGInlineKeyBoard conformDeleteUser(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Подтвердить", CONFIRM_DELETE_USER, 1));
        keyBoard.addButton(new TGInlineButton("Отмена", USER_SETTINGS, 1));
        return keyBoard;
    }

    public static void deleteUser(CallbackQuery callbackQuery){
        final User user = ImEventApplication.getInstance().getBean(UsersService.class).getUserByTelegramID(callbackQuery.getMessage().getChatId());
        final List<Event> allEvents = ImEventApplication.getInstance().getBean(EventsService.class).getAllEvents();
        for (Event event: allEvents){
            event.removeParticipant(user);
            ImEventApplication.getInstance().getBean(EventsService.class).removeUserToEvent(user, event);
            final Optional<User> admin = ImEventApplication.getInstance().getBean(UsersService.class).getAllUsers().stream()
                    .filter(User::getIsAdmin)
                    .filter(u -> !u.getTelegramId().equals(user.getTelegramId()))
                    .findFirst();
            if (event.getInitiator().getTelegramId().equals(user.getTelegramId())){
                if (admin.isPresent()){
                    final boolean b = ImEventApplication.getInstance().getBean(EventsService.class).changeInitiator(event.getId(), admin.get());
                    System.out.println(b);
                }
            }
        }
        ImEventApplication.getInstance().getBean(UsersService.class).deleteUser(callbackQuery.getMessage().getChatId());
        TGMenu.showMenu(callbackQuery);


    }





}
