package com.myehr.controller.flow;

import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.common.collect.Maps;
import com.myehr.common.exception.DcfException;
import com.myehr.common.mybatis.MybatisUtil;
import com.myehr.common.mybatis.Pager;
import com.myehr.common.util.DateUtil;
import com.myehr.common.util.ResultMapNew;
import com.myehr.common.util.datasource.CustomerContextHolder;
import com.myehr.common.utils.ActUtils;
import com.myehr.controller.form.parambean.CardformInitDataParams;
import com.myehr.controller.webservice.cxf.erp.CUXOKCBPMSERAPKGPortType_CUXOKCBPMSERAPKGPort_Client;
import com.myehr.mapper.activiti.ActHiActinstMapper;
import com.myehr.mapper.activiti.ActHiProcinstMapper;
import com.myehr.mapper.activiti.ActReModelMapper;
import com.myehr.mapper.activiti.ActRuTaskMapper;
import com.myehr.mapper.activiti.SysActTitleMapper;
import com.myehr.mapper.activiti.expand.ActNodePropertiesExpandMapper;
import com.myehr.mapper.formmanage.form.SysActfreeConductMapper;
import com.myehr.mapper.formmanage.form.SysActfreeHisMapper;
import com.myehr.mapper.formmanage.form.SysActfreeModelMapper;
import com.myehr.mapper.formmanage.form.SysActfreeWayMapper;
import com.myehr.mapper.sysdict.SysDictEntryMapper;
import com.myehr.mapper.sysdict.SysDictTypeMapper;
import com.myehr.mapper.sysuser.SysUserMapper;
import com.myehr.mapper.task.SysTaskMapper;
import com.myehr.pojo.act.Act;
import com.myehr.pojo.activiti.ActHiActinst;
import com.myehr.pojo.activiti.ActHiActinstExample;
import com.myehr.pojo.activiti.ActHiProcinst;
import com.myehr.pojo.activiti.ActHiProcinstExample;
import com.myehr.pojo.activiti.ActReModel;
import com.myehr.pojo.activiti.ActReModelExample;
import com.myehr.pojo.activiti.ActRuTask;
import com.myehr.pojo.activiti.ActRuTaskExample;
import com.myehr.pojo.activiti.SysActTitle;
import com.myehr.pojo.activiti.SysActTitleExample;
import com.myehr.pojo.dict.DictData;
import com.myehr.pojo.dict.SysDictEntry;
import com.myehr.pojo.dict.SysDictEntryExample;
import com.myehr.pojo.dict.SysDictType;
import com.myehr.pojo.dict.SysDictTypeExample;
import com.myehr.pojo.formmanage.cache.SysFormButtonCache;
import com.myehr.pojo.formmanage.cache.SysFormColumnCache;
import com.myehr.pojo.formmanage.cache.SysFormGeneralExecSqlCache;
import com.myehr.pojo.formmanage.cache.SysFormconfigCache;
import com.myehr.pojo.formmanage.form.SysActfreeConduct;
import com.myehr.pojo.formmanage.form.SysActfreeConductExample;
import com.myehr.pojo.formmanage.form.SysActfreeHis;
import com.myehr.pojo.formmanage.form.SysActfreeHisExample;
import com.myehr.pojo.formmanage.form.SysActfreeModel;
import com.myehr.pojo.formmanage.form.SysActfreeModelExample;
import com.myehr.pojo.formmanage.form.SysActfreeWay;
import com.myehr.pojo.formmanage.form.SysActfreeWayExample;
import com.myehr.pojo.sysuser.SysUser;
import com.myehr.pojo.task.SysTask;
import com.myehr.pojo.task.SysTaskExample;
import com.myehr.service.flow.ActTaskService;
import com.myehr.service.formmanage.form.IFormService;
import com.myehr.service.formmanage.form.ISysformconfigService;
import com.myehr.service.formmanage.runtime.IRuntmeButtonService;
import com.myehr.service.impl.formmanage.runtime.RuntimeButtonServiceImpl;
import com.myehr.service.impl.formmanage.runtime.RuntimeUtil;
import com.myehr.service.primaryKey.PrimaryKeyService;


/**
 * 流程个人任务相关Controller
 * @author ThinkGem
 * @version 2013-11-03
 */
@Controller
@RequestMapping(value = "act/task")
public class ActTaskController {
	private static Logger logger = LoggerFactory.getLogger(ActTaskController.class);
	@Autowired
	private ActTaskService actTaskService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;
	
	@Autowired
	private RepositoryService repositoryService;
	
	@Autowired
	private RuntimeService runtimeService;
	
	protected static String adminPath = "";
	
//	@Autowired
	@Resource(name = "IFormService")
	private IFormService formService;
	
//	@Autowired
	@Resource(name = "PrimaryKeyService")
	private PrimaryKeyService pkService;
	
	@Autowired 
	private ActHiActinstMapper actHiActinstMapper;
	
	@Autowired 
	private ActRuTaskMapper actRuTaskMapper;
	
	@Autowired 
	private SysTaskMapper sysTaskMapper;
	
	@Autowired 
	private SysUserMapper sysUserMapper;
	
	@Autowired 
	private ActReModelMapper actReModelMapper;
	
	@Autowired 
	private ActHiProcinstMapper actHiProcinstMapper;
	
	@Autowired
	private ActNodePropertiesExpandMapper actNodePropertiesExpandMapper;
	
	@Resource(name = "IRuntmeButtonService")
	private IRuntmeButtonService runtimeButtonServiceImpl;
	
	@Autowired
	private SysDictTypeMapper sysDictTypeMapper;
	
	@Autowired
	private SysDictEntryMapper sysDictEntryMapper;

	@Autowired
	private ISysformconfigService sysformconfigService;
	
	@Autowired SysActTitleMapper actTitleMapper;
	@Autowired SysActfreeWayMapper actfreeWayMapper;
	@Autowired SysActfreeModelMapper actfreeModelMapper;
	@Autowired SysActfreeHisMapper actfreeHisMapper;
	@Autowired SysActfreeConductMapper actfreeConductMapper;
	/**
	 * 获取待办列表
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "todo")
	@ResponseBody
	public ResultActListMap todoList(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) {
		ResultActListMap result = new ResultActListMap();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		try {
			Map paramsMap = params.getParamsMap();
			String showType = (String)paramsMap.get("showType");
			String flowCode = (String)paramsMap.get("flowCode");
			if (flowCode!=null && !flowCode.equals("")&&!flowCode.equals("FlowRoot")) {
				List<ActReModel> models = sysformconfigService.getActModelsByCode(flowCode);
				Act act = new Act();
				result = actTaskService.todoList(act,userId,showType,models,params);
			}else if (flowCode!=null&&flowCode.equals("FlowRoot")) {
				Act act = new Act();
				result = actTaskService.todoList_New(act,userId,showType,null,params);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
		return result;
	}
	/**
	 * 移动端获取待办列表(带访问地址)
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "todoForMobile")
	@ResponseBody
	public ResultActListForVueMap todoForMobile(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) {
		ResultActListForVueMap result = new ResultActListForVueMap();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		try {
			Map paramsMap = params.getParamsMap();
			String showType = (String)paramsMap.get("showType");
			Act act = new Act();
			result = actTaskService.todoListForMobile(act,userId,showType,null,params);
		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
		return result;
	}
	
	/**
	 * 保存流程标题
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "saveActTitle")
	public void saveActTitle(HttpServletRequest request) throws Exception {
		String modelKey = request.getParameter("modelKey");
		String title = request.getParameter("title");
		String string = request.getParameter("businessId");//businessId
		Long businssId = Long.valueOf(string);
		SysActTitleExample example = new SysActTitleExample();
		example.or().andBusinessidEqualTo(businssId).andModelKeyEqualTo(modelKey);
		List<SysActTitle> actTitles = actTitleMapper.selectByExample(example);
		SysActTitle actTitle = new SysActTitle();
		if (actTitles.size()>0) {
			actTitle = actTitles.get(0);
			actTitle.setTitle(title);
			actTitleMapper.updateByPrimaryKey(actTitle);
		} else {
			actTitle.setBusinessid(businssId);
			actTitle.setModelKey(modelKey);
			actTitle.setTitle(title);
			actTitleMapper.insert(actTitle);
		}
		
	}
	
	/*
	 * 可发起流程数
	 */
	@RequestMapping(value = "getTotalModel")
	@ResponseBody
	public Object getTotalModel(HttpServletRequest request){
		String userId = request.getSession().getAttribute("userid")+"";
	    String sql = "";
	    if (userId.equals("1")) {
	      sql = "SELECT COUNT (1) as num FROM (SELECT DISTINCT * FROM ( SELECT act_re_model.ID_ AS id, act_re_model.REV_ AS rev,act_re_model.NAME_ AS name,act_re_model.KEY_ AS key_,act_re_model.CATEGORY_ category, act_re_model.CREATE_TIME_ AS createTime,act_re_model.LAST_UPDATE_TIME_ AS lastUpdateTime,act_re_model.VERSION_ AS version, act_re_model.META_INFO_ AS metaInfo,act_re_model.DEPLOYMENT_ID_ AS deoloymentId,act_re_model.EDITOR_SOURCE_VALUE_ID_ AS editorSourceValueId, act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ AS editorSourceExtraValueId,act_re_model.TENANT_ID_ AS tenantId,act_node_properties_expand.ACT_NODE_FORM_ID AS actNodeFormId FROM act_re_model LEFT JOIN act_node_properties_expand ON act_re_model.id_ = act_node_properties_expand.ACT_MODEL_ID WHERE act_node_properties_expand.ACT_NODE_KEY = 'start' UNION SELECT act_re_model.ID_ AS id,act_re_model.REV_ AS rev,act_re_model.NAME_ AS name,act_re_model.KEY_ AS key_, act_re_model.CATEGORY_ category,act_re_model.CREATE_TIME_ AS createTime,act_re_model.LAST_UPDATE_TIME_ AS lastUpdateTime, act_re_model.VERSION_ AS version,act_re_model.META_INFO_ AS metaInfo,act_re_model.DEPLOYMENT_ID_ AS deoloymentId, act_re_model.EDITOR_SOURCE_VALUE_ID_ AS editorSourceValueId,act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ AS editorSourceExtraValueId, act_re_model.TENANT_ID_ AS tenantId,act_node_properties_expand.ACT_NODE_FORM_ID AS actNodeFormId FROM act_re_model LEFT JOIN act_node_properties_expand ON act_re_model.id_ = act_node_properties_expand.ACT_MODEL_ID WHERE act_re_model.ID_ NOT IN ( SELECT ACT_MODEL_ID FROM act_node_properties_expand ) ) t ) a";
	    } else {
	      sql = 
	      
	        "SELECT COUNT (1) as num FROM (SELECT DISTINCT * FROM ( SELECT act_re_model.ID_ AS id, act_re_model.REV_ AS rev,act_re_model.NAME_ AS name,act_re_model.KEY_ AS key_,act_re_model.CATEGORY_ category, act_re_model.CREATE_TIME_ AS createTime,act_re_model.LAST_UPDATE_TIME_ AS lastUpdateTime,act_re_model.VERSION_ AS version, act_re_model.META_INFO_ AS metaInfo,act_re_model.DEPLOYMENT_ID_ AS deoloymentId,act_re_model.EDITOR_SOURCE_VALUE_ID_ AS editorSourceValueId, act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ AS editorSourceExtraValueId,act_re_model.TENANT_ID_ AS tenantId,act_node_properties_expand.ACT_NODE_FORM_ID AS actNodeFormId FROM act_re_model LEFT JOIN act_node_properties_expand ON act_re_model.id_ = act_node_properties_expand.ACT_MODEL_ID join sys_act_role on act_re_model.ID_ = sys_act_role.actid WHERE act_node_properties_expand.ACT_NODE_KEY = 'start' and sys_act_role.roleid in (SELECT SYS_USER_ROLE.ROLE_ID from SYS_USER_ROLE WHERE SYS_USER_ROLE.USER_ID = " + userId + ") UNION SELECT " + "act_re_model.ID_ AS id,act_re_model.REV_ AS rev,act_re_model.NAME_ AS name,act_re_model.KEY_ AS key_, " + "act_re_model.CATEGORY_ category,act_re_model.CREATE_TIME_ AS createTime,act_re_model.LAST_UPDATE_TIME_ AS lastUpdateTime, " + "act_re_model.VERSION_ AS version,act_re_model.META_INFO_ AS metaInfo,act_re_model.DEPLOYMENT_ID_ AS deoloymentId, " + "act_re_model.EDITOR_SOURCE_VALUE_ID_ AS editorSourceValueId,act_re_model.EDITOR_SOURCE_EXTRA_VALUE_ID_ AS editorSourceExtraValueId, " + "act_re_model.TENANT_ID_ AS tenantId,act_node_properties_expand.ACT_NODE_FORM_ID AS actNodeFormId FROM act_re_model " + "LEFT JOIN act_node_properties_expand ON act_re_model.id_ = act_node_properties_expand.ACT_MODEL_ID join sys_act_role on act_re_model.ID_ = sys_act_role.actid  WHERE act_re_model.ID_ NOT IN ( " + "SELECT ACT_MODEL_ID FROM act_node_properties_expand ) and sys_act_role.roleid in (SELECT SYS_USER_ROLE.ROLE_ID from SYS_USER_ROLE WHERE SYS_USER_ROLE.USER_ID = " + userId + ") ) t ) a";
	    }
	    int freeModel = 0;
	    int freeHis = 0;
	    int freeWait = 0;
	    try
	    {
	      SysActfreeModelExample example = new SysActfreeModelExample();
	      List<SysActfreeModel> models = this.actfreeModelMapper.selectByExample(example);
	      freeModel = models.size();
	      SysActfreeHisExample example2 = new SysActfreeHisExample();
	      example2.or().andAssigreeEqualTo(userId);
	      List<SysActfreeHis> his = this.actfreeHisMapper.selectByExample(example2);
	      freeHis = his.size();
	      SysActfreeConductExample example3 = new SysActfreeConductExample();
	      example3.or().andAssigneeEqualTo(userId);
	      List<SysActfreeConduct> conducts = this.actfreeConductMapper.selectByExample(example3);
	      freeWait = conducts.size();
	    }
	    catch (Exception localException1) {}
	    Map map = new HashMap();
	    String[] nums = new String[3];
	    try
	    {
	      map = (Map)MybatisUtil.queryOne("runtime.selectSql", sql);
	      String num0 = map.get("num")+"";
	      nums[0] = (Integer.valueOf(num0) + freeModel)+"";
	      String num1 = this.actTaskService.getHistoricListTotal(userId)+"";
	      nums[1] = (Integer.valueOf(num1) + freeHis)+"";
	      ActReModelExample modelExample = new ActReModelExample();
	      List<ActReModel> models = this.actReModelMapper.selectByExample(modelExample);
	      String num2 = this.actTaskService.getTodoListTotal(userId, models)+"";
	      nums[2] = (Integer.valueOf(num2) + freeWait)+"";
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();logger.error(e.getMessage(), e);
	    }
	    return nums;
	}
	
	
	/**
	 * 获取待办流程列表
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "todoNewList")
	@ResponseBody
	public ResultActListMap todoNewList(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) throws Exception {
		ResultActListMap result = new ResultActListMap();
		Map paramsMap = params.getParamsMap();
		String showType = (String)paramsMap.get("showType");
		String flowCode = (String)paramsMap.get("flowCode");
		ActReModelExample modelExample = new ActReModelExample();
		com.myehr.pojo.activiti.ActReModelExample.Criteria modelCriteria = modelExample.createCriteria();
		
		if (flowCode!=null && !flowCode.equals("")) {
			modelCriteria.andCategoryEqualTo(flowCode);
		}
		List<ActReModel> models = actReModelMapper.selectByExample(modelExample);
		
		Act act = new Act();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		return actTaskService.todoList(act,userId,showType,models,params);
	}
	
	/**
	 * 获取我发起的流程
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "myStartList")
	@ResponseBody
	public ResultProcinstListMap myStartList(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) throws Exception {
		ResultProcinstListMap result = new ResultProcinstListMap();
		Map paramsMap = params.getParamsMap();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		
		ActHiProcinstExample example = new ActHiProcinstExample();
		example.or().andStartUserIdEqualTo(userId);
		
		List<ActHiProcinst> hiProcinsts = actHiProcinstMapper.selectByExample(example);
		
		result.setRows(hiProcinsts);
		result.setTotal(hiProcinsts.size());
		return result;
	}
	
	/**
	 * 获取已办任务
	 * @param page
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "historic")
	@ResponseBody
	public ResultActListMap historicList(HttpServletRequest request, @RequestBody CardformInitDataParams params,Model model) throws Exception {
		ResultActListMap result = new ResultActListMap();
		Act act = new Act();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		result = actTaskService.historicList(act,userId,params);
		return result;
	}
	
	/**
	 * 获取自由流程已办任务
	 * @param page
	 * @param procDefKey 流程定义标识
	 * @return
	 */
	@RequestMapping(value = "historicFree")
	@ResponseBody
	public ResultActListMap historicFreeList(HttpServletRequest request, @RequestBody CardformInitDataParams params,Model model) throws Exception {
		ResultActListMap result = new ResultActListMap();
		Act act = new Act();
		String userId  = ((BigDecimal) request.getSession().getAttribute("userid"))+"";
		result = actTaskService.historicFreeList(act,userId,params);
		return result;
	}

	/**
	 * 获取流转历史列表
	 * @param procInsId 流程实例
	 * @param startAct 开始活动节点名称
	 * @param endAct 结束活动节点名称
	 */
	@RequestMapping(value = "histoicFlow")
	public String histoicFlow(Act act, String startAct, String endAct, Model model){
		if (StringUtils.isNotBlank(act.getProcInsId())){
			List<Act> histoicFlowList = actTaskService.histoicFlowList(act.getProcInsId(), startAct, endAct);
			model.addAttribute("histoicFlowList", histoicFlowList);
		}
		return "modules/act/actTaskHistoricFlow";
	}
	
	
	/**
	 * 业务系统获取流转历史接口
	 */
	@RequestMapping(value = "itfhistoicFlow")
	public String itfhistoicFlow(Model model,HttpServletRequest request,HttpServletResponse response){
		HistoricTaskInstanceQuery histTaskQuery = historyService.createHistoricTaskInstanceQuery().processInstanceBusinessKeyLike("%"+request.getParameter("businessId")+"%").
				includeProcessVariables().orderByHistoricTaskInstanceEndTime().desc();
		List<HistoricTaskInstance> histList = histTaskQuery.list();
		if(histList!=null&&histList.size()>0){
			//流程实例对象
			ProcessInstance ProcInsId= runtimeService.createProcessInstanceQuery().processInstanceId(histList.get(0).getProcessInstanceId()).singleResult();
			if (ProcInsId!=null){
				List<Act> histoicFlowList = actTaskService.histoicFlowList(ProcInsId.getId(),"", "");
				model.addAttribute("histoicFlowList", histoicFlowList);
			}
		}
		return "modules/act/actTaskHistoricFlow";
	}
	
	
	
	
	/**
	 * 获取流程列表
	 * @param category 流程分类
	 */
	@RequestMapping(value = "process")
	public String processList(String category, HttpServletRequest request, HttpServletResponse response, Model model) {
	    /*Page<Object[]> page = new Page<Object[]>(request, response);
	    page = actTaskService.processList(page, category);
		model.addAttribute("page", page);
		model.addAttribute("category", category);*/
		return "modules/act/actTaskProcessList";
	}
	
	/**
	 * 获取流程表单
	 * @param taskId	任务ID
	 * @param taskName	任务名称
	 * @param taskDefKey 任务环节标识
	 * @param procInsId 流程实例ID
	 * @param procDefId 流程定义ID
	 */
	@RequestMapping(value = "form")
	public String form(Act act, HttpServletRequest request, Model model){
		
		// 获取流程XML上的表单KEY
		String formKey = actTaskService.getFormKey(act.getProcDefId(), act.getTaskDefKey());

		// 获取流程实例对象
		if (act.getProcInsId() != null){
			act.setProcIns(actTaskService.getProcIns(act.getProcInsId()));
		}
		
		
		
		
		return "redirect:" + ActUtils.getcsFormUrl(formKey, act);
	}
	
	
	/**
	 * 开发测试启动流程用！！！！！
	 */
	@RequestMapping(value = "csform")
	public String csform(Act act, HttpServletRequest request, Model model){
		
		// 获取流程XML上的表单KEY
		String formKey =actTaskService.getFormKey(act.getProcDefId(), act.getTaskDefKey());
		// 获取流程实例对象
		if (act.getProcInsId() != null){
			act.setProcIns(actTaskService.getProcIns(act.getProcInsId()));
		}
		
		return "redirect:" + ActUtils.getcsFormUrl(formKey, act);
	}
	
	/**
	 * 启动流程
	 * @param procDefKey 流程定义KEY
	 * @param businessTable 业务表表名
	 * @param businessId	业务表编号
	 */
	@RequestMapping(value = "start")
	@ResponseBody
	public String[] start(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) throws Exception {
		HttpSession session = request.getSession();
		Map paramsMap = params.getParamsMap();
		String buttonId = params.getButtonId();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		Map leave =params.getParam();
		
		String formId =params.getFormId();
		SysFormconfigCache form = formService.getForm(formId);

		String businessTable = form.getFormDefSaveTable();
		String pkColumn = form.getPkColumn().getFormFiledTablename();
		
		int businessId = pkService.getMaxId(businessTable, pkColumn)-1;
		
		Map actFlowParams=params.getActFlowParams();
		Act act = new Act(); 
		act.setProcDefKey((String)actFlowParams.get("key"));
		act.setBusinessId(businessId+"");
		act.setBusinessTable(businessTable);
		Map<String, Object> vars = Maps.newHashMap();
		leave.put("userId", userId);
		for (Object key : leave.keySet()) {
			vars.put(key+"", leave.get(key));	
		}
		act.setVars(vars);
		//4s
		String id = actTaskService.startProcess(vars,leave,act.getProcDefKey(), act.getBusinessTable(), act.getBusinessId(), act.getTitle(),userId);
		act.setProcInsId(id);
		String[] recodeStrings = actTaskService.initTask(userId,act,businessId,id);
		return recodeStrings;
		

//		} catch (Exception e) {
//			
//		}
		
		//后置触发
		
		/*String[] res = new String[2];
		String sourceType = "sqlserver";
		if (form.getPojoform().getFormDefSource()!=null && form.getPojoform().getFormDefSource().equals("oracle1")) {
			sourceType = "oracle";
			CustomerContextHolder.setContextType(CustomerContextHolder.session_factory_oracle);
		}
		if(paramsMap!=null&&buttonId!=null&&!"null".equals(buttonId)&&!"0".equals(buttonId)) {
			SysFormButtonCache button = form.getButtonByButtonId(buttonId);
			SysFormGeneralExecSqlCache buttonExecSql = button.getHzSql();
			if(buttonExecSql!=null&&buttonExecSql.getPojo()!=null&&buttonExecSql.getExecSqlId()>0){
				String execSql = buttonExecSql.getExecSql();
				execSql = execSql.trim();
				SysFormColumnCache c = form.serchColumnByColumnName(res[0],formId);
				long c_i= c.getFormColumnId();
				paramsMap.put("c_"+c_i, res[1]);
				try {
					if(execSql.toUpperCase().indexOf("call ") == 0){//存储过程
						return RuntimeUtil.execPrepare(buttonExecSql, pkService,paramsMap,MybatisUtil.getConnection());
					} else {//sql
						try {
							RuntimeUtil.execsql(buttonExecSql,pkService,paramsMap,session,sourceType);
						} catch (Exception e) {
							return new String[]{"error", e.getMessage()};
						}
					}
				}catch (Exception e) {
					// TODO: handle exception
					throw new DcfException(e.getMessage());
				}
				return new String[]{"000000","操作成功"};
			}
			return new String[]{"000000","操作成功"};		
		} */
		
	}
	
	/*public void initTask(String userId,Act act,int businessId,String id) {
		SysUserExample userExample = new SysUserExample();
		userExample.or().andUserIdEqualTo(Integer.parseInt(userId));
		SysUser sysUser = sysUserMapper.selectByExample(userExample).get(0);
		String userName = sysUser.getUserName();
		
		ActReModelExample actReModelExample = new ActReModelExample();
		actReModelExample.or().andKeyEqualTo(act.getProcDefKey());
		ActReModel actReModel = actReModelMapper.selectByExample(actReModelExample).get(0);
		String modelName = (String) actReModel.getName();
		
		passTask(userId,act,id,actReModel,businessId,userName+"的"+modelName);
		
	}*/
	
	/*public void passTask(String userId,Act act,String id,ActReModel actReModel,int businessId,String title) {
		ActRuTaskExample actRuTaskExample = new ActRuTaskExample();
		actRuTaskExample.or().andProcInstIdEqualTo(id);
		List<ActRuTask> actRuTasks = actRuTaskMapper.selectByExample(actRuTaskExample);
		for (int i = 0; i < actRuTasks.size(); i++) {
			ActRuTask actRuTask = actRuTasks.get(i);
			
			ActNodePropertiesExpandExample actNodePropertiesExpandExample = new ActNodePropertiesExpandExample();
			actNodePropertiesExpandExample.or().andActModelIdEqualTo(actReModel.getId()+"").andActNodeNameEqualTo(actRuTask.getName()+"");
			ActNodePropertiesExpand ActNodePropertiesExpand = actNodePropertiesExpandMapper.selectByExample(actNodePropertiesExpandExample).get(0);
			if (ActNodePropertiesExpand.getIsAutomaticallyPass().equals("true")) {
				actTaskService.complete(actRuTask.getId()+"", actRuTask.getProcInstId()+"", "通过|通过", act.getVars().getVariableMap());
				
				SysTaskExample taskExample = new SysTaskExample();
				taskExample.or().andProcInsIdEqualTo(actRuTask.getProcInstId()+"").andProcTaskIdEqualTo(actRuTask.getId()+"");
				List<SysTask>  sysTaskList= sysTaskMapper.selectByExample(taskExample);
				
				SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Map<String,String> objMap = new HashMap<String,String>();
				objMap.put("contractid", act.getBusinessId());
				objMap.put("contractid", "3");
				objMap.put("contractid", userId);
				objMap.put("approvalstatus", "APPROVED");
				objMap.put("approvalcomments", "通过");
				objMap.put("approvedate", sdf.format(new Date()));
				if (sysTaskList.size()>0) {
					objMap.put("wfstatus", "P");
				}else {
					objMap.put("wfstatus", "O");
				}
				
				CUXOKCBPMSERAPKGPortType_CUXOKCBPMSERAPKGPort_Client.Test(objMap);
				
				
				if (sysTaskList.size()>0) {
					SysTask sysTask = new SysTask();
					sysTask = sysTaskList.get(0);
					sysTask.setSysTaskIdentifier("finish");
					sysTask.setSysTaskCompleteTime(new Date());
					sysTaskMapper.updateByPrimaryKey(sysTask);
				}else {
					SysTask sysTask = new SysTask();
					sysTask.setProcBusinessId(businessId+"");
					sysTask.setProcFormId(Long.parseLong(actRuTask.getFormKey()+""));
					sysTask.setProcInsId(actRuTask.getProcInstId()+"");
					sysTask.setProcTaskId(actRuTask.getId()+"");
					sysTask.setSysTaskContent(title);
					sysTask.setSysTaskStatus("0");
					sysTask.setSysTaskType("6");
					sysTask.setSysTaskIdentifier("finish");
					sysTask.setSysTaskCreateUserid(userId);
					sysTask.setSysTaskCreateTime(new Date());
					sysTask.setSysTaskCompleteUserid(Long.parseLong(actRuTask.getAssignee()+""));
					sysTask.setSysTaskCompleteTime(new Date());
					sysTaskMapper.insert(sysTask);
				}
				
				
				ActRuTaskExample actRuTaskExample1 = new ActRuTaskExample();
				actRuTaskExample1.or().andProcInstIdEqualTo(id);
				List<ActRuTask> taskList = actRuTaskMapper.selectByExample(actRuTaskExample1);
				for (int j = 0; j < taskList.size(); j++) {
					SysTaskExample taskExample1 = new SysTaskExample();
					taskExample1.or().andProcInsIdEqualTo(id).andProcTaskIdEqualTo(taskList.get(j).getId()+"");
					List<SysTask> sysTaskList1 = sysTaskMapper.selectByExample(taskExample1);
					if (sysTaskList1.size()==0) {
						SysTask sysTask = new SysTask();
						sysTask.setProcBusinessId(businessId+"");
						sysTask.setProcFormId(Long.parseLong(taskList.get(j).getFormKey()+""));
						sysTask.setProcInsId(id);
						sysTask.setProcTaskId(taskList.get(j).getId()+"");
						sysTask.setSysTaskContent(title);
						sysTask.setSysTaskStatus("0");
						sysTask.setSysTaskType("6");
						sysTask.setSysTaskIdentifier("todo");
						sysTask.setSysTaskCreateUserid(userId);
						sysTask.setSysTaskCreateTime(new Date());
						sysTask.setSysTaskCompleteUserid(Long.parseLong(taskList.get(j).getAssignee()+""));
						sysTaskMapper.insert(sysTask);
					}
				}
				passTask(userId,act,id,actReModel,businessId,title);
			}else {
				SysTaskExample taskExample1 = new SysTaskExample();
				taskExample1.or().andProcInsIdEqualTo(actRuTask.getProcInstId()+"").andProcTaskIdEqualTo(actRuTask.getId()+"");
				List<SysTask> sysTaskList1 = sysTaskMapper.selectByExample(taskExample1);
				if (sysTaskList1.size()==0) {
					SysTask sysTask = new SysTask();
					sysTask.setProcBusinessId(businessId+"");
					sysTask.setProcFormId(Long.parseLong(actRuTask.getFormKey()+""));
					sysTask.setProcInsId(id);
					sysTask.setProcTaskId(actRuTask.getId()+"");
					sysTask.setSysTaskContent(title);
					sysTask.setSysTaskStatus("0");
					sysTask.setSysTaskType("6");
					sysTask.setSysTaskIdentifier("todo");
					sysTask.setSysTaskCreateUserid(userId);
					sysTask.setSysTaskCreateTime(new Date());
					sysTask.setSysTaskCompleteUserid(Long.parseLong(actRuTask.getAssignee()+""));
					sysTaskMapper.insert(sysTask);
				}
			}
		}
	}*/

	/**
	 * 签收任务
	 * @param taskId 任务ID
	 */
	@RequestMapping(value = "claim")
	@ResponseBody
	public String claim(HttpServletRequest request,Model model) {
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		String taskId = request.getParameter("taskId");
		actTaskService.claim(taskId, userId);
		return "true";//adminPath + "/act/task";
	}
	
	/**
	 * 完成任务
	 * @param taskId 任务ID
	 * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
	 * @param comment 任务提交意见的内容
	 * @param vars 任务流程变量，如下
	 * 		vars.keys=flag,pass
	 * 		vars.values=1,true
	 * 		vars.types=S,B  @see com.thinkgem.jeesite.modules.act.utils.PropertyType
	 */
	@RequestMapping(value = "complete")
	@ResponseBody
	public String[] complete(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) {
		String[] reCode = {"000000","success"};
		try {
			HttpSession session = request.getSession();
			String userId = (BigDecimal)session.getAttribute("userid")+"";
			Map leave =params.getParam();
			String formId =params.getFormId();
			Map actFlowParams=params.getActFlowParams();
			Act act = new Act(); 
			act.setProcDefKey((String)actFlowParams.get("key"));
			act.setTaskId((String)actFlowParams.get("taskId"));
			act.setProcInsId((String)actFlowParams.get("procInsId"));
			act.setComment((String)actFlowParams.get("approveRemark"));
			Map<String, Object> vars = Maps.newHashMap();
			leave.put("userId", userId);
			for (Object key : leave.keySet()) {
				vars.put(key+"", leave.get(key));	
			}
			act.setVars(vars);
			
			String buttonType = (String)actFlowParams.get("buttonType");
			String rejectType = (String)actFlowParams.get("rejectType");
			if (buttonType.equals("通过")) {
				ActRuTaskExample actRuTaskExampleBefore = new ActRuTaskExample();
				actRuTaskExampleBefore.or().andProcInstIdEqualTo(act.getProcInsId());
				actTaskService.complete(act.getTaskId(), act.getProcInsId(), "通过|"+act.getComment(), act.getVars().getVariableMap());
					
				//			actTaskService.complete(act.getTaskId(), act.getProcInsId(), "通过|"+act.getComment(), act.getVars().getVariableMap());
				
				ActRuTaskExample actRuTaskExampleAfter = new ActRuTaskExample();
				actRuTaskExampleAfter.or().andProcInstIdEqualTo(act.getProcInsId());
				List<ActRuTask> actRuTask = actRuTaskMapper.selectByExample(actRuTaskExampleAfter);
				
				String sql = "select USER_CODE from sys_user where user_id = "+userId;
				List<Map> users = null;
				try {
					users = MybatisUtil.queryList("runtime.selectSql", sql);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();logger.error(e.getMessage(),e);
				}
				
				String userCode = users.get(0).get("USER_CODE")+"";
				
				SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Map<String,String> objMap = new HashMap<String,String>();
				objMap.put("contractid", leave.get("OKCHV_OKC_HEADER_ID")+"");
				objMap.put("approvelevel", "3");
				objMap.put("approvedid", userCode);
				objMap.put("approvalstatus", "APPROVED");
				objMap.put("approvalcomments", (String)actFlowParams.get("approveRemark"));
				objMap.put("approvedate", sdf.format(new Date()));
				if (actRuTask.size()>0) {
					objMap.put("wfstatus", "P");
				}else {
					objMap.put("wfstatus", "O");
				}
				
				//String result = CUXOKCBPMSERAPKGPortType_CUXOKCBPMSERAPKGPort_Client.Test(objMap);
				
				SysTaskExample taskExample = new SysTaskExample();
				taskExample.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(act.getTaskId());
				SysTask sysTask = sysTaskMapper.selectByExample(taskExample).get(0);
				sysTask.setSysTaskIdentifier("finish");
				sysTask.setSysTaskCompleteTime(new Date());
				sysTaskMapper.updateByPrimaryKey(sysTask);
	
				if (actRuTask.size()>0) {
					String[] aa =(actRuTask.get(0).getProcDefId()+"").split(":"); 
					String procDefKey = aa[0];
					
					ActReModelExample actReModelExample = new ActReModelExample();
					actReModelExample.or().andKeyEqualTo(procDefKey);
					ActReModel actReModel = actReModelMapper.selectByExample(actReModelExample).get(0);
					reCode = actTaskService.passTask(userId,act,act.getProcInsId(),actReModel,Integer.parseInt(sysTask.getProcBusinessId()),sysTask.getSysTaskContent(),userId);
				}
			}else if (buttonType.equals("提交")){//只走流程无具体实意
				ActRuTaskExample actRuTaskExampleBefore = new ActRuTaskExample();
				actRuTaskExampleBefore.or().andProcInstIdEqualTo(act.getProcInsId());
				
				actTaskService.complete(act.getTaskId(), act.getProcInsId(), "通过|"+act.getComment(), act.getVars().getVariableMap());
				
				ActRuTaskExample actRuTaskExampleAfter = new ActRuTaskExample();
				actRuTaskExampleAfter.or().andProcInstIdEqualTo(act.getProcInsId());
				List<ActRuTask> actRuTask = actRuTaskMapper.selectByExample(actRuTaskExampleAfter);
				
				String sql = "select USER_CODE from sys_user where user_id = "+userId;
				List<Map> users = null;
				try {
					users = MybatisUtil.queryList("runtime.selectSql", sql);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();logger.error(e.getMessage(),e);
				}
				
				String userCode = users.get(0).get("USER_CODE")+"";
				
				SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Map<String,String> objMap = new HashMap<String,String>();
				objMap.put("contractid", leave.get("OKCHV_OKC_HEADER_ID")+"");
				objMap.put("approvelevel", "3");
				objMap.put("approvedid", userCode);
				objMap.put("approvalstatus", "APPROVED");
				objMap.put("approvalcomments", (String)actFlowParams.get("approveRemark"));
				objMap.put("approvedate", sdf.format(new Date()));
				if (actRuTask.size()>0) {
					objMap.put("wfstatus", "P");
				}else {
					objMap.put("wfstatus", "O");
				}
				
				//String result = CUXOKCBPMSERAPKGPortType_CUXOKCBPMSERAPKGPort_Client.Test(objMap);
				
				SysTaskExample taskExample = new SysTaskExample();
				taskExample.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(act.getTaskId());
				SysTask sysTask = sysTaskMapper.selectByExample(taskExample).get(0);
				sysTask.setSysTaskIdentifier("finish");
				sysTask.setSysTaskCompleteTime(new Date());
				sysTaskMapper.updateByPrimaryKey(sysTask);
	
				if (actRuTask.size()>0) {
					String[] aa =(actRuTask.get(0).getProcDefId()+"").split(":"); 
					String procDefKey = aa[0];
					
					ActReModelExample actReModelExample = new ActReModelExample();
					actReModelExample.or().andKeyEqualTo(procDefKey);
					ActReModel actReModel = actReModelMapper.selectByExample(actReModelExample).get(0);
					reCode = actTaskService.passTask(userId,act,act.getProcInsId(),actReModel,Integer.parseInt(sysTask.getProcBusinessId()),sysTask.getSysTaskContent(),userId);
				}
			}else if (buttonType.equals("驳回")){
				if (rejectType.equals("previousNode")) {
					ActHiActinstExample example = new ActHiActinstExample();
					com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
					criteria.andProcInstIdEqualTo(act.getProcInsId());
					criteria.andEndTimeIsNotNull();
					criteria.andActTypeEqualTo("userTask");
					example.setOrderByClause("END_TIME_");
					
					List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
					
					actTaskService.completeReject(act.getProcInsId(), act.getTaskId(), (String)ActHiActinst.get(ActHiActinst.size()-1).getActId(),  vars,"驳回到上节点|"+act.getComment());
					SimpleDateFormat sdf =   new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
					Map<String,String> objMap = new HashMap<String,String>();
					objMap.put("contractid", act.getBusinessId());
					objMap.put("contractid", "3");
					objMap.put("contractid", userId);
					objMap.put("approvalstatus", "REJECT");
					objMap.put("approvalcomments", (String)actFlowParams.get("approveRemark"));
					objMap.put("approvedate", sdf.format(new Date()));
					objMap.put("wfstatus", "P");
					
	//				String result = CUXOKCBPMSERAPKGPortType_CUXOKCBPMSERAPKGPort_Client.Test(objMap);
					
					
					SysTaskExample taskExample = new SysTaskExample();
					taskExample.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(act.getTaskId());
					SysTask sysTask = sysTaskMapper.selectByExample(taskExample).get(0);
					sysTask.setSysTaskIdentifier("finish");
					sysTask.setSysTaskCompleteTime(new Date());
					sysTaskMapper.updateByPrimaryKey(sysTask);
					
					ActRuTaskExample actRuTaskExample = new ActRuTaskExample();
					actRuTaskExample.or().andProcInstIdEqualTo(act.getProcInsId());
					List<ActRuTask> taskList = actRuTaskMapper.selectByExample(actRuTaskExample);
					for (int i = 0; i < taskList.size(); i++) {
						SysTaskExample taskExample1 = new SysTaskExample();
						taskExample1.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(taskList.get(i).getId()+"");
						List<SysTask> sysTaskList1 = sysTaskMapper.selectByExample(taskExample1);
						if (sysTaskList1.size()==0) {
							sysTask.setProcBusinessId(sysTask.getProcBusinessId()+"");
							sysTask.setProcFormId(Long.parseLong(taskList.get(i).getFormKey()+""));
							sysTask.setProcInsId(act.getProcInsId());
							sysTask.setProcTaskId(taskList.get(i).getId()+"");
							sysTask.setSysTaskContent(sysTask.getSysTaskContent());
							sysTask.setSysTaskStatus("0");
							sysTask.setSysTaskType("6");
							sysTask.setSysTaskIdentifier("todo");
							sysTask.setSysTaskCreateUserid(userId);
							sysTask.setSysTaskCreateTime(new Date());
							sysTask.setSysTaskCompleteUserid(Long.parseLong(taskList.get(i).getAssignee()+""));
							sysTaskMapper.insert(sysTask);
						}
					}
				}else if (rejectType.equals("startUser")) {	
					ActHiActinstExample example = new ActHiActinstExample();
					example.or().andProcInstIdEqualTo(act.getProcInsId());
					example.setOrderByClause("START_TIME_");
					List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
					
					actTaskService.completeReject(act.getProcInsId(), act.getTaskId(), (String)ActHiActinst.get(1).getActId(),  vars,"驳回给提交人"+act.getComment());
				
					SysTaskExample taskExample = new SysTaskExample();
					taskExample.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(act.getTaskId());
					SysTask sysTask = sysTaskMapper.selectByExample(taskExample).get(0);
					sysTask.setSysTaskIdentifier("finish");
					sysTask.setSysTaskCompleteTime(new Date());
					sysTaskMapper.updateByPrimaryKey(sysTask);
					
					ActRuTaskExample actRuTaskExample = new ActRuTaskExample();
					actRuTaskExample.or().andProcInstIdEqualTo(act.getProcInsId());
					List<ActRuTask> taskList = actRuTaskMapper.selectByExample(actRuTaskExample);
					for (int i = 0; i < taskList.size(); i++) {
						sysTask.setProcBusinessId(sysTask.getProcBusinessId()+"");
						sysTask.setProcFormId(Long.parseLong(taskList.get(i).getFormKey()+""));
						sysTask.setProcInsId(act.getProcInsId());
						sysTask.setProcTaskId(taskList.get(i).getId()+"");
						sysTask.setSysTaskContent(sysTask.getSysTaskContent());
						sysTask.setSysTaskStatus("0");
						sysTask.setSysTaskType("6");
						sysTask.setSysTaskIdentifier("todo");
						sysTask.setSysTaskCreateUserid(userId);
						sysTask.setSysTaskCreateTime(new Date());
						sysTask.setSysTaskCompleteUserid(Long.parseLong(taskList.get(i).getAssignee()+""));
						sysTaskMapper.insert(sysTask);
					}
				}else if (rejectType.equals("free")) {				
					String rejectTask = (String)actFlowParams.get("rejectTask");				
					actTaskService.completeReject(act.getProcInsId(), act.getTaskId(), rejectTask,  vars,"驳回到自由节点"+act.getComment());
				
					SysTaskExample taskExample = new SysTaskExample();
					taskExample.or().andProcInsIdEqualTo(act.getProcInsId()).andProcTaskIdEqualTo(act.getTaskId());
					SysTask sysTask = sysTaskMapper.selectByExample(taskExample).get(0);
					sysTask.setSysTaskIdentifier("finish");
					sysTask.setSysTaskCompleteTime(new Date());
					sysTaskMapper.updateByPrimaryKey(sysTask);
					
					ActRuTaskExample actRuTaskExample = new ActRuTaskExample();
					actRuTaskExample.or().andProcInstIdEqualTo(act.getProcInsId());
					List<ActRuTask> taskList = actRuTaskMapper.selectByExample(actRuTaskExample);
					for (int i = 0; i < taskList.size(); i++) {
						sysTask.setProcBusinessId(sysTask.getProcBusinessId()+"");
						sysTask.setProcFormId(Long.parseLong(taskList.get(i).getFormKey()+""));
						sysTask.setProcInsId(act.getProcInsId());
						sysTask.setProcTaskId(taskList.get(i).getId()+"");
						sysTask.setSysTaskContent(sysTask.getSysTaskContent());
						sysTask.setSysTaskStatus("0");
						sysTask.setSysTaskType("6");
						sysTask.setSysTaskIdentifier("todo");
						sysTask.setSysTaskCreateUserid(userId);
						sysTask.setSysTaskCreateTime(new Date());
						sysTask.setSysTaskCompleteUserid(Long.parseLong(taskList.get(i).getAssignee()+""));
						sysTaskMapper.insert(sysTask);
					}
				}else {
					ActHiActinstExample example = new ActHiActinstExample();
					com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
					criteria.andProcInstIdEqualTo(act.getProcInsId());
					criteria.andEndTimeIsNotNull();
					example.setOrderByClause("END_TIME_");
					List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
					
					actTaskService.completeReject(act.getProcInsId(), act.getTaskId(), (String)ActHiActinst.get(ActHiActinst.size()-1).getActId(),  vars,"驳回到上节点|"+act.getComment());
				}
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.getMessage();
		}
		return reCode;//adminPath + "/act/task";
	}
	
	/**
	 * 完成任务
	 * @param taskId 任务ID
	 * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
	 * @param comment 任务提交意见的内容
	 * @param vars 任务流程变量，如下
	 * 		vars.keys=flag,pass
	 * 		vars.values=1,true
	 * 		vars.types=S,B  @see com.thinkgem.jeesite.modules.act.utils.PropertyType
	 */
	@RequestMapping(value = "addMoreTask")
	@ResponseBody
	public String addMoreTask(HttpServletRequest request,Model model) {
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		
		String procDefId = request.getParameter("procDefId");
		String procInsId = request.getParameter("procInsId");
		String taskDefKey = request.getParameter("taskDefKey");
		String assignees = "";
		actTaskService.insertTasksAfter(procDefId,procInsId,taskDefKey,null,"8262","17205","17221");
		
		return "true";//adminPath + "/act/task";
	}
	
	
	/**
	 * 批量审批
	 * @param taskId 任务ID
	 * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
	 * @param comment 任务提交意见的内容
	 * @param vars 任务流程变量，如下
	 * 		vars.keys=flag,pass
	 * 		vars.values=1,true
	 * 		vars.types=S,B  @see com.thinkgem.jeesite.modules.act.utils.PropertyType
	 */
	@RequestMapping(value = "batchComplete")
	@ResponseBody
	public String batchComplete(HttpServletRequest request, @RequestBody BatchApproveData params,Model model) {
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		String approveRemark =params.getApproveRemark();
		List<Map> selectsParam = (List<Map>)params.getSelectsParam();
		
		for (int i = 0; i < selectsParam.size(); i++) {
			Map actFlowParams = selectsParam.get(i);
			Act act = new Act(); 
			act.setProcDefKey((String)actFlowParams.get("procDefId"));
			act.setTaskId((String)actFlowParams.get("taskId"));
			act.setProcInsId((String)actFlowParams.get("procInsId"));
			act.setComment(approveRemark);
			
			Map<String, Object> vars = Maps.newHashMap();
			vars = (Map<String, Object>)actFlowParams.get("vars");
			act.setVars(vars);
			
			actTaskService.complete(act.getTaskId(), act.getProcInsId(), "通过|"+act.getComment(), act.getVars().getVariableMap());
		}
		
		/*for (Object key : leave.keySet()) {
			vars.put(key+"", leave.get(key));	
		}
		act.setVars(vars);*/
		
		return "true";//adminPath + "/act/task";
	}
	
	
	/**
	 * 查询审批列表
	 * @param procInsId 流程启动ID
	 */
	@RequestMapping(value = "queryApproveList")
	@ResponseBody
	public List<Map> queryApproveList(HttpServletRequest request) {
		String procInsId = request.getParameter("procInsId");
		String sql = "select CONVERT(varchar(100), a.end_time_, 120) as ENDTIME, a.act_name_ as ACTNAME, b.message_ as MESSAGE,c.user_name as USER_NAME from act_hi_actinst a join act_hi_comment b on a.task_id_ = b.task_id_ join sys_user c on a.assignee_ = c.user_id where a.end_time_ is not null and a.proc_inst_id_='"+procInsId+"' order by a.end_time_ asc";
		Pager page = new Pager();
		page.setStart(1);
		//page.setLimt(10);
		Map<String, String> maps=new HashMap<String, String>();
        maps.put("formDefSql", sql);
        List<Map> p = null;
        try {
			p = MybatisUtil.queryListWithPage("runtime.queryByFormDefSql", maps,page);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
		return p;
	}
	
	/**
	 * 查询审批列表
	 * @param procInsId 流程启动ID
	 */
	@RequestMapping(value = "queryHisApproveUser")
	@ResponseBody
	public List<Map> queryHisApproveUser(HttpServletRequest request) {
		String procInsId = request.getParameter("procInsId");
		String sql = "select b.TASK_ID_ as taskId,CONVERT(varchar(100), a.end_time_, 120) as ENDTIME, a.act_name_ as ACTNAME, b.message_ as MESSAGE,c.user_name as USER_NAME from act_hi_actinst a join act_hi_comment b on a.task_id_ = b.task_id_ join sys_user c on a.assignee_ = c.user_id where a.end_time_ is not null and a.proc_inst_id_='"+procInsId+"' order by a.end_time_ asc";
		Pager page = new Pager();
		page.setStart(1);
		//page.setLimt(10);
		Map<String, String> maps=new HashMap<String, String>();
        maps.put("formDefSql", sql);
        List<Map> p = null;
        try {
			p = MybatisUtil.queryListWithPage("runtime.queryByFormDefSql", maps,page);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
		return p;
	}
	
	/**
	 * 查询审批列表
	 * @param procInsId 流程启动ID
	 */
	@RequestMapping(value = "queryHisApproveUserForFree")
	@ResponseBody
	public List<SysActfreeWay> queryHisApproveUserForFree(HttpServletRequest request) {
		String taskId = request.getParameter("taskId");
		String orderby = request.getParameter("orderby");
		SysActfreeWayExample example = new SysActfreeWayExample();
		example.or().andTaskidEqualTo(taskId).andOrderbyBetween("0", orderby);
		example.setOrderByClause("ORDERBY");
		List<SysActfreeWay> p = actfreeWayMapper.selectByExample(example);
		return p;
	}
	
	/**
	 * 查询所有用户列表
	 * @param request
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/queryHisUserComment")
	public @ResponseBody ResultMapNew queryAllUserList(HttpServletRequest request) throws Exception{
		String procInsId = request.getParameter("procInsId");
		String taskId = request.getParameter("taskId");
		
		String sql = "select TASK_DEF_KEY_ as nodeKey,proc_def_id_ as pId from act_ru_task where ID_='"+taskId+"'";
		Map tasks = null;
		String nodeKey = "";
		String modelId = "";
		try {
			tasks  = MybatisUtil.queryOne("runtime.selectSql", sql);
			nodeKey = tasks.get("nodeKey")+"";
			String pId = tasks.get("pId")+"";
			ActReModel model = sysformconfigService.getActModelByKey(pId.split(":")[0]);
			modelId = model.getId()+"";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
		
		String sql1 = " with CityTree AS "+
				" ( "+
				" 	SELECT * from MODEL_NODE_SEQ where model_node='"+nodeKey+"' and MODEL_PRO_DEF = '"+modelId+"'"+
				" 	UNION ALL "+
				" 	SELECT MODEL_NODE_SEQ.* from CityTree "+
				" 	JOIN MODEL_NODE_SEQ on CityTree.model_pre_node= MODEL_NODE_SEQ.model_node AND CityTree.MODEL_PRO_DEF=MODEL_NODE_SEQ.MODEL_PRO_DEF"+ 
				" ) "+
				" select * from  CityTree";
		
		List<Map> preNodes = null;
		try {
			preNodes  = MybatisUtil.queryList("runtime.selectSql", sql1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
		String preNodeStrings = "";
		for (int i = 0; i < preNodes.size(); i++) {
			preNodeStrings+="'"+preNodes.get(i).get("MODEL_PRE_NODE")+"',";
		}
		
		preNodeStrings=preNodeStrings.substring(0, preNodeStrings.length()-1);
	
		ResultMapNew resultMap = new ResultMapNew();
		String sql3 = "select * from ACT_HI_COMMENT left join ACT_HI_TASKINST on ACT_HI_COMMENT.TASK_ID_ = ACT_HI_TASKINST.ID_ where ACT_HI_TASKINST.PROC_INST_ID_ = '"+procInsId+"' ORDER by TIME_ DESC" ;// and ACT_HI_TASKINST.TASK_DEF_KEY_ in ("+preNodeStrings+")";
		List<Map> hisComments = null;
		try {
			hisComments = MybatisUtil.queryList("runtime.selectSql", sql3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
	
		for (int i = 0; i < hisComments.size(); i++) {
			String userId = hisComments.get(i).get("ASSIGNEE_")+"";
			SysUser user = sysformconfigService.getUserByUserid(userId);
			hisComments.get(i).put("ASSIGNEE_NAME", user.getUserName());
			String endTime = hisComments.get(i).get("END_TIME_")+"";
			hisComments.get(i).put("END_TIME_DATE",endTime);
		}
		
		resultMap.setRows(hisComments);
		resultMap.setTotal(hisComments.size());
		
		return resultMap;
	}
	
	
	/**
	 * 选择自由节点
	 * @param taskId 任务ID
	 * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
	 * @param comment 任务提交意见的内容
	 * @param vars 任务流程变量，如下
	 * 		vars.keys=flag,pass
	 * 		vars.values=1,true
	 * 		vars.types=S,B  @see com.thinkgem.jeesite.modules.act.utils.PropertyType
	 */
	@RequestMapping(value = "freeTask")
	@ResponseBody
	public List<ActHiActinst> freeTask(HttpServletRequest request, @RequestBody ActSaveDataParams params,Model model) {
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		String formId =params.getFormId();
		Map actFlowParams=params.getActFlowParams();
		Act act = new Act(); 
		act.setProcDefKey((String)actFlowParams.get("key"));
		act.setTaskId((String)actFlowParams.get("taskId"));
		act.setProcInsId((String)actFlowParams.get("procInsId"));

		String approveType = (String)actFlowParams.get("approveType");
		String rejectType = (String)actFlowParams.get("rejectType");
		
		ActHiActinstExample example = new ActHiActinstExample();
		com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
		criteria.andProcInstIdEqualTo(act.getProcInsId());
		List<ActHiActinst> actHiActinst = actHiActinstMapper.selectByExample(example);
		
		List<ActHiActinst> actHiActinst1 = new  ArrayList<ActHiActinst>();
		actHiActinst1.add(actHiActinst.get(0));
		for (int i = 1; i < actHiActinst.size(); i++) {
			if (!actHiActinst.get(i-1).getActName().equals(actHiActinst.get(i).getActName())) {
				actHiActinst1.add(actHiActinst.get(i));
			}
		}
		
		
		
		return actHiActinst1;//adminPath + "/act/task";
	}
	
	/**
	 * 流程转办
	 */
	@RequestMapping(value = "turnTo")
	public void turnTo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String procInsId = request.getParameter("procInsId");
		String taskDefKey = request.getParameter("taskDefKey");
		String userId = request.getParameter("userId");
		
		//taskService.setAssignee(taskId,userId); 
		ActHiActinstExample example = new ActHiActinstExample();
		com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
		criteria.andProcInstIdEqualTo(procInsId);
		List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
		
		ActRuTaskExample taskExample = new ActRuTaskExample();
		com.myehr.pojo.activiti.ActRuTaskExample.Criteria taskCriteria = taskExample.createCriteria();
		taskCriteria.andProcInstIdEqualTo(procInsId);
		taskCriteria.andTaskDefKeyEqualTo(taskDefKey);
		ActRuTask actRuTask = actRuTaskMapper.selectByExample(taskExample).get(0);
		actRuTask.setAssignee(userId);
		actRuTaskMapper.updateByPrimaryKey(actRuTask);
	}
	
	/**
	 * 读取带跟踪的图片
	 */
	@RequestMapping(value = "trace/photo")
	public void tracePhoto(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String procInsId = request.getParameter("procInsId");
		String type = request.getParameter("type");
		
		ActHiActinstExample example = new ActHiActinstExample();
		com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
		criteria.andProcInstIdEqualTo(procInsId);
		example.setOrderByClause("START_TIME_ DESC");
		List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
		
		String procDefId = (String)ActHiActinst.get(0).getProcDefId();
		String execId = (String)ActHiActinst.get(ActHiActinst.size()-1).getExecutionId();
		
		InputStream imageStream = null;
		if (type.equals("task")) {
			imageStream=actTaskService.tracePhotoTask(procDefId, execId,procInsId);
		}else {
			imageStream=actTaskService.tracePhotoLink(procDefId, execId,procInsId);
			
		}
		
		
		// 输出资源内容到相应对象
		byte[] b = new byte[1024];
		int len;
		while ((len = imageStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}
	
	/**
	 * 输出跟踪流程信息
	 * 
	 * @param processInstanceId
	 * @return
	 * @throws Exception
	 */
	@ResponseBody
	@RequestMapping(value = "trace/info/{proInsId}")
	public List<Map<String, Object>> traceInfo(@PathVariable("proInsId") String proInsId) throws Exception {
		List<Map<String, Object>> activityInfos = actTaskService.traceProcess(proInsId);
		return activityInfos;
	}

	/**
	 * 显示流程图
	 */
	@RequestMapping(value = "processPic")
	public void processPic(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String procInsId = request.getParameter("procInsId");
		ActHiActinstExample example = new ActHiActinstExample();
		com.myehr.pojo.activiti.ActHiActinstExample.Criteria criteria = example.createCriteria();
		criteria.andProcInstIdEqualTo(procInsId);
		List<ActHiActinst> ActHiActinst = actHiActinstMapper.selectByExample(example);
		
		String procDefId = (String)ActHiActinst.get(0).getProcDefId();
		
		ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
		String diagramResourceName = procDef.getDiagramResourceName();
		InputStream imageStream = repositoryService.getResourceAsStream(procDef.getDeploymentId(), diagramResourceName);
		byte[] b = new byte[1024];
		int len = -1;
		while ((len = imageStream.read(b, 0, 1024)) != -1) {
			response.getOutputStream().write(b, 0, len);
		}
	}
	
	/**
	 * 撤回流程
	 */
	@ResponseBody
	@RequestMapping(value = "withdrawTaskTo")
	public String[] withdrawTaskTo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String taskId = request.getParameter("taskId");
		String objId = request.getParameter("businessKey");
		String pId = request.getParameter("pId");
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		return actTaskService.withdrawTaskTo(taskId,userId,objId,pId);
	}
	
	/**
	 * 终止流程
	 */
	@ResponseBody
	@RequestMapping(value = "stopActTask")
	public String[] stopActTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String taskId = request.getParameter("taskId");
		String objId = request.getParameter("businessKey");
		String pId = request.getParameter("pId");
		HttpSession session = request.getSession();
		String userId = (BigDecimal)session.getAttribute("userid")+"";
		return actTaskService.stopActTask(taskId,userId,objId,pId);
		
	}
	 
	/**
	 * 获取跟踪信息
	 
	@RequestMapping(value = "processMap")
	public String processMap(String procDefId, String proInstId, Model model)
			throws Exception {
		List<ActivityImpl> actImpls = new ArrayList<ActivityImpl>();
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery().processDefinitionId(procDefId)
				.singleResult();
		ProcessDefinitionImpl pdImpl = (ProcessDefinitionImpl) processDefinition;
		String processDefinitionId = pdImpl.getId();// 流程标识
		ProcessDefinitionEntity def = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
				.getDeployedProcessDefinition(processDefinitionId);
		List<ActivityImpl> activitiList = def.getActivities();// 获得当前任务的所有节点
		List<String> activeActivityIds = runtimeService.getActiveActivityIds(proInstId);
		for (String activeId : activeActivityIds) {
			for (ActivityImpl activityImpl : activitiList) {
				String id = activityImpl.getId();
				if (activityImpl.isScope()) {
					if (activityImpl.getActivities().size() > 1) {
						List<ActivityImpl> subAcList = activityImpl
								.getActivities();
						for (ActivityImpl subActImpl : subAcList) {
							String subid = subActImpl.getId();
							logger.info("subImpl:" + subid);
							if (activeId.equals(subid)) {// 获得执行到那个节点
								actImpls.add(subActImpl);
								break;
							}
						}
					}
				}
				if (activeId.equals(id)) {// 获得执行到那个节点
					actImpls.add(activityImpl);
					logger.info(id);
				}
			}
		}
		model.addAttribute("procDefId", procDefId);
		model.addAttribute("proInstId", proInstId);
		model.addAttribute("actImpls", actImpls);
		return "modules/act/actTaskMap";
	}*/
	
	/**
	 * 删除任务
	 * @param taskId 流程实例ID
	 * @param reason 删除原因
	 */
	@RequiresPermissions("act:process:edit")
	@RequestMapping(value = "deleteTask")
	public String deleteTask(String taskId, String reason, RedirectAttributes redirectAttributes) {
		if (StringUtils.isBlank(reason)){
			//addMessage(redirectAttributes, "请填写删除原因");
		}else{
			actTaskService.deleteTask(taskId, reason);
			//addMessage(redirectAttributes, "删除任务成功，任务ID=" + taskId);
		}
		return "redirect:" + adminPath + "/act/task";
	}
}
