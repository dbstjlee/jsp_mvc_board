package com.tenco.tboard.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.tenco.tboard.model.Board;
import com.tenco.tboard.repository.interfaces.BoardRepository;
import com.tenco.tboard.util.DBUtil;

public class BoardRepositoryImpl implements BoardRepository {

	private static final String SELECT_ALL_BOARDS = " SELECT * FROM board ORDER BY created_at DESC limit ? offset ? ";
	private static final String COUNT_ALL_BOARDS = " SELECT count(*) AS count FROM board ";
	private static final String INSERT_BOARD_SQL = " INSERT INTO board(user_id, title, content) values(?, ?, ?) ";
	private static final String DELETE_BOARD_SQL = " DELETE FROM board WHERE id = ? ";
	private static final String SELECT_BOARD_BY_ID = " SELECT * FROM board WHERE id = ? ";
	private static final String UPDATE_BOARD_SQL = " UPDATE board SET title = ?, content = ? WHERE id = ? ";
	
	
	/**
	 * 게시글 등록(유저id, 제목, 내용)
	 */
	@Override
	public void addBoard(Board board) {
		// 논리적인 하나의 작업의 단위
		try (Connection conn = DBUtil.getConnetion()){
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt = conn.prepareStatement(INSERT_BOARD_SQL)){
				pstmt.setInt(1, board.getUserId());	
				pstmt.setString(2, board.getTitle());	
				pstmt.setString(3, board.getContent());	
				pstmt.executeUpdate();
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 게시글 수정(게시글 id 기준으로 제목, 내용 수정)
	 */
	@Override
	public void updateBoard(Board board) {
		try (Connection conn = DBUtil.getConnetion()){
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt = conn.prepareStatement(UPDATE_BOARD_SQL)){
				pstmt.setString(1, board.getTitle());	
				pstmt.setString(2, board.getContent());	
				pstmt.setInt(3, board.getId());	
				pstmt.executeUpdate();
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 게시글 삭제(게시글 id 기준으로 삭제)
	 */
	@Override
	public void deleteBoard(int id) {
		try (Connection conn = DBUtil.getConnetion()){
			conn.setAutoCommit(false);
			try (PreparedStatement pstmt = conn.prepareStatement(DELETE_BOARD_SQL)){
				pstmt.setInt(1, id);
				pstmt.executeUpdate();
				conn.commit();
			} catch (Exception e) {
				conn.rollback();
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 게시글 id 기준으로 게시글 조회(단일행)
	 */
	@Override
	public Board getBoardById(int id) {
		Board board = null;
		try (Connection conn = DBUtil.getConnetion();
			PreparedStatement pstmt = conn.prepareStatement(SELECT_BOARD_BY_ID) ){ 
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()){
				if (rs.next()) {
					board = Board.builder()
							.id(rs.getInt("id"))
							.userId(rs.getInt("user_id"))
							.title(rs.getString("title"))
							.content(rs.getString("content"))
							.createdAt(rs.getTimestamp("created_at"))
							.build();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return board;
	}

	/**
	 * 모든 게시글 조회(한 페이지당 게시글 개수 제한)
	 */
	@Override
	public List<Board> getAllBoards(int limit, int offset) {
		List<Board> boardList = new ArrayList<>();
		try (Connection conn = DBUtil.getConnetion();
			PreparedStatement pstmt = conn.prepareStatement(SELECT_ALL_BOARDS)) {
			pstmt.setInt(1, limit);
			pstmt.setInt(2, offset);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				boardList.add(
						Board.builder()
						.id(rs.getInt("id"))
						.userId(rs.getInt("user_id"))
						.title(rs.getString("title"))
						.content(rs.getString("content"))
						.createdAt(rs.getTimestamp("created_at"))
						.build());
			}
			System.out.println("BoardRepositoryImpl - 로깅 : count " + boardList.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return boardList;
	}

	/** 
	 * 게시글 개수
	 */
	@Override
	public int getTotalBoardCount() {
		int totalBoards = 0;
		try (Connection conn = DBUtil.getConnetion();
			PreparedStatement pstmt = conn.prepareStatement(COUNT_ALL_BOARDS)) {
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				totalBoards = rs.getInt("count");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(" 로깅 totalCount : " + totalBoards);
		return totalBoards;
	}
}