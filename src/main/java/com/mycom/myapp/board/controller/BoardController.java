package com.mycom.myapp.board.controller;

import com.mycom.myapp.board.dto.BoardRequestDto;
import com.mycom.myapp.board.dto.BoardResponseDto;
import com.mycom.myapp.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

	private final BoardService boardService;

	// 1. 게시글 목록 조회 (누구나 가능)
	@GetMapping
	public Map<String, Object> list() {
		List<BoardResponseDto> list = boardService.getBoardList();

		Map<String, Object> response = new HashMap<>();
		response.put("result", "success");
		response.put("list", list);
		return response;
	}

	// 2. 게시글 상세 조회 (누구나 가능)
	@GetMapping("/{boardId}")
	public Map<String, Object> detail(@PathVariable Long boardId) {
		BoardResponseDto dto = boardService.getBoard(boardId);

		Map<String, Object> response = new HashMap<>();
		response.put("result", "success");
		response.put("dto", dto);
		return response;
	}

	// 3. 게시글 작성 (로그인 필수)
	@PostMapping
	public Map<String, String> write(@RequestBody BoardRequestDto dto, Authentication auth) {
		// Authentication auth: 토큰에서 파싱된 사용자 정보 (SecurityConfig가 넣어줌)
		boardService.writeBoard(dto, auth.getName()); // auth.getName() = email 또는 username

		Map<String, String> response = new HashMap<>();
		response.put("result", "success");
		response.put("message", "게시글이 등록되었습니다.");
		return response;
	}

	// 4. 게시글 수정 (로그인 + 본인 필수)
	@PutMapping("/{boardId}")
	public Map<String, String> update(@PathVariable Long boardId,
									  @RequestBody BoardRequestDto dto,
									  Authentication auth) {
		try {
			boardService.updateBoard(boardId, dto, auth.getName());

			Map<String, String> response = new HashMap<>();
			response.put("result", "success");
			response.put("message", "게시글이 수정되었습니다.");
			return response;
		} catch (IllegalStateException e) {
			// 본인이 아닐 경우 에러 처리
			Map<String, String> response = new HashMap<>();
			response.put("result", "fail");
			response.put("message", e.getMessage());
			return response;
		}
	}

	// 5. 게시글 삭제 (로그인 + 본인 필수)
	@DeleteMapping("/{boardId}")
	public Map<String, String> delete(@PathVariable Long boardId, Authentication auth) {
		try {
			boardService.deleteBoard(boardId, auth.getName());

			Map<String, String> response = new HashMap<>();
			response.put("result", "success");
			response.put("message", "게시글이 삭제되었습니다.");
			return response;
		} catch (IllegalStateException e) {
			Map<String, String> response = new HashMap<>();
			response.put("result", "fail");
			response.put("message", e.getMessage());
			return response;
		}
	}
}