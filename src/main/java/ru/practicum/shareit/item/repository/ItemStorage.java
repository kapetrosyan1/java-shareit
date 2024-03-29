package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
    Page<Item> findAllByOwnerId(Long ownerId, Pageable pageable);

    @Query(value = "select i.id from items i " +
            "where i.owner_id = :owner " +
            "order by i.id", nativeQuery = true)
    List<Long> findAllOwnersItemsIds(@Param("owner") Long ownerId);

    @Query("select i from Item i " +
            "where upper(i.name) like upper(concat('%', :query, '%')) " +
            " or upper(i.description) like upper(concat('%', :query, '%'))" +
            " and i.available = true ")
    Page<Item> searchItems(@Param("query") String search, Pageable pageable);

    List<Item> findByItemRequestIn(List<ItemRequest> itemRequests, Sort sort);

    List<Item> findByItemRequestId(Long itemRequestId, Sort sort);
}