package ru.practicum.shareit.item.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JoinFormula;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.User;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "items")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "is_available", nullable = false)
    private Boolean available;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private Long requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(SELECT b.id FROM bookings b " +
            "WHERE b.item_id = id " +
            "AND b.start_time < LOCALTIMESTAMP(6) " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start_time DESC LIMIT 1)")
    private Booking lastBooking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinFormula("(SELECT b.id FROM bookings b " +
            "WHERE b.item_id = id " +
            "AND b.start_time > LOCALTIMESTAMP(6) " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start_time ASC LIMIT 1)")
    private Booking nextBooking;

    @OneToMany(mappedBy = "item")
    private Set<Comment> comments;
}
