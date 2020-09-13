package mybatis;

public class MyBoardDTO {

   private int idx;//게시물 idx
   private String id;//사용자 아이디
   private String name;//사용자 이름
   private String contents;//게시물 내용
	  
	  //기본 생성자는 만들 필요없음
   //getter setter
	public int getIdx() {
		return idx;
	}
	public void setIdx(int idx) {
		this.idx = idx;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getContents() {
		return contents;
	}
	public void setContents(String contents) {
		this.contents = contents;
	}
	  
	  
}
