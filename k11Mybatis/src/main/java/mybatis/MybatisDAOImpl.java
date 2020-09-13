package mybatis;

import java.util.ArrayList;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

//@Service 는 없어도 되지만 표현을 위해 넣어줌
@Service
public interface MybatisDAOImpl {

	/*
	 방명록 리스트에서 사용할 메소드를 추상메소드로 정의함
	 아래 추상메소드를 통해 컨트롤러는 Mapper 의 각엘리먼트를 호출하게 된다.
	 */
	
	//검색 기능 추가전
	//public int getTotalCount();
	//public ArrayList<MyBoardDTO> listPage(int s, int e);
	
	//검색 기능 추가후 : 파라미터를 저장한 DTO 객체를 매개변수로 받음
	
	//!!!map 버전
	//public int getTotalCount(Map<String, Object> map);
	//public ArrayList<MyBoardDTO> listPage(Map<String, Object> map);
	
	//!!!!DTO 버전
	public int getTotalCount(ParameterDTO parameterDTO);
	public ArrayList<MyBoardDTO> listPage(ParameterDTO parameterDTO);
	
	
	/*
	 방명록 글쓰기
	 
	  파라미터 전달시 Mapper 에서 즉시 사용할 이름을 지정하고 싶을때
	 @Param 어노테이션을 사용한다. 
	  아래와 같이 지정하면 Mapper 에서 #{_name} 과 같이 사용할 수 있다.
	  
	  mybatice 에서 파라미터를 전달하는 세번째 방식
	 */
	public void write(@Param("_name")String name,@Param("_contents")String contents,@Param("_id")String id);

	//수정폼 로딩하기
	public MyBoardDTO view(ParameterDTO parameterDTO);
	
	//수정처리하기
	public int modify(MyBoardDTO myBoardDTO);
	
	//삭제
	public int delete(String idx,String id);
	
	
		
	

}
