package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository extends JpaRepository<Item, Long> {

    Collection<Item> getByOwnerIdOrderByIdAsc(Long ownerId);

    @Query("select i from Item as i where i.available = true " +
            "and (lower(i.name) like lower(concat('%', :searchQuery, '%')) " +
                  "or lower(i.description) like lower(concat('%', :searchQuery, '%')))")
    Collection<Item> findAvailableBySubstring(String searchQuery);
}
