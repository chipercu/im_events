package com.infomaximum.im_event.Components;

import com.infomaximum.im_event.ImEventApplication;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Service.TelegramBotService;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Created by a.kiperku
 * Date: 26.07.2023
 */

public class TGEvent implements CallBackConst{


    public static void createEvent(CallbackQuery callbackQuery){
        final TGEditMessage message = new TGEditMessage(callbackQuery, "Введите название мероприятия");
        message.setReplyMarkup(cancelButton());
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
    public static void cancelCreateEvent(CallbackQuery callbackQuery){
        TGMenu.showMenu(callbackQuery);
    }

    public static TGInlineKeyBoard cancelButton(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Отмена создания", CANCEL_CREATE_EVENT, 1));
        return keyBoard;
    }


    public static void setEventStartTime(Event event, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя события: " + event.getName() + "\n\n" +
                        "Задайте время начала мероприятия\n " +
                        "формат: dd-MM-yyyy HH:mm\n" +
                        "пример: 23-07-2023 18:30");
        message.setReplyMarkup(cancelButton());
        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }

    public static void setEventType(Event event, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя события: " + event.getName() + "\n" +
                        "Когда: " + event.getStart_date() + "\n" +
                        "Выберите тип мероприятия");
        message.setReplyMarkup(eventTypeButtons());
        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }

    public static TGInlineKeyBoard eventTypeButtons(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(3);
        keyBoard.addButton(new TGInlineButton("SPORT", EVENT_TYPES + SPORT, 1));
        keyBoard.addButton(new TGInlineButton("EDUCATION", EVENT_TYPES +  EDUCATION, 1));
        keyBoard.addButton(new TGInlineButton("CULTURE", EVENT_TYPES +  CULTURE, 1));
        keyBoard.addButton(new TGInlineButton("GAME", EVENT_TYPES +  GAME, 2));
        keyBoard.addButton(new TGInlineButton("CINEMA", EVENT_TYPES +  CINEMA, 2));
        keyBoard.addButton(new TGInlineButton("CHILL", EVENT_TYPES +  CHILL, 2));
        keyBoard.addButton(new TGInlineButton("Отмена создания", CANCEL_CREATE_EVENT, 3));
        return keyBoard;
    }

    public static void setDescription(CallbackQuery callbackQuery, Event event) {
        final TGEditMessage message = new TGEditMessage(callbackQuery,
                "Имя события: " + event.getName() + "\n" +
                        "Когда: " + event.getStart_date() + "\n" +
                        "Тип: " + event.getEventType() + "\n" +
                        "Задайте описание мероприятия");
        message.setReplyMarkup(cancelButton());
        try {
            ImEventApplication.getInstance().getBean(TelegramBotService.class).execute(message);
        } catch (TelegramApiException ignored) {

        }
    }

    public static void confirmCreateEvent(Event event, Message updateMessage, Integer messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(updateMessage.getChatId());
        message.setMessageId(messageId);
        message.setText("Имя события: " + event.getName() + "\n" +
                "Когда: " + event.getStart_date() + "\n" +
                "Тип: " + event.getEventType() + "\n" +
                "Описание: " + event.getDescription());
        message.setReplyMarkup(confirmOrCancel());
        ImEventApplication.getInstance().getBean(TelegramBotService.class).executeMessage(message);
    }

    public static TGInlineKeyBoard confirmOrCancel(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Создать", CONFIRM_CREATE_EVENT, 1));
        keyBoard.addButton(new TGInlineButton("Отмена создания", CANCEL_CREATE_EVENT, 1));
        return keyBoard;
    }

    public static void confirmDeleteEvent(CallbackQuery callbackQuery) {
        TGEditMessage message = new TGEditMessage(callbackQuery, "Вы уверены что хотите удалить данное событье!");





    }

    public static TGInlineKeyBoard confirmDeleteEventButtons(){
        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
        keyBoard.addButton(new TGInlineButton("Подтвердить", CONFIRM_DELETE_EVENT, 1));
        keyBoard.addButton(new TGInlineButton("Отмена", MENU, 1));
        return keyBoard;
    }
}
