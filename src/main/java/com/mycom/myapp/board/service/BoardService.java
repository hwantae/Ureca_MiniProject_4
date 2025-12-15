package com.mycom.myapp.board.service;

import com.mycom.myapp.board.domain.Board;
import com.mycom.myapp.board.dto.BoardRequestDto;
import com.mycom.myapp.board.dto.BoardResponseDto;
import com.mycom.myapp.board.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {
    private final BoardRepository boardRepository;

    public List<BoardResponseDto> getBoardList(){
        return boardRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BoardResponseDto::fromEntity) // Entity -> DTO 변환
                .collect(Collectors.toList());
    }

    public BoardResponseDto getBoard(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        return BoardResponseDto.fromEntity(board);
    }

    @Transactional // 쓰기 작업 허용
    public Long writeBoard(BoardRequestDto dto, String username) {
        // DTO를 Entity로 변환하면서 로그인한 사용자 이름(username)을 넣음
        Board board = dto.toEntity(username);
        Board savedBoard = boardRepository.save(board);
        return savedBoard.getBoardId();
    }

    @Transactional
    public Long updateBoard(Long boardId, BoardRequestDto dto, String username) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 본인 확인 로직
        checkOwnership(board, username);

        board.update(dto.getTitle(), dto.getContent());
        return board.getBoardId();
    }

    @Transactional
    public void deleteBoard(Long boardId, String username) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 본인 확인 로직
        checkOwnership(board, username);

        boardRepository.delete(board);
    }

    private void checkOwnership(Board board, String username) {
        if (!board.getWriter().equals(username)) {
            throw new IllegalStateException("해당 게시글의 수정/삭제 권한이 없습니다.");
        }
    }
}
