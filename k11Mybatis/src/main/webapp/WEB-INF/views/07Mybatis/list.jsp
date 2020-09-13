<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.16.0/umd/popper.min.js"></script>
<script>
	function deleteRow(idx){
		var c = confirm("삭제할까요?");
		if(c){
			location.href = 'delete.do?idx='+idx;
		}
	}
</script>
<title>Insert title here</title>
</head>
<body>
<div class="container">
	<h2>회원제 방명록(한줄게시판)</h2>
	
	
	
	<!-- 글쓰기 버튼 및 로그인 / 로그아웃 버튼-->
	<div class="text-right">
		<c:choose>
			<c:when test="${not empty sessionScope.siteUserInfo }">
				<button class="btn btn-danger" onclick="location.href='logout.do';">
					로그아웃
				</button>
			</c:when>
			<c:otherwise>
				<button class="btn btn-info" onclick="location.href='login.do';">
					로그인
				</button>
			</c:otherwise>
			</c:choose>
		&nbsp;&nbsp;
		<button class="btn btn-success" onclick="location.href='write.do';">
			방명록 쓰기
		</button>
	</div>
		
	<!-- 검색폼 -->
	
	<div class="text-center">
	<form method="get">
		<select name="searchField">
			<option value="contents">내용</option>
			<option value="name">작성자</option>
		</select>
		<input type="text" name="searchTxt" />
		<input type="submit" value="검색" />
	</form>
	</div>
	
		
	<!-- 방명록 반복 부분 s -->
	<c:forEach items="${lists }" var="row">		
		<div class="border mt-2 mb-2">
			<div class="media">
				<div class="media-left mr-3">
					<img src="../images/img_avatar3.png" class="media-object" style="width:60px">
				</div>
				<div class="media-body">
					<h4 class="media-heading">작성자:${row.name }(${row.id })</h4>
					<p>${row.contents }</p>
				</div>	  
				<!--  수정,삭제버튼 -->
				<div class="media-right">
					<!--
						 세션영역의 id와 게시물의 id를 비교한다.
						즉 작성자 본인에게만 수정 /삭제 버튼이 보이게 처리한다.
					 -->
					<c:if test="${sessionScope.siteUserInfo.id eq row.id }">
						<button class="btn btn-success" onclick="location.href='modify.do?idx=${row.idx}';">
							수정
						</button>
						<button class="btn btn-success" onclick="javascript:deleteRow(${row.idx});">
							삭제
						</button>
					</c:if>
				</div>
			</div>
		</div>
	</c:forEach>
	
	<!-- 방명록 반복 부분 e -->
	 <table width="90%">
		<tr class="pagination justify-content-center">
			<td align="center">
				${pagingImg }
			</td>
		</tr>
	</table>
	<%-- <ul class="pagination justify-content-center">
		<li>
		${pagingImg }
		</li>
	</ul> --%>
	
</div>
</body>
</html>