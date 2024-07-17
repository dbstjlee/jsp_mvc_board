package com.tenco.tboard.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import com.tenco.tboard.model.Board;
import com.tenco.tboard.model.Comment;
import com.tenco.tboard.model.User;
import com.tenco.tboard.repository.BoardRepositoryImpl;
import com.tenco.tboard.repository.CommentRepositoryImpl;
import com.tenco.tboard.repository.interfaces.BoardRepository;
import com.tenco.tboard.repository.interfaces.CommentRepository;
import com.tenco.tboard.util.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * html <a></a> 태그는 GET 방식!!(POST으로 보내면 인식 못함)
 */
@WebServlet("/board/*")
public class BoardController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private BoardRepository boardRepository;
	private CommentRepository commentRepository;

	@Override
	public void init() throws ServletException {
		boardRepository = new BoardRepositoryImpl();
		commentRepository = new CommentRepositoryImpl();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		// HttpSession이 존재하면 현재 HttpSession을 반환하고, 존재하지 않으면 새로이 생성하지 않고 그냥 null을 반환
		if (session == null || session.getAttribute("principal") == null) {
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
		case "/updateComment":
			handleUpdateComment(request, response, session);
		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

	/**
	 * 댓글 수정 기능
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException 
	 */
	private void handleUpdateComment(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
		int boardId = Integer.parseInt(request.getParameter("id"));
		Board board = boardRepository.getBoardById(boardId);
		if(board == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
	}

	/**
	 * 댓글 삭제 기능(GET 방식 처리)
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleDeleteComment(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {
		
		int commentId = Integer.parseInt(request.getParameter("id"));
		Comment comment = commentRepository.getCommentById(commentId);
		
		int boardId = Integer.parseInt(request.getParameter("id"));
		Board board = boardRepository.getBoardById(boardId);
		
		if(board == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		User user = (User) session.getAttribute("principal");
		if (user != null) {
			request.setAttribute("userId", user.getId());
		}
		
		commentRepository.deleteComment(commentId);
		response.sendRedirect(request.getContextPath()+ "/board/view?id="+ boardId);
		//response.sendRedirect(request.getContextPath() + "/board/list");
	}

	/**
	 * 상세 보기 화면 이동(GET 방식 처리)
	 * 
	 * @param request
	 * @param response
	 * @param session
	 */
	private void showViewBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session) {

		try {
			int boardId = Integer.parseInt(request.getParameter("id"));
			Board board = boardRepository.getBoardById(boardId);
			if (board == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}

			// 현재 로그인한 사용자의 ID
			User user = (User) session.getAttribute("principal");
			if (user != null) {
				request.setAttribute("userId", user.getId());
			}

			// 댓글 조회 및 권한 확인
			List<Comment> commentList = commentRepository.getCommentsByBoardId(boardId);
			request.setAttribute("board", board);
			request.setAttribute("commentList", commentList);

			request.getRequestDispatcher("/WEB-INF/views/board/view.jsp").forward(request, response);

		} catch (Exception e) {
			
		}
	}

	/**
	 * 수정 폼 화면 이동(인증 검사 반드시 처리)
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 * @throws ServletException
	 */
	private void showEditBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		int boardId = Integer.parseInt(request.getParameter("id"));
		Board board = boardRepository.getBoardById(boardId);
		if (board == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		request.setAttribute("board", board);
		// .jsp로 값을 보내려면 setAttribute로 값을 먼저 설정해야 한다. 
		// 값을 받은 .jsp 파일에서는 EL 표현식으로 value="${board.title} 이렇게 값을 불러오도록 한다.
		
		// 경로 보내는 방법 2가지(getRequestDispatcher, sendRedirect)
		request.getRequestDispatcher("/WEB-INF/views/board/edit.jsp").forward(request, response);
		//response.sendRedirect(request.getContextPath() + "/board/edit?id=" + boardId);

	}

	/**
	 * 게시글 삭제 기능 만들기
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 * @throws ServletException
	 */

	private void handleDeleteBoard(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {
		
		int boardId = Integer.parseInt(request.getParameter("id"));
		Board board = boardRepository.getBoardById(boardId);
		boardRepository.deleteBoard(boardId);
		response.sendRedirect(request.getContextPath() + "/board/list");
	}

	/**
	 * 게시글 생성 화면 이동
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @throws IOException
	 * @throws ServletException
	 */
	private void showCreateBoardForm(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {

		request.getRequestDispatcher("/WEB-INF/views/board/create.jsp").forward(request, response);
	}

	/**
	 * 페이징 처리 하기
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	private void handleListBoards(HttpServletRequest request, HttpServletResponse response, HttpSession session)
			throws ServletException, IOException {
		int page = 1; // 기본 페이지 번호
		int pageSize = 3; // 한 페이지당 보여질 게시글의 수

		try {
			String pageStr = request.getParameter("page"); // Parameter는 setAttribute와 다름.
			// list.jsp 내의 <a
			// href="${pageContext.request.contextPath}/board/list?page=${i}">${i}</a>에서
			// ?(쿼리 스트링)으로 page를 셋팅한 것과 마찬가지임.
			// => setter 없이 getParameter를 할 수 있음.
			if (pageStr != null) {
				page = Integer.parseInt(pageStr);
			}
		} catch (Exception e) {
			page = 1;
		}

		int offset = (page - 1) * pageSize; // 시작 위치 계산( offset 값 계산) // 0, 3, 6, 9
		List<Board> boardList = boardRepository.getAllBoards(pageSize, offset);

		// 전체 게시글 수 조회
		int totalBoards = boardRepository.getTotalBoardCount();
		// 총 페이지 수 계산 --> [1][2][3][4]
		int totalPages = (int) Math.ceil((double) totalBoards / pageSize);
		// ((double)totalBoards / pageSize) 와 (double)(totalBoards / pageSize)는 다름
		// 예) 10.0/3 -> totalBoards가 double 이어서 pageSize도 자동으로 double 로 변형됨. -> 10.0/3.0
		// = 3.333..
		// (double)(totalBoards / pageSize)는 이미 연산된 것을 double로 변환되는 것.
		// 예) 10/3 은 각각 int => 3 -> double => 3.0

		// Math.ceil : 소수점 올림

		request.setAttribute("boardList", boardList);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("currentPage", page);

		// 현재 로그인한 사용자 ID 설정
		if (session != null) {
			User user = (User) session.getAttribute("principal");
			if (user != null) {
				request.setAttribute("userId", user.getId());
			}
		}
		request.getRequestDispatcher("/WEB-INF/views/board/list.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getPathInfo();
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("principal") == null) {
			response.sendRedirect(request.getContextPath() + "/user/signin");
			return;
		}

		//response.setContentType("text/html;charset=UTF-8");

		

		switch (action) {
		case "/create":
			handleCreateBoard(response, request, session);
			break;
		case "/edit":
			handleEditBoard(response, request, session);
			break;

		case "/addComment":
			handleAddComment(response, request, session);
			break;

		default:
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			break;
		}
	}

	/**
	 * 게시글 수정 기능
	 * @param response
	 * @param request
	 * @param session
	 * @throws IOException 
	 */
	private void handleEditBoard(HttpServletResponse response, HttpServletRequest request, HttpSession session) throws IOException {
		int boardId = Integer.parseInt(request.getParameter("boardId"));
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		
		Board board = Board.builder()
					  .id(boardId)
					  .title(title)
					  .content(content)
					  .build();
		boardRepository.updateBoard(board);
		
		response.sendRedirect(request.getContextPath() + "/board/list");
	}

	/**
	 * 댓글 추가 기능
	 * 
	 * @param request
	 * @param session
	 * @throws IOException
	 */
	private void handleAddComment(HttpServletResponse response, HttpServletRequest request, HttpSession session)
			throws IOException {
		
		// 데이터 추출
		int boardId = Integer.parseInt(request.getParameter("boardId"));
		String content = request.getParameter("content");
		User user = (User) session.getAttribute("principal");

		// 데이터 저장 기능
		Comment comment = Comment
				.builder()
				.userId(user.getId())
				.boardId(boardId)
				.username(user.getUsername())
				.content(content)
				.createdAt(user.getCreatedAt())
				.build();
		commentRepository.addComment(comment);

		// 응답 처리
		response.sendRedirect(request.getContextPath() + "/board/view?id=" + boardId);
	}

	/**
	 * 게시글 생성 처리
	 * 
	 * @param response
	 * @param request
	 * @param session
	 * @throws IOException
	 */
	private void handleCreateBoard(HttpServletResponse response, HttpServletRequest request, HttpSession session)
			throws IOException {

		// 유효성 검사 생략
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		User user = (User) session.getAttribute("principal");

		Board board = Board.builder().userId(user.getId()).title(title).content(content).build();
		boardRepository.addBoard(board);
		response.sendRedirect(request.getContextPath() + "/board/list?page=1");

	}

}