package com.infomaximum.im_event.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */

@Entity
@Data
@NoArgsConstructor
@Table(name = "events_types")
public class EventType {

    public EventType(EVENT_TYPE eventType) {
        this.eventType = eventType;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type")
    private EVENT_TYPE eventType;

}
