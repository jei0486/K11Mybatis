package mybatis;

import org.apache.ibatis.annotations.Param;

public interface MybatisMemberImpl {
	
	//로그인 처리
	public MemberVO login(String id, String pass);
	
	
		

}
