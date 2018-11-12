<%@ page language="java" import="java.util.*" import="com.myehr.common.util.LangUtil" pageEncoding="UTF-8"%>
<%@ include file="/common/common.jsp" %>
<%
	String nodeId = request.getParameter("nodeId");
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>任务编辑</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	<link href="${pageContext.request.contextPath }/jsp/task/addTask/css/meeting-style.css" rel="stylesheet">
	<link href="${pageContext.request.contextPath }/jsp/task/taskManage/css/animate.css" rel="stylesheet">
	<script src="${pageContext.request.contextPath }/jsp/task/taskManage/js/content.js?v=1.0.0"></script>

  </head>
<body class="gray-bg" style="text-align:center">
<div class="wrapper wrapper-content">
<div class="row animated fadeInDown">
<form id="form_1881" name="form_1881" >
<div class="container-fluid">
	<div style="margin-top:5px;">
		<input id="SYS_SYSTEM_SCHEME.sysMeetingTaskId" name="sysMeetingTaskId" type="hidden" class="form-control"  style="width:200px;"/>
		<input id="SYS_SYSTEM_SCHEME.sysTaskId" name="sysTaskId" type="hidden" class="form-control"  style="width:200px;"/>
		<input id="SYS_SYSTEM_SCHEME.sysMeetingTaskHostid" name="sysMeetingTaskHostid" type="hidden" class="form-control"  style="width:200px;"/>
		<input id="SYS_SYSTEM_SCHEME.sysMeetingTaskParticipants" name="sysMeetingTaskParticipants" type="hidden" class="form-control"  style="width:200px;"/>
		<div style="border:1px solid #ccc; position:relative;border-radius:5px;margin-bottom:10px">
		<h3 style=" background:#FFF;color:blue; display:block;font-size:12;position:absolute; left:10px; top:-27px;text-align:center"></h3>
		<table style="width:100%;table-layout:fixed;" class="cbs-table"  >
			<tr style="height:65px">
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"会议主题")%>: </lable></td>
				<td style="padding-left:10px;"  align="left"  ><input id="SYS_SYSTEM_SCHEME.sysTaskTitle" name="sysTaskTitle" type="text" class="form-control"  vtype="illegalChar;maxLength:null;minLength:null;" style="width:200px;"/></td>
			</tr>
			<tr style="height:65px">
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"会议开始时间")%>: </lable></td>
				<td style="padding-left:10px;"  align="left"  ><div class='controls input-append date form_date2 input-group' data-date=""  data-link-field="dtp_input2" data-link-format="yyyy-mm-dd" style="float:left;width: 134px;">
					<input type='text' readonly class="form-control " dateType="controls input-append date form_date2" id="SYS_SYSTEM_SCHEME.sysMeetingTaskStart" styleType="dateTime"   style="width:134px;float:left;" name="sysMeetingTaskStart" required="true"  format="yyyy-mm-dd hh:ii" valueformat="yyyy-mm-dd hh:ii" />
						<span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
						<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
					</div>
				</td>
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"会议结束时间")%>: </lable></td>
				<td style="padding-left:10px;"  align="left"  ><div class='controls input-append date form_date2 input-group' data-date=""  data-link-field="dtp_input2" data-link-format="yyyy-mm-dd" style="float:left;width: 134px;">
					<input type='text' readonly class="form-control " dateType="controls input-append date form_date2" id="SYS_SYSTEM_SCHEME.sysMeetingTaskEnd" styleType="dateTime"   style="width:134px;float:left;" name="sysMeetingTaskEnd" required="true"  format="yyyy-mm-dd hh:ii" valueformat="yyyy-mm-dd hh:ii" />
						<span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
						<span class="input-group-addon"><span class="glyphicon glyphicon-th"></span></span>
					</div>
				</td>
			</tr>
			<tr style="height:65px">
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"组织人")%>: </lable></td>
				<td id = "hostid" style="padding-left:10px;"  align="left" colspan="3">
					<div class="meetingUser" style="background-color:#c678b4;"><span style="font-size:46px" onclick="chooseUser('host')">+</span></div>
				</td>
			</tr>
			<tr style="height:65px">
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"参与人")%>: </lable></td>
				<td id = "participants" style="padding-left:10px;"  align="left" colspan="3">
					<div class="meetingUser" style="background-color:#c678b4;"><span style="font-size:46px" onclick="chooseUser('participants')">+</span></div>
				</td>
			</tr>
			<tr style="height:65px">
				<td style="padding-left:10px;"  width="150"  ><lable><%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"资源")%>: </lable></td>
				<td style="padding-left:10px;"  align="left"  ><input id="SYS_SYSTEM_SCHEME.sysMeetingTaskChildren" name="sysMeetingTaskChildren" type="text" class="form-control"  vtype="illegalChar;maxLength:null;minLength:null;" style="width:200px;"/></td>
			</tr>
		</table>
	</div>

	<table style="width:100%;table-layout:fixed;padding-top:5px;padding-right:5px;">
	</table>
	<div style="margin:0 auto; display:inline-block;height: 35px;">
		<div style="margin-right:10px;display:inline-block">
		    <input type="button" id="save_formemp_emp_edit"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"保存")%> onclick="save_formemp_emp_edit_click_1881()"/>
		</div>
		<div style="margin-right:10px;display:inline-block">
		    <input type="button" id="closeemp_emp_edit"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"关闭")%> onclick="closex_1881()"/>
		</div>
	</div>
</div>
</div>
</form>
</div>
</div>
<script>
var classes = $("select");
var parame={
	id:'',
	value:"N",
	width: "100px",  
	height:"45px",
	url:'${pageContext.request.contextPath }/dict/searchSysDictEntryTypeCode.action?userId=${sessionScope.userid}',
	jsonParam:{},
	chang:function (e){
		var aaa = e;
	}
};

$(function () {  
	$('.form_date1').datetimepicker({ 
	 language:  'zh-CN',  
	 format:"yyyy-mm-dd",  
	 weekStart: 1,  
	 todayBtn:  1,  
	 autoclose: 1,  
	 todayHighlight: 1,  
	 startView: 2,  
	 minView: 2,  
	 forceParse: 0,  
	 pickerPosition: "bottom-left"  
	});  
	$('.form_date2').datetimepicker({ 
	 language:  'zh-CN',  
	 format:"yyyy-mm-dd hh:ii",  
	 weekStart: 1,  
	 todayBtn:  1,  
	 autoclose: 1,  
	 todayHighlight: 1,  
	 startView: 2,  
	 minView: 0,  
	 forceParse: 0,  
	 pickerPosition: "bottom-left"  
	});  
	$('.form_date3').datetimepicker({ 
	 language:  'zh-CN',  
	 format:"hh:ii",  
	 weekStart: 1,  
	 todayBtn:  1,  
	 autoclose: 1,  
	 todayHighlight: 1,  
	 startView: 0,  
	 minView: 2,  
	 forceParse: 0,  
	 pickerPosition: "bottom-left"  
	});  
	$('.form_date4').datetimepicker({ 
	 language:  'zh-CN',  
	 format:"yyyy-mm-dd",  
	 weekStart: 1,  
	 todayBtn:  1,  
	 autoclose: 1,  
	 todayHighlight: 1,  
	 startView: 4,  
	 minView: 4,  
	 forceParse: 0,  
	 pickerPosition: "bottom-left"  
	});  
});
	function getdata_1881(buttonId,formId){
		var param = {};
		var data = {buttonId:buttonId,formId:formId,param:{},paramsMap:{}};
		$.map($('[name=form_'+formId+']').serializeArray(), function(item, index){
			var property =item['name'];
			var value = item['value'];
			param[property] = value;
		});
		data.param = param;
		return data;
	}
	function formatDatebox(value,key) {
			var type = document.getElementById(key).getAttribute("dateType");
			type = type.substr(type.length-1,1);
		    var year=value.substr(0,4);
		    var index1=value.indexOf("-");
		    var index2=value.lastIndexOf("-");
		    var d1=parseInt(index2)-(parseInt(index1)+1);
		    var month=value.substr((parseInt(index1)+1),d1);
		    var kg=value.indexOf(" ");
		    d2=parseInt(kg)-parseInt(index2);
		    var day=value.substr(parseInt(index2)+1,d2);
		    var mh=value.indexOf(":");
		    d3=parseInt(mh)-(parseInt(kg)+1);
		    var hh=value.substr(parseInt(kg)+1,d3);
		    var mh2=value.lastIndexOf(":");
		    d4=parseInt(mh2)-(parseInt(mh)+1);
		    var mm=value.substr(parseInt(mh)+1,d4);
		    if(mm!=0){
		    	mm = parseInt(mm)+6;
		    	if(parseInt(mm)<10){
		    		mm = "0"+mm;
		    	}else if(parseInt(mm)==60){
		    		mm="00";
		    		hh=parseInt(hh)+1;
		    		}else if(parseInt(mm)>60){
		    			mm=parseInt(mm)-60;
		    			mm="0"+mm;
		    			hh=parseInt(hh)+1;
		    			}
		    }
		    var mh2=value.lastIndexOf(":");
		    var ss=value.substr(parseInt(mh2)+1);
		    if(type == 1){
			        return year+"-"+month+"-"+day;
		    }else if(type == 2){
			        return year+"-"+month+"-"+day+" "+hh+":"+mm;
		    }else if(type == 3){
			        return hh+":"+mm;
		    }else if(type == 4){
			        return year;
		    }else if(type == 5){
			        return year+month;
		    }
	}
	var _formId_1881='1881';
	var containerParam={};
	window.onload=function(){
		//var gohome = '<div class="gohome"><a class="animated bounceInUp" href="jsp/task/taskCalendar/taskCalender.jsp" title="返回日历"><i class="fa fa-home"></i></a></div>';
		//$('body').append(gohome);
		for(var i=0;i<classes.length;i++){
			parame.id=classes[i].id;
			var type=$(classes[i]).attr('multiSelect');
			parame.placeholder="";
			var dataField=$(classes[i]).attr('dataField');
			var data=$(classes[i]).attr('data');
			parame.jsonParam.dictTypeCode=$(classes[i]).attr('DictName');
			if(type=="true"){
				myehr_initSelectMore(parame,dataField,data);
			}else{
				myehr_initSelect(parame,dataField,containerParam,data);
			}
		}

		document.getElementById("SYS_SYSTEM_SCHEME.sysTaskId").value="${param.sysTaskId}";
		var sysTaskId = '${param.sysTaskId}';
		
		if(sysTaskId!='' && sysTaskId!=null){
			_initData(sysTaskId);
		}
		
		

	}

	function save_formemp_emp_edit_click_1881(){
		var paramsMap = {'userId' : '${sessionScope.userid}'};
		var _param = {};
		_param.formId=_formId_1881;
		_param.buttonId=2803;
		_param = getdata_1881(_param.buttonId,_formId_1881);
		_param.paramsMap = paramsMap;
		$.ajax({
			url:'${pageContext.request.contextPath }/sysTask/saveSysMeetingTask.action',
			type:'post',
			data: JSON.stringify(_param),
			cache: false,
			contentType: 'application/json;charset=utf-8',
			success: function (text) {
				if(text[0]==0){
					alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"保存失败！")%>");
				}else if(text[0]=='error'){
					alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作异常")%>");
				}else{
					alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作成功")%>");
					closex_1881();
				}
			}
		});
	}

	function _initData(pkId){
		var _form = getForm("#form_1881");
		var _Type = getType("#form_1881");
		$.ajax({
			url:'${pageContext.request.contextPath }/sysTask/querySysMeetingTaskByTaskId.action?sysTaskId='+pkId,
			type:'post',
			//data: JSON.stringify(_param),
			cache: false,
			contentType: 'application/json;charset=utf-8',
			success: function (text) {
				if(text !=null){
					var object = text;
					for(var key in object){
						if(_form[key]!=null&&_form[key]!="undefined"){
							if(_Type[key]=="dateTime"){
								date = new Date(object[key]).Format("yyyy-MM-dd hh:mm:ss");
								document.getElementById(_form[key]).value=formatDatebox(date,_form[key]);
							}else{
								document.getElementById(_form[key]).value=object[key];
							}
						}
					};
					showUsers(text.sysMeetingTaskHostid);
					showUsers1(text.sysMeetingTaskParticipants);
				}
			},
			error: function (jqXHR, textStatus, errorThrown) {
			}
		});
	}
	
	function showUsers(userIds){
		$.ajax({
			url:'${pageContext.request.contextPath }/sysTask/queryUsersByUserIds.action?userIds='+userIds,
			type:'post',
			//data: JSON.stringify(procDefId),
			cache: false,
			contentType: 'application/json;charset=utf-8',
			success: function (text) {
				if(text!=null){
					var backgroundColor = [];
					backgroundColor[0]='background-color:#c678b4;';
					backgroundColor[1]='background-color:#8476ce;';
					backgroundColor[2]='background-color:#f578a4;';
					backgroundColor[3]='background-color:#ea6566;';
					var html = "";
					for(var i=0;i<text.length;i++){
						var flag = i%4;
						html+='<div class="meetingUser" style="'+backgroundColor[flag]+'">'+text[i].userName+'</div>';
					}
					html+='<div class="meetingUser" style="background-color:#ffffff;border:2px solid #b4b4b4"><span style="font-size:46px;color:#b4b4b4" onclick="chooseUser(\'host\')">+</span></div>';
					$('#hostid').html(html);
				}
			}
		});
	}
	
	function showUsers1(userIds){
		$.ajax({
			url:'${pageContext.request.contextPath }/sysTask/queryUsersByUserIds.action?userIds='+userIds,
			type:'post',
			//data: JSON.stringify(procDefId),
			cache: false,
			contentType: 'application/json;charset=utf-8',
			success: function (text) {
				if(text!=null){
					var backgroundColor = [];
					backgroundColor[0]='background-color:#c678b4;';
					backgroundColor[1]='background-color:#8476ce;';
					backgroundColor[2]='background-color:#f578a4;';
					backgroundColor[3]='background-color:#ea6566;';
					var html = "";
					for(var i=0;i<text.length;i++){
						var flag = i%4;
						html+='<div class="meetingUser" style="'+backgroundColor[flag]+'">'+text[i].userName+'</div>';
					}
					html+='<div class="meetingUser" style="background-color:#ffffff;border:2px solid #b4b4b4"><span style="font-size:46px;color:#b4b4b4" onclick="chooseUser(\'participants\')">+</span></div>';
					$('#participants').html(html);
				}
			}
		});
	}
	
	function chooseUser(type){
		var url = '';
		var title = '';
		if(type=='host'){
			url = '/myehr/jsp/task/addTask/userList.jsp?type='+type+'&sysTaskId=${param.sysTaskId}';
			title = '选择主持人'
		}else{
			url = '/myehr/jsp/task/addTask/userList.jsp?type='+type+'&sysTaskId=${param.sysTaskId}';
			title = '选择参与人'
		}
    	layer.open({
			type: 2,
			title:title,
			shadeClose: true,
			shade: 0.8,
			//offset: ['0px', '0px'],
			area: ['500', '600'],
			content: url,
			success:function(layero,index){
			},
			end:function(){
				location.reload();
			}
		});
	}
	
	
	//关闭
	function closex_1881(){ 
	    var index = parent.layer.getFrameIndex(window.name); //获取当前窗体索引
	    parent.layer.close(index);//执行关闭
	}
	function getForm(formId){
		var object=[];
		$(formId).find("input").each(function(){
			var xxx = this.id.split(".")[1];
			if(xxx!=undefined){
				var xx = xxx.split("_");
				var arr = [];
				var id = "";
				for(var i=0;i<xx.length;i++){
					if(i<xx.length-1){
						id = id+xx[i]+"_";
					}else{
						id = id+xx[i];
					}
				}
				object[id]=this.id;
			}
		})
		$(formId).find("select").each(function(){
			var xxx = this.id.split(".")[1];
			if(xxx!=undefined){
				var xx = xxx.split("_");
				var arr = [];
				var id = "";
				for(var i=0;i<xx.length;i++){
					if(i<xx.length-1){
						id = id+xx[i]+"_";
					}else{
						id = id+xx[i];
					}
				}
				object[id]=this.id;
			}
		})
		return object;
	}
	function getType(formId){
		var object=[];
			$(formId).find("input").each(function(){
				var vtype = $(this).attr('format');
				if(vtype!=undefined){
					var xxx = this.id.split(".")[1];
						var xx = xxx.split("_");
						var arr = [];
						var id = "";
						for(var i=0;i<xx.length;i++){
							if(i<xx.length-1){
								id = id+xx[i]+"_";
							}else{
								id = id+xx[i];
							}
						}
					object[id]="dateTime";
				}
			})
		return object;
	}
</script>
</body>
</html>