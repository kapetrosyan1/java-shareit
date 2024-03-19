package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage extends JpaRepository<Item, Long> {
    @Query("select i from Item i " +
            "join fetch i.owner " +
            "where i.id = ?1")
    Optional<Item> findByIdWithOwner(Long id);

    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    @Query(value = "select i.id from items i " +
            "where i.owner_id = ?1 " +
            "order by i.id", nativeQuery = true)
    List<Long> findAllOwnersItemsIds(Long ownerId);

    @Query("select i from Item i " +
            "where upper(i.name) like upper(concat('%', ?1, '%')) " +
            " or upper(i.description) like upper(concat('%', ?1, '%'))" +
            " and i.available = true ")
    List<Item> searchItems(String substring);
}