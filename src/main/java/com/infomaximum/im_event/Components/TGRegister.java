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



    public static void initRegistry(CallbackQuery callbackQuery){
        final TGEditMessage message = new TGEditMessage(callbackQuery, "Введите свое Имя");
        message.setReplyMarkup(cancelButton());
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
    public static void cancelRegistry(CallbackQuery callbackQuery){
        ImEventApplication.getInstance().getBean(TelegramBotService.class).checkReg(callbackQuery, callbackQuery.getMessage().getChatId());
    }

    public static void setName(User newUser, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя : " + newUser.getName() + "\n\n" + "Введите свою Фамилию");
        message.setReplyMarkup(cancelButton());

        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }
    public static void setSurname(User newUser, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя : " + newUser.getName() +
                "\n" + "Фамилия: " + newUser.getSurname() +
                "\n\n" + "Введите свою Почту Infomaximum");
        message.setReplyMarkup(cancelButton());

        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }
    public static void setEmail(User newUser, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя : " + newUser.getName() +
                "\n" + "Фамилия: " + newUser.getSurname() +
                "\n" + "Почта: " + newUser.getEmail() +
                "\n\n" + "Проверьте данные и подтвердите регистрацию, \nна почту придет код подтверждения регистрации");
        message.setReplyMarkup(confirmOrCancel());

        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }
    public static void enterConfirmPassword(CallbackQuery callbackQuery){
        SendMessage message = new SendMessage();
        message.setChatId(callbackQuery.getMessage().getChatId());
        message.setText("Введите код подтверждения полученный на почту");
        message.setReplyMarkup(cancelButton());
        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }
    public static TGInlineKeyBoard cancelButton(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Отмена регистрации", CANCEL_REGISTRY, 1));
        return keyBoard;
    }
    public static TGInlineKeyBoard confirmOrCancel(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Подтвердить почту", CONFIRM_REGISTRY, 1));
        keyBoard.addButton(new TGInlineButton("Отмена регистрации", CANCEL_REGISTRY, 1));
        return keyBoard;
    }

}
