package com.ibs.mybatis;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SessionFactoryUtil {

	private static final String MYBATIS_CONFIG_PATH = "mybatis-config/mybatis-config.xml";

	private static InputStream is = null;

	private static SqlSessionFactory sqlSessionFactory = null;

	static {
		try {
			is = Resources.getResourceAsStream(MYBATIS_CONFIG_PATH);
			// SqlSessionFactoryBuilder这个类可以被实例化,使用和丢弃。一旦创建了 SqlSessionFactory
			// 后,这个类就不需 要存在了。
			// 因此 SqlSessionFactoryBuilder 实例的最佳范围是方法范围 (也就是本地方法变量)。
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 私有构造器
	 */
	private SessionFactoryUtil() {
		
	};

	/**
	 * SqlSessionFactory 的最佳实践是在应用运行期间不要重复创建多次 最简单的就是使用单例模式或者静态单例模式。
	 * 
	 * @return SqlSessionFactory
	 */
	public static SqlSessionFactory getSqlSessionFactoryIntance() {

		return sqlSessionFactory;

	}

	/**
	 * 每个线程都应该有它自己的 SqlSession 实例。 SqlSession 的实例不能被共享,也是线程 不安全的。
	 * 因此最佳的范围是请求或方法范围。 绝对不能将 SqlSession 实例的引用放在一个 类的静态字段甚至是实例字段中。
	 * 
	 * @return SqlSession
	 */
	public static SqlSession getSqlSessionIntance() {

		return sqlSessionFactory.openSession();

	}

}
