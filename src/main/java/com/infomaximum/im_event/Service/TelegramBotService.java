package com.infomaximum.im_event.Service;

import com.infomaximum.im_event.Components.*;
import com.infomaximum.im_event.Config.TelegramBotConfig;
import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Created by a.kiperku
 * Date: 23.07.2023
 */
@Component
public class TelegramBotService extends TelegramLongPollingBot implements CallBackConst {

    private final JavaMailSender emailSender;

    private final TelegramBotConfig botConfig;
    private final EventsService eventsService;
    private final UsersService usersService;
    private static final int defaultInfoMessageTime = 2000;


    private final Map<Long, Event> eventCreateMap = new HashMap<>();
    private final Map<Long, User> userCreateMap = new HashMap<>();
    private final Map<Long, Integer> lastMessageMap = new HashMap<>();


    public TelegramBotService(TelegramBotConfig botConfig, EventsService eventsService, UsersService usersService, JavaMailSender emailSender) {
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
        this.emailSender = emailSender;
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
        if (callbackQuery == null) {
            if (usersService.getUserByTelegramID(chatId[0]) != null) {
                return true;
            }
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
            return false;
        }
        return true;
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            CallbackQuery callbackQuery = update.getCallbackQuery();
            final String data = callbackQuery.getData();
            final long eventId = Long.parseLong(data.split(":")[1]);
            final TGUtil tgUtil = new TGUtil();
            if (data.startsWith(SEE_EVENT)) {
                getEvent(callbackQuery, eventsService.getEventById(eventId), chatId);
            } else if (data.startsWith(MENU)) {
                TGMenu.showMenu(callbackQuery);
            }

            else if (data.startsWith(REGISTER)) {
                final User newUser = new User(chatId, false);
                userCreateMap.put(callbackQuery.getMessage().getChatId(), newUser);
                lastMessageMap.put(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId());
                TGRegister.initRegistry(callbackQuery);
            } else if (data.startsWith(CANCEL_REGISTRY)) {
                lastMessageMap.remove(chatId);
                TGRegister.cancelRegistry(callbackQuery);
            } else if (data.startsWith(CONFIRM_REGISTRY)) {
                TGRegister.enterConfirmPassword(callbackQuery);
            } else if (data.startsWith(DELETE_USER)) {
                UserSettings.deleteAccount(callbackQuery);
            } else if (data.startsWith(CONFIRM_DELETE_USER)) {
                UserSettings.deleteUser(callbackQuery);
            }

            else if (data.startsWith(SHOW_ALL_EVENT)) {
                tgUtil.seeAllEvents(callbackQuery);
            } else if (data.startsWith(REG_TO_EVENT)) {
                tgUtil.regToEvent(callbackQuery);
            } else if (data.startsWith(UNREG_TO_EVENT)) {
                tgUtil.unregToEvent(callbackQuery);
            } else if (data.startsWith(REFRESH_EVENT)) {
                loading(chatId);
                getEvent(callbackQuery, eventsService.getEventById(eventId), chatId);
            }


            else if (data.startsWith(USER_SETTINGS)) {
                UserSettings.showUserSettings(callbackQuery);
            }

            else if (data.startsWith(CREATE_EVENT)) {
                final User userByTelegramID = usersService.getUserByTelegramID(chatId);
                if (userByTelegramID != null){
                    final Event event = new Event(userByTelegramID);
                    eventCreateMap.put(chatId, event);
                    lastMessageMap.put(chatId, callbackQuery.getMessage().getMessageId());
                    TGEvent.createEvent(callbackQuery);
                }
            } else if (data.startsWith(EVENT_TYPES)) {
                String eventType = data.split(":")[2];
                final Event event = eventCreateMap.get(chatId);
                switch (eventType){
                    case SPORT -> event.setEventType(EVENT_TYPE.SPORT);
                    case EDUCATION -> event.setEventType(EVENT_TYPE.EDUCATION);
                    case CULTURE -> event.setEventType(EVENT_TYPE.CULTURE);
                    case GAME -> event.setEventType(EVENT_TYPE.GAME);
                    case CINEMA -> event.setEventType(EVENT_TYPE.CINEMA);
                    case CHILL -> event.setEventType(EVENT_TYPE.CHILL);
                }
                TGEvent.setDescription(callbackQuery, event);
            } else if (data.startsWith(CONFIRM_CREATE_EVENT)) {
                final Event event = eventCreateMap.get(chatId);
                eventsService.addNewEvent(event);
                for (User user: usersService.getAllUsers()){
                    SendMessage message = new SendMessage();
                    message.setChatId(user.getTelegramId());
                    message.setText("Новое мероприятие:" +
                            "\n" + "Название: " + event.getName() +
                            "\n" + "Когда: " + event.getStart_date() +
                            "\n" + "Описание: " + event.getDescription()
                            );
                    TGInlineKeyBoard keyBoard = new TGInlineKeyBoard(1);
                    keyBoard.addButton(new TGInlineButton("Открыть", SEE_EVENT + ":" + event.getId(), 1));
                    message.setReplyMarkup(keyBoard);
                    executeMessage(message);
                }
            } else if (data.startsWith(CANCEL_CREATE_EVENT)) {
                Event event = eventCreateMap.get(chatId);
                if (event != null) {
                    eventCreateMap.remove(chatId);
                    infoMessage(chatId, "Создание мероприятия отменена");
                }
                TGEvent.cancelCreateEvent(callbackQuery);
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
            } else if (data.startsWith(DELETE_EVENT)) {
                TGEvent.confirmDeleteEvent(callbackQuery);

                eventsService.deleteEvent(eventId);
                TGMenu.showMenu(callbackQuery);
            } else if (data.startsWith(CONFIRM_DELETE_EVENT)) {
                eventsService.deleteEvent(eventId);
                TGMenu.showMenu(callbackQuery);
            } else if (data.startsWith(MAIL)) {
                Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom("chipercualexandru@mail.ru");
                    message.setTo("a.kiperku@infomaximum.com");
                    message.setSubject("subject");
                    message.setText("Вы получили данное сообщение от сервиса IM.EVENTS \n при  регистрации аккаунта \n Ваш код подтверждения : 545454");
                    emailSender.send(message);
                }, 100, TimeUnit.MILLISECONDS);

            }
        }


        if (update.hasMessage() && update.getMessage().hasText()) {

            final Message updateMessage = update.getMessage();
            long chatId = update.getMessage().getChatId();
            switch (updateMessage.getText()) {
                case "/help":
                case "/start":
                    userCreateMap.remove(chatId);
                    if (checkReg(null, chatId)) {
                        TGMenu.showMenu(chatId);
                    }
                    break;
            }

            if (eventCreateMap.containsKey(chatId)) {
                String messageText = update.getMessage().getText();

                Event event = eventCreateMap.get(chatId);
                if (event != null) {
                    if (event.getName() == null || event.getName().isEmpty()) {
                        event.setName(messageText);
                        TGEvent.setEventStartTime(event, updateMessage, lastMessageMap.get(chatId));
                    } else if (event.getStart_date() == null || event.getStart_date().isEmpty()) {
                        event.setStart_date(messageText);
                        TGEvent.setEventType(event, updateMessage, lastMessageMap.get(chatId));
                    } else if (event.getDescription() == null || event.getDescription().isEmpty()) {
                        event.setDescription(messageText);
                        TGEvent.confirmCreateEvent(event, updateMessage, lastMessageMap.get(chatId));
                    }
                }
            }

            if (userCreateMap.containsKey(chatId)) {
                final User newUser = userCreateMap.get(chatId);
                final String text = updateMessage.getText();
                final int length = text.length();
                if (newUser.getName() == null || newUser.getName().isEmpty()) {
                    if (length < 3 || length > 15){
                        infoMessage(chatId, "Имя должно быть больше 2 и меньше 15 символов");
                        return;
                    }
                    newUser.setName(updateMessage.getText());
                    TGRegister.setName(newUser, updateMessage, lastMessageMap.get(chatId));
                } else if (newUser.getSurname() == null || newUser.getSurname().isEmpty()) {
                    if (length < 3 || length > 15){
                        infoMessage(chatId, "Фамилия должно быть больше 2 и меньше 15 символов");
                        return;
                    }
                    newUser.setSurname(updateMessage.getText());
                    TGRegister.setSurname(newUser, updateMessage, lastMessageMap.get(chatId));
                } else if (newUser.getEmail() == null || newUser.getEmail().isEmpty()) {
                    if (!text.contains("@")){
                        infoMessage(chatId, "Не правильный формат почты");
                        return;
                    }
                    if (!(text.split("@")[1].equals("infomaximum.com") || text.split("@")[1].equals("infomaximum.biz"))) {
                        infoMessage(chatId, "Разрешено использовать только корпоративную почту Infomaximum");
                        return;
                    }
                    for (User u: usersService.getAllUsers()){
                        if (u.getEmail().equals(text)){
                            infoMessage(chatId, "Пользователь с такой уже зарегистрирован");
                            return;
                        }
                    }
                    newUser.setEmail(updateMessage.getText());
                    final int pass = ThreadLocalRandom.current().nextInt(111111, 999999);

                    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                        SimpleMailMessage message = new SimpleMailMessage();
                        message.setFrom("chipercualexandru@mail.ru");
                        message.setTo(newUser.getEmail());
                        message.setSubject("IM.EVENTS");
                        message.setText("Вы получили данное сообщение от сервиса IM.EVENTS \n при  регистрации аккаунта \n Ваш код подтверждения : " + pass);
                        emailSender.send(message);
                    }, 100, TimeUnit.MILLISECONDS);

                    newUser.setPassword(String.valueOf(pass)); //TODO : ЗАТЫЧКА реализовать рандом с отправкой на почту
                    TGRegister.setEmail(newUser, updateMessage, lastMessageMap.get(chatId));
                } else {
                    if (!text.equals(newUser.getPassword())){
                        newUser.setCoins(-3.0);
                        infoMessage(chatId, "Пароль не верный - у вас еще " + newUser.getCoins().intValue() + " попытки");
                        return;
                    }
                    if (usersService.registryFromTG(newUser)){
                        infoMessage(chatId, "Вы успешно зарегистрировались!");
                        TGMenu.showMenu(chatId);
                    }else {
                        infoMessage(chatId, "Не удалось завершить регистрацию! попробуйте еще раз");
                        TGMenu.showMenu(chatId);
                    }
                }
            }


        }
    }

    public void infoMessage(long chatId, String text) {
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

        } catch (TelegramApiException ignored) {

        }
    }

    public void loading(long chatId) {
        try {
            final String[] text = {" *"};
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(text[0]);
            Message sentOutMessage = execute(message);

            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                EditMessageText editMessageText = new EditMessageText();
                editMessageText.setChatId(chatId);
                editMessageText.setMessageId(sentOutMessage.getMessageId());
                text[0] = text[0] + " *";
                editMessageText.setText(text[0]);
                executeMessage(editMessageText);
            }, 0, 100, TimeUnit.MILLISECONDS);

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(sentOutMessage.getChatId());
                deleteMessage.setMessageId(sentOutMessage.getMessageId());
                try {
                    execute(deleteMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }, 3000, TimeUnit.MILLISECONDS);

        } catch (TelegramApiException ignored) {

        }
    }

    private void getEvent(CallbackQuery callbackQuery, Event event, long chatId) {
        final User user = usersService.getUserByTelegramID(chatId);
        if (checkReg(callbackQuery)) {
            if (event != null) {
                TGEditMessage message = new TGEditMessage(callbackQuery, EventInfo.showEvent(event));
                message.setReplyMarkup(TGUtil.getEventKeyBoard(user, event));
                executeMessage(message);
            } else {
                sendMessage(chatId, "Проверьте id мероприятия\n /event");
            }
        }
    }




    public void executeMessage(CallbackQuery callbackQuery, EditMessageText message) {
        try {
            if (checkReg(callbackQuery)) {
                execute(message);
            }
        } catch (TelegramApiException ignored) {
        }
    }
    public void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException ignored) {
        }
    }

    public void executeMessage(SendMessage message) {
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
        } catch (TelegramApiException ignored) {
        }
    }
}
