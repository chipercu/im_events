package com.infomaximum.im_event.Repository;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.Event;
import com.infomaximum.im_event.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Repository
public interface EventsRepository extends JpaRepository<Event, Long> {

    List<Event> getEventsByEventType(EVENT_TYPE eventType);
    Optional<Event> getEventById(Long id);
    Optional<Event> getEventByName(String name);
}
