package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Components.*;
import com.infomaximum.im_event.Config.TelegramBotConfig;
import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */
@Component
public class TelegramBotService extends TelegramLongPollingBot implements CallBackConst {

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;
    private final UsersService usersService;
    private static final int defaultInfoMessageTime = 2000;



    private final Map<Long, Event> eventCreateMap = new HashMap<>();
    private final Map<Long, User> userCreateMap = new HashMap<>();


    public TelegramBotService(TelegramBotConfig botConfig, EventsService eventsService, UsersService usersService) {
        this.botConfig = botConfig;
        this.eventsService = eventsService;
        this.usersService = usersService;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/help", "Помощь по навигации и настройке бота"));
        try {
            this.execute(new SetMyCommands(listofCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }


    public boolean checkReg(CallbackQuery callbackQuery, long... chatId) {
        if (callbackQuery == null){
            SendMessage message = new SendMessage();
            message.setChatId(chatId[0]);
            message.setText("Для использования бота вы должны\n быть зарегистрированы на IM.EVENTS");
            TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
            keyBoard.addButton(new TGInlineButton("Регистрация", REGISTER, 1));
            message.setReplyMarkup(keyBoard);
            executeMessage(message);
            return false;
        }

        final User user = usersService.getUserByTelegramID(callbackQuery.getMessage().getChatId());
        if (user == null) {
            TGEditMessage message = new TGEditMessage(callbackQuery, "Для использования бота вы должны\n быть зарегистрированы на IM.EVENTS");
            TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
            keyBoard.addButton(new TGInlineButton("Регистрация", REGISTER, 1));
            message.setReplyMarkup(keyBoard);
            executeMessage(message);
//            sendMessage(chatId, "До использования бота вы должны\n быть зарегистрированы на IM.EVENTS\n/reg Имя Фамилия почта пароль");
            return false;
        }
        return true;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()){
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            final User user = usersService.getUserByTelegramID(chatId);
            CallbackQuery callbackQuery = update.getCallbackQuery();
            final String data = callbackQuery.getData();
            final long eventId = Long.parseLong(data.split(":")[1]);
            final TGUtil tgUtil = new TGUtil();
            if (data.startsWith(SEE_EVENT)){
                getEvent(callbackQuery, eventsService.getEventById(eventId), chatId);
            }else if (data.startsWith(MENU)){
                TGMenu.showMenu(callbackQuery);
            } else if (data.startsWith(REGISTER)) {
                userCreateMap.put(callbackQuery.getMessage().getChatId(), new User());
                TGRegister.initRegistry(callbackQuery);
            } else if (data.startsWith(CANCEL_REGISTRY)) {
                TGRegister.cancelRegistry(callbackQuery);
            } else if (data.startsWith(SHOW_ALL_EVENT)) {
                tgUtil.seeAllEvents(callbackQuery);
            } else if (data.startsWith(REG_TO_EVENT)) {
                tgUtil.regToEvent(callbackQuery);
            } else if (data.startsWith(UNREG_TO_EVENT)) {
                tgUtil.unregToEvent(callbackQuery);

            } else if (data.startsWith(SAVE_EVENT)) {
                final Event event = eventCreateMap.get(eventId);
                eventsService.addEvent(
                        event.getName(),
                        event.getInitiator(),
                        event.getStart_date(),
                        event.getEventType(),
                        event.getIsRepeatable(),
                        event.getCoins(),
                        event.getDescription()
                );

            } else if (data.startsWith(CANCEL_CREATE_EVENT)) {
                Event event = eventCreateMap.get(chatId);
                if (event != null){
                    eventCreateMap.remove(chatId);
                    sendMessage(chatId, "Создание мероприятия отменена");
                }
            }
        }


        if (update.hasMessage() && update.getMessage().hasText()) {

            final Message updateMessage = update.getMessage();
            long chatId = update.getMessage().getChatId();
            switch (updateMessage.getText()) {
                case "/help":
                case "/start":
                    if (checkReg(null, chatId)){
                        TGMenu.showMenu(chatId);
                    }
                    break;
            }

            if (eventCreateMap.containsKey(chatId)){
                String messageText = update.getMessage().getText();
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
//                        sendMessage(chatId, "Имя события: " + messageText + "\n" +
//                                "Когда: " + event.getStart_date() + "\n" +
//                                "Тип: " + event.getEventType() + "\n" +
//                                "Описание: " + event.getDescription());
                        TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
                        keyBoard.addButton(new TGInlineButton("Подтвердить", SAVE_EVENT + ":" + chatId, 1));
                        keyBoard.addButton(new TGInlineButton("Отмена", CANCEL_CREATE_EVENT, 1));
                        SendMessage message = new SendMessage();
                        message.setChatId(chatId);
                        message.setText("Имя события: " + messageText + "\n" +
                                "Когда: " + event.getStart_date() + "\n" +
                                "Тип: " + event.getEventType() + "\n" +
                                "Описание: " + event.getDescription());
                        message.setReplyMarkup(keyBoard);
//                        executeMessage(message);

                    }
                }
            }

            if (userCreateMap.containsKey(chatId)){
                final User newUser = userCreateMap.get(chatId);
                if (newUser.getName() == null || newUser.getName().isEmpty()){
                    newUser.setName(updateMessage.getText());
                    TGRegister.setName(newUser, updateMessage);
                }
            }


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
    public void infoMessage(long chatId, String text){
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text);
            Message sentOutMessage = execute(message);
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                try {
                    DeleteMessage deleteMessage = new DeleteMessage();
                    deleteMessage.setChatId(sentOutMessage.getChatId());
                    deleteMessage.setMessageId(sentOutMessage.getMessageId());
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }, defaultInfoMessageTime, TimeUnit.MILLISECONDS);

        }catch (TelegramApiException ignored){

        }
    }

    private void getEvent(CallbackQuery callbackQuery, Event event, long chatId) {
        final User user = usersService.getUserByTelegramID(chatId);
        if (checkReg(callbackQuery)){
            if (event != null){
                TGEditMessage message = new TGEditMessage(callbackQuery, EventInfo.showEvent(event));

                TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(2);
                keyBoard.addButtons(new TGInlineButton("Записаться", REG_TO_EVENT  +":"+ event.getId(), 1));
                keyBoard.addButtons(new TGInlineButton("Отписаться", UNREG_TO_EVENT +":" + event.getId(), 1));

                if (user.getIsAdmin()){
                    keyBoard.addButtons(new TGInlineButton("Завершить", CLOSE_EVENT +":" + event.getId(), 2));
                    keyBoard.addButtons(new TGInlineButton("Редактировать", REDACT_EVENT +":" + event.getId(), 2));
                }

                message.setReplyMarkup(keyBoard);
                executeMessage(message);
            }else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }

//    private void deleteEvent(long chatId, String[] commands, User user) {
//        if (checkReg(user, chatId)){
//            final Long event_id = Long.parseLong(commands[1]);
//            final Event eventById = eventsService.getEventById(event_id);
//            if (eventById != null){
//                final List<User> allUsers = usersService.getAllUsers();
//                for (User u: allUsers){
//                    sendMessage(u.getTelegramId(), String.format("Событие %s удалено", eventById.getName()));
//                }
//            }
//            eventsService.deleteEvent("Дина", event_id);
//        }
//    }


    public void sendAllEvents(CallbackQuery callbackQuery) {
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
            sendMessage(callbackQuery.getMessage().getChatId(), "На данный момент нет мероприятии");
        }
        sendAllEventsButtons(eventsFormat, callbackQuery);

    }

    private void sendAllEventsButtons(Map<Long, String> events, CallbackQuery callbackQuery){
        final TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(events.size());
        int i = 1;
        for (Map.Entry<Long, String> event: events.entrySet()){
            keyBoard.addButton(new TGInlineButton(event.getValue(), SEE_EVENT +":" + event.getKey(), i));
            i++;
        }
        TGEditMessage message = new TGEditMessage(callbackQuery, "Мероприятия");
        message.setReplyMarkup(keyBoard);
        executeMessage(message);
    }


    public void executeMessage(CallbackQuery callbackQuery, EditMessageText message){
        try {
            if (checkReg(callbackQuery)){
                execute(message);
            }
        } catch (TelegramApiException ignored) {

        }
    }

    public void executeMessage(EditMessageText message){
        try {
            execute(message);
        } catch (TelegramApiException ignored) {

        }
    }
    public void executeMessage(SendMessage message){
        try {
            execute(message);
        } catch (TelegramApiException ignored) {

        }
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
