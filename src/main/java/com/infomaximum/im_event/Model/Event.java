package com.infomaximum.im_event.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * Created by a.kiperku
 * Date: 21.07.2023
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "events")
public class Event {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_initiator")
    private User initiator;
    @Column(name = "isActive")
    private Boolean isActive;
    @Column(name = "isRepeatable")
    private Boolean isRepeatable;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_participants")
    private List<User> participants;

    @Column(name = "create_date")
    private Date create_date;
    @Column(name = "start_date")
    private Date start_date;
    @Column(name = "coins")
    private Integer coins;

    @Column(name = "event_type")
    private EVENT_TYPE eventType;

    public Event(String name, User initiator, Date start_date, EVENT_TYPE eventType, Boolean isRepeatable) {
        this.name = name;
        this.initiator = initiator;
        this.create_date = new Date();
        this.start_date = start_date;
        this.coins = 0;
        this.eventType = eventType;
        this.isRepeatable = isRepeatable;
        this.isActive = true;
    }

    public void addCoins(Integer coin) {
        this.coins = coin;
    }
}
