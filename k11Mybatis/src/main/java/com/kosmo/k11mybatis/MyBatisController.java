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
import org.springframework.web.servlet.ModelAndView;

import mybatis.MemberVO;
import mybatis.MyBoardDTO;
import mybatis.MybatisDAOImpl;
import mybatis.MybatisMemberImpl;
import mybatis.ParameterDTO;
import util.EnvFileReader;
import util.PagingUtil;

@Controller
public class MyBatisController {

	/*
	 servlet-context.xml 에서 생성한 빈을 자동으로 주입받아 Mybatis를 사용할 준비를 한다.
	 @Autowired 는 타입만 일치하면 자동으로 주입받을 수 있다. 
	 */
    @Autowired
    private SqlSession sqlSession;
	
    //방명록 리스트
	@RequestMapping("/mybatis/list.do")
	public String list(Model model,HttpServletRequest req) {
		/*
		 map 보다 DTO 가 편리한 이유
		 
		  파라미터 처리를 위한 DTO 객체는 계속 쓸 수 있지만 map 은 생성후 다시 버려야한다.
		  따라서 DTO 를 쓰는것이 재활용하기 좋다.
		 */
		
		//파라미터 저장을 위한 map 생성 -> 진슬버전
		Map<String,Object> map = new HashMap<String, Object>();   
		//파라미터 저장을 위한 DTO 객체 생성 -> 쌤버전
		ParameterDTO parameterDTO = new ParameterDTO(); 
		
		
		//페이지 처리를 위한 설정값
		int pageSize = Integer.parseInt(EnvFileReader.getValue("SpringBbsInit.properties", "springBoard.pageSize"));
		int blockPage = Integer.parseInt(EnvFileReader.getValue("SpringBbsInit.properties", "springBoard.blockPage"));
		
		int nowPage = req.getParameter("nowPage")==null ? 1: Integer.parseInt(req.getParameter("nowPage"));

		//현재 페이지에 대한 파라미터 처리 및 시작/끝의 rownum 구하기
		int start = (nowPage-1) * pageSize +1;
		int end = nowPage * pageSize;
		
		//위에서 계산한 start,  end 를 DTO or MAP 에 저장하기 
		map.put("start",start);
		map.put("end",end);
		
		parameterDTO.setStart(start);
		parameterDTO.setEnd(end);
		
		
		String addQueryString = "";
		String  searchField = req.getParameter("searchField");
		String  searchTxt = req.getParameter("searchTxt");
		
		if(searchTxt!=null) {
			addQueryString = String.format("searchField=%s&searchTxt=%s&", searchField,searchTxt);
			map.put("searchField",searchField);
			map.put("searchTxt",searchTxt);
			
			parameterDTO.setSearchField(searchField);
			parameterDTO.setSearchTxt(searchTxt);
			System.out.println("getSearchTxt : "+ parameterDTO.getSearchTxt());
		}
		
		//map 버전
		//int totalRecordCount =  sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount(map);
		int totalRecordCount =  sqlSession.getMapper(MybatisDAOImpl.class).getTotalCount(parameterDTO);
		
		//전체페이지수 계산
		int totalPage = (int)Math.ceil((double)totalRecordCount/pageSize);
		
		//리스트 페이지에 출력할 게시물 가져오기 //4
		//map버전
		//ArrayList<MyBoardDTO> lists = sqlSession.getMapper(MybatisDAOImpl.class).listPage(map);
		ArrayList<MyBoardDTO> lists = sqlSession.getMapper(MybatisDAOImpl.class).listPage(parameterDTO);
		
		
		//레코드에 대한 가공을 위해 foreach 문으로 반복
		for (MyBoardDTO dto : lists) {
			
			//내용에 대해 줄바꿈 처리
			String temp = dto.getContents().replace("\r\n", "<br/>");
			dto.setContents(temp);
		}
		
		//페이지 번호에 대한 처리
		String pagingImg = PagingUtil.pagingImg(totalRecordCount, pageSize, blockPage, nowPage,
				req.getContextPath()+"/mybatis/list.do?"+addQueryString);
		model.addAttribute("pagingImg",pagingImg);
		
		//model 객체에 저장
		model.addAttribute("lists",lists);
		return "07Mybatis/list";
	}
	
	
	@RequestMapping("/mybatis/write.do")
	public String write(Model model , HttpSession session, HttpServletRequest req) {
		
		//글쓰기 페이지로 진입시 세션영역에 데이터가 없다면 로그인 페이지로 이동
		if(session.getAttribute("siteUserInfo")==null) {
			/*
			 로그인에 성공할 경우 글쓰기 페이지로 이동하기 위해 
			 돌아갈 경로를 아래와 같이 저장함
			 */
			model.addAttribute("backUrl","07Mybatis/write");
			return "redirect:login.do";
		}
		return "07Mybatis/write";
	}
	
	@RequestMapping("/mybatis/login.do")
	public String login(Model model ) {
		return "07Mybatis/login";
	}
	
	
	@RequestMapping("/mybatis/loginAction.do")
	public ModelAndView loginAction(Model model , HttpSession session, HttpServletRequest req) {
	
		ModelAndView mv = new ModelAndView();
		
		//로그인 메소드를 호출함
		MemberVO vo =  sqlSession.getMapper(MybatisMemberImpl.class).login(req.getParameter("id"),req.getParameter("pass"));
		
		if(vo==null) {
			//로그인에 실패한 경우 ...
			mv.addObject("LoginNG","아이디/패스워드가 틀렸습니다.");
			mv.setViewName("07Mybatis/login");
			return mv;
		}
		else {
			//로그인에 성공한 경우 세션영역애 VO 객체를 저장한다.
			session.setAttribute("siteUserInfo", vo);
		}
		
		//로그인 후 페이지 이동
		String backUrl = req.getParameter("backUrl");
		if(backUrl==null || backUrl.equals("")) {
			//돌아갈 페이지가 없다면 로그인 페이지로 이동한다.
			mv.setViewName("07Mybatis/login");
		}
		else {
			//지정된 페이지로 이동한다.
			mv.setViewName(backUrl);
		}
		return mv;
	
	}
	
	//글쓰기 처리
	@RequestMapping(value="/mybatis/writeAction.do", method = RequestMethod.POST)
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
	
	//로그아웃
	@RequestMapping("/mybatis/logout.do")
	public String logout(HttpSession session) {
		//세션 영역을 비워준다,
		//invalidate 함수도 있지만 세션영역에 다른 기능을 저장할수 도있기 때문에 
		//로그인 관련된 것만 지워주는 것이 효율적이다.
		session.setAttribute("siteUserInfo", null);
		return "redirect:login.do";
	}
	
	//글 수정하기
	@RequestMapping("/mybatis/modify.do")
	public String modify(Model model , HttpServletRequest req, HttpSession session) {
		
		//로그인 확인
		if(session.getAttribute("siteUserInfo")==null) {
			return "redirect:login.do";
		}
		
		/*
		 여러개의 폼값을 한번에 Mapper 쪽으로 전달하기 위해 DTO 객체를 사용한다
		 해당객체는 Mapper 에서 즉시 사용할수 있다.
		 */
		ParameterDTO parameterDTO = new ParameterDTO();
		parameterDTO.setBoard_idx(req.getParameter("idx"));//일련번호
		parameterDTO.setUser_id(((MemberVO)session.getAttribute("siteUserInfo")).getId());//사용자 아이디
		
		//mybatis 호출시 DTO 객체를 파라미터로 전달
		MyBoardDTO dto = sqlSession.getMapper(MybatisDAOImpl.class).view(parameterDTO);
		
		model.addAttribute("dto",dto);
		
		return "07Mybatis/modify";
	}
	
	/*
	 커맨드 객체를 사용하려면
	 1. 필드명이 동일해야함
	 2. 첫글자는 소문자이며 그 외 문자는  대소문자까지 동일해야함
	 3. 
	 */
	
	//글 수정 처리  하기 ( 수정 처리)
	@RequestMapping("/mybatis/modifyAction.do")
	public String modifyAction(HttpSession session , MyBoardDTO myBoardDTO) {
		
		//로그인 확인
		if(session.getAttribute("siteUserInfo")==null) {
			return "redirect:login.do";
		}
		//커맨드 객체로 폼값을 한번에 받아서 Mapper로 전달함
		int applyRow = sqlSession.getMapper(MybatisDAOImpl.class).modify(myBoardDTO);
		System.out.println("수정 처리 된 레코드 수 :" + applyRow);
		
		return "redirect:list.do";
	}
	
	//글 삭제하기
	@RequestMapping("/mybatis/delete.do")
	public String delete(HttpSession session , HttpServletRequest req) {
		
		//로그인 확인
		if(session.getAttribute("siteUserInfo")==null) {
			return "redirect:login.do";
		}
		int applyRow = sqlSession.getMapper(MybatisDAOImpl.class).delete(
				req.getParameter("idx"),
				((MemberVO)session.getAttribute("siteUserInfo")).getId());
				
		System.out.println("삭제 처리 된 레코드 수 :" + applyRow);
		
		return "redirect:list.do";
	}
	
	
	
	
	
	
	
	
	
	
	
	
}
