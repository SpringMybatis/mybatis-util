package com.ibs.mybatis;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;

public class UserTest {

	public static void main(String[] args) {
		// SqlSessionFactory ssf = null;
		SqlSession sqlSession = null;
		try {
			// ssf = SessionFactoryUtil.getSqlSessionFactoryIntance();
			sqlSession = SessionFactoryUtil.getSqlSessionIntance();
			// 获取List
			// List<User> userList = sqlSession.selectList("com.ibs.mybatis.UserMapper.selectUserAll");
			// System.out.println(userList.size());
			// String sql1 = getExecSql(sqlSession,"com.ibs.mybatis.UserMapper.selectUserAll", null);
			// System.out.println(sql1);
			// 获取User
			User u = new User();
			u.setUserName("admin");
			/*Map<String,Object> dataMap = new HashMap<String, Object>();
			dataMap.put("user", u);*/
			// User u1 = sqlSession.selectOne("com.ibs.mybatis.UserMapper.selectUserByName", dataMap);
			// System.out.println(u1.getPassWord());
			//String sql2 = getExecSql(sqlSession,"com.ibs.mybatis.UserMapper.selectUserByName", dataMap);
			//System.out.println(sql2);
			
			/*String sql3 = SqlHelper.getNamespaceSql(sqlSession, "com.ibs.mybatis.UserMapper.selectUserByName", u);
			System.out.println(sql3);*/
			
			Map<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("user", u);
			String sql4 = SqlHelper.getNamespaceSql(sqlSession, "com.ibs.mybatis.UserMapper.selectUserByMap", dataMap);
			
			System.out.println(sql4);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sqlSession != null) {
				sqlSession.close();
			}
		}
	}
	
	public String testA(String a) {

		return null;
	}

	public boolean testB(String a) {

		return false;
	}
	
	public static String getExecSql(SqlSession sqlSession, String statement,
			Map<String, Object> params) throws Exception {
		// 获取配置信息
		Configuration configuration = sqlSession.getConfiguration();
		// mapper-id-xml
		MappedStatement mappedStatement = configuration.getMappedStatement(statement);
		// 获取绑定的SQL
		BoundSql boundSql = mappedStatement.getBoundSql(params);

		String sql = boundSql.getSql();

		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		if (parameterMappings != null) {
			Object[] parameterArray = new Object[parameterMappings.size()];
			ParameterMapping parameterMapping = null;
			Object value = null;
			Object parameterObject = null;
			MetaObject metaObject = null;
			// PropertyTokenizer prop = null;
			String propertyName = null;
			String[] names = null;
			String arrayProtoName = null;
			for (int i = 0; i < parameterMappings.size(); i++) {
				parameterMapping = parameterMappings.get(i);
				if (parameterMapping.getMode() != ParameterMode.OUT) {
					propertyName = parameterMapping.getProperty();
					names = propertyName.split("\\.");
					if (propertyName.indexOf(".") != -1 && names.length == 2) {
						String proName = names[0];
						// 如果是集合对象需要改变取值的属性名
						if (proName.startsWith(ForEachSqlNode.ITEM_PREFIX)) {
							propertyName = proName.substring(ForEachSqlNode.ITEM_PREFIX.length(),proName.lastIndexOf("_"));
							propertyName = propertyName + "s";
							parameterObject = params.get(propertyName);
							propertyName = proName;
							arrayProtoName = names[1];
						} else {
							parameterObject = params.get(proName);
							propertyName = names[1];
						}
					} else if (propertyName.indexOf(".") != -1 && names.length == 3) {
						parameterObject = params.get(names[0]); // map
						if (parameterObject instanceof Map) {
							parameterObject = ((Map) parameterObject).get(names[1]);
						}
						propertyName = names[2];
					} else {
						parameterObject = params.get(propertyName);
					}

					metaObject = params == null ? null : MetaObject
							.forObject(parameterObject, configuration
									.getObjectFactory(), configuration
									.getObjectWrapperFactory());

					// prop = new PropertyTokenizer(propertyName);

					if (parameterObject == null) {
						value = null;
					} else if (mappedStatement.getConfiguration()
							.getTypeHandlerRegistry().hasTypeHandler(
									parameterObject.getClass())) {
						value = parameterObject;
					}
					// 检查是否包含参数对象信息,如果当前包含，则取出参数信息
					else if (boundSql.hasAdditionalParameter(propertyName)) {
						value = boundSql.getAdditionalParameter(propertyName);
						// 如果当前为集合对象
						if (propertyName.startsWith(ForEachSqlNode.ITEM_PREFIX)) {

							value = MetaObject.forObject(value,
									configuration.getObjectFactory(),
									configuration.getObjectWrapperFactory())
									.getValue(arrayProtoName);

						}
					} else {
						Object protoValue = metaObject.getOriginalObject();

						if (null != protoValue) {

							if (protoValue instanceof Map) {
								value = metaObject == null ? null : metaObject
										.getValue(propertyName);
							} else {// 通过反射获取值
								PropertyDescriptor p = new PropertyDescriptor(
										propertyName, protoValue.getClass());
								Method method = p.getReadMethod();
								value = method.invoke(protoValue);
							}
						}
					}
					if (value instanceof String) {
						value = "'" + value.toString() + "'";
					} else if (value instanceof Date) {
						DateFormat formatter = DateFormat.getDateTimeInstance(
								DateFormat.DEFAULT, DateFormat.DEFAULT,
								Locale.CHINA);
						value = "Timestamp'" + formatter.format(value) + "'";
					}
					parameterArray[i] = value;
				}
			}

			for (int j = 0; j < parameterArray.length; j++) {
				Object param = parameterArray[j];
				sql = sql.replaceFirst("\\?", param.toString());

			}
			sql = sql.replaceAll("(\r?\n(\\s*\r?\n)+)", "\r\n");
		}

		return sql;
	}

}
