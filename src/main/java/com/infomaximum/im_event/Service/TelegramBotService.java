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
import java.util.Comparator;
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
            final String[] text = messageText.split(" ");
            switch (text[0]){
                case "/delete":
                    final Long event_id = Long.parseLong(text[1]);
                    sendMessage(chatId, eventsService.deleteEvent("Дина", event_id));
                    break;
                case "/events":
                    sendAllEvents(chatId);
                    break;
                case "/event":
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
        final List<Event> allEvents = eventsService.getAllEvents().stream().filter(Event::getIsActive).sorted((o1, o2) -> {
            final Date date1 = new Date(o1.getStart_date());
            final Date date2 = new Date(o1.getStart_date());
            return (int) (date1.getTime() - date2.getTime());
        }).toList();
        if (allEvents.isEmpty()){
            answer = new StringBuilder("На данный момент нет мероприятии");
        }
        answer.append("Список мероприятии:\n");
        for (Event event: allEvents) {
            answer.append("1.").append(event.getName()).append("\n")
                    .append("   date: ").append(event.getStart_date());
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
