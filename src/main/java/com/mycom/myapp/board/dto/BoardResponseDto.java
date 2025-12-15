package com.mycom.myapp.board.dto;

import com.mycom.myapp.board.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponseDto {

    private Long boardId;
    private String title;
    private String content;
    private String writer;
    private String createdAt; // 화면에 보여주기 좋게 String으로 변환

    // Entity -> DTO 변환 편의 메서드 (Factory Method)
    public static BoardResponseDto fromEntity(Board board) {
        return BoardResponseDto.builder()
                .boardId(board.getBoardId())
                .title(board.getTitle())
                .content(board.getContent())
                .writer(board.getWriter())
                // 날짜를 "2025-12-15 14:00" 형식의 문자열로 변환
                .createdAt(board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}
