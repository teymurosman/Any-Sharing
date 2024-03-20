package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdOrderByIdAsc(Long ownerId, Pageable page);

    @Query("select i from Item as i where i.available = true " +
            "and (lower(i.name) like lower(concat('%', :searchQuery, '%')) " +
                  "or lower(i.description) like lower(concat('%', :searchQuery, '%')))")
    List<Item> findAvailableBySubstring(String searchQuery, Pageable page);

    List<Item> findAllByRequestId(Long requestId);
}
