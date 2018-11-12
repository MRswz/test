package com.myehr.service.impl.formmanage.runtime;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.myehr.common.exception.DcfException;
import com.myehr.common.mybatis.MybatisUtil;
import com.myehr.common.util.ChangeCode;
import com.myehr.common.util.MyEhrJDBCUtil;
import com.myehr.common.util.SqlJection;
import com.myehr.common.util.StringUtils;
import com.myehr.pojo.field.SysField;
import com.myehr.pojo.formmanage.cache.SysCardFormColumnUtil;
import com.myehr.pojo.formmanage.cache.SysDatepickerCache;
import com.myehr.pojo.formmanage.cache.SysFormColumnCache;
import com.myehr.pojo.formmanage.cache.SysFormComboboxCache;
import com.myehr.pojo.formmanage.cache.SysFormGeneralExecSqlCache;
import com.myehr.pojo.formmanage.cache.SysFormWhereCache;
import com.myehr.pojo.formmanage.cache.SysFormconfigCache;
import com.myehr.pojo.formmanage.cache.SysGridFilterCache;
import com.myehr.pojo.formmanage.cache.SysGridFilterColumnCache;
import com.myehr.pojo.formmanage.cache.UserObject;
import com.myehr.pojo.sysParam.SysRequestParam;
import com.myehr.service.primaryKey.PrimaryKeyService;
import com.myehr.service.sysUserRole.sysUserRoleService;



public class RuntimeUtil {
	private static Logger logger = LoggerFactory.getLogger(RuntimeUtil.class);
	//private static final Logger log = DcfUtil.getLogger(RuntimeUtil.class);
	public static String dataBaseType ;
	static{
		Connection conn = null;
		//DriverManagerDataSource 
		try {
			BasicDataSource dataSource =  (BasicDataSource)MybatisUtil.getBasicDataSource();
			conn = dataSource.getConnection();
			DatabaseMetaData metadata = conn.getMetaData();
			if(metadata.getDatabaseProductName().indexOf("Oracle") != -1){
				dataBaseType = "Oracle";
			}
		} catch (SQLException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();logger.error(e.getMessage(),e);
		}finally{
			if(conn!=null){
				try {
					conn.close();
					conn = null;
				} catch (SQLException e) {
					// TODO 自动生成 catch 块
					e.printStackTrace();logger.error(e.getMessage(),e);
				}
			}
		}
	}
	
	/**
	 * json转map
	 * @param json
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Map<String,Object> jsonToMap(String json){
		JSONObject mapjson = JSONObject.fromObject(json);
		Iterator<String> it = mapjson.keys();
		Map<String,Object> params = new HashMap<String,Object>();
		while(it.hasNext()){
			String key = it.next();
			String value = mapjson.getString(key);
			params.put(key, value);
		}
		return params;
	}
	
	/**
	 * 获取检查id是否存在的sql
	 * @param pkname
	 * @param pkValue
	 * @param tablename
	 * @return
	 */
	public static String getCheckSqlPkDataSql(String pkname,String pkValue,String tablename){
		
		return "select 1 from "+tablename +" where "+ChangeCode.getRealCode(pkname) +"="+pkValue;
	}
	


	public static String getInsertSqlImport(List<String> insert,List<String> values,List<SysField> fields,String tablename,HttpSession session,String sourceType) throws Exception{

		String productName =sourceType.toUpperCase();
		String insertStr = "";
		String valuesStr = "";
		for(int i=0; i<insert.size(); i++){
			String fieldName = insert.get(i);
			//获取当前字段的数据类型 
			String fieldType = getFieldByFieldTablenameImport(fieldName,fields);
			Object value = values.get(i);
			//logger.info(fieldName);
			String tempValue = null;
			String tempField = ChangeCode.getUniqueCode(tablename, fieldName)+",";
			if("ORACLE".equalsIgnoreCase(productName)) { //oracle数据库
				if(value==null||"".equals(value)||"-".equals(value) ){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValue(fieldType,value+"",null,true,session,null,sourceType);
				}
			}else if("SQLSERVER".equalsIgnoreCase(productName)){
				if(value==null||"".equals(value)){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValueMsSql(fieldType,value+"",null,true,session,null,sourceType);
				}
			}
			insertStr+=ChangeCode.getRealCode(tempField);
			
			valuesStr+=tempValue+",";
			
		}
		insertStr = insertStr.substring(0,insertStr.length()-1);
		valuesStr = valuesStr.substring(0,valuesStr.length()-1);
		return "INSERT INTO "+tablename+" ("+insertStr+") VALUES ("+valuesStr+")";
	}
	
	public static String getFieldByFieldTablenameImport(String fieldTablename,List<SysField> fields ){
		for(int i=0;i<fields.size(); i++){
			if(fieldTablename.equalsIgnoreCase(ChangeCode.getRealCode(fields.get(i).getFieldTablename()))){
				return fields.get(i).getFieldType();
			}
		}
		
		return null;
	}
	
	public static String getIntroduceInsertSql(List<String> insert,List<String> values,List<SysField> fields,String tablename,HttpSession session) throws Exception{
 
		String productName =(String)session.getAttribute("productName");
		String insertStr = "";
		String valuesStr = "";
		for(int i=0; i<insert.size(); i++){
			String fieldName = insert.get(i);
			//获取当前字段的数据类型 
			String fieldType = getFieldByFieldTablename(fieldName.toUpperCase(),fields);
			
			Object value = values.get(i);
			logger.info(fieldName);
			String tempValue = null;
			String tempField = fieldName+",";
			if("Oracle".equalsIgnoreCase(productName)) { //oracle数据库
				if(value==null||"".equals(value)||"-".equals(value) ){
					tempValue = "null";
				}else {
					//gfdgfdg
					if(fieldType.toUpperCase().equalsIgnoreCase("DATETIME")||fieldType.toUpperCase().equalsIgnoreCase("SMALLDATETIME")){
						String format = "yyyy-MM-dd HH:mm:ss";
						SimpleDateFormat sdf = new SimpleDateFormat(format);
						tempValue = getFieldValue(fieldType,sdf.format(new Date(Long.valueOf(value+""))),null,true,session,null,"sqlserver");
					}else{
						tempValue = getFieldValue(fieldType,value+"",null,true,session,null,"sqlserver");
					}
				}
			}else if("SqlServer".equalsIgnoreCase(productName)){
				if(value==null||"".equals(value)){
					tempValue = "null,";
				}else {
					//gfdgfdg
					tempValue = getFieldValueMsSql(fieldType,value+"",null,true,session,null,"sqlserver");
				}
			}
			insertStr+=ChangeCode.getRealCode(tempField);
			
			valuesStr+=tempValue+",";
			
		}
		insertStr = insertStr.substring(0,insertStr.length()-1);
		valuesStr = valuesStr.substring(0,valuesStr.length()-1);
		return "INSERT INTO "+tablename+" ("+insertStr+") VALUES ("+valuesStr+")";
	}
	
	public static String getInsertSql(List<String> insert,List<String> values,List<SysField> fields,String tablename,HttpSession session,String sourceType) throws Exception{

		String productName =sourceType.toUpperCase();
		String insertStr = "";
		String valuesStr = "";
		for(int i=0; i<insert.size(); i++){
			String fieldName = insert.get(i);
			//获取当前字段的数据类型 
			String fieldType = getFieldByFieldTablename(fieldName,fields);
			Object value = values.get(i);
			logger.info(fieldName);
			String tempValue = null;
			String tempField = fieldName+",";
			if("ORACLE".equalsIgnoreCase(productName)) { //oracle数据库
				if(value==null||"".equals(value)||"-".equals(value) ){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValue(fieldType,value+"",null,true,session,null,sourceType);
				}
			}else if("SQLSERVER".equalsIgnoreCase(productName)){
				if(value==null||"".equals(value)){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValueMsSql(fieldType,value+"",null,true,session,null,sourceType);
				}
			}
			insertStr+=ChangeCode.getRealCode(tempField);
			
			valuesStr+=tempValue+",";
			
		}
		SqlJection sql=new SqlJection();
		insertStr = insertStr.substring(0,insertStr.length()-1);
		valuesStr = valuesStr.substring(0,valuesStr.length()-1);
		String[] valuesStrs=valuesStr.split(",");
		boolean flag=false;
		for(int i=0;i<valuesStrs.length;i++){
			String unchek=valuesStrs[i].replace("'", "");
			System.out.println(unchek);
			if(!sql.isValid(unchek)){
				flag=true;
				break;
			}
		}
		if(!flag){
			  return "INSERT INTO "+tablename+" ("+insertStr+") VALUES ("+valuesStr+")";
			}
		return null;
	}
	
	public static String getFieldByFieldTablename(String fieldTablename,List<SysField> fields ){
		for(int i=0;i<fields.size(); i++){
			if(fieldTablename.equalsIgnoreCase(fields.get(i).getFieldTablename().toUpperCase())){
				return fields.get(i).getFieldType();
			}
		}
		
		return null;
	}
	
	/**
	 * 此方法针对Oraccle数据库
	 * 
	 * @param fieldType 数据库字段类型 
	 * @param value 字段值
	 * @param joinRule where条件中的连接规则
	 * @param b 是否在末尾增加","
	 * @return
	 * @throws Exception
	 */
	public static String  getFieldValue(String fieldType,String value,String cpRule,boolean b,HttpSession session,Map<String, String> containerParam,String sourceType) throws Exception{
		String productName = sourceType.toUpperCase();
		if(productName.equalsIgnoreCase("ORACLE")){
			String tempValue =null;
			String d = "";
			if(!b){
				d = "";
			}
			if(fieldType.indexOf("TIMESTAMP")>=0||"TIMESTAMP".equalsIgnoreCase(fieldType)||"datetime".equalsIgnoreCase(fieldType)||"DATETIME".equalsIgnoreCase(fieldType)||"DATE".equalsIgnoreCase(fieldType)||"date".equalsIgnoreCase(fieldType)||"smalldatetime".equalsIgnoreCase(fieldType)||"SMALLDATETIME".equalsIgnoreCase(fieldType)){ //日期格式处理
				String reg1 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyy-mmm-dd hh:mm:ss" 格式的日期
				String reg2 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyy/mmm/dd hh:mm:ss" 格式的日期
				String reg1_1 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}"; //"yyyy-mmm-dd hh:mm" 格式的日期
				String reg2_1 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}"; //"yyyy/mmm/dd hh:mm:ss" 格式的日期
				String reg1_3 = "[0-9]{1,2}:[0-9]{1,2}"; //"hh:mm" 格式的日期
				String reg1_2 = "[0-9]{4}[0-9]{1,2}[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyymmmdd hh:mm:ss" 格式的日期
				String reg3 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}";//"yyyy-mmm-dd" 格式的日期
				String reg4 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}";//"yyyy/mmm/dd" 格式的日期
				String reg5 = "[0-9]{4}[0-9]{1,2}[0-9]{1,2}";//"yyyymmmdd" 格式的日期
				Pattern p = Pattern.compile(reg1);
				Matcher m = p.matcher(value.toString());
			
				if(m.find()){
					 String ret = m.group();
					 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
					 //tempValue = " cast('"+ret+"' as datetime)";
				}else {
					p = Pattern.compile(reg1_1);
					m = p.matcher(value.toString());
					if(m.find()){
						 String ret = m.group();
						 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
						 //tempValue = " cast('"+ret+"' as datetime)";
					}else {
						p = Pattern.compile(reg2);
						m = p.matcher(value.toString());
						if(m.find()){
							 String ret = m.group();
							 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
							 //tempValue = " cast('"+ret+"' as datetime)";
						}else {
							p = Pattern.compile(reg2_1);
							m = p.matcher(value.toString());
							if(m.find()){
								 String ret = m.group();
								 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
								 //tempValue = " cast('"+ret+"' as datetime)";
							}else {
								p = Pattern.compile(reg3);
								m = p.matcher(value.toString());
								if(m.find()){
									 String ret = m.group();
									 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
									 //tempValue = " cast('"+ret+"' as datetime)";
									 
								}else {
									p = Pattern.compile(reg4);
									m = p.matcher(value.toString());
									if(m.find()){
										 String ret = m.group();
										 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
										 //tempValue = " cast('"+ret+"' as datetime)";
									}else {
										p = Pattern.compile(reg5);
										m = p.matcher(value.toString());
										if(m.find()){
											 String ret = m.group();
											 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
											 //tempValue = " cast('"+ret+"' as datetime)";
										}else {
											p = Pattern.compile(reg1_2);
											m = p.matcher(value.toString());
											if(m.find()){
												 String ret = m.group();
												 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
												 //tempValue = " cast('"+ret+"' as datetime)";
											}else {
												p = Pattern.compile(reg1_3);
												m = p.matcher(value.toString());
												if(m.find()){
													 String ret = m.group();
													 //tempValue = " cast('"+ret+"' as datetime)";
													 tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
												}
											}
										}
									}
								}
							}
						}
					}
				}
				
				if(tempValue==null&&value!=null&&!"".equalsIgnoreCase(value)){
					Exception e = new Exception("日期数据格式处理异常,日期格式值为"+value);
					//log.error(e);
					throw e;
				}
			}
			if("numeric".equalsIgnoreCase(fieldType)||"NUMERIC".equalsIgnoreCase(fieldType)||"NUMBER".equalsIgnoreCase(fieldType)||"decimal".equalsIgnoreCase(fieldType)||"number".equalsIgnoreCase(fieldType)||"DECIMAL".equalsIgnoreCase(fieldType)||"int".equalsIgnoreCase(fieldType)||"INT".equalsIgnoreCase(fieldType)){ //数字类型
				if("".equalsIgnoreCase(value)){
					value = "null";
				}
				if("in".equalsIgnoreCase(cpRule)||"not in".equalsIgnoreCase(cpRule)){
					tempValue = dealInOrNotInRule("number",value)+d;
				}else {
					tempValue = value+d;
				}
			}
			if("VARCHAR2".equalsIgnoreCase(fieldType)||"varchar".equalsIgnoreCase(fieldType)||"VARCHAR".equalsIgnoreCase(fieldType)||"VARCHAR2".equalsIgnoreCase(fieldType)||"CLOB".equalsIgnoreCase(fieldType)||"NVARCHAR2".equalsIgnoreCase(fieldType)||"CHAR".equalsIgnoreCase(fieldType)||"char".equalsIgnoreCase(fieldType) ||"nvarchar".equalsIgnoreCase(fieldType) ||"NVARCHAR".equalsIgnoreCase(fieldType)){ //字符类型
				
				if("in".equalsIgnoreCase(cpRule)||"not in".equalsIgnoreCase(cpRule)){
					tempValue = dealInOrNotInRule("string",value)+d;
				}else if("like".equalsIgnoreCase(cpRule)){
					tempValue = "'%"+value+"%'"+d;  
				}else if("likeL".equalsIgnoreCase(cpRule)){
					tempValue = "'"+value+"%'"+d;  
				}else if("likeR".equalsIgnoreCase(cpRule)){
					tempValue = "'%"+value+"'"+d;  
				}else {
					if(value==null||"null".equalsIgnoreCase(value)||"".equalsIgnoreCase(value)){
						tempValue = "null"+d;
					}else {
						tempValue = "'"+value+"'"+d;
					}
				}
			}
			
			if(tempValue==null || tempValue.equalsIgnoreCase("%%")){
				tempValue = "null"+d;
			}
			
			return tempValue;
		}else if(productName.equalsIgnoreCase("SQLSERVER")){
			return getFieldValueMsSql(fieldType, value,cpRule,b,session,null,sourceType);
		}
		return null;
	}
	
	/**
	 * 此方法针对SqlServer数据库
	 * 
	 * @param fieldType 数据库字段类型 
	 * @param value 字段值
	 * @param joinRule where条件中的连接规则
	 * @param b 是否在末尾增加","
	 * @return
	 * @throws Exception
	 */
	public static String  getFieldValueMsSql(String fieldType,String value,String cpRule,boolean b,HttpSession session,Map<String, String> containerParam,String sourceType) throws Exception{
		//(DECIMAL,17 ,null,true)
		String tempValue =null;
		String d = "";
		if(!b){
			d = "";
		}
		
		if(fieldType.indexOf("TIMESTAMP")>=0||"TIMESTAMP".equalsIgnoreCase(fieldType)||"datetime".equalsIgnoreCase(fieldType)||"DATETIME".equalsIgnoreCase(fieldType)||"DATE".equalsIgnoreCase(fieldType)||"date".equalsIgnoreCase(fieldType)||"smalldatetime".equalsIgnoreCase(fieldType)||"SMALLDATETIME".equalsIgnoreCase(fieldType)){ //日期格式处理
			if (value==null||value.equals("null")) {
				return null;
			}
			String reg1 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyy-mmm-dd hh:mm:ss" 格式的日期
			String reg2 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyy/mmm/dd hh:mm:ss" 格式的日期
			String reg1_1 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}"; //"yyyy-mmm-dd hh:mm" 格式的日期
			String reg1_4 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}  [0-9]{1,2}:[0-9]{1,2}"; //"yyyy-mmm-dd hh:mm" 格式的日期
			String reg2_1 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}"; //"yyyy/mmm/dd hh:mm:ss" 格式的日期
			String reg1_3 = "[0-9]{1,2}:[0-9]{1,2}"; //"hh:mm" 格式的日期
			String reg1_2 = "[0-9]{4}[0-9]{1,2}[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}"; //"yyyymmmdd hh:mm:ss" 格式的日期
			String reg3 = "[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}";//"yyyy-mmm-dd" 格式的日期
			String reg4 = "[0-9]{4}/[0-9]{1,2}/[0-9]{1,2}";//"yyyy/mmm/dd" 格式的日期
			String reg5 = "[0-9]{4}[0-9]{1,2}[0-9]{1,2}";//"yyyymmmdd" 格式的日期
			Pattern p = Pattern.compile(reg1);
			Matcher m = p.matcher(value.toString());
		
			if(m.find()){
				 String ret = m.group();
				 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
				 tempValue = " cast('"+ret+"' as datetime)";
			}else {
				p = Pattern.compile(reg1_1);
				m = p.matcher(value.toString());
				if(m.find()){
					 String ret = m.group();
					 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
					 tempValue = " cast('"+ret+"' as datetime)";
				}else{
					p = Pattern.compile(reg1_4);
					m = p.matcher(value.toString());
					if(m.find()){
						 String ret = m.group();
						 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
						 tempValue = " cast('"+ret+"' as datetime)";
					}else {
						p = Pattern.compile(reg2);
						m = p.matcher(value.toString());
						if(m.find()){
							 String ret = m.group();
							 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
							 tempValue = " cast('"+ret+"' as datetime)";
						}else {
							p = Pattern.compile(reg2_1);
							m = p.matcher(value.toString());
							if(m.find()){
								 String ret = m.group();
								 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
								 tempValue = " cast('"+ret+"' as datetime)";
							}else {
								p = Pattern.compile(reg3);
								m = p.matcher(value.toString());
								if(m.find()){
									 String ret = m.group();
									 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
									 tempValue = " cast('"+ret+"' as datetime)";
									 
								}else {
									p = Pattern.compile(reg4);
									m = p.matcher(value.toString());
									if(m.find()){
										 String ret = m.group();
										 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
										 tempValue = " cast('"+ret+"' as datetime)";
									}else {
										p = Pattern.compile(reg5);
										m = p.matcher(value.toString());
										if(m.find()){
											 String ret = m.group();
											 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
											 tempValue = " cast('"+ret+"' as datetime)";
										}else {
											p = Pattern.compile(reg1_2);
											m = p.matcher(value.toString());
											if(m.find()){
												 String ret = m.group();
												 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
												 tempValue = " cast('"+ret+"' as datetime)";
											}else {
												p = Pattern.compile(reg1_3);
												m = p.matcher(value.toString());
												if(m.find()){
													 String ret = m.group();
													 tempValue = " cast('"+ret+"' as datetime)";
													 //tempValue = "TO_DATE('"+ret+"','YYYY-MM-DD HH24:MI:SS')"+d;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(tempValue==null&&value!=null&&!"".equalsIgnoreCase(value)){
				Exception e = new Exception("日期数据格式处理异常,日期格式值为"+value);
				//log.error(e);
				throw e;
			}
		}
		if("numeric".equalsIgnoreCase(fieldType)||"NUMERIC".equalsIgnoreCase(fieldType)||"NUMBER".equalsIgnoreCase(fieldType)||"decimal".equalsIgnoreCase(fieldType)||"number".equalsIgnoreCase(fieldType)||"DECIMAL".equalsIgnoreCase(fieldType)||"int".equalsIgnoreCase(fieldType)||"INT".equalsIgnoreCase(fieldType)){ //数字类型
			if("".equalsIgnoreCase(value)){
				value = "null";
			}
			if("in".equalsIgnoreCase(cpRule)||"not in".equalsIgnoreCase(cpRule)){
				tempValue = dealInOrNotInRule("number",value)+d;
			}else {
				tempValue = value+d;
			}
		}
		if("VARCHAR2".equalsIgnoreCase(fieldType)||"varchar".equalsIgnoreCase(fieldType)||"VARCHAR".equalsIgnoreCase(fieldType)||"VARCHAR2".equalsIgnoreCase(fieldType)||"CLOB".equalsIgnoreCase(fieldType)||"NVARCHAR2".equalsIgnoreCase(fieldType)||"CHAR".equalsIgnoreCase(fieldType)||"char".equalsIgnoreCase(fieldType) ||"nvarchar".equalsIgnoreCase(fieldType) ||"NVARCHAR".equalsIgnoreCase(fieldType)){ //字符类型
			
			if("in".equalsIgnoreCase(cpRule)||"not in".equalsIgnoreCase(cpRule)){
				tempValue = dealInOrNotInRule("string",value)+d;
			}else if("like".equalsIgnoreCase(cpRule)){
				tempValue = "'%"+value+"%'"+d;  
			}else if("likeL".equalsIgnoreCase(cpRule)){
				tempValue = "'"+value+"%'"+d;  
			}else if("likeR".equalsIgnoreCase(cpRule)){
				tempValue = "'%"+value+"'"+d;  
			}else {
				if(value==null||"null".equalsIgnoreCase(value)||"".equalsIgnoreCase(value)){
					tempValue = "null"+d;
				}else {
					tempValue = "'"+value+"'"+d;
				}
			}
		}
		
		if(tempValue==null || tempValue.equalsIgnoreCase("%%")){
			tempValue = "null"+d;
		}
		
		SqlJection sql=new SqlJection();
		int a=tempValue.indexOf("'");
		int c=tempValue.indexOf("%");
		String e=tempValue.replace("'", "");
		if(a!=-1&&c!=-1){
		String values=tempValue.substring(2, tempValue.length()-2);
		
		     if(sql.isValid(values)){
			       return tempValue;
		      }else{
		    	  return null;
		      }
		     
		 }else if(sql.isValid(e)){
			return tempValue;
		 }
		    return  null;
	 }
	/**
	 * 
	 * @param dataType number 代表数据中 string 代表字符
	 * @param value
	 * @param joinRule
	 * @return
	 */
	public static String dealInOrNotInRule(String dataType,String value){
		String tempValue = null;
		if("number".equalsIgnoreCase(dataType)){
			tempValue = " ("+value+") ";
		}else { //打单引号
			tempValue = "('"+value.replaceAll(",", "','")+"')";
		}
		return  tempValue;
	}
	
	
	public static String getWhereSql(SysFormconfigCache form, List<SysFormWhereCache> wheres,Map requestParam,Map<String,String> filterParam,
			SysGridFilterCache filter, Map<String, String>[] heightGrade,UserObject user22,HttpServletRequest request,Map<String, String> containerParam,String sourceType) throws Exception{
		String whereSql = "";
		String andSql = "";
		HttpSession session = request.getSession();
		//if("Oracle".equalsIgnoreCase(dataBaseType)) { //oracle数据库
		if(1<2) {
//			生成条件sql语句
			//初始化化过滤的条件
			for(SysFormWhereCache where:wheres){
				String valueType = where.getFormWhereValueType();
				SysFormColumnCache c = where.getColumn();
				//logger.info(c.getEntityColumn());
				//logger.info(c.getFormFiledTablename());
				//String columnName = c.getFormFiledTablename();
				String columnName = (String)c.getEntityColumn().get("fieldTablename");
//				columnName = columnName.toUpperCase();
				String cpRule = where.getFormWhereCpRule();
				String joinRule = where.getFormWhereJoinRule();
				String leftBracket = where.getFormWhereLeftBracket();
				String rightBracket = where.getFormWhereRightBracket();
				String value = where.getFormWhereValue().toUpperCase(); //参数名或者参数值
				leftBracket = leftBracket==null?"":leftBracket;
				rightBracket = rightBracket==null?"":rightBracket;
				
				if("is null".equalsIgnoreCase(cpRule)||"is not null".equalsIgnoreCase(cpRule)){
					whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+rightBracket;
					continue;
				}
				
				if("session".equalsIgnoreCase(valueType)){ //session区
					String sessionParamValue = value;
					String _value  = "";
					_value = session.getAttribute(sessionParamValue)+"";//SysCardFormColumnUtil.getSessionValue(null,sessionParamValue,user);//获取session对应值
					Map obj = c.getEntityColumn();
					if(_value.equalsIgnoreCase("null")){
						String _value1 = session.getAttribute(sessionParamValue.toLowerCase())+"";
						if (_value1.equalsIgnoreCase("null")) {
							if("Y".equalsIgnoreCase(where.getFormWhereNullIs())) {
								whereSql += " "+joinRule+" "+leftBracket+columnName+" IS NULL "+rightBracket;
							}
						}else {
							String dataType = (String)obj.get("fieldType");
							whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,_value1,cpRule,false,session,containerParam,sourceType)+rightBracket;
						}
					}else {
						String dataType = (String)obj.get("fieldType");
						whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,_value,cpRule,false,session,containerParam,sourceType)+rightBracket;
					}
				}else if("global".equalsIgnoreCase(valueType)){ //全局参数区
					
				}else if("customParam".equalsIgnoreCase(valueType)){ //容器参数
					String sessionParamValue = value;
					String _value  = "";
					_value = (String)session.getAttribute(sessionParamValue+"_paramSession") ;
					Map obj = c.getEntityColumn();
					String dataType = (String)obj.get("fieldType");
					whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,_value,cpRule,false,session,null,sourceType)+rightBracket;
				}else if("constant".equalsIgnoreCase(valueType)){
					Map obj = c.getEntityColumn();
					String dataType = (String)obj.get("fieldType");
					whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,value,cpRule,false,session,null,sourceType)+rightBracket;
				}else if("parameter".equalsIgnoreCase(valueType)||"urlParam".equalsIgnoreCase(valueType)) { //request请求参数
					logger.info(requestParam.get(value)+"");
					String paramValue = requestParam.get(value)==null?null:requestParam.get(value).toString();
					//确定数据类型 
					Map obj = c.getEntityColumn();
					if(paramValue==null){
						if("Y".equalsIgnoreCase(where.getFormWhereNullIs())) {
							whereSql += " "+joinRule+" "+leftBracket+columnName+" IS NULL "+rightBracket;
						}
					}else {
						String dataType = (String)obj.get("fieldType");
						if (cpRule.equalsIgnoreCase("likeL") || cpRule.equalsIgnoreCase("likeR")) {
							whereSql += " "+joinRule+" "+leftBracket+columnName+" like "+getFieldValue(dataType,paramValue,cpRule,false,session,null,sourceType)+rightBracket;
							
						}else {
							whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,paramValue,cpRule,false,session,null,sourceType)+rightBracket;
							
						}
					}
					
				}
					
			}
			//流程特殊参数的特殊处理
			String flowPkValue = (String)requestParam.get("_workflowBusPrimaryKeyParamName");
			if(flowPkValue!=null&&!"".equalsIgnoreCase(flowPkValue)) {
				SysFormColumnCache pkColumn = form.getPkColumn();
				Map obj = pkColumn.getEntityColumn();
				String  pkColumnName =(String)obj.get("fieldTablename");
				String pkCoumnDataType = (String)obj.get("fieldType");
				whereSql += " AND "+" "+pkColumnName+" "+"="+" "+getFieldValue(pkCoumnDataType,flowPkValue,"=",false,session,null,sourceType);
			}
			/*//处理字段条件查询
			if(filter!=null&&filter.getPojo()!=null&&filter.getGridFilterId()!=0){
				List<SysGridFilterColumnCache> fs = filter.getFilterColumns();
				if(filter!=null&&filter.getPojo()!=null&&filter.getFilterColumns().size()>0){
					for(SysGridFilterColumnCache f:fs){
						boolean isIntervailQuery =  "Y".equalsIgnoreCase(f.getPojo().getGridColumnIsIntervais());
						String columnName = f.getColumn().getFormFiledTablename();
						//确定数据类型
						Map obj = f.getColumn().getEntityColumn();
						String cpRule = f.getPojo().getGridColumnFilterRule();
						if(isIntervailQuery){
							String columnMinValue = filterParam.get("_INTERVAIL_MIN_"+columnName);
							String columnMaxValue = filterParam.get("_INTERVAIL_MAX_"+columnName);
							whereSql+=RuntimeUtil.getFilterColumnStr(obj, columnName, columnMinValue, columnMaxValue, cpRule,session);
						}else {
							String columnValue = filterParam.get(columnName)==null?null:filterParam.get(columnName).toString()+"";
							if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
								String dataType = null;
								if(obj==null){
									dataType = "VARCHAR2";
								}else {
									dataType = (String)obj.get("fieldType");
								}
								whereSql += " and"+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session);
							}
						}
						
					}
				}
			}*/
			//处理字段条件查询
			if(filter!=null&&filter.getPojo()!=null&&filter.getGridFilterId()!=0){
				List<SysGridFilterColumnCache> fs = filter.getFilterColumns();
				if(filter!=null&&filter.getPojo()!=null&&filter.getFilterColumns().size()>0){
					String columnValue = "";
					String columnName = "";
					int flag = 0 ;
					int newSearch = 0 ;
					boolean flag2 = false;

					
					for(SysGridFilterColumnCache f:fs){
						logger.info(f.getPojo().getGridColumnLable());
						logger.info(f.getColumn().getPojo().getFormColumnLable());
						String entityName = f.getColumn().getPojo().getFormEntityTablename();
						boolean isSelected = false;
						//确定数据类型
						columnName = f.getColumn().getFormFiledTablename();
						Map obj = f.getColumn().getEntityColumn();
						String cpRule = f.getPojo().getGridColumnFilterRule();
//							String columnValue = filterParam.get(columnName)==null?null:filterParam.get(columnName).toString()+"";
						columnName = ChangeCode.getUniqueCode(entityName, f.getColumn().getFormFiledTablename()).toUpperCase();
						columnValue = filterParam.get(columnName)==null?null:filterParam.get(columnName).toString()+"";
						
						try {
							SysFormComboboxCache objCombobox = (SysFormComboboxCache) f.getColumn().getFormColumGui();
							isSelected = true;
						} catch (Exception e) {
							// TODO: handle exception
							try {
								SysDatepickerCache objCache = (SysDatepickerCache) f.getColumn().getFormColumGui();
								isSelected = true;
							} catch (Exception e2) {
								// TODO: handle exception
							}
						}
						boolean isIntervailQuery =  "Y".equalsIgnoreCase(f.getPojo().getGridColumnIsIntervais());
						if (isIntervailQuery) {
							String columnMinValue = filterParam.get("_INTERVAIL_MIN_"+columnName);
							String columnMaxValue = filterParam.get("_INTERVAIL_MAX_"+columnName);
							andSql+=RuntimeUtil.getFilterColumnStr(obj, columnName, columnMinValue, columnMaxValue, cpRule,session,entityName,sourceType);
							flag++;
						}else {
							if (cpRule.equalsIgnoreCase("in")) {
								if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
									String dataType = null;
									if(obj==null){
										dataType = "VARCHAR2";
									}else {
										dataType = (String)obj.get("fieldType");
									}
									andSql += " and "+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
								}
								flag++;
							}else if(isSelected){
								if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
									String dataType = null;
									if(obj==null){
										dataType = "VARCHAR2";
									}else {
										dataType = (String)obj.get("fieldType");
									}
									andSql += " and "+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
								}
								flag++;
								}else {
									cpRule = "like";
									if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
										String dataType = null;
										if(obj==null){
											dataType = "VARCHAR2";
										}else {
											dataType = (String)obj.get("fieldType");
											if ("numeric".equalsIgnoreCase(dataType)||"NUMERIC".equalsIgnoreCase(dataType)||"NUMBER".equalsIgnoreCase(dataType)||"decimal".equalsIgnoreCase(dataType)||"number".equalsIgnoreCase(dataType)||"DECIMAL".equalsIgnoreCase(dataType)||"int".equalsIgnoreCase(dataType)) {
												dataType = "VARCHAR2";
											}
										}
										if(newSearch>0){
											whereSql += " or"+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
											if (flag==fs.size()-1&&flag2) {
												whereSql +=")";
											}
										}else {
											whereSql += " and("+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
											flag2 = true;
											if (flag==fs.size()-1&&flag2) {
												whereSql +=")";
											}
										}
										flag++;
										newSearch++;
									}else {
										if (flag==fs.size()-1&&flag2) {
											whereSql +=")";
										}
										flag++;
									}
							}
						}
					}

				}
			}
			
			//处理高级查询
			if(heightGrade!=null&&heightGrade.length>0){
				for(Map<String,String> temp:heightGrade){
					String formColumnId = temp.get("formWhereColumnId");
					SysFormColumnCache c = form.serchColumnByColumnId(Long.parseLong(formColumnId));
					if(c!=null){
						String columnName = c.getFormFiledTablename();
						String entityName = c.getFormEntityTablename();
						String leftBracket = temp.get("leftBracket");
						leftBracket = leftBracket==null?"":leftBracket;
						String rightBracket = temp.get("rightBracket");
						rightBracket = rightBracket==null?"":rightBracket;
						String columnValue =temp.get("formWhereValue");
						String cpRule = temp.get("formWhereCpRule");
						Map obj = c.getEntityColumn();
//						DataObject obj = f.getColumn().getEntityColumn();
						if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
							String dataType = "VARCHAR2";
							if(obj!=null){
								dataType =(String)obj.get("fieldType");
							}
							
							String formColumnGuiType = c.getFormColumnGuiType();
							boolean isTrans = false;
							if ("2".equalsIgnoreCase(formColumnGuiType)) {
								Object object = c.getFormColumGui();
								if (object != null) {
									SysFormComboboxCache  combobox = (SysFormComboboxCache)object;
									String comboboxGuiInitType = combobox.getComboboxGuiInitType();
									if ("dict".equalsIgnoreCase(comboboxGuiInitType)) {
										String comboboxGuiInitValue = combobox.getComboboxGuiInitValue();
										if (StringUtils.isNotNullAndBlank(comboboxGuiInitValue)) {
											isTrans = true;
											String dictSql = "SELECT DEN.DICT_ENTRY_CODE FROM SYS_DICT_ENTRY DEN, SYS_DICT_TYPE DT WHERE DEN.DICT_TYPE_ID=DT.DICT_TYPE_ID AND DT.DICT_TYPE_CODE='" + comboboxGuiInitValue + "'";											
											dictSql += " and DEN.DICT_ENTRY_NAME " + cpRule + " '" + getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType)+"'";
											whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" in (" + dictSql + ")" + rightBracket;
										}
									}else if("sql".equalsIgnoreCase(comboboxGuiInitType)){
										isTrans = true;
										String comboboxGuiInitValue = combobox.getComboboxGuiInitValue();
										String Sql = "select DICT_TYPE_REMARK FROM SYS_DICT_TYPE WHERE DICT_TYPE_CODE = '"+comboboxGuiInitValue+"'";
										System.out.println(Sql);
										List<Map> code=MybatisUtil.queryList("runtime.selectSql", Sql);
										String sqlcode1 = code+"";
										String sqlcode ="select * from ("+ sqlcode1.substring(sqlcode1.indexOf("=")+1, sqlcode1.indexOf("}"))+")a "+"where a.DICTENTRY= '"+columnValue+"'";
										System.out.println(sqlcode);
										List<Map> code1=MybatisUtil.queryList("runtime.selectSql", sqlcode);
										System.out.println(code1);
										
										
										String code1str = code1+"";
										code1str = code1str.substring(code1str.indexOf("{")+1, code1str.indexOf("}"));
										code1str = code1str.substring(code1str.indexOf("DICTVALUE=")+10);
										System.out.println(code1str);

										whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" "+cpRule+" "+code1str+rightBracket;
									}
								}
							} 
							
							if (isTrans == false) {
								whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType)+rightBracket;
							}
						}else if(cpRule.equalsIgnoreCase("is null")){
							String dataType = "VARCHAR2";
							if(obj!=null){
								dataType =(String)obj.get("fieldType");
							}
							//String cpRule = temp.get("formWhereCpRule");
							String formColumnGuiType = c.getFormColumnGuiType();
							boolean isTrans = false;
							if ("2".equalsIgnoreCase(formColumnGuiType)) {
								Object object = c.getFormColumGui();
								if (object != null) {
									SysFormComboboxCache  combobox = (SysFormComboboxCache)object;
									String comboboxGuiInitType = combobox.getComboboxGuiInitType();
									if ("dict".equalsIgnoreCase(comboboxGuiInitType)) {
										String comboboxGuiInitValue = combobox.getComboboxGuiInitValue();
										if (StringUtils.isNotNullAndBlank(comboboxGuiInitValue)) {
											isTrans = true;
											String dictSql = "SELECT DEN.DICT_ENTRY_CODE FROM SYS_DICT_ENTRY DEN, SYS_DICT_TYPE DT WHERE DEN.DICT_TYPE_ID=DT.DICT_TYPE_ID AND DT.DICT_TYPE_CODE='" + comboboxGuiInitValue + "'";											
											//dictSql += " and DEN.DICT_ENTRY_NAME " + cpRule + " " + getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
											whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" is null" + rightBracket;
										}
									}
								}
							} 
							
							if (isTrans == false) {
								if("null".equalsIgnoreCase(getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType))){
								whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" "+cpRule+" "+rightBracket;
							}else{
								whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+ChangeCode.getUniqueCode(entityName, columnName)+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType)+rightBracket;

							}
							}
						}
					}
				}
			}
		}
		whereSql +=" "+ andSql;
		return whereSql;
	}
	//WT SqlServer
	@SuppressWarnings("unchecked")
	public static String getWhereSqlSqlServer(SysFormconfigCache form, List<SysFormWhereCache> wheres,
			Map requestParam,Map<String,String> filterParam, SysGridFilterCache filter, Map<String, String>[] heightGrade,UserObject user,SysRequestParam request,Map<String, String> containerParam,String sourceType) throws Exception{
		String whereSql = "";
			HttpSession session = request.getSession();
//			生成条件sql语句
			//初始化化过滤的条件
			for(SysFormWhereCache where:wheres){
				String valueType = where.getFormWhereValueType();
				SysFormColumnCache c = where.getColumn();
				String columnName = c.getFormFiledTablename();
				String cpRule = where.getFormWhereCpRule();
				String joinRule = where.getFormWhereJoinRule();
				String leftBracket = where.getFormWhereLeftBracket();
				String rightBracket = where.getFormWhereRightBracket();
				String value = where.getFormWhereValue(); //参数名或者参数值
				leftBracket = leftBracket==null?"":leftBracket;
				rightBracket = rightBracket==null?"":rightBracket;
				
				if("is null".equalsIgnoreCase(cpRule)||"is not null".equalsIgnoreCase(cpRule)){
					whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+rightBracket;
					continue;
				}
				
				if("session".equalsIgnoreCase(valueType)){ //session区
					String sessionParamValue = value;
					String _value  = "";
					_value = SysCardFormColumnUtil.getSessionValue(request,sessionParamValue,user);//获取session对应值
					Map obj = c.getEntityColumn();
					if(_value==null){
						if("Y".equalsIgnoreCase(where.getFormWhereNullIs())) {
							whereSql += " "+joinRule+" "+leftBracket+columnName+" IS NULL "+rightBracket;
						}
					}else {
						String dataType = (String)obj.get("fieldType");
						whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,_value,cpRule,false,session,null,sourceType)+rightBracket;
					}
				}else if("global".equalsIgnoreCase(valueType)){ //全局参数区
					
				}else if("constant".equalsIgnoreCase(valueType)){
					Map obj = c.getEntityColumn();
					String dataType = (String)obj.get("fieldType");
					whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,value,cpRule,false,session,null,sourceType)+rightBracket;
				}else if("parameter".equalsIgnoreCase(valueType)) { //request请求参数
					String paramValue = requestParam.get(value)==null?null:(String)requestParam.get(value);
					//确定数据类型 
					Map obj = c.getEntityColumn();
					if(paramValue==null){
						if("Y".equalsIgnoreCase(where.getFormWhereNullIs())) {
							whereSql += " "+joinRule+" "+leftBracket+columnName+" IS NULL "+rightBracket;
						}
					}else {
						String dataType = (String)obj.get("fieldType");
						whereSql += " "+joinRule+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValueMsSql(dataType,value,cpRule,false,session,null,sourceType)+rightBracket;
					}
				}
					
			}
			//流程特殊参数的特殊处理
			String flowPkValue = (String)requestParam.get("_workflowBusPrimaryKeyParamName");
			if(flowPkValue!=null&&!"".equalsIgnoreCase(flowPkValue)) {
				SysFormColumnCache pkColumn = form.getPkColumn();
				Map obj = pkColumn.getEntityColumn();
				String  pkColumnName = (String)obj.get("fieldTablename");
				String pkCoumnDataType = (String)obj.get("fieldType");
				whereSql += " AND "+" "+pkColumnName+" "+"="+" "+getFieldValue(pkCoumnDataType,flowPkValue,"=",false,session,null,sourceType);
			}
			//处理字段条件查询
			if(filter!=null&&filter.getGridFilterId()!=0){
				List<SysGridFilterColumnCache> fs = filter.getFilterColumns();
				if(filter!=null&&filter.getFilterColumns().size()>0){
					for(SysGridFilterColumnCache f:fs){
						boolean isIntervailQuery =  "Y".equalsIgnoreCase(f.getGridColumnIsIntervais());
						String columnName = f.getColumn().getFormFiledTablename();
						String entityName = f.getColumn().getPojo().getFormEntityTablename();
						//确定数据类型
						Map obj = f.getColumn().getEntityColumn();
						String cpRule = f.getGridColumnFilterRule();
						if(isIntervailQuery){
							String columnMinValue = filterParam.get("_INTERVAIL_MIN_"+columnName);
							String columnMaxValue = filterParam.get("_INTERVAIL_MAX_"+columnName);
							whereSql+=RuntimeUtil.getFilterColumnStr(obj, columnName, columnMinValue, columnMaxValue, cpRule,session,entityName,sourceType);
						}else {
							String columnValue = filterParam.get(columnName);
							if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
								String dataType = null;
								if(obj==null){
									dataType = "VARCHAR2";
								}else {
									dataType = (String)obj.get("fieldType");
								}
								if (cpRule.equalsIgnoreCase("likeL") || cpRule.equalsIgnoreCase("likeR")) {
									whereSql += " and"+" "+columnName+" like "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
								}else {
									whereSql += " and"+" "+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
								}
								
							}
						}
						
					}
				}
			}
			
			//处理高级查询
			if(heightGrade!=null&&heightGrade.length>0){
				for(Map<String,String> temp:heightGrade){
					String formColumnId = temp.get("formWhereColumnId");
					SysFormColumnCache c = form.serchColumnByColumnId(Long.parseLong(formColumnId));
					if(c!=null){
						String columnName = c.getFormFiledTablename();
						String leftBracket = temp.get("leftBracket");
						leftBracket = leftBracket==null?"":leftBracket;
						String rightBracket = temp.get("rightBracket");
						rightBracket = rightBracket==null?"":rightBracket;
						String columnValue =temp.get("formWhereValue");
						Map obj = c.getEntityColumn();
//						DataObject obj = f.getColumn().getEntityColumn();
						if(columnValue!=null&&!"".equalsIgnoreCase(columnValue)) {
							String dataType = "VARCHAR2";
							if(obj!=null){
								dataType = (String)obj.get("fieldType");
							}
							String cpRule = temp.get("formWhereCpRule");
							String formColumnGuiType = c.getFormColumnGuiType();
							boolean isTrans = false;
							if ("2".equalsIgnoreCase(formColumnGuiType)) {
								Object object = c.getFormColumGui();
								if (object != null) {
									SysFormComboboxCache combobox = (SysFormComboboxCache)object;
									String comboboxGuiInitType = combobox.getComboboxGuiInitType();
									if ("dict".equalsIgnoreCase(comboboxGuiInitType)) {
										String comboboxGuiInitValue = combobox.getComboboxGuiInitValue();
										if (StringUtils.isNotNullAndBlank(comboboxGuiInitValue)) {
											isTrans = true;
											String dictSql = "SELECT DEN.DICT_ENTRY_CODE FROM SYS_DICT_ENTRY DEN, SYS_DICT_TYPE DT WHERE DEN.DICT_TYPE_ID=DT.DICT_TYPE_ID AND DT.DICT_TYPE_CODE='" + comboboxGuiInitValue + "'";
											
											dictSql += " and DEN.DICT_ENTRY_NAME " + cpRule + " " + getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType);
											whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+columnName+" in (" + dictSql + ")" + rightBracket;
										}
									}
								}
							} 
							
							if (isTrans == false) {
								whereSql += " "+temp.get("formWhereJoinRule")+" "+leftBracket+columnName+" "+cpRule+" "+getFieldValue(dataType,columnValue,cpRule,false,session,null,sourceType)+rightBracket;
							}
						}
					}
				}
			}
		return whereSql;
	}
	
	/**
	 * 获取区间查询字段的sql段
	 * @param obj
	 * @param columnName
	 * @param columnValue
	 * @param cpRule
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static String getFilterColumnStr(Map obj ,String columnName,String columnMinValue,String columnMaxValue,String cpRule,HttpSession sessioin,String entityName,String sourceType) throws Exception{
		if((columnMinValue==null||"".equalsIgnoreCase(columnMinValue))&&(columnMaxValue==null||"".equalsIgnoreCase(columnMaxValue))) {
			return "";
		}
		String whereSql = "and (";
		boolean isMin = false;
		if(columnMinValue!=null&&!"".equalsIgnoreCase(columnMinValue)) {
			String dataType = null;
			if(obj==null){
				dataType = "VARCHAR2";
			}else {
				dataType = (String)obj.get("fieldType");
			}
			whereSql += columnName+" >= "+" "+getFieldValue(dataType,columnMinValue,cpRule,false,sessioin,null,sourceType);
			isMin = true;
		}
		if(columnMaxValue!=null&&!"".equalsIgnoreCase(columnMaxValue)) {
			String dataType = null;
			if(obj==null){
				dataType = "VARCHAR2";
			}else {
				dataType = (String)obj.get("fieldType");
			}
			whereSql += (isMin?" and":"")+" "+columnName+" "+"<="+" "+getFieldValue(dataType,columnMaxValue,cpRule,false,sessioin,null,sourceType);
		}
		whereSql += " ) ";
		return whereSql;
	}
	
	/**
	 * form表单中保存按钮生成updatesql方法
	 * @param update
	 * @param values
	 * @param fields
	 * @param tablename
	 * @param pkField
	 * @param pkValue
	 * @return
	 * @throws Exception  
	 */
	@SuppressWarnings("unchecked")
	public static String getUpdateSqlImport(List<String> update, List values, List<SysField> fields, String tablename, String pkField, Long pkValue,HttpSession session,String sourceType) throws Exception {
		// TODO 自动生成方法存根

		String updateStr = "";
		for(int i=0; i<update.size(); i++){
			String fieldName = update.get(i);
			//获取当前字段的数据类型 
			String fieldType = getFieldByFieldTablenameImport(fieldName,fields);
			Object value = values.get(i);
			String tempValue = null;
			String tempField = ChangeCode.getUniqueCode(tablename, fieldName);
				if(value==null||"-".equals(value) ){
					tempValue = "null";
				}else {
					tempValue = getFieldValue(fieldType,(String) value,null,false,session,null,sourceType);
				}
				updateStr= updateStr + ChangeCode.getRealCode(tempField)+"="+tempValue+",";
			
		}
		updateStr = updateStr.substring(0,updateStr.length()-1);
		return "UPDATE "+tablename+" SET  "+updateStr+" WHERE "+ChangeCode.getRealCode(pkField)+" = "+pkValue;
	}

	/**
	 * form表单中保存按钮生成updatesql方法
	 * @param update
	 * @param values
	 * @param fields
	 * @param tablename
	 * @param pkField
	 * @param pkValue
	 * @return
	 * @throws Exception  
	 */
	@SuppressWarnings("unchecked")
	public static String getUpdateSql(List<String> update, List values, List<SysField> fields, String tablename, String pkField, Long pkValue,HttpSession session,String sourceType) throws Exception {
		// TODO 自动生成方法存根

		String updateStr = "";
		for(int i=0; i<update.size(); i++){
			String fieldName = update.get(i);
			//获取当前字段的数据类型 
			String fieldType = getFieldByFieldTablename(fieldName,fields);
			Object value = values.get(i);
			String tempValue = null;
			String tempField = fieldName;
				if(value==null||"-".equals(value) ){
					tempValue = "null";
				}else {
					tempValue = getFieldValue(fieldType,(String) value,null,false,session,null,sourceType);
				}
				updateStr= updateStr + ChangeCode.getRealCode(tempField)+"="+tempValue+",";
			
		}
		SqlJection sql=new SqlJection();
		updateStr = updateStr.substring(0,updateStr.length()-1);
		String[] updateStrs=updateStr.split(",");
		boolean flag=false;
		for(int i=0;i<updateStrs.length;i++){
			String updateS=updateStrs[i].replace("'", "");
			if(!sql.isValid(updateS)){
				flag=true;
				break;
			}
		}
		    if(!flag){
			
			return "UPDATE "+tablename+" SET  "+updateStr+" WHERE "+ChangeCode.getRealCode(pkField)+" = "+pkValue;
			}
	
		//判断修改该传入参数是否非法
		
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public static String removeData(SysFormconfigCache form,Map obj) throws DcfException{
		SysFormColumnCache pkColumn = form.getPkColumn();
		String sql = null;
		if(pkColumn!=null&&pkColumn.getFormColumnId()!=0){
			String[] ef = pkColumn.getFiledName();
			Object objPkValue = obj.get(ef[1]);
			if(objPkValue==null||"".equals(objPkValue)){
				throw new DcfException("主键主键字段值为空");
			}
			String pkValue = objPkValue.toString();
			
			sql = RuntimeUtil.getDeleteSql(ef[0], ef[1], pkValue);
			
			
		}else {
			throw new DcfException("无主键字段");
		}
		return sql;
	}
	
	/**
	 * 
	 * @param form
	 * @param paramdata
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public static String getRemoveTabData(SysFormconfigCache form,Map<String,String>[] paramdata,UserObject user,HttpServletRequest request,String sourceType) throws Exception{
		if(paramdata!=null){
			for(int i=0; i<paramdata.length; i++){
				String sql = RuntimeUtil.getWhereSql(form, form.getWheres(), paramdata[i], null, null, null, null,request,null,sourceType);
				logger.info("**********"+sql);
			}
		}
		return null;
	}
	
	public static String getDeleteSql(String tablename,String pkName ,String pkValue){
		
		return "DELETE FROM "+tablename +" WHERE "+ChangeCode.getRealCode(pkName) +"='" +pkValue+"'";
	}
	
	@SuppressWarnings("unchecked")
	public static String getSelectFieldFormat(String fieldName,Map fieldEntity){
		String dataType = (String)fieldEntity.get("fieldType");
		
		fieldName = ChangeCode.getUniqueCode((String)fieldEntity.get("entityCode"), fieldName);
		
		if("Oracle".equalsIgnoreCase(dataBaseType)) { //oracle数据库
			if("DATE".equalsIgnoreCase(dataType)||"date".equalsIgnoreCase(dataType)||dataType.indexOf("TIMESTAMP")>=0||dataType.indexOf("timestamp")>=0){
				return "TO_CHAR(t."+fieldName+",'YYYY-MM-DD HH24:MI:SS') "+fieldName;
			}
		}
		return "t."+fieldName;
	}
	
	
	/**
	 * 执行自定义存储过程
	 * @param formId
	 * @param execSqlRelaid
	 * @param p
	 * @return
	 */
	public static String[] execPrepare(SysFormGeneralExecSqlCache buttonExecSql,PrimaryKeyService pkService,Map<String, Object> p,Connection conn,HttpSession session){
		String[] sql = buttonExecSql.getSqlList();
		List<String[]> ps = buttonExecSql.getParams();
		String[]  tempSqls = new String[100];
		for(int k=0;k<sql.length;k++){
			String tempSql = sql[k];
			for(int i=0; i<ps.size(); i++){
				String[] temp = ps.get(i);
				String key = null;
				String rstr = null;
				String value = null;
				if("c".equalsIgnoreCase(temp[0])){
					key = temp[0]+"_" + temp[1].substring(temp[1].indexOf("_")+1);
				}else if("r".equalsIgnoreCase(temp[0])){
					key = temp[0]+"_" + temp[1];
				}else if("k".equalsIgnoreCase(temp[0])){
					value = temp[1].replace("-", ".");
					long pk;
					try {
						pk = pkService.getPrimaryKey(value);
					} catch (Exception e) {
						e.printStackTrace();logger.error(e.getMessage(),e);
						return new String[]{"-1",null};
					}
					value = Long.toString(pk);
				}else if("s".equalsIgnoreCase(temp[0])){
					value = session.getAttribute(temp[1])+"";
				}
				else {
					key = temp[0]+"_"+temp[1];
				}
				if(!"k".equalsIgnoreCase(temp[0])&&!"s".equalsIgnoreCase(temp[0])) {
					value = (String)p.get(key);
					if(value == null){
						value= "";
					}
				}
				String[] values = value.split(",");
				rstr = "["+temp[0]+":"+temp[1]+"]";
				int n = values.length;
				if ("[s:userId]".equalsIgnoreCase(rstr)) {
					for (int j = 0; j < tempSqls.length; j++) {
						if ((tempSqls[j]==null)) {
							n=j;
							break;
						}
					}
				}
				for (int j = 0; j < n; j++) {
					String tempSql1=tempSql;
					
					if (tempSqls[j]!=null) {
						tempSql1=tempSqls[j];
					}
					while(tempSql1.indexOf(rstr)>=0){
						if ("[s:userId]".equalsIgnoreCase(rstr)) {
							tempSql1 = tempSql1.replace("[s:userId]", values[0]);
						}else{
							if ("'null'".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
								tempSql1 = tempSql1.replace("'" + rstr + "'", "null");
								tempSql1 = tempSql1.replace(rstr, "null");
							} else {
								tempSql1 = tempSql1.replace(rstr, values[j]);
							}	
						}
					}
					tempSqls[j]=tempSql1;
				}
			}
			String[] result=null;
			for (int i = 0; i < tempSqls.length; i++) {
				if (tempSqls[i]!=null) {
					logger.info("sql:"+tempSqls[i]);
					//log.info("sql:"+tempSql);
					result = MyEhrJDBCUtil.execPrepare(conn,tempSqls[i]);
				}
			}
			if(conn!=null){  
			    try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();logger.error(e.getMessage(),e);
				} 
			    conn = null;
			}  
			if(sql.length-1 == k){
				return result;
			}
			
		}
		return new String[]{"-1",null};
	}
	
	
	/**
	 * 解析容器参数并执行自定义存储过程
	 * @param formId
	 * @param execSqlRelaid
	 * @param p
	 * @param containerParam
	 * @return
	 */
	public static String[] execPrepare(SysFormGeneralExecSqlCache buttonExecSql,PrimaryKeyService pkService,Map<String, Object> p,Connection conn,Map<String, String> containerParam){
		String[] sql = buttonExecSql.getSqlList();
		List<String[]> ps = buttonExecSql.getParams();
		int num = 1;
		if (ps.size()>0) {
			for (int i = 0; i <ps.size(); i++) {
				String[] a = ps.get(i);
				if (a[0].equalsIgnoreCase("s")) {
					for (int j = 0; j < sql.length; j++) {
						String execSql = sql[j];
						String s_value = p.get(a[0]+"_"+a[1])+"";
						String s_param = "["+a[0]+":"+a[1]+"]";
						execSql=execSql.replace(s_param,s_value);
						
						logger.info(execSql);
						sql[j] = execSql;
					}
				}else {
					String[] b = a[1].split("_");
					String aStr = a[0]+"_"+b[1];
					String c = (String)p.get(aStr);
					String[] d = c.split(",");
					if(num<d.length){
						num= d.length;
					}
					
				}
				
			}
		}
		String[]  tempSqls = new String[num];
		int n = 0;
		for(int k=0;k<sql.length;k++){
			String tempSql = sql[k];
			tempSql = MyEhrJDBCUtil.repleaceContainerParam(containerParam, tempSql);
			if (ps.size()>0) {
				for(int i=0; i<ps.size(); i++){
					String[] temp = ps.get(i);
					String key = null;
					String rstr = null;
					String value = null;
					if("c".equalsIgnoreCase(temp[0])){
						key = temp[0]+"_" + temp[1].substring(temp[1].indexOf("_")+1);
					}else if("r".equalsIgnoreCase(temp[0])){
						key = temp[0]+"_" + temp[1];
					}else if("k".equalsIgnoreCase(temp[0])){
						value = temp[1].replace("-", ".");
						long pk;
						try {
							pk = pkService.getPrimaryKey(value);
						} catch (Exception e) {
							e.printStackTrace();logger.error(e.getMessage(),e);
							return new String[]{"-1",null};
						}
						value = Long.toString(pk);
					}
					else {
						key = temp[0]+"_"+temp[1];
					}
					if(!"k".equalsIgnoreCase(temp[0])) {
						value = (String)p.get(key);
						if(value == null){
							value= "";
						}
					}
					if (value.split(",").length>n||value.split(",").length==n) {
						String[] values = value.split(",");
						rstr = "["+temp[0]+":"+temp[1]+"]";
						n = values.length;
						for (int j = 0; j < n; j++) {
							String tempSql1=tempSql;
							
							if (tempSqls[j]!=null) {
								tempSql1=tempSqls[j];
							}
							while(tempSql1.indexOf(rstr)>=0){
								if ("'null'".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
									tempSql1 = tempSql1.replace("'" + rstr + "'", "null");
									tempSql1 = tempSql1.replace(rstr, "null");
								} else {
									tempSql1 = tempSql1.replace(rstr, values[j]);
								}	
							}
							tempSqls[j]=tempSql1;
						}
					}else{
						String[] values = value.split(",");
						rstr = "["+temp[0]+":"+temp[1]+"]";
						for (int j = 0; j < n; j++) {
							String tempSql1=tempSql;
							if (tempSqls[j]!=null) {
								tempSql1=tempSqls[j];
							}
							while(tempSql1.indexOf(rstr)>=0){
								if ("'null'".equalsIgnoreCase(value) || "null".equalsIgnoreCase(value)) {
									tempSql1 = tempSql1.replace("'" + rstr + "'", "null");
									tempSql1 = tempSql1.replace(rstr, "null");
								} else {
									if(j<values.length){
										tempSql1 = tempSql1.replace(rstr, values[j]);
									}else{
										tempSql1 = tempSql1.replace(rstr, values[0]);
									}
								}	
							}
							tempSqls[j]=tempSql1;
						}
					}
					
					
				}
			}else {
				for (int i = 0; i < sql.length; i++) {
					tempSqls[k]=tempSql;
				}
				
			}
			
			String[] result=null;
			for (int i = 0; i < tempSqls.length; i++) {
				if (tempSqls[i]!=null) {
					logger.info("sql:"+tempSqls[i]);
					//log.info("sql:"+tempSql);
					result = MyEhrJDBCUtil.execPrepare(conn,tempSqls[i]);
				}
			}
			if(conn!=null){  
			    try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();logger.error(e.getMessage(),e);
				} 
			    conn = null;
			}  
			if(sql.length-1 == k){
				return result;
			}
			
		}
		return new String[]{"-1",null};
	}
	
	/**
	 * 执行自定义sql
	 * @param formId
	 * @param execSqlRelaid
	 * @param p
	 * @return
	 * @throws DcfException 
	 */
	@SuppressWarnings("unchecked")
	public static List<Map> execsql(SysFormGeneralExecSqlCache buttonExecSql,PrimaryKeyService pkService,Map<String, Object> p,HttpSession session,String sourceType) throws DcfException {
		String[] sql = buttonExecSql.getSqlList();
		List<String[]> ps = buttonExecSql.getParams();
		if (sql!=null) {
			for(int k=0;k<sql.length;k++){
				String tempSql = sql[k];
				for(int i=0; i<ps.size(); i++){
					String[] temp = ps.get(i);
					String key = null;
					String rstr = null;
					String value = null;
					if("c".equalsIgnoreCase(temp[0])){
						key = temp[0]+"_" + temp[1].substring(temp[1].indexOf("_")+1);
					}else if("r".equalsIgnoreCase(temp[0])){
						key = temp[0]+"_" + temp[1];
					}else if("k".equalsIgnoreCase(temp[0])){
						value = temp[1].replace("-", ".");
						long pk;
						try {
							pk = pkService.getPrimaryKey(value);
						} catch (Exception e) {
							e.printStackTrace();logger.error(e.getMessage(),e);
							return null;
						}
						value = Long.toString(pk);
					}
					else {
						key = temp[0]+"_"+temp[1];
					}
					if(!"k".equalsIgnoreCase(temp[0])) {
						value = (String)p.get(key);
						if(value == null){
							value = "null";
						}
					}
					rstr = "["+temp[0]+":"+temp[1]+"]";
					while(tempSql.indexOf(rstr)>=0){
						if(value==null||"null".equalsIgnoreCase(value)){
							value ="null";
							tempSql = tempSql.replace("'"+rstr+"'", value);
						}
						tempSql = tempSql.replace(rstr, value);
						
					}
				}
				//log.info("sql:"+tempSql);
				logger.info("sql:"+tempSql);
				tempSql=tempSql.replace("'undefined'", "null");
				String _value = (String)p.get("_combobox_value");
				if(_value!=null){
					SysFormComboboxCache combobox = (SysFormComboboxCache)p.get("_combobox_combobox");
					String valueField = combobox.getComboboxValuefield();
					String textField = combobox.getComboboxTextfield();
					if("Oracle".equalsIgnoreCase(dataBaseType)) { //oracle数据库
						try {
							//gfdgfdg
							String tempValue = getFieldValue(combobox.getColumn().getDatabaseType(),_value+"","=",false,session,null,sourceType);
							tempSql = "select tt."+textField+" from ("+tempSql+") tt where tt."+valueField+" = "+tempValue;
							
						}catch (Exception e) {
							throw new DcfException(e.getMessage());
						}
					}
				}
				List<Map> dataObject;
				try {
					dataObject = MybatisUtil.queryList("runtime.selectSql", tempSql.toUpperCase());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();logger.error(e.getMessage(),e);
					throw new DcfException(e);
				}
				if(sql.length-1 == k){
					return dataObject;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 执行自定义sql
	 * @param formId
	 * @param execSqlRelaid
	 * @param p
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String execsqlForColumnName(SysFormGeneralExecSqlCache buttonExecSql, PrimaryKeyService pkService,Map<String, String> p) {
		String[] sql = buttonExecSql.getSqlList();
		List<String[]> ps = buttonExecSql.getParams();
		if(sql.length > 0){
			String tempSql = sql[0];
			for(int i=0; i<ps.size(); i++){
				String[] temp = ps.get(i);
				String key = null;
				String rstr = null;
				String value = null;
				if("c".equalsIgnoreCase(temp[0])){
					key = temp[0]+"_" + temp[1].substring(temp[1].indexOf("_")+1);
				}else if("k".equalsIgnoreCase(temp[0])){
					value = temp[1].replace("-", ".");
					long pk;
					try {
						pk = pkService.getPrimaryKey(value);
					} catch (Exception e) {
						e.printStackTrace();logger.error(e.getMessage(),e);
						return null;
					}
					value = Long.toString(pk);
				}
				else {
					key = temp[0]+"_"+temp[1];
				}
				if(!"k".equalsIgnoreCase(temp[0])) {
					value = p.get(key);
					if(value == null){
						return null;
					}
				}
				rstr = "["+temp[0]+":"+temp[1]+"]";
				while(tempSql.indexOf(rstr)>=0){
					tempSql = tempSql.replace(rstr, value);
				}
			}
			logger.info("sql:"+tempSql);
			List<Map> dataObject = null;
			try {
				dataObject = MybatisUtil.queryList("runtime.selectSql", new HashMap().put("selectSql", tempSql));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.info(e.getMessage());
				e.printStackTrace();logger.error(e.getMessage(),e);
			}
			//DataObject[] dataObject = das.queryByNamedSql(DataObject.class, "com.dcf.form.runtime.cardform.excuteQuerySql", tempSql);
			if(dataObject!=null&&dataObject.size() > 0){
				return dataObject.get(0).get(0).toString();
			}
		}
		return null;
	}
	
	/**
	 * 检查业务主键是否重复
	 * @param tablename
	 * @param param
	 * @param busPkField
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static String getCheckSqlBusPkDataSql(String tablename, Map<String, String> param,List<Map> busPkField,HttpSession session,String sourceType) throws Exception {
		// TODO 自动生成方法存根
		String productName =(String)session.getAttribute("productName");
		String whereStr = "";
		for(int i=0; i<busPkField.size(); i++){
			String fieldName = (String)busPkField.get(i).get("fieldTablename");
			//获取当前字段的数据类型 
			String fieldType =  (String)busPkField.get(i).get("fieldType");;
			String value = param.get(fieldName);
			String tempValue = null;
			String tempField = fieldName;
			if("Oracle".equalsIgnoreCase(productName)) { //oracle数据库
				if(value==null){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValue(fieldType, value,null,false,session,null,sourceType);
				}
			}else if("SqlServer".equalsIgnoreCase(productName)){//sqlserver数据库
				if(value==null){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValueMsSql(fieldType, value,null,false,session,null,sourceType);
				}
			}
			whereStr += ChangeCode.getRealCode(tempField)+"="+tempValue+" and ";
			
		}
		if(whereStr.equalsIgnoreCase("")){
			return null;
		}
		whereStr = whereStr.substring(0,whereStr.length()-5);
		return "select 1 from "+tablename+" where "+whereStr;
	}
	
	/**
	 * 检查业务主键是否重复
	 * @param tablename
	 * @param param
	 * @param busPkField
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static String getCheckSqlBusPkDataSqlImport(String tablename, Map<String, String> param,List<Map> busPkField,HttpSession session,String sourceType) throws Exception {
		// TODO 自动生成方法存根
		String productName =(String)session.getAttribute("productName");
		String whereStr = "";
		for(int i=0; i<busPkField.size(); i++){
			String fieldName = ChangeCode.getRealCode((String)busPkField.get(i).get("fieldTablename"));
			//获取当前字段的数据类型 
			String fieldType =  (String)busPkField.get(i).get("fieldType");;
			String value = param.get(fieldName);
			String tempValue = null;
			String tempField = fieldName;
			if("Oracle".equalsIgnoreCase(productName)) { //oracle数据库
				if(value==null){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValue(fieldType, value,null,false,session,null,sourceType);
				}
			}else if("SqlServer".equalsIgnoreCase(productName)){//sqlserver数据库
				if(value==null){
					tempValue = "null";
				}else {
					//gfdgfdg
					tempValue = getFieldValueMsSql(fieldType, value,null,false,session,null,sourceType);
				}
			}
			whereStr += tempField+"="+tempValue+" and ";
			
		}
		if(whereStr.equalsIgnoreCase("")){
			return null;
		}
		whereStr = whereStr.substring(0,whereStr.length()-5);
		return "select 1 from "+tablename+" where "+whereStr;
	}
}