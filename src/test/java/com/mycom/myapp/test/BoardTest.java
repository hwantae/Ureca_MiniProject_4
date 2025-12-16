package com.mycom.myapp.test;

import com.mycom.myapp.board.domain.Board;
import com.mycom.myapp.board.dto.BoardRequestDto;
import com.mycom.myapp.board.dto.BoardResponseDto;
import com.mycom.myapp.board.repository.BoardRepository;
import com.mycom.myapp.board.service.BoardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BoardTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardService boardService;

    @Test
    @DisplayName("게시글 작성 성공")
    void writeBoard_Success() {
        // given
        BoardRequestDto requestDto = BoardRequestDto.builder()
                .title("제목")
                .content("내용")
                .build();

        Board savedBoard = Board.builder()
                .boardId(1L)
                .title("제목")
                .writer("user1")
                .build();

        given(boardRepository.save(any(Board.class))).willReturn(savedBoard);

        // when
        Long resultId = boardService.writeBoard(requestDto, "user1");

        // then
        assertThat(resultId).isEqualTo(1L);
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    @DisplayName("게시글 상세 조회 성공")
    void getBoard_Success() {
        // given
        Board board = Board.builder()
                .boardId(1L)
                .title("제목")
                .content("내용")
                .writer("user1")
                .createdAt(LocalDateTime.now()) // DTO 변환 시 NPE 방지
                .build();

        given(boardRepository.findById(1L)).willReturn(Optional.of(board));

        // when
        BoardResponseDto response = boardService.getBoard(1L);

        // then
        assertThat(response.getTitle()).isEqualTo("제목");
        assertThat(response.getWriter()).isEqualTo("user1");
    }

    @Test
    @DisplayName("게시글 수정 실패 - 작성자가 아님")
    void updateBoard_Fail_NotOwner() {
        // given
        Long boardId = 1L;
        String owner = "user1";
        String intruder = "user2"; // 다른 사용자

        Board board = Board.builder()
                .boardId(boardId)
                .writer(owner) // 작성자는 user1
                .build();

        BoardRequestDto updateDto = BoardRequestDto.builder()
                .title("수정 제목")
                .content("수정 내용")
                .build();

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when & then
        // user2가 수정을 시도하면 IllegalStateException 발생해야 함
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            boardService.updateBoard(boardId, updateDto, intruder);
        });

        assertThat(exception.getMessage()).isEqualTo("해당 게시글의 수정/삭제 권한이 없습니다.");
    }

    @Test
    @DisplayName("게시글 수정 성공 - 작성자 본인")
    void updateBoard_Success() {
        // given
        Long boardId = 1L;
        String owner = "user1";

        Board board = Board.builder()
                .boardId(boardId)
                .title("원래 제목")
                .content("원래 내용")
                .writer(owner)
                .build();

        BoardRequestDto updateDto = BoardRequestDto.builder()
                .title("수정 제목")
                .content("수정 내용")
                .build();

        given(boardRepository.findById(boardId)).willReturn(Optional.of(board));

        // when
        boardService.updateBoard(boardId, updateDto, owner);

        // then
        // 객체의 상태가 변경되었는지 확인 (Dirty Checking 로직 검증)
        assertThat(board.getTitle()).isEqualTo("수정 제목");
        assertThat(board.getContent()).isEqualTo("수정 내용");
    }
}