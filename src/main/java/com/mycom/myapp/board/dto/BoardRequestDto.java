package com.mycom.myapp.board.dto;

import com.mycom.myapp.board.domain.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardRequestDto {

    // 제목 (필수)
    private String title;

    // 내용 (필수)
    private String content;

    // DTO -> Entity 변환 편의 메서드
    // (Controller에서 토큰으로 찾은 writer 이름을 넣어줍니다)
    public Board toEntity(String writer) {
        return Board.builder()
                .title(this.title)
                .content(this.content)
                .writer(writer)
                .build();
    }
}