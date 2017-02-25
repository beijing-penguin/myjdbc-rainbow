#快速入门
##快速入门教程，将以视频内容推出，尽情期待！
##事务配置请参考另外一份SpringConfig.txt aop配置说明
##Myjdbc是一个轻量级orm持久层操作api，只依赖commons-logging日志架包<br />
##支持0配置0注解对实体对象的增删改查，也支持直接传入sql操作数据库
##主要支持MYSQL，兼容其他以jdbc为驱动的数据库<br />
##本框架采用低耦合分层软件架构（共2层）：第一层DBHelper---经过DataBaseOperateProxy代理---第二层DataBaseDaoImp。每层总共享SqlContext上下文中的数据<br />
##支持完整的sql日志打印与日志是否输出动态控制
##支持多数据源操作
##支持无缝对接当当开源的分库分表sharding-jdbc
