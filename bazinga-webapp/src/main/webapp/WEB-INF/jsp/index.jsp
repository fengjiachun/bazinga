<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" href="${ctx}/resources/bootstrap-table.min.css">
<script type="text/javascript"
	src="${ctx}/resources/jquery-1.11.1.min.js" /></script>
<script type="text/javascript"
	src="${ctx}/resources/bootstrap-table.min.js"></script>
<link rel="stylesheet" href="${ctx}/resources/bootstrap.css">
<script type="text/javascript" src="${ctx}/resources/bootstrap.js"></script>
<link rel="stylesheet" href="${ctx}/resources/starter-template.css">
<title>Bazinga RPC</title>
</head>
<body>
	<nav class="navbar navbar-inverse navbar-fixed-top">
	<div class="container">
		<div class="navbar-header">
			<button type="button" class="navbar-toggle collapsed"
				data-toggle="collapse" data-target="#navbar" aria-expanded="false"
				aria-controls="navbar">
				<span class="sr-only">Toggle navigation</span> <span
					class="icon-bar"></span> <span class="icon-bar"></span> <span
					class="icon-bar"></span>
			</button>
			<a class="navbar-brand" href="#">Bazinga RPC</a>
		</div>
		<div id="navbar" class="collapse navbar-collapse">
			<ul class="nav navbar-nav">
				<li class="active"><a href="#">Home</a></li>
				<li><a href="#about">About</a></li>
				<li><a href="#contact">Contact</a></li>
			</ul>
		</div>
		<!--/.nav-collapse -->
	</div>
	</nav>

	<div class="container">

		<div class="starter-template">
			<div class="col-lg-6 input-group">
				<input type="text" id="searchIpt" class="form-control"> <span
					class="input-group-btn">
					<button class="btn btn-default" id="searchBtn" type="button">检索</button>
				</span>
			</div>
		</div>

		<div class="page-container">
			<div style="height: 500px">
				<table class="table table-bordered table-striped" id="searchTable">
				</table>
			</div>
		</div>

	</div>


</body>

<script type="text/javascript">

$(function(){
	
	$("#searchBtn").on("click",function(){
		 var serviceName = $("#searchIpt").val();
		 if(serviceName.length > 0){
			 console.info("search");
			 initFailHistoryBootstrapTable();
		 }
		
	});
});

	function initFailHistoryBootstrapTable(){
		 $("#searchTable").bootstrapTable({
			 method:'get',
			 url:"${ctx}/bazinga/search",
			 cache: false, 
			 striped: true,
			 pagination: true,
			 pageList: [10,20,50],
			 pageSize:10,
			 pageNumber:1,
			 queryParams: function(params){
				    var serviceName = $("#searchIpt").val();
			 		paramsReturn = {
						limit: params.limit,
						offset: params.offset,
						serviceName:serviceName
					};
				 	return paramsReturn;
			 },
			 sidePagination:'server',
			 contentType: "application/x-www-form-urlencoded",
			 showColumns: true, 
			 smartDisplay:true,
			 columns: [
	          {  
					field: 'id',  
					title: '编号',  
					align: 'center',  
					width: '40',  
					valign: 'middle',	
	          },
	          {  
	        	  	field: 'name', 
	          	    title: '服务名',  
					align: 'center',  
					width: '240',  
					valign: 'middle', 
	          },
	          {
					title: '用户操作',  
					align: 'center',  
					width: '120',  
					valign: 'middle', 
					formatter: function(value,row,index){
		        		 var str = "";
		            		 str += '<button class="btn btn-secondary btn-xs"><i class="fa fa-edit"></i><span>查看</span></button>';
		            	 return str;
	   			}
	          },
		     ]
		 });
	}
	
</script>
</html>