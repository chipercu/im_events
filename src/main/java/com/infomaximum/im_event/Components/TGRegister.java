package com.infomaximum.im_event.Components;

import com.infomaximum.im_event.ImEventApplication;
import com.infomaximum.im_event.Model.User;
import com.infomaximum.im_event.Service.TelegramBotService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TGRegister implements CallBackConst{



    public static void showMenu(CallbackQuery callbackQuery){
        EditMessageText message = new EditMessageText();
        message.setChatId(callbackQuery.getMessage().getChatId());
        message.setMessageId(callbackQuery.getMessage().getMessageId());
        message.setText("Введите свое Имя");
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(2);
        keyBoard.addButton(new TGInlineButton("Посмотреть все", SHOW_ALL_EVENT, 1));
        keyBoard.addButton(new TGInlineButton("По категориям", CATEGORY_EVENT, 1));
        keyBoard.addButton(new TGInlineButton("Создать событье", CREATE_EVENT, 2));
        keyBoard.addButton(new TGInlineButton("Настройки", USER_SETTINGS, 2));
        message.setReplyMarkup(keyBoard);
        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(callbackQuery, message);
    }

    public static void initRegistry(CallbackQuery callbackQuery){
        final TGEditMessage message = new TGEditMessage(callbackQuery, "Введите свое Имя");
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Отмена регистрации", CANCEL_REGISTRY, 1));
        message.setReplyMarkup(keyBoard);
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    public static void cancelRegistry(CallbackQuery callbackQuery){
        ImEventApplication.getInstance().getBean(TelegramBotService.class).checkReg(callbackQuery, callbackQuery.getMessage().getChatId());
    }

    public static void setName(User newUser, Message updateMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(updateMessage.getChatId());
        message.setText("Имя : " + newUser.getName() + "\n\n" + "Введите свою Фамилию");
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Отмена регистрации", CANCEL_REGISTRY, 1));
        message.setReplyMarkup(keyBoard);
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
}
