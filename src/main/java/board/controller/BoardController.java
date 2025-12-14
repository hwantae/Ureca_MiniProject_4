package board.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BoardController {

    @GetMapping("/boards")
    public String getBoard() {
        return """
				{
					"result":"success",
					"list":[
						{
							"boardId": 1,
							"title": "제목 1",
							"content": "내용 1"
						},
						{
							"boardId": 2,
							"title": "제목 2",
							"content": "내용 2"
						},
						{
							"boardId": 3,
							"title": "제목 3",
							"content": "내용 3"
						}
					]
				}
				""";
    }
}