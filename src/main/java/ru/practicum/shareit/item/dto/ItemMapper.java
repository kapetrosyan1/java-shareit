package ru.practicum.shareit.item.dto;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.comment.CommentResponseDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@UtilityClass
public class ItemMapper {
    public Item toItem(ItemRequestDto itemRequestDto) {
        Item item = new Item();
        item.setId(itemRequestDto.getId());
        item.setName(itemRequestDto.getName());
        item.setDescription(itemRequestDto.getDescription());
        item.setAvailable(itemRequestDto.getAvailable());
        return item;
    }

    public ItemResponseDto toItemResponseDto(Item item) {
        ItemResponseDto itemResponseDto = new ItemResponseDto();
        itemResponseDto.setId(item.getId());
        itemResponseDto.setName(item.getName());
        itemResponseDto.setDescription(item.getDescription());
        itemResponseDto.setAvailable(item.getAvailable());
        return itemResponseDto;
    }

    public ItemResponseDtoWithBookings toItemResponseDtoWithBookings(Item item) {
        ItemResponseDtoWithBookings itemDtoWithBookings = new ItemResponseDtoWithBookings();
        itemDtoWithBookings.setId(item.getId());
        itemDtoWithBookings.setName(item.getName());
        itemDtoWithBookings.setDescription(item.getDescription());
        itemDtoWithBookings.setAvailable(item.getAvailable());
        return itemDtoWithBookings;
    }

    public ItemResponseDtoWithBookings toItemResponseDtoWithBookings(Item item, List<CommentResponseDto> comments) {
        ItemResponseDtoWithBookings itemDtoWithBookings = new ItemResponseDtoWithBookings();
        itemDtoWithBookings.setId(item.getId());
        itemDtoWithBookings.setName(item.getName());
        itemDtoWithBookings.setDescription(item.getDescription());
        itemDtoWithBookings.setAvailable(item.getAvailable());
        itemDtoWithBookings.setComments(comments);
        return itemDtoWithBookings;
    }
}