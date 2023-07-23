package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Config.TelegramBotConfig;
import com.infomaximum.im_event.Model.Event;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */
@Component
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;

    public TelegramBotService(TelegramBotConfig botConfig, EventsService eventsService) {
        this.botConfig = botConfig;
        this.eventsService = eventsService;
    }

    @Override
    public String getBotUsername() {
       return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }



    @Override
    public void onUpdateReceived(Update update) {

        String currency = "";

        if(update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText){
                case "/delete":
                    final Long event_id = Long.parseLong(messageText.split(" ")[1]);
                    sendMessage(chatId, eventsService.deleteEvent("Дина", event_id));
                case "/getEvents":
                    sendAllEvents(chatId);
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

    private void sendAllEvents(Long chatId){
        StringBuilder answer = new StringBuilder();
        final List<Event> allEvents = eventsService.getAllEvents().stream().filter(Event::getIsActive).toList();
        if (allEvents.isEmpty()){
            answer = new StringBuilder("На данный момент нет мероприятии");
        }
        answer.append("Список мероприятии:");
        for (Event event: allEvents) {
            answer.append(" -").append(event.getName()).append("\n");
        }
        sendMessage(chatId, answer.toString());
    }

    private void startCommandReceived(Long chatId, String name) {
        String answer = "";
        sendMessage(chatId, answer);
    }


    private void sendMessage(Long chatId, String textToSend){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }
}
