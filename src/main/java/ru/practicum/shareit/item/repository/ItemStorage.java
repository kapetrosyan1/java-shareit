package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemStorage extends JpaRepository<Item, Long> {
    @Query("select i from Item i " +
            "join fetch i.owner " +
            "where i.id = :id")
    Optional<Item> findByIdWithOwner(@Param("id") Long id);

    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    @Query(value = "select i.id from items i " +
            "where i.owner_id = :owner " +
            "order by i.id", nativeQuery = true)
    List<Long> findAllOwnersItemsIds(@Param("owner") Long ownerId);

    @Query("select i from Item i " +
            "where upper(i.name) like upper(concat('%', :query, '%')) " +
            " or upper(i.description) like upper(concat('%', :query, '%'))" +
            " and i.available = true ")
    List<Item> searchItems(@Param("query") String search);
}