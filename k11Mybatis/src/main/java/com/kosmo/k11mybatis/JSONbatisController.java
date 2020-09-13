package com.kosmo.k11mybatis;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import mybatis.MemberVO;
import mybatis.MyBoardDTO;
import mybatis.MybatisDAOImpl;
import mybatis.MybatisMemberImpl;
import mybatis.ParameterDTO;
import oracle.net.aso.s;
import util.EnvFileReader;
import util.PagingUtil;

@Controller
public class JSONbatisController {
	
	//MyBatis 를 사용하기 위해 빈 자동주입
	@Autowired
	private SqlSession sqlSession;
	
	//방명록 게시판의 틀이 되는 페이지
	@RequestMapping("/mybatisJSON/list.do")
	public String board() {
		
		return "08JSON/board";
	}
	
	/*
	 방명록 게시판의 실제 반복리스트 부분을 출력함
	 해당 페이지는 위 list.do로 Ajax 를 통해 load 되어 출력됨
	 */
	@RequestMapping("/mybatisJSON/aList.do")
	public String alist(Model model,HttpServletRequest req) {
		
		//파라미터 저장을 위한 DTO 객체 생성 -> 쌤버전
		ParameterDTO parameterDTO = new ParameterDTO(); 
		String searchField =  req.getParameter("searchField");		
		String searchTxt =  req.getParameter("searchTxt");		
		
		String addQueryString = "";
		
		
		//페이지 처리를 위한 설정값
		int pageSize = Integer.parseInt(EnvFileReader.getValue("SpringBbsInit.properties", "springBoard.pageSize"));
		int blockPage = Integer.parseInt(EnvFileReader.getValue("SpringBbsInit.properties", "springBoard.blockPage"));
		
		int nowPage = req.getParameter("nowPage")==null ? 1: Integer.parseInt(req.getParameter("nowPage"));

		//현재 페이지에 대한 파라미터 처리 및 시작/끝의 rownum 구하기
		int start = (nowPage-1) * pageSize +1;
		int end = nowPage * pageSize;
		
		parameterDTO.setStart(start);
		parameterDTO.setEnd(end);
		
		if(searchTxt!=null) {
			addQueryString = String.format("searchField=%s&searchTxt=%s&", searchField,searchTxt);
			
			parameterDTO.setSearchField(searchField);
			parameterDTO.setSearchTxt(searchTxt);
			System.out.println("getSearchTxt : "+ parameterDTO.getSearchTxt());
		}
		
		
		
		int totalRecordCount =  sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount(parameterDTO);
		
		//전체페이지수 계산
		int totalPage = (int)Math.ceil((double)totalRecordCount/pageSize);
		
		//리스트 페이지에 출력할 게시물 가져오기 //4
		ArrayList<MyBoardDTO> lists = sqlSession.getMapper(MybatisDAOImpl.class).listPage(parameterDTO);
		
		
		//레코드에 대한 가공을 위해 foreach 문으로 반복
		for (MyBoardDTO dto : lists) {
			
			//내용에 대해 줄바꿈 처리
			String temp = dto.getContents().replace("\r\n", "<br/>");
			dto.setContents(temp);
		}
		
		//페이지 번호에 대한 처리 : Ajax 로 처리하기 위해 메소드 변경
				String pagingImg = PagingUtil.pagingAjax(totalRecordCount, pageSize, blockPage, nowPage,
						req.getContextPath()+"/mybatisJSON/list.do?"+addQueryString);
				model.addAttribute("pagingImg",pagingImg);
		//model 객체에 저장
		model.addAttribute("lists",lists);	
		
		return "08JSON/aList";
	}
	
	@RequestMapping("/mybatisJSON/login.do")
	public String login() {
		
		return "08JSON/login";
	}
	
	
	@ResponseBody
	@RequestMapping("/mybatisJSON/loginAction.do")
	public Map<String, Object> loginAction(HttpServletRequest req, HttpSession session) {
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		MemberVO vo = sqlSession.getMapper(MybatisMemberImpl.class).login(req.getParameter("id"),req.getParameter("pass"));
		
		if(vo==null) {
			//로그인 실패
			map.put("loginResult", 0);
			map.put("loginMessage", "로그인 실패");
		}
		else {
			//로그인에 성공한 경우 Session 영역에 속성저장
			session.setAttribute("siteUserInfo", vo);
			map.put("loginResult", 1);
			map.put("loginMessage", "로그인 성공");
			
		}
		
		return map;
	}
	
	//로그아웃
	@RequestMapping("/mybatisJSON/logout.do")
	public String logout(HttpSession session) {
		
		//session.invalidate();
		session.setAttribute("siteUserInfo", null);
		
		return "redirect:login.do";
	}
	
	//글쓰기 폼
	@RequestMapping("/mybatisJSON/write.do")
	public String write(Model model , HttpSession session, HttpServletRequest req) {
		
		//글쓰기 페이지로 진입시 세션영역에 데이터가 없다면 로그인 페이지로 이동
		if(session.getAttribute("siteUserInfo")==null) {
			/*
			 로그인에 성공할 경우 글쓰기 페이지로 이동하기 위해 
			 돌아갈 경로를 아래와 같이 저장함
			 */
			model.addAttribute("backUrl","08JSON/write");
			return "redirect:login.do";
		}
		return "08JSON/write";
	}
	
	
	//글쓰기 처리
	@RequestMapping(value="/mybatisJSON/writeAction.do", method = RequestMethod.POST)
	public String writeAction(Model model, HttpServletRequest req, HttpSession session) {
		
		//세션 영역에 사용자 정보가 있는지 확인
		if(session.getAttribute("siteUserInfo")==null) {
			//로그인이 해제된 상태라면 로그인 페이지로 이동한다.
			return "redirect:login.do";
		}
		
		//Mybatis 사용 
		sqlSession.getMapper(MybatisDAOImpl.class).write(
				req.getParameter("name"),req.getParameter("contents"),
				//session 영역에서 아이디를 가져와서 memverVO 에 저장
					((MemberVO)session.getAttribute("siteUserInfo")).getId());
		/*
		 세션 영역에 저장된 MemberVO 객체에서 아이디 가져오기 
		 1. Object 타입으로 저장된 VO 객체를 가져온다.
		 2. MemberVO 타입으로 형변환한다.
		 3. 형변환된 객체를 통해 getter() 를 호출하여 아이디를 얻어온다.
		 
		 */
		
		//글 작성이 완료되면 리스트로 이동한다.
		return "redirect:list.do";
	}
	
	//삭제 처리의 결과를 JSON으로 반환 
	@RequestMapping("/mybatisJSON/deleteAction.do")
	@ResponseBody
	public Map<String, Object> deleteAction(HttpServletRequest req, HttpSession session){
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		//로그인 되었는지 확인
		if(session.getAttribute("siteUserInfo")==null) {
			map.put("statusCode", 1);
			return map;
		}
		
		//mapper 호출 -> 삭제 처리
		int result = sqlSession.getMapper(MybatisDAOImpl.class).delete(req.getParameter("idx"),
				((MemberVO)session.getAttribute("siteUserInfo")).getId());
		
		/*
		 Mybatis 사용시 delete 쿼리를 실행하게되면 처리후 실제 삭제된
		 행의 갯수를 반환한다.
		 이를 통해 컨트롤러에서는 성공/실패 여부를 판단할 수 있다.
		 */
		if(result<=0) {
			//삭제 실패시 0
			map.put("statusCode", 0);
		}
		else {
			//삭제 성공시 2
			map.put("statusCode", 2);
		}
		
		
		return map;
	}
	
	
	
	
	
	
	
	
	
	
	

}
