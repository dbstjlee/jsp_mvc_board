package com.tenco.tboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

import com.tenco.tboard.model.Board;
import com.tenco.tboard.model.User;
import com.tenco.tboard.repository.BoardRepositoryImpl;
import com.tenco.tboard.repository.interfaces.BoardRepository;
import com.tenco.tboard.repository.interfaces.UserRepository;

@WebServlet("/board/*")
public class BoardController extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private BoardRepository boardRepository;
	
	@Override
	public void init() throws ServletException {
		boardRepository = new BoardRepositoryImpl();
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		// HttpSession이 존재하면 현재 HttpSession을 반환하고, 존재하지 않으면 새로이 생성하지 않고 그냥 null을 반환
		if(session == null || session.getAttribute("principal") == null ) {
			// 로그인 안 되어 있는 상태
			response.sendRedirect(request.getContextPath() + "/user/signin");
			return;
		}
		
		switch (action) {
		case "/delete":
			handleDeleteBoard(request, response, session);
			break;
		case "/update":
			showEditBoardForm(request, response, session);
			break;
		case "/create":
			showCreateBoardForm(request, response, session);  
			break;
		case "/list":
			handleListBoards(request, response, session);
			break;
		case "/view":
			showViewBoard(request, response, session);
			break;
		case "/deleteComment":
			handleDeleteComment(request, response, session);
			break;
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
	
	/**
	 * 댓글 삭제 기능(GET 방식 처리)
	 * @param request
	 * @param response
	 * @param session
	 */
	private void handleDeleteComment(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		
	}

	/** 
	 * 상세 보기 화면 이동(GET 방식 처리)
	 * @param request
	 * @param response
	 * @param session
	 */
	private void showViewBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		
		try {
			int id = Integer.parseInt(request.getParameter("id"));
			Board board = boardRepository.getBoardById(id);
			 if (board == null) {
				 response.sendError(HttpServletResponse.SC_NOT_FOUND);
				 return;
			 }
			 request.setAttribute("board", board);
			 
			 // 현재 로그인한 사용자의 ID
			 User user =  (User) session.getAttribute("principal");
			 if(user != null) {
				 request.setAttribute("userId", user.getId());
			 }
			 
			 // TODO - 댓글 조회 및 권한 확인 추가 예정
			 request.getRequestDispatcher("/WEB-INF/views/board/view.jsp").forward(request, response);
			 
			 
		} catch (Exception e) {
			//잘못된 접근입니다. back()
		}
	}

	/**
	 * 수정 폼 화면 이동(인증 검사 반드시 처리)
	 * @param request
	 * @param response
	 * @param session
	 */
	private void showEditBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		
		
	}

	/**
	 * 게시글 삭제 기능 만들기
	 * @param request
	 * @param response
	 * @param session
	 */
	
	private void handleDeleteBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		
		
	}

	/**
	 * 게시글 생성 화면 이동
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void showCreateBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
		request.getRequestDispatcher("/WEB-INF/views/board/create.jsp").forward(request, response);
	}

	/**
	 * 페이징 처리 하기 
	 * @param request
	 * @param response
	 * @throws IOException 
	 * @throws ServletException 
	 */
	private void handleListBoards(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
		int page = 1; // 기본 페이지 번호 
		int pageSize = 3; // 한 페이지당 보여질 게시글의 수  
		
		try {
			 String pageStr = request.getParameter("page"); // Parameter는 setAttribute와 다름.
			 // list.jsp 내의 <a href="${pageContext.request.contextPath}/board/list?page=${i}">${i}</a>에서 ?(쿼리 스트링)으로 page를 셋팅한 것과 마찬가지임.
			 // => setter 없이 getParameter를 할 수 있음.
			 if(pageStr != null ) {
				 page = Integer.parseInt(pageStr);
			 }
		} catch (Exception e) {
			page = 1; 
		}
		
		int offset = (page - 1) * pageSize; // 시작 위치 계산( offset 값 계산) // 0, 3, 6, 9
 		List<Board> boardList =  boardRepository.getAllBoards(pageSize, offset);
		
		// 전체 게시글 수 조회 
		int totalBoards = boardRepository.getTotalBoardCount();
		// 총 페이지 수 계산 -->  [1][2][3][4]
		int totalPages = (int) Math.ceil((double)totalBoards / pageSize); 
		// ((double)totalBoards / pageSize) 와 (double)(totalBoards / pageSize)는 다름
		// 예) 10.0/3 -> totalBoards가 double 이어서 pageSize도 자동으로 double 로 변형됨. -> 10.0/3.0 = 3.333..
		// (double)(totalBoards / pageSize)는 이미 연산된 것을 double로 변환되는 것. 
		// 예) 10/3 은 각각 int => 3 -> double => 3.0 
		
		// Math.ceil : 소수점 올림
		
		request.setAttribute("boardList", boardList);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("currentPage", page);
		
		// 현재 로그인한 사용자 ID 설정 
		if(session != null) {
 			 User user = (User)session.getAttribute("principal");
 			 if(user != null) {
 				 request.setAttribute("userId", user.getId());
 			 }
		}
		request.getRequestDispatcher("/WEB-INF/views/board/list.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		if(session == null || session.getAttribute("principal") == null ) {
			response.sendRedirect(request.getContextPath() + "/user/signin");
			return;
		}
		
		switch (action) {
		case "/create":
			handleCreateBoard(response, request, session);
			break;
		case "/list":
			break;
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}
	
/**
 * 게시글 생성 처리
 * @param response
 * @param request
 * @param session
 * @throws IOException 
 */
	private void handleCreateBoard(HttpServletResponse response, HttpServletRequest request, HttpSession session) throws IOException {
		
		// 유효성 검사 생략
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		User user = (User) session.getAttribute("principal");
		
		Board board = Board.builder()
					.userId(user.getId())
					.title(title)
					.content(content)
					.build();
		boardRepository.addBoard(board);
		response.sendRedirect(request.getContextPath() + "/board/list?page=1");
		
	}

}