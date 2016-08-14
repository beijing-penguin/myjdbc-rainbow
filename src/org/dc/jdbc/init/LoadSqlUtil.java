package org.dc.jdbc.init;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dc.jdbc.config.JDBCConfig;
import org.dc.jdbc.entity.SqlContext;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class LoadSqlUtil {
	private static final Log LOG = LogFactory.getLog(LoadSqlUtil.class);

	/**
	 * 加载xml文件中的sql，遍历该目录下，以及子目录下的所有xml文件。
	 * @param readPath xml文件绝对地址
	 * @throws Exception
	 */
	public static void loadSql(String readPath) throws Exception{
		//创建SAXReader对象  
		SAXReader reader = new SAXReader();
		File file = new File(readPath);
		File[] files =  file.listFiles();
		LOG.info("文件数"+files.length+"");
		for (int i = 0; i < files.length; i++) {
			File f = files[i];
			if(f.isFile()){
				//读取文件 转换成Document  
				Document document = reader.read(f);
				//获取根节点元素对象  
				Element root = document.getRootElement();  
				List<?> sqlNodeList = root.elements("sql");
				for (Object aSqlNodeList : sqlNodeList) {
					Element sqlnode = (Element) aSqlNodeList;
					String key = f.getName().substring(0, f.getName().length()-4)+"."+sqlnode.attributeValue("id");
					key = "$"+key;
					//检查是否有冲突
					if(SqlContext.sqlSourceMap.containsKey(key)){
						throw new Exception("sql的id有重复，id="+key);
					}
					LOG.info("加载key="+key);
					if(JDBCConfig.isPrintSqlLog){
						SqlContext.putSourceSql(key, sqlnode.getTextTrim());
					}else{
						SqlContext.putSourceSql(key, sqlnode.getTextTrim());
					}
				}
			}else{
				loadSql(f.getPath());
			}
		}
	}
}
