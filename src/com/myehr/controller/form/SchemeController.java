package com.myehr.controller.form;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.myehr.common.mybatis.MybatisUtil;
import com.myehr.common.mybatis.Pager;
import com.myehr.controller.form.parambean.CardformInitDataParams;
import com.myehr.controller.form.parambean.ResultMap;
import com.myehr.controller.form.parambean.SaveButtonParams;
import com.myehr.controller.form.parambean.SysRuleResultMap;
import com.myehr.controller.form.parambean.SysSchemeResultMap;
import com.myehr.mapper.formmanage.button.SysFormButtonCalculateMapper;
import com.myehr.mapper.formmanage.button.SysFormButtonRemoveMapper;
import com.myehr.mapper.formmanage.button.SysFormButtonSaveMapper;
import com.myehr.mapper.formmanage.form.SysFormButtonMapper;
import com.myehr.mapper.formmanage.form.SysFormCalculateSolutionMapper;
import com.myehr.mapper.formmanage.form.SysFormGeneralParamMapper;
import com.myehr.mapper.formmanage.form.SysRuleMapper;
import com.myehr.mapper.formmanage.form.SysSolutionFolderTreeMapper;
import com.myehr.mapper.formmanage.form.SysSolutionMapper;
import com.myehr.pojo.formmanage.button.SysFormButtonCalculate;
import com.myehr.pojo.formmanage.button.SysFormButtonCalculateExample;
import com.myehr.pojo.formmanage.button.SysFormButtonRemove;
import com.myehr.pojo.formmanage.button.SysFormButtonRemoveExample;
import com.myehr.pojo.formmanage.button.SysFormButtonSave;
import com.myehr.pojo.formmanage.button.SysFormButtonSaveExample;
import com.myehr.pojo.formmanage.form.SysFormButton;
import com.myehr.pojo.formmanage.form.SysFormButtonExample;
import com.myehr.pojo.formmanage.form.SysFormCalculateSolution;
import com.myehr.pojo.formmanage.form.SysFormGeneralParam;
import com.myehr.pojo.formmanage.form.SysFormGeneralParamExample;
import com.myehr.pojo.formmanage.form.SysRule;
import com.myehr.pojo.formmanage.form.SysRuleExample;
import com.myehr.pojo.formmanage.form.SysRuleWithBLOBs;
import com.myehr.pojo.formmanage.form.SysSolution;
import com.myehr.pojo.formmanage.form.SysSolutionExample;
import com.myehr.pojo.formmanage.form.SysSolutionFolderTree;
import com.myehr.pojo.formmanage.form.SysSolutionFolderTreeExample;
import com.myehr.service.impl.formmanage.form.SysformconfigService;
import com.myehr.service.impl.primaryKey.PrimaryKeyServiceImpl;
import com.myehr.service.primaryKey.PrimaryKeyService;
import com.myehr.test.dao.TMapperExt;

@Controller
@RequestMapping("/scheme")
public class SchemeController {
	private static Logger logger = LoggerFactory.getLogger(SchemeController.class);
	@Autowired
	private SysSolutionMapper sysSolutionMapper;
	
	@Autowired
	private SysFormButtonCalculateMapper sysFormButtonCalculateMapper;
	
	@Autowired
	private SysRuleMapper sysRuleMapper;
	
	@Autowired
	private SysFormCalculateSolutionMapper sysFormCalculateSolutionMapper;
	
	@Autowired
	private SysSolutionFolderTreeMapper sysSolutionFolderTreeMapper;
	
	@Autowired
	private SysFormButtonSaveMapper sysFormButtonSaveMapper;
	
	@Autowired
	private SysFormButtonRemoveMapper sysFormButtonRemoveMapper;
	@Autowired
    SysFormButtonMapper sysFormButtonMapper;
//	@Autowired
	@Resource(name = "TMapperExt")
	private TMapperExt tMapperExt;
	
//	@Autowired
	@Resource(name = "PrimaryKeyService")
	private PrimaryKeyService keyserviceImpl;
	
	@Autowired
	private SysFormGeneralParamMapper sysFormGeneralParamMapper;
	@Autowired
	private SysformconfigService sysformconfigService;
	
	@RequestMapping("/querySchemeList")
	public @ResponseBody SysSchemeResultMap querySchemeList(HttpServletRequest request, @RequestBody CardformInitDataParams params) throws Exception{
		Map requestParam = params.getRequestParam();
		Map filter = params.getFilter();
		String offset = params.getOffset();
		String limit = params.getLimit();
		//Map paramsMap = params.getParamsMap();
		Pager page = params.getPage();
		page = new Pager();
		if(offset==null&&limit==null){
			
		}else {
			int start=Integer.parseInt(offset);
			page.setStart(start); //不能设置为0
			page.setLimt(Integer.parseInt(limit));
		}
		
		String nodeId = (String) requestParam.get("nodeId");
		String searchValue = (String) filter.get("searchValue");
		
		
		SysSchemeResultMap resultMap=new SysSchemeResultMap();
		SysSolutionExample example = new SysSolutionExample();
		com.myehr.pojo.formmanage.form.SysSolutionExample.Criteria criteria = example.createCriteria();
		
		if (nodeId==null || nodeId.equals("root")) {
			
		}else {
			criteria.andFolderIdEqualTo(new BigDecimal(nodeId));
		}
		
		if (searchValue !="" && searchValue!=null) {
			criteria.andSolutionCodeLike("%"+searchValue+"%");
			example.or().andSolutionNameLike("%"+searchValue+"%");
		}
		
		List<SysSolution> sysSolutions = sysSolutionMapper.selectByExample(example);
		
		List<SysSolution> menuList = new ArrayList<SysSolution>();
		int end = 0;
		if (page.getStart()+page.getLimt()>sysSolutions.size()) {
			end = sysSolutions.size();
		}else {
			end = page.getStart()+page.getLimt();
		}
		
		for (int i = page.getStart(); i < end; i++) {
			menuList.add(sysSolutions.get(i));
		}

		resultMap.setTotal(sysSolutions.size());
		resultMap.setRows(menuList);
		return resultMap;
	}
	
	@RequestMapping("/querySchemeById")
	public @ResponseBody SysSolution queryAddButtonById(HttpServletRequest request) throws Exception{
		String solutionId = request.getParameter("solutionId");
		
		SysSolutionExample example = new SysSolutionExample();
		com.myehr.pojo.formmanage.form.SysSolutionExample.Criteria criteria = example.createCriteria();
		criteria.andSolutionIdEqualTo(Long.parseLong(solutionId));
		List<SysSolution> sysSolutionS = sysSolutionMapper.selectByExample(example);
		if (sysSolutionS.size()>0) {
			return sysSolutionS.get(0);
		}
		return null;
	}
	
	@RequestMapping("/querySchemeByButtonId")
	public @ResponseBody ResultMap querySchemeByButtonId(HttpServletRequest request, @RequestBody CardformInitDataParams params) throws Exception{
		Map requestParam = params.getRequestParam();
		Map filter = params.getFilter();
		String offset = params.getOffset();
		String limit = params.getLimit();
		//Map paramsMap = params.getParamsMap();
		Pager page = params.getPage();
		page = new Pager();
		if(offset==null&&limit==null){
			
		}else {
			int start=Integer.parseInt(offset);
			page.setStart(start); //不能设置为0
			page.setLimt(Integer.parseInt(limit));
		}
		
		String calculateId = (String) filter.get("calculateId");
		String searchValue = (String) filter.get("searchValue");
		
		String sql = "select t1.*,t2.solution_name,t2.solution_code from SYS_FORM_CALCULATE_SOLUTION t1 join Sys_Solution t2 on t2.solution_id = t1.solution_id where CALCULATE_ID = "+calculateId;
		List<Map> schemes = tMapperExt.queryByFormDefSql(sql);
		
		
		ResultMap resultMap=new ResultMap();
		
		
		List<Map> schemeList = new ArrayList<Map>();
		int end = 0;
		if (page.getStart()+page.getLimt()>schemes.size()) {
			end = schemes.size();
		}else {
			end = page.getStart()+page.getLimt();
		}
		
		for (int i = page.getStart(); i < end; i++) {
			schemeList.add(schemes.get(i));
		}

		resultMap.setTotal(schemes.size());
		resultMap.setRows(schemeList);
		return resultMap;
	}
	
	
	@RequestMapping("/querySysFormButtonCalculateById")
	public @ResponseBody SysFormButtonCalculate querySysFormButtonCalculateById(HttpServletRequest request) throws Exception{
		String userId = (BigDecimal)request.getSession().getAttribute("userid")+"";
		String calculateButtonId = request.getParameter("formButtonId");
		
		SysFormButtonCalculateExample example = new SysFormButtonCalculateExample();
		com.myehr.pojo.formmanage.button.SysFormButtonCalculateExample.Criteria criteria = example.createCriteria();
		criteria.andCalculateButtonIdEqualTo(new BigDecimal(calculateButtonId));
		List<SysFormButtonCalculate> sysFormButtonCalculateS = sysFormButtonCalculateMapper.selectByExample(example);
		if (sysFormButtonCalculateS.size()>0) {
			return sysFormButtonCalculateS.get(0);
		}else{
			SysFormButtonCalculate  calculate = new SysFormButtonCalculate();
			calculate.setCalculateButtonId(new BigDecimal(calculateButtonId));
			calculate.setCalculateRowCount(new BigDecimal(2));
			calculate.setCalculateWinTitle("待录参数");
			calculate.setCalculateWinHeight("300");
			calculate.setCalculateWinWidth("200");
			calculate.setOperatorName(userId);
			calculate.setOperatorTime(new Date());
			sysFormButtonCalculateMapper.insert(calculate);
			return sysFormButtonCalculateMapper.selectByExample(example).get(0);
		}
	}
	
	/**
	 * 保存计算按钮信息
	 * @param request
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/saveSysFormButtonCalculate")
	public @ResponseBody String SysFormButtonCalculate(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		String userId = (BigDecimal)request.getSession().getAttribute("userid")+"";
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		String calculateId = (String) param.get("calculateId")+"";
		SysFormButtonCalculate  calculate = new SysFormButtonCalculate();
		if (calculateId==null || calculateId.equals("")) {
			calculate.setCalculateButtonId(new BigDecimal((String) param.get("calculateButtonId")));
			calculate.setCalculateRowCount(new BigDecimal((String) param.get("calculateRowCount")));
			calculate.setCalculateWinTitle((String) param.get("calculateWinTitle"));
			calculate.setCalculateWinHeight((String) param.get("calculateWinHeight"));
			calculate.setCalculateWinWidth((String) param.get("calculateWinWidth"));
			calculate.setOperatorName(userId);
			calculate.setOperatorTime(new Date());
			reCode = sysFormButtonCalculateMapper.insert(calculate);
		}else {
			calculate.setCalculateId(new BigDecimal((String) param.get("calculateId")));
			calculate.setCalculateButtonId(new BigDecimal((String) param.get("calculateButtonId")));
			calculate.setCalculateRowCount(new BigDecimal((String) param.get("calculateRowCount")));
			calculate.setCalculateWinTitle((String) param.get("calculateWinTitle"));
			calculate.setCalculateWinHeight((String) param.get("calculateWinHeight"));
			calculate.setCalculateWinWidth((String) param.get("calculateWinWidth"));
			calculate.setOperatorName(userId);
			calculate.setOperatorTime(new Date());
			reCode = sysFormButtonCalculateMapper.updateByPrimaryKey(calculate);
		}
		
		return menuJson;
	}
	
	@RequestMapping("/saveScheme")
	public @ResponseBody String saveScheme(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		String solutionId = (String) param.get("solutionId");
		SysSolution sysSolution = new SysSolution();
		if (solutionId==null || solutionId=="") {
			sysSolution.setFolderId(new BigDecimal((String) param.get("folderId")));
			sysSolution.setSolutionCode((String) param.get("solutionCode"));
			sysSolution.setSolutionName((String) param.get("solutionName"));
			sysSolution.setSolutionParentId((String) param.get("solutionParentId"));
			sysSolution.setSolutionSort((String) param.get("solutionSort"));
			sysSolution.setOperatorName((String) paramsMap.get("userId"));
			sysSolution.setOperatorTime(new Date());
			reCode = sysSolutionMapper.insert(sysSolution);
		}else {
			SysSolutionExample example = new SysSolutionExample();
			com.myehr.pojo.formmanage.form.SysSolutionExample.Criteria criteria = example.createCriteria();
			criteria.andSolutionIdEqualTo(Long.parseLong(solutionId));
			List<SysSolution> sysSolutionS = sysSolutionMapper.selectByExample(example);
			if (sysSolutionS.size()>0) {
				sysSolution = sysSolutionS.get(0);
			}
			
			sysSolution.setSolutionId(Long.parseLong(solutionId));
			sysSolution.setFolderId(new BigDecimal((String) param.get("folderId")));
			sysSolution.setSolutionCode((String) param.get("solutionCode"));
			sysSolution.setSolutionName((String) param.get("solutionName"));
			sysSolution.setSolutionParentId((String) param.get("solutionParentId"));
			sysSolution.setSolutionSort((String) param.get("solutionSort"));
			reCode = sysSolutionMapper.updateByPrimaryKey(sysSolution);
		}
		
		return menuJson;
	}
	
	@RequestMapping("/queryFormulaList")
	public @ResponseBody SysRuleResultMap queryFormulaList(HttpServletRequest request, @RequestBody CardformInitDataParams params) throws Exception{
		Map requestParam = params.getRequestParam();
		Map filter = params.getFilter();
		String offset = params.getOffset();
		String limit = params.getLimit();
		//Map paramsMap = params.getParamsMap();
		Pager page = params.getPage();
		page = new Pager();
		if(offset==null&&limit==null){
			
		}else {
			int start=Integer.parseInt(offset);
			page.setStart(start); //不能设置为0
			page.setLimt(Integer.parseInt(limit));
		}
		
		String solutionId = (String) requestParam.get("solutionId");
		String searchValue = (String) filter.get("searchValue");
		
		
		SysRuleResultMap resultMap=new SysRuleResultMap();
		SysRuleExample example = new SysRuleExample();
		com.myehr.pojo.formmanage.form.SysRuleExample.Criteria criteria = example.createCriteria();
		
		if (searchValue !="" && searchValue!=null) {
			criteria.andRuleCodeLike("%"+searchValue+"%");
			example.or().andRuleNameLike("%"+searchValue+"%");
		}
		
		criteria.andRuleSolutionIdEqualTo(new BigDecimal(solutionId));
		example.setOrderByClause("RULE_SORT");
		List<SysRule> sysRules = sysRuleMapper.selectByExample(example);
		
		List<SysRule> ruleList = new ArrayList<SysRule>();
		int end = 0;
		if (page.getStart()+page.getLimt()>sysRules.size()) {
			end = sysRules.size();
		}else {
			end = page.getStart()+page.getLimt();
		}
		
		for (int i = page.getStart(); i < end; i++) {
			ruleList.add(sysRules.get(i));
		}

		resultMap.setTotal(sysRules.size());
		resultMap.setRows(ruleList);
		return resultMap;
	}
	
	@RequestMapping("/queryRuleById")
	public @ResponseBody Map queryRuleById(HttpServletRequest request) throws Exception{
		String ruleId = request.getParameter("ruleId");
		
		String sql = "select sys_rule.RULE_ID as ruleId, " +
				"sys_rule.RULE_CODE as ruleCode, " +
			    "sys_rule.RULE_INFO as ruleInfo, " +
			    "sys_rule.RULE_NAME as ruleName, " +
			    "sys_rule.RULE_STATUS as ruleStatus, " +
			    "sys_rule.RULE_JAVA_SQL as ruleJavaSql, " +
			    "sys_rule.RULE_EXE_SQL as ruleExeSql, " +
			    "sys_rule.RULE_SORT as ruleSort, " +
			    "sys_rule.OPERATOR_TIME as operatorTime, " +
			    "sys_rule.OPERATOR_NAME as operatorName, " +
			    "sys_rule.APPROVE_TIME as approveTime, " +
			    "sys_rule.APPROVE_NAME as approveName, " +
			    "sys_rule.RULE_SOLUTION_ID as ruleSolutionId, " +
			    "sys_rule.DELETE_MARK as deleteMark, " +
			    "sys_rule.RULE_ISEXC as ruleIsexc, " +
			    "sys_rule.RULE_FORM_ID as ruleFormId, " +
				"SYS_FORMCONFIG.form_def_name as ruleFormName "+
				" from sys_rule left join SYS_FORMCONFIG on sys_rule.RULE_FORM_ID =  SYS_FORMCONFIG.FORM_DEF_ID where sys_rule.RULE_ID = "+ruleId;
		List<Map> sysRuleS = tMapperExt.queryByFormDefSql(sql);
		
		
		if (sysRuleS.size()>0) {
			return sysRuleS.get(0);
		}
		return null;
	}
	
	
	@RequestMapping("/saveRule")
	public @ResponseBody String saveRule(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		String ruleId = (String) param.get("ruleId");
		SysRuleWithBLOBs sysRule = new SysRuleWithBLOBs();
		if (ruleId==null || ruleId=="") {
			String ruleFormId = (String) param.get("ruleFormId");
			if (ruleFormId!=null && !ruleFormId.equals("")) {
				sysRule.setRuleFormId(Integer.parseInt((String) param.get("ruleFormId")));
			}
			sysRule.setRuleSolutionId(new BigDecimal((String) param.get("ruleSolutionId")));
			sysRule.setRuleCode((String) param.get("ruleCode"));
			sysRule.setRuleName((String) param.get("ruleName"));
			sysRule.setRuleSort(new BigDecimal((String) param.get("ruleSort")));
			sysRule.setRuleIsexc((String) param.get("ruleIsexc"));
			sysRule.setRuleExeSql((String) param.get("ruleExeSql"));
			sysRule.setOperatorName((String) paramsMap.get("userId"));
			sysRule.setOperatorTime(new Date());
			reCode = sysRuleMapper.insert(sysRule);
		}else {
			SysRuleExample example = new SysRuleExample();
			com.myehr.pojo.formmanage.form.SysRuleExample.Criteria criteria = example.createCriteria();
			criteria.andRuleIdEqualTo(Long.parseLong(ruleId));
			List<SysRuleWithBLOBs> sysRuleS = sysRuleMapper.selectByExampleWithBLOBs(example);
			if (sysRuleS.size()>0) {
				sysRule = sysRuleS.get(0);
			}
			sysRule.setRuleId(Long.parseLong(ruleId));
			sysRule.setRuleSolutionId(new BigDecimal((String) param.get("ruleSolutionId")));
			sysRule.setRuleCode((String) param.get("ruleCode"));
			sysRule.setRuleName((String) param.get("ruleName"));
			sysRule.setRuleSort(new BigDecimal((String) param.get("ruleSort")));
			sysRule.setRuleIsexc((String) param.get("ruleIsexc"));
			sysRule.setRuleExeSql((String) param.get("ruleExeSql"));
			sysRule.setRuleJavaSql((String) param.get("ruleExeSql"));
			String formId = (String) param.get("ruleFormId");
			if (formId!=null&&!formId.equals("")) {
				sysRule.setRuleFormId(Integer.parseInt(formId));
			}
			sysRule.setOperatorName((String) paramsMap.get("userId"));
			sysRule.setOperatorTime(new Date());
			reCode = sysRuleMapper.updateByPrimaryKeyWithBLOBs(sysRule);
		}
		String sql = param.get("ruleExeSql")+"";
		saveSqlParam(sql,ruleId,(String) paramsMap.get("userId"));
		int a= Integer.parseInt((String) param.get("ruleSolutionId"));
		String sql1="SELECT sb.FORM_BUTTON_FORM_DEF_ID  FROM " 
			   +" SYS_FORM_BUTTON sb "
	           +" JOIN SYS_FORM_BUTTON_CALCULATE SBC ON SB.FORM_BUTTON_ID= SBC.CALCULATE_BUTTON_ID"
	           +" JOIN SYS_FORM_CALCULATE_SOLUTION SS ON SS.CALCULATE_ID = SBC.CALCULATE_ID"
	           +" JOIN SYS_SOLUTION SN ON SN.SOLUTION_ID = SS.SOLUTION_ID "
	           +" WHERE SN.SOLUTION_ID= "+a;
		List<Map> maps=MybatisUtil.queryList("runtime.selectSql", sql1);
		for(Map map:maps){
			String formId= map.get("FORM_BUTTON_FORM_DEF_ID")+"";
			sysformconfigService.setFormbuttons(formId);
			SysFormButtonExample example1 = new SysFormButtonExample();
			example1.or().andFormButtonFormDefIdEqualTo(new BigDecimal(formId));
			List<SysFormButton> formButtons = sysFormButtonMapper.selectByExample(example1);
			for (int i = 0; i < formButtons.size(); i++) {
				SysFormButton button = formButtons.get(i);
				String formButtonId = button.getFormButtonId()+"";
				if ("calculate".equals(button.getFormButtonType())) {//判断计算按钮，并刷新计算按钮缓存
					sysformconfigService.setButtonCalculate(formButtonId);
				}
				
			}
			sysformconfigService.setForm(formId);
		}

		return menuJson;
	}
	
	private void saveSqlParam(String sql,String ruleId,String userId) {
		// TODO Auto-generated method stub
		//获取所有实体字段名并替换为表字段名
		//String reg = "\\[.+\\.*?\\]";
		String reg = "\\[[^\\[\\]\\n]*\\]";
		Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(sql);
        //删除计算公式对应参数
        SysFormGeneralParamExample example = new SysFormGeneralParamExample();
        example.or().andParamTypeEqualTo("CALCULATE").andParamTypeValueEqualTo(new BigDecimal(ruleId));
        sysFormGeneralParamMapper.deleteByExample(example);
        List<SysFormGeneralParam> params = new ArrayList<SysFormGeneralParam>();
        while(m .find()){
        	   SysFormGeneralParam param = new SysFormGeneralParam();
        	   String ret = m.group();
        	   String value = ret.substring(ret.indexOf("[")+1,ret.indexOf("]"));
        	   String[] paramStr = value.split(":");
        	   if(!paramStr[0].equals("s")){
        		   param.setParamName(value);
            	   param.setParamType("CALCULATE");
            	   param.setParamTypeValue(new BigDecimal(ruleId));
            	   param.setParamFromType("计算参数");
            	   param.setParamFromValue(value);
            	   param.setOperatorName(userId);
            	   param.setOperatorTime(new Date());
            	   sysFormGeneralParamMapper.insert(param);
            	   params.add(param);
        	   }
        }
        try {
			sysformconfigService.setGenParamByRuleId(ruleId,params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();logger.error(e.getMessage(),e);
		}
	}

	@RequestMapping("/deleteRule")
	public @ResponseBody String deleteRule(HttpServletRequest request) throws Exception{
		String menuJson = "";
		
		List<Long> values = new ArrayList<Long>();
		String ruleIds = (String) request.getParameter("ruleIds");
		String[] aa = ruleIds.split(",");
		for (int i = 0; i < aa.length; i++) {
			values.add(Long.parseLong(aa[i]));
		}
		SysRuleExample example = new SysRuleExample();
		example.or().andRuleIdIn(values);
		sysRuleMapper.deleteByExample(example);
		menuJson = "0";
		return menuJson;
	}
	
	/**
	 * 保存选择方案
	 * @param request
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/saveSelectScheme")
	public @ResponseBody String saveSelectScheme(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		List<Map> selectdatas = (List<Map>) paramsMap.get("selectdata");
		String calculateId = (String) paramsMap.get("calculateId");
		String userId = (String) paramsMap.get("userId");
		
		String sql = "select t1.*,t2.solution_name,t2.solution_code from SYS_FORM_CALCULATE_SOLUTION t1 join Sys_Solution t2 on t2.solution_id = t1.solution_id where CALCULATE_ID = "+calculateId;
		List<Map> schemes = tMapperExt.queryByFormDefSql(sql);
		
		for (int i = 0; i < selectdatas.size(); i++) {
			Map data = selectdatas.get(i);
			int flag = 0;
			SysFormCalculateSolution calculateSolution = new SysFormCalculateSolution();
			for (int j = 0; j < schemes.size(); j++) {
				Map scheme = schemes.get(j);
				String solutionCode=(String)data.get("solutionCode");
				String solutionCode1=(String)scheme.get("solution_code");
				logger.info(solutionCode+"******"+solutionCode1 );
				if (solutionCode.equals(solutionCode1)) {
					flag=1;
				}
			}
			if (flag==0) {
				calculateSolution.setCalculateId(new BigDecimal(calculateId));
				calculateSolution.setSolutionId(new BigDecimal((Integer)data.get("solutionId")));
				calculateSolution.setSort(new BigDecimal(1));
				calculateSolution.setOperatorName(userId);
				calculateSolution.setOperatorTime(new Date());
				reCode = sysFormCalculateSolutionMapper.insert(calculateSolution);
			}
			
		}
		
		return menuJson;
	}
	
	/**
	 * 删除选择方案
	 * @param request
	 * @param params
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/deleteSelectScheme")
	public @ResponseBody String deleteSelectScheme(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		List<Map> selectdatas = (List<Map>) paramsMap.get("selectdata");
		
		for (int i = 0; i < selectdatas.size(); i++) {
			reCode = sysFormCalculateSolutionMapper.deleteByPrimaryKey(new BigDecimal((Integer)selectdatas.get(i).get("CALCULATE_SOLUTION_ID")));
		}
		
		return reCode+"";
	}
	
	@RequestMapping("/querySaveButtonById")
	public @ResponseBody SysFormButtonSave querySaveButtonById(HttpServletRequest request) throws Exception{
		String buttonSaveButtonId = request.getParameter("buttonSaveButtonId");
		
		SysFormButtonSaveExample example = new SysFormButtonSaveExample();
		com.myehr.pojo.formmanage.button.SysFormButtonSaveExample.Criteria criteria = example.createCriteria();
		criteria.andButtonSaveButtonIdEqualTo(new BigDecimal(buttonSaveButtonId));
		List<SysFormButtonSave> buttonSaves = sysFormButtonSaveMapper.selectByExample(example);
		if (buttonSaves.size()>0) {
			return buttonSaves.get(0);
		}
		return null;
	}
	
	@RequestMapping("/saveSaveButton")
	public @ResponseBody String saveSaveButton(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		String buttonSaveId = (String) param.get("buttonSaveId");
		SysFormButtonSave buttonSave = new SysFormButtonSave();
		if (buttonSaveId==null || buttonSaveId=="") {
			buttonSave.setButtonSaveButtonId(new BigDecimal((String) param.get("buttonSaveButtonId")));
			buttonSave.setButtonSaveExcType((String) param.get("buttonSaveExcType"));
			buttonSave.setButtonSaveSuccessDeal((String) param.get("buttonSaveSuccessDeal"));
			buttonSave.setButtonSaveInitFun((String) param.get("buttonSaveInitFun"));
			buttonSave.setButtonSaveFun((String) param.get("buttonSaveFun"));
			buttonSave.setButtonSaveSuccessFun((String) param.get("buttonSaveSuccessFun"));
			reCode = sysFormButtonSaveMapper.insert(buttonSave);
		}else {
			buttonSave.setButtonSaveId(new BigDecimal(buttonSaveId));
			buttonSave.setButtonSaveButtonId(new BigDecimal((String) param.get("buttonSaveButtonId")));
			buttonSave.setButtonSaveExcType((String) param.get("buttonSaveExcType"));
			buttonSave.setButtonSaveSuccessDeal((String) param.get("buttonSaveSuccessDeal"));
			buttonSave.setButtonSaveInitFun((String) param.get("buttonSaveInitFun"));
			buttonSave.setButtonSaveFun((String) param.get("buttonSaveFun"));
			buttonSave.setButtonSaveSuccessFun((String) param.get("buttonSaveSuccessFun"));
			reCode = sysFormButtonSaveMapper.updateByPrimaryKey(buttonSave);
		}
		
		return menuJson;
	}
	
	
	@RequestMapping("/queryRemoveButtonById")
	public @ResponseBody SysFormButtonRemove queryRemoveButtonById(HttpServletRequest request) throws Exception{
		String buttonRemoveButtonId = request.getParameter("buttonRemoveButtonId");
		
		SysFormButtonRemoveExample example = new SysFormButtonRemoveExample();
		com.myehr.pojo.formmanage.button.SysFormButtonRemoveExample.Criteria criteria = example.createCriteria();
		criteria.andButtonRemoveButtonIdEqualTo(new BigDecimal(buttonRemoveButtonId));
		List<SysFormButtonRemove> buttonRemoves = sysFormButtonRemoveMapper.selectByExample(example);
		if (buttonRemoves.size()>0) {
			return buttonRemoves.get(0);
		}
		return null;
	}
	
	@RequestMapping("/saveRemoveButton")
	public @ResponseBody String saveRemoveButton(HttpServletRequest request, @RequestBody SaveButtonParams params) throws Exception{
		String menuJson = "";
		
		Map param = params.getParam();
		Map paramsMap = params.getParamsMap();
		int reCode = 0;
		
		String buttonRemoveId = (String) param.get("buttonRemoveId");
		SysFormButtonRemove buttonRemove = new SysFormButtonRemove();
		if (buttonRemoveId==null || buttonRemoveId=="") {
			buttonRemove.setButtonRemoveButtonId(new BigDecimal((String) param.get("buttonRemoveButtonId")));
			buttonRemove.setButtonRemoveType((String) param.get("buttonRemoveType"));
			buttonRemove.setButtonRemoveExcType((String) param.get("buttonRemoveExcType"));
			buttonRemove.setButtonRemoveInitFun((String) param.get("buttonRemoveInitFun"));
			buttonRemove.setButtonRemoveFun((String) param.get("buttonRemoveFun"));
			reCode = sysFormButtonRemoveMapper.insert(buttonRemove);
		}else {
			buttonRemove.setButtonRemoveId(new BigDecimal(buttonRemoveId));
			buttonRemove.setButtonRemoveButtonId(new BigDecimal((String) param.get("buttonRemoveButtonId")));
			buttonRemove.setButtonRemoveType((String) param.get("buttonRemoveType"));
			buttonRemove.setButtonRemoveExcType((String) param.get("buttonRemoveExcType"));
			buttonRemove.setButtonRemoveInitFun((String) param.get("buttonRemoveInitFun"));
			buttonRemove.setButtonRemoveFun((String) param.get("buttonRemoveFun"));
			reCode = sysFormButtonRemoveMapper.updateByPrimaryKey(buttonRemove);
		}
		
		return menuJson;
	}
	
	
	@RequestMapping("/buildSchemeTree")
	public @ResponseBody List<Map> buildSchemeTree(HttpServletRequest request) throws Exception{
		List<Map> results = new ArrayList<Map>();
		
		SysSolutionExample example = new SysSolutionExample();
		com.myehr.pojo.formmanage.form.SysSolutionExample.Criteria criteria = example.createCriteria();
		List<SysSolution> sysSolutionS = sysSolutionMapper.selectByExample(example);
		
		SysRuleExample example1 = new SysRuleExample();
		com.myehr.pojo.formmanage.form.SysRuleExample.Criteria criteria1 = example1.createCriteria();
		List<SysRule> sysRules = sysRuleMapper.selectByExample(example1);
		
		SysSolutionFolderTreeExample example2 = new SysSolutionFolderTreeExample();
		com.myehr.pojo.formmanage.form.SysSolutionFolderTreeExample.Criteria criteria2 = example2.createCriteria();
		List<SysSolutionFolderTree> solutionFolderTrees = sysSolutionFolderTreeMapper.selectByExample(example2);
		
		Map mapRoot=new HashMap();
		mapRoot.put("expanded", false);
		mapRoot.put("isLeaf", false);
		mapRoot.put("isParent", false); 
		mapRoot.put("open", false); 
		mapRoot.put("nodeId", "root");
		mapRoot.put("nodeName", "方案管理树");
		mapRoot.put("nodeCode", "root");
		mapRoot.put("nodeType", "Default");
		mapRoot.put("parentCode", "0");
		mapRoot.put("ORG_DEL", "N");
		results.add(mapRoot);
		
		for (int i = 0; i < solutionFolderTrees.size(); i++) {
			String parentCode = "";
			if (solutionFolderTrees.get(i).getFolderTreeParentId()==null) {
				parentCode = "root";
			}else {
				parentCode = "folder"+solutionFolderTrees.get(i).getFolderTreeParentId()+"";
			}
			Map map=new HashMap();
			map.put("expanded", false);
			map.put("isLeaf", false);
			map.put("isParent", false); 
			map.put("open", false);
			map.put("nodeId", "folder"+solutionFolderTrees.get(i).getFolderTreeId());
			map.put("nodeName", solutionFolderTrees.get(i).getFolderTreeName());
			map.put("nodeCode", solutionFolderTrees.get(i).getFolderTreeCode());
			map.put("nodeDesc", solutionFolderTrees.get(i).getFolderTreeDesc());
			map.put("nodeType", "N");
			map.put("parentCode", parentCode);
			results.add(map); 
		}
		
		for (int i = 0; i < sysSolutionS.size(); i++) {
			Map map=new HashMap();
			map.put("expanded", false);
			map.put("isLeaf", false);
			map.put("isParent", false); 
			map.put("open", false);
			map.put("nodeId", "solution"+sysSolutionS.get(i).getSolutionId());
			map.put("nodeName", sysSolutionS.get(i).getSolutionName());
			map.put("nodeCode", sysSolutionS.get(i).getSolutionCode());
			map.put("nodeType", "F");
			map.put("parentCode", "folder"+sysSolutionS.get(i).getFolderId());
			results.add(map);
		}
		
		for (int i = 0; i < sysRules.size(); i++) {
			Map map=new HashMap();
			map.put("expanded", false);
			map.put("isLeaf", false);
			map.put("isParent", false); 
			map.put("open", false);
			map.put("nodeId", sysRules.get(i).getRuleId());
			map.put("nodeName", sysRules.get(i).getRuleName());
			map.put("nodeCode", sysRules.get(i).getRuleCode());
			map.put("nodeType", "T");
			map.put("parentCode", "solution"+sysRules.get(i).getRuleSolutionId());
			results.add(map);
		}
		return results;
	}
	
	
	@RequestMapping("/deleteScheme")
	public @ResponseBody String deleteScheme(HttpServletRequest request) throws Exception{
		String reCode = "";
		
		String solutionId = request.getParameter("solutionId");
		String[] solutionIds = solutionId.split(",");
		for (int i = 0; i < solutionIds.length; i++) {
			reCode = sysSolutionMapper.deleteByPrimaryKey(Long.parseLong(solutionIds[i]))+"";
			
			SysRuleExample example = new SysRuleExample();
			com.myehr.pojo.formmanage.form.SysRuleExample.Criteria criteria = example.createCriteria();
			criteria.andRuleSolutionIdEqualTo(new BigDecimal(solutionIds[i]));
			sysRuleMapper.deleteByExample(example);
		}	
		return reCode;
	}
	
	@RequestMapping("/addTreeSolutionFolder")
	public @ResponseBody String addTreeSchemeFolder(HttpServletRequest request,@RequestBody SysSolutionFolderTree folderTree) throws Exception{
		String reCode = "0";
		SysSolutionFolderTreeExample example = new SysSolutionFolderTreeExample();
		String userId = (String)(request.getSession().getAttribute("userid")+"");
		example.or().andFolderTreeCodeEqualTo(folderTree.getFolderTreeCode());
		if (folderTree.getFolderTreeId()!=null) {
			List<SysSolutionFolderTree> fList = sysSolutionFolderTreeMapper.selectByExample(example);
			if (fList.size()>0&&folderTree.getFolderTreeId().toString().equals(fList.get(0).getFolderTreeId().toString())) {
				fList.get(0).setFolderTreeCode(folderTree.getFolderTreeCode());
				fList.get(0).setFolderTreeName(folderTree.getFolderTreeName());
				fList.get(0).setFolderTreeDesc(folderTree.getFolderTreeDesc());
				fList.get(0).setOperatorName(userId);
				fList.get(0).setOperatorTime(new Date());
				sysSolutionFolderTreeMapper.updateByPrimaryKey(fList.get(0));
				return reCode;
			} else if (fList.size()==0) {
				SysSolutionFolderTree folderTree2 = sysSolutionFolderTreeMapper.selectByPrimaryKey(folderTree.getFolderTreeId());
				folderTree2.setFolderTreeCode(folderTree.getFolderTreeCode());
				folderTree2.setFolderTreeName(folderTree.getFolderTreeName());
				folderTree2.setFolderTreeDesc(folderTree.getFolderTreeDesc());
				folderTree2.setOperatorName(userId);
				folderTree2.setOperatorTime(new Date());
				sysSolutionFolderTreeMapper.updateByPrimaryKey(folderTree2);
				return reCode;
			} else {
				return "1";//树节点编码重复
			}
		} else {
			if(sysSolutionFolderTreeMapper.selectByExample(example).size()>0){
				return "1";//树节点编码重复
			}else {
				folderTree.setOperatorName(userId);
				folderTree.setOperatorTime(new Date());
				if (folderTree.getFolderTreeParentId()!=null&&folderTree.getFolderTreeParentId().intValue()!=0) {
					
				}else{
					folderTree.setFolderTreeParentId(null);
				}
				sysSolutionFolderTreeMapper.insert(folderTree);
				SysSolutionFolderTree folderTree2 = sysSolutionFolderTreeMapper.selectByExample(example).get(0);
				folderTree2.setFolderTreeSeq(folderTree2.getFolderTreeId()+".");
				sysSolutionFolderTreeMapper.updateByPrimaryKey(folderTree2);
				return reCode;
			}
		}
	}
	
	
	//删除树节点
		@RequestMapping("/deleteTreeSolutionFolder")
		public @ResponseBody String deleteTreeSolutionFolder(HttpServletRequest request) throws Exception{
			String reCode = "0";
			String folderTreeId = request.getParameter("folderTreeId");
			sysSolutionFolderTreeMapper.deleteByPrimaryKey(new BigDecimal(folderTreeId));
			return reCode;
		}
	
}