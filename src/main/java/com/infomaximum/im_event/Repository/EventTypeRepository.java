package com.infomaximum.im_event.Repository;

import com.infomaximum.im_event.Model.EVENT_TYPE;
import com.infomaximum.im_event.Model.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Repository
public interface EventTypeRepository extends JpaRepository<EventType, Long> {

    EventType getEventTypeByEventType(EVENT_TYPE event_type);

}
