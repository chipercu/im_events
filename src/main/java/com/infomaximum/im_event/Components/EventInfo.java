package com.infomaximum.im_event.Components;

import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;

import java.util.List;

/**
 * Created by a.kiperku
 * Date: 25.07.2023
 */

public class EventInfo {


    public static String showEvent(Event event){
        StringBuilder message = new StringBuilder();
        final List<User> participants = event.getParticipants();
        String[] split = event.getStart_date().split(":");
        String date = split[0] + ":" + split[1];
        message.append(event.getName()).append("\n")
                .append("Когда: ").append(date).append("\n")
                .append("Организатор: ").append(event.getInitiator().getName()).append(" ").append(event.getInitiator().getSurname()).append("\n")
                .append(event.getDescription()).append("\n\n");
        if (!participants.isEmpty()){
            if (participants.size() > 5){
                message.append("Участников: ").append(participants.size()).append("\n");
            }else {
                message.append("Участники:\n");
                for (int i = 0; i < participants.size(); i++) {
                    message.append(i + 1).append(".").append(participants.get(i).getName()).append(" ").append(participants.get(i).getSurname()).append("\n");
                }
            }
        }
        return message.toString();
    }


}
