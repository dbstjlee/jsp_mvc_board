package com.tenco.tboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/board/*")
public class BoardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Override
	public void init() throws ServletException {
		// BoardRepository 추가 예정
		
	}
	
	
    public BoardController() {
    	
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		// TODO 인증 처리는 추후 추가(로그인 여부)
		switch (action) {
		case "/create":
			// 게시글 생성 페이지 이동 처리
			break;
		case "/list":
			handlelistBoards(request, response);
			break;

		default:
			break;
		}
	}

	/**
	 * 페이징 처리하기
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void handlelistBoards(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 페이지 요청 시 다시 돌아와서 출력됨.
		
		// 게시글 목록 조회 기능
		// 페이징 처리
		int page = 1; // 기본 페이지 번호
		int pageSize = 3; // 한 페이지 당 보여질 게시글의 수
		
		try {
			String pagestr = request.getParameter("page");
			if(pagestr != null) {
				page = Integer.parseInt(pagestr);
				
			}
		} catch (Exception e) {
			//  유효하지 않은 번호를 마음대로 보낼 경우
			page = 1;
		}
		// pageSize = 3이다.
		// page 1, page 2, page 3 요청 - 동적으로 시작값을 계산하는 산수 공식 넣기
		int offset = (page -1) * pageSize;// offset은 0부터 시작 // 시작 위치 계산(offset 값 계산)
		
		System.out.println("page : " + page);
		System.out.println("offset : " + offset);
		request.getRequestDispatcher("/WEB-INF/views/board/list.jsp").forward(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}

}
