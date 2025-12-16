package com.mycom.myapp.test;

import com.mycom.myapp.board.dto.BoardRequestDto;
import com.mycom.myapp.board.dto.BoardResponseDto;
import com.mycom.myapp.board.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional // 테스트 종료 후 자동 롤백
public class BoardIntegrationTest {

    @Autowired
    private BoardService boardService;

    @Test
    @DisplayName("게시글 전체 라이프사이클 (작성 -> 조회 -> 수정 -> 삭제)")
    void boardLifeCycleTest() {
        // 1. 게시글 작성
        String username = "tester@example.com";
        BoardRequestDto writeDto = BoardRequestDto.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .build();

        Long savedId = boardService.writeBoard(writeDto, username);
        assertThat(savedId).isNotNull();

        // 2. 게시글 상세 조회
        BoardResponseDto responseDto = boardService.getBoard(savedId);
        assertThat(responseDto.getTitle()).isEqualTo("테스트 제목");
        assertThat(responseDto.getWriter()).isEqualTo(username);
        // 포맷팅된 날짜가 null이 아닌지 확인
        assertThat(responseDto.getCreatedAt()).isNotNull();

        // 3. 게시글 수정
        BoardRequestDto updateDto = BoardRequestDto.builder()
                .title("수정된 제목")
                .content("수정된 내용")
                .build();

        boardService.updateBoard(savedId, updateDto, username);

        // 수정 확인
        BoardResponseDto updatedResponse = boardService.getBoard(savedId);
        assertThat(updatedResponse.getTitle()).isEqualTo("수정된 제목");

        // 4. 게시글 삭제
        boardService.deleteBoard(savedId, username);

        // 삭제 확인 (조회 시 예외 발생해야 함)
        assertThrows(IllegalArgumentException.class, () -> {
            boardService.getBoard(savedId);
        });
    }
}