<%@ page language="java" import="java.util.*" import="com.myehr.common.util.LangUtil" pageEncoding="UTF-8"%>
<%@ include file="/common/common.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<style type="text/css">
.fixed-table-body{
   max-height:480px;
}
</style>
<body>
<div class="container-fluid" style="height:100%;overflow-y:auto;">
	<div style="margin-top:5px;">
		<div class="input-group">
			<div id="grid_org_list_filter">
				<table class="cbs-table" style="margin-bottom:5px">
					<tr>
					</tr>
				</table>
			</div>
		</div>
		<div class="BTNGROUP_1041" style="margin:0 auto; display:inline-block;height: 35px;">
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="select"  class="btn btn-danger" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"终止")%> onclick="stopActTask()"/>
			</div>
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="approve"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"审批")%> onclick="approve()"/>
			</div>
			<!--
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="batchApprove"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"批量审批")%> onclick="batchApprove()"/>
			</div>
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="actClaim"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"签收")%> onclick="actClaim()"/>
			</div>
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="actPicture"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程图")%> onclick="actPicture('task')"/>
			</div>
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="actPictureLink"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程图路线")%> onclick="actPicture('link')"/>
			</div>
			<div style="margin-right:10px;display:inline-block">
			    <input type="button" id="addorg_list"  class="btn btn-primary" value=<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"审批历史")%> onclick="actHis()"/>
			</div>
			-->
		</div>
		<div id="grid_org_list_filter" style="float:right;display:inline-block;position:relative;">
			<input id="FILTER_1041" name="FILTER_1041" placeholder="<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"菜单编码")%>,<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"菜单名称")%>" style="padding: 6px 30px 6px 10px;" title="<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"菜单编码")%>,<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"菜单名称")%>" type="text" class="form-control" oninput="grid_org_list_fun11()" />
			<i class="icon-search" style="position: absolute;right: 12px;top: 2px;cursor: pointer;"></i>
		</div>
	</div>
	<table id="org_list_List">
	</table>
</div>
<script>
$(function() {
    querys_1041();
    initDict();
    initDate();
    console.log($(".BTNGROUP_1041>div").length);
    var DIVAMOUNT = 5;
    if($(".BTNGROUP_1041>div").length > DIVAMOUNT){
    	var len = $(".BTNGROUP_1041>div").length;
    	$(".BTNGROUP_1041>div:gt(4)").hide();
    	var selectObj = $('<div class="btn-group"><button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">更多 <span class="caret"></span></button><ul class="dropdown-menu" style="min-width:90px;left:-14px;"></ul></div>');
    	$(".BTNGROUP_1041").append(selectObj);
    	for(var i=DIVAMOUNT;i<len;i++){
    		var liObj = $('<li style="margin-left:14px;"></li>');
    		liObj.append($(".BTNGROUP_1041>div").eq(i).clone(true));
    		selectObj.find('.dropdown-menu').append(liObj);
    		selectObj.find('.dropdown-menu').append($('<li role="separator" class="divider"></li>'));
    	}
    	selectObj.find('.dropdown-menu div').show();
    }
});
	var _formId_1041='1041';
    function querys_1041() {
        $("#org_list_List").bootstrapTable({
            url :'${pageContext.request.contextPath }/act/task/todo.action?flowType='+'${param.flowType}',
            contentType: 'application/json;charset=utf-8',
            dataType:'json',
            method:'post',
            height : '578',
            undefinedText : '-',
            pagination : false,
            striped : true,
            queryParams : grid_org_list_fun,
            cache : false,
            //pageSize : 10, 
            //pageList : [10,70,10000], 
            toolbar : "#toolbar",
            showColumns : false,
            showExport : false,
            clickToSelect: true,
            showRefresh : false,
            sidePagination : "server",
            columns : [{
                field : 'state',
                checkbox : true,
                align : 'center',
                valign : 'middle'
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"序号")%>',
                field : 'defaultXH',
                align : 'center',
                visible : true,
                valign : 'left',
                formatter: function (value, row, index) {
                    if(value==undefined){
                        value=index+1;
                    }
                    return "<a style=\"font-size:14px;text-decoration:none;color:black;width:50px;\" name=\"defaultXH\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"序号\">"+value+"</a>";
                }
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务标题")%>',
                field : 'title',
                align : 'center',
                visible : true,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w400\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"title\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务标题\">"+value+"</a>";
				}
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务编号")%>',
                field : 'taskId',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
                    if(value==undefined){
                        value='-';
                    }
                    return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"taskId\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务编号\">"+value+"</a>";
                }
            },
			 {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务名称")%>',
                field : 'taskName',
                align : 'center',
                visible : true,
                valign : 'left',
                formatter: function (value, row, index) {
                    if(value==undefined){
                        value='-';
                    }
                    return "<a  class=\"eli w200\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"taskName\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务名称\">"+value+"</a>";
                }
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务环节")%>',
                field : 'taskDefKey',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"taskDefKey\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务环节\">"+value+"</a>";
                }
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程实例ID")%>',
                field : 'procInsId',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"procInsId\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"流程实例ID\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程定义ID")%>',
                field : 'procDefId',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"procDefId\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"流程定义ID\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程定义标识")%>',
                field : 'procDefKey',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"procDefKey\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"流程定义标识\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"业务绑定Table")%>',
                field : 'businessTable',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"businessTable\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"业务绑定Table\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"业务绑定ID")%>',
                field : 'businessId',
                align : 'center',
                visible : true,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w50\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"businessId\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"业务绑定ID\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"表单ID")%>',
                field : 'formId',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"formId\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"表单ID\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务状态")%>',
                field : 'status',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"status\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务状态\">"+value+"</a>";
				}
            },
			{
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"任务到达时间")%>',
                field : 'actCreateTime',
                align : 'center',
                visible : true,
                valign : 'left',
                formatter: function (value, row, index) {
					var date = "-";
					if(value!=null){
						date = new Date(value).Format("yyyy-MM-dd hh:mm:ss");
					}
					return "<a class=\"eli w200\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"actCreateTime\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"任务到达时间\">" + date + "</a>";
				}
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程图查看权限")%>',
                field : 'isViewProcessPicture',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"isViewProcessPicture\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"流程图查看权限\">"+value+"</a>";
				}
            },
            {
                title : '<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"批量审批权限")%>',
                field : 'isAllowBatchApprove',
                align : 'center',
                visible : false,
                valign : 'left',
                formatter: function (value, row, index) {
					if(value==undefined){
						value='-';
					}
					return "<a  class=\"eli w100\" style=\"font-size:14px;text-decoration:none;color:black;width:200px;\" name=\"isAllowBatchApprove\" data-type=\"text\" data-pk=\""+row.Id+"\" data-title=\"批量审批权限\">"+value+"</a>";
				}
            },
            ],
            onClickRow: function (row, $element) {
                curRow = row;
            },
            onDblClickRow: function (row, $element) {
            	curRow = row;
            	approveForDC(row);
            },
			onLoadSuccess: function (aa, bb, cc) {
                $("#org_list_List a").editable({
                    disabled: true,
                    emptytext: "-",
                    url: function (params) {
                        var sName = $(this).attr("name");
                        curRow[sName] = params.value;
                    },
                    type: 'text'
                });
            },
            responseHandler : function(res) {  
                return {  
                    total : res.total,  
                    rows : res.rows  
                };
            }
        });
        window.operateEvents = {  
            'click #remove': function (e, value, row, index) {  
            $.ajax({  
                type: 'POST', 
                data: JSON.stringify(row),  
                url: '${pageContext.request.contextPath }/dictType/removeSysDictType.action',  
                success: function (data) {  
                    alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"删除成功")%>');
                    window.location.href = window.location.href; 
                    $('#org_list_List').bootstrapTable(
                        'remove', {  
                            field: 'Id',  
                            values: [row.Id],  
                        }
                    );  
                    $('#org_list_List').bootstrapTable(
                        'refresh',{
                            url:'${pageContext.request.contextPath }/dictType/findDictTypeList2.action'  
                        }
                    );
                }
            });
        }
    };
}
/** 刷新页面 */ 
function refresh_org_list() { 
    $('#org_list_List').bootstrapTable('refresh'); 
}
/**查询条件与分页数据 */
function queryParams(pageReqeust) {
    pageReqeust.search = $("#search").val();
    pageReqeust.enabled = $("#enabled").val();
    pageReqeust.querys = $("#querys").val();
    pageReqeust.pageNo = this.offset;
    pageReqeust.pageSize = this.pageNumber;
    return pageReqeust;
}
function saveMenuIcon(){
	var menuCode='${param.menuCode}';
	var select;
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	if(selects!=null&&selects.length>0){
		select = selects[0];
	}else {
		alert('请选择一行数据');
		return ;
	}
	
	var _param={};
    _param.menuCode=menuCode;
    _param.select=selects[0];
	var url='/myehr/form/toForm.action?formId='+select.formId+'&isInit=true&AL_ID='+select.businessinstid+'&processinstid='+select.processinstid+'&taskId='+select.taskId;
	
	layer.open({
		type: 2,
		title:'<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程审批")%>',
		shadeClose: true,
		shade: 0.8,
		maxmin:true,
		zIndex:9999,
		area: ['950px', '500px'],
		content: url,
		success:function(layero,index){
		$('.layui-layer-max').click();
		},
		end:function(){
			refresh_org_list();
		}
	});
}

function approve(){
	var selects = $("#org_list_List").bootstrapTable('getSelections');;
	if(selects!=null && selects.length>0){
		select = selects[0];
	}else {
		alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"请选择一行数据")%>');return ;
	}
	
	var url='/myehr/form/toForm.action?formId='+select.formId+'&key='+select.procDefKey+'&isInit=true&orderBy='+select.taskDefKey+'&taskId='+select.taskId+'&procInsId='+select.procInsId+'&businessId='+select.businessId;
	
	layer.open({
		type: 2,
		title:'<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程审批")%>',
		shadeClose: true,
		shade: 0.8,
		maxmin:true,
		zIndex:9999,
		area: ['950px', '500px'],
		content: url,
		success:function(layero,index){
		$('.layui-layer-max').click();
		},
		end:function(){
			    refresh_org_list();
		}
	});
}

function approveForDC(e){
	var select = e ;
	
	var url='/myehr/form/toForm.action?formId='+select.formId+'&key='+select.procDefKey+'&isInit=true&orderBy='+select.taskDefKey+'&taskId='+select.taskId+'&procInsId='+select.procInsId+'&businessId='+select.businessId;
	
	layer.open({
		type: 2,
		title:'<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程审批")%>',
		shadeClose: true,
		shade: 0.8,
		maxmin:true,
		zIndex:9999,
		area: ['950px', '500px'],
		content: url,
		success:function(layero,index){
		$('.layui-layer-max').click();
		},
		end:function(){
			    refresh_org_list();
		}
	});
}

function batchApprove(){
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	for(var i=0;i<selects.length;i++){
		if(selects[i].isAllowBatchApprove!="true"){
			var index = i+1;
			alert('第'+index+'条任务编号为'+selects[i].taskId+'的'+selects[i].title+'不允许批量审批');
			return ;
		}
	}
	
	content = "<textarea id='approveRemark' rows='7' cols='63' ></textarea>";

	//var url='/myehr/form/toForm.action?formId='+select.formId+'&isInit=true&taskId='+select.taskId+'&procInsId='+select.procInsId+'&businessId='+select.businessId;
	
	layer.open({
		title:'请填写审批意见',
		shadeClose: true,
		zIndex:9999,
		content: content,
		area: ['500px', '300px'],
		btn: ['确定', '取消'],
		yes:function(index, layero){
			var approveRemark=document.getElementById("approveRemark").value;
			layer.close(index);
			saveData(selects,approveRemark);
		},
	})
}

function saveData(selects,approveRemark){
	var _param = {};
	var param = {};
	_param.selectsParam=selects;
	_param.approveRemark=approveRemark;
	$.ajax({
		url:'${pageContext.request.contextPath }/act/task/batchComplete.action',
		type:'post',
		data: JSON.stringify(_param),
		cache: false,
		contentType: 'application/json;charset=utf-8',
		success: function (text) {
			if(text[0]==0){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"保存失败！")%>");
			}else if(text[0]=='error'){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作异常")%>");
			}else {
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作成功")%>");
				closex_1881()
			}
		}
	});
}

function actClaim(){
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	if(selects!=null && selects.length>0){
		select = selects[0];
	}else {
		alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"请选择一行数据")%>');return ;
	}
	
	var taskId = select.taskId;
	
	var url='/myehr/act/task/claim.action?taskId='+taskId;
	
	$.ajax({
		url:url,
		type:'post',
		//data: JSON.stringify(_param),
		cache: false,
		contentType: 'application/json;charset=utf-8',
		success: function (text) {
			if(text[0]==0){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"保存失败！")%>");
			}else if(text[0]=='error'){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作异常")%>");
			}else {
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作成功")%>");
				closex_1881()
			}
		}
	});
}

function actPicture(type){
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	if(selects!=null && selects.length>0){
		select = selects[0];
		if(select.isViewProcessPicture=="true"){
			alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"对不起，您没有查看权限！")%>');return ;
		}
	}else {
		alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"请选择一行数据")%>');return ;
	}
	
	var procInsId = select.procInsId;
	
	var url='/myehr/act/task/trace/photo.action?procInsId='+procInsId+'&type='+type;
	
	layer.open({
		type: 2,
		title:'<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"流程审批")%>',
		shadeClose: true,
		shade: 0.8,
		maxmin:true,
		zIndex:9999,
		area: ['950px', '500px'],
		content: url,
		success:function(layero,index){
		$('.layui-layer-max').click();
		},
		end:function(){
			//refresh_org_list();
		}
	});
}

function stopActTask(){
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	if(selects!=null && selects.length>0){
	}else {
		alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"请选择一行数据")%>');return ;
	}
	$.ajax({  
         type: 'POST',   
         url: '${pageContext.request.contextPath }/act/task/stopActTask.action?taskId='+selects[0].taskId+'&businessKey='+selects[0].businessKey+'&pId='+selects[0].procInsId,  
         success: function (data) {  
         	  if(data[0]=='success'){
         	  	layer.alert(data[1], {
					  icon: 6,//1:√;2:×;3:问号;4:锁;5:哭脸;6.笑脸;7:！;
					  skin: 'layer-ext-moon' //
					})
         	  }else if(data[0]=='false'){
         	  	layer.alert(data[1], {
					  icon: 5,//1:√;2:×;3:问号;4:锁;5:哭脸;6.笑脸;7:！;
					  skin: 'layer-ext-moon' //
					})
         	  }
         	  refresh_org_list();
         }
    })
}

function actHis(){
	var selects = $("#org_list_List").bootstrapTable('getSelections');
	if(selects!=null && selects.length>0){
		select = selects[0];
	}else {
		alert('<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"请选择一行数据")%>');return ;
	}
	
	var url='/myehr/act/task/queryHisUserComment.action?procInsId='+select.procInsId+'&taskId='+select.taskId;
	
	$.ajax({
		url:url,
		type:'post',
		cache: false,
		contentType: 'application/json;charset=utf-8',
		success: function (text) {
			if(text[0]==0){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"保存失败！")%>");
			}else if(text[0]=='error'){
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作异常")%>");
			}else {
				alert("<%=LangUtil.getLangByCode((String)session.getAttribute("langType"),"操作成功")%>");
				closex_1881();
			}
		}
	});
}

function grid_org_list_fun(pageReqeust){
	var _filterdata={};
	
	var searchValue = document.getElementById("FILTER_1041").value;
	_filterdata={searchValue:searchValue};
	var userId = '${sessionScope.userid}';
	var paramsMap = {userId:userId,flowCode:'${param.flowCode}'};
	var requestParam={};
	
	pageReqeust.paramsMap=paramsMap;
	pageReqeust.requestParam=requestParam;
	pageReqeust.filter=_filterdata;
	pageReqeust.formId=_formId_1041;
	pageReqeust.isView=null;
	return JSON.stringify(pageReqeust);
}
function grid_org_list_fun11(){
	$('#org_list_List').bootstrapTable('refresh');
}
/**下拉控件 */
var classes = $("select");
var parame={
    id:'',
    value:"N",
    width: "100px",
    height:"45px",
    url:'${pageContext.request.contextPath }/dict/searchSysDictEntryTypeCode.action?userId=${sessionScope.userid}',
	  jsonParam:{},
    chang:function (e){
    }
};
function initDict(){
    for(var i=0;i<classes.length;i++){
        parame.id=classes[i].id;
		  var type=$(classes[i]).attr('multiSelect');
        parame.placeholder="";
        parame.jsonParam.dictTypeCode=$(classes[i]).attr('DictName');
		  if(type=="true"){
		  	myehr_initSelectMore(parame);
		  }else{
	        myehr_initSelect(parame);
		  }
    }
}
/**初始化日期控件  */
function initDate(){
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
} 

function closex() {    
	var index = parent.layer.getFrameIndex(window.name);
	parent.layer.close(index);//执行关闭
}

/**取url参数值  */
function getQueryString(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]); return null;
}
</script>
</body>
</html>
