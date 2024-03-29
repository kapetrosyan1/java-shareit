package ru.practicum.shareit.item.comment;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class CommentMapper {
    public Comment toComment(CommentRequestDto commentRequestDto, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setAuthor(author);
        comment.setItem(item);
        return comment;
    }

    public CommentResponseDto toCommentResponseDto(Comment comment) {
        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setText(comment.getText());
        commentResponseDto.setAuthorName(comment.getAuthor().getName());
        commentResponseDto.setCreated(comment.getCreated());
        return commentResponseDto;
    }

    public List<CommentResponseDto> toCommentResponseDtoList(List<Comment> comments) {
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();
        for (Comment comment : comments) {
            commentResponseDtoList.add(toCommentResponseDto(comment));
        }
        return commentResponseDtoList;
    }
}