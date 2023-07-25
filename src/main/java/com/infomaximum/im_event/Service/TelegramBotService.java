package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Components.EventInfo;
import com.infomaximum.im_event.Components.TGInlineButton;
import com.infomaximum.im_event.Components.TGInlineKeyBoard;
import com.infomaximum.im_event.Components.TGUtil;
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
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;
    private final UsersService usersService;
    private static final int defaultInfoMessageTime = 2000;

    private final static String SEE_EVENT = "SEE_EVENT";
    private final static String CLOSE_EVENT = "CLOSE_EVENT";
    private final static String REDACT_EVENT = "REDACT_EVENT";
    private final static String REG_TO_EVENT = "REG_TO_EVENT";
    private final static String UNREG_TO_EVENT = "UNREG_TO_EVENT";
    private final static String SAVE_EVENT = "SAVE_EVENT";
    private final static String CANCEL_CREATE_EVENT = "CANCEL_CREATE_EVENT:0";

    private final Map<Long, Event> eventCreateMap = new HashMap<>();


    public TelegramBotService(TelegramBotConfig botConfig, EventsService eventsService, UsersService usersService) {
        this.botConfig = botConfig;
        this.eventsService = eventsService;
        this.usersService = usersService;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/all", "просмотр списка активных мероприятий"));
        listofCommands.add(new BotCommand("/reg", "подписка на IM.EVENT"));
//        listofCommands.add(new BotCommand("/createEvent", "создать новое мероприятие"));
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


    private boolean checkReg(User user, long chatId) {
        if (user == null) {
            sendMessage(chatId, "До использования бота вы должны\n быть зарегистрированы на IM.EVENTS\n/reg Имя Фамилия почта пароль");
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
                getEvent(eventsService.getEventById(eventId), user, chatId);
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
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            final String[] commands = messageText.split(" ");
            final User user = usersService.getUserByTelegramID(chatId);

            if (eventCreateMap.containsKey(chatId)){
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
                        executeMessage(message);

                    }
                }
            }

            switch (commands[0]) {
                case "/delete":
                    deleteEvent(chatId, commands, user);
                    break;
                case "/createEvent":
                    if (checkReg(user, chatId)){
                        Event event = new Event(user);
                        if (!eventCreateMap.containsKey(chatId)){
                            eventCreateMap.put(chatId, event);
                            sendMessage(chatId, "Задайте имя мероприятия");
                        }
                    }
                    break;
                case "/all":
                    getAllEvents(chatId, user);
                    break;
                case "/reg":
                    registryUser(chatId, commands);
                    break;
                case "/help":
                    help(chatId, user);
                    break;
            }
        }
    }

    private void getAllEvents(long chatId, User user) {
        if (checkReg(user, chatId)){
            sendAllEvents(chatId);
        }
    }

    private void help(long chatId, User user) {
        if (checkReg(user, chatId)){
            String commandList = """
                command:
                /all (просмотр списка активных мероприятий)
                /event id (просмотр мероприятия)
                /reg (подписка на IM.EVENT)
                """;
            sendMessage(chatId, commandList);
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

    private void getEvent(Event event, User user, long chatId) {
        if (checkReg(user, chatId)){

            if (event != null){
                final SendMessage sendMessage = new SendMessage();
                sendMessage.setText(EventInfo.showEvent(event));
                sendMessage.setChatId(chatId);

                TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(2);
                keyBoard.addButtons(new TGInlineButton("Записаться", REG_TO_EVENT  +":"+ event.getId(), 1));
                keyBoard.addButtons(new TGInlineButton("Отписаться", UNREG_TO_EVENT +":" + event.getId(), 1));

                if (user.getIsAdmin()){
                    keyBoard.addButtons(new TGInlineButton("Завершить", CLOSE_EVENT +":" + event.getId(), 2));
                    keyBoard.addButtons(new TGInlineButton("Редактировать", REDACT_EVENT +":" + event.getId(), 2));
                }

                sendMessage.setReplyMarkup(keyBoard);
                executeMessage(sendMessage);
            }else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }

    private void deleteEvent(long chatId, String[] commands, User user) {
        if (checkReg(user, chatId)){
            final Long event_id = Long.parseLong(commands[1]);
            final Event eventById = eventsService.getEventById(event_id);
            if (eventById != null){
                final List<User> allUsers = usersService.getAllUsers();
                for (User u: allUsers){
                    sendMessage(u.getTelegramId(), String.format("Событие %s удалено", eventById.getName()));
                }
            }
            eventsService.deleteEvent("Дина", event_id);
        }
    }


    public void sendAllEvents(Long chatId) {
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
            sendMessage(chatId, "На данный момент нет мероприятии");
        }
        sendAllEventsButtons(eventsFormat, chatId);

    }

    private void sendAllEventsButtons(Map<Long, String> events, Long chatId){
        final TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(events.size());
        int i = 1;
        for (Map.Entry<Long, String> event: events.entrySet()){
            keyBoard.addButton(new TGInlineButton(event.getValue(), SEE_EVENT +":" + event.getKey(), i));
            i++;
        }
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Мероприятия");
        message.setReplyMarkup(keyBoard);

        executeMessage(message);
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
