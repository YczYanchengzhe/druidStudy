# sql 解析流程梳理

- 首先做一个测试 case , 尝试使用 visit 访问 sql 解析的数据

```java
import com.alibaba.druid.sql.SQLUtils;

class Demo {

	public void test_select_4() throws Exception {
		String sql = "sElect * from t_service left join t_servicefunction on t_service.id = t_servicefunction.sid where servicename like '%frame%' and t_service.type=1 group by sname  having orgid='201509041611044dc486c6' order by clusterName desc  limit 1,20";
		// sql 解析
		List<SQLStatement> statementList = SQLUtils.parseStatements(sql, DbType.mysql);
		// 创建访问器
		SchemaStatVisitor statVisitor = SQLUtils.createSchemaStatVisitor(DbType.mysql);
		// 格式化输出 visit
		SQLASTOutputVisitor sqlastOutputVisitor = SQLUtils.createFormatOutputVisitor(out /* 输出的字符串 */, statementList /*要解析的语法树*/, DbType.mysql /*数据库类型*/);
		SQLASTOutputVisitor sqlastOutputVisitor = SQLUtils.createOutputVisitor(out /* 输出的字符串 */,DbType.mysql);
		// 遍历解析的结果 通过访问器进行访问
		for (SQLStatement stmt : statementList) {
			// 这里入口其实是 sql statement
			stmt.accept(statVisitor);
		}
		System.out.println("使用visitor数据表：" + statVisitor.getTables());
		System.out.println("使用visitor字段：" + statVisitor.getColumns());
		for (TableStat.Column column : statVisitor.getColumns()) {
			if (column.isSelect()) {
				System.out.println("查询的字段：" + column.getFullName() + "," + column.getName());
			}
		}
		System.out.println("使用visitor条件：" + statVisitor.getConditions());
		System.out.println("使用visitor分组：" + statVisitor.getGroupByColumns());
		System.out.println("使用visitor排序：" + statVisitor.getOrderByColumns());
	}
}

```

- 在前面我们已经简要分析了 sql 解析的大体流程 ,下面我们来看下 通过 visit 如何访问解析出来的数据

```java
class Demo1 {
	public static SchemaStatVisitor createSchemaStatVisitor(SchemaRepository repository, DbType dbType) {
		if (repository == null) {
			// 初始化时候 默认是 null , 会根据db 类型初始化 SchemaRepository
			// 这里会定义一堆 visit 去访问语法树
			repository = new SchemaRepository(dbType);
		}

		if (dbType == null) {
			return new SchemaStatVisitor(repository);
		}

		switch (dbType) {
			case oracle:
				return new OracleSchemaStatVisitor(repository);
			case mysql:
			case mariadb:
			case elastic_search:
				// 基于 repository 对于 mysql 进行 MySqlSchemaStatVisitor 创建 , 这里是进行了一层封装
				return new MySqlSchemaStatVisitor(repository);
			// 省略其他类型.....
		}
	}
}
```

- 开始进行解析前的准备工作

```java
public abstract class SQLObjectImpl implements SQLObject {

	public final void accept(SQLASTVisitor visitor) {
		if (visitor == null) {
			throw new IllegalArgumentException();
		}
		// 前置访问
		visitor.preVisit(this);

		accept0(visitor);
		// 后置 visit
		visitor.postVisit(this);
	}

}

public class SQLSelectStatement extends SQLStatementImpl {

	protected void accept0(SQLASTVisitor visitor) {
		if (visitor.visit(this)) {
			if (this.select != null) {
				this.select.accept(visitor);
			}
		}
		visitor.endVisit(this);
	}
}

public class MySqlSchemaStatVisitor extends SchemaStatVisitor implements MySqlASTVisitor {

	public boolean visit(SQLSelectStatement x) {
		// 判断是否是根(存在数据库 , 并且当前 selectStatement 的父亲是 null ,说明此时是第一个 sql 子句)
		if (repository != null
				&& x.getParent() == null) {
			// 默认不会传入解析参数，表示解析所有字段
			repository.resolve(x);
		}

		return true;
	}
}

/**
 * 解析的类型
 */
public static enum Option {
	/**
	 * 解析所有列
	 */
	ResolveAllColumn,
	/**
	 * 解析标识符别名
	 */
	ResolveIdentifierAlias,
	/**
	 * 解析列是否模糊查询
	 */
	CheckColumnAmbiguous;
}

public class SchemaRepository {

	public void resolve(SQLSelectStatement stmt, SchemaResolveVisitor.Option... options) {
		if (stmt == null) {
			return;
		}
		// 根据解析参数创建一个解析访问器
		SchemaResolveVisitor resolveVisitor = createResolveVisitor(options);
		// 通过访问器访问 ： 默认情况下参数都是空的
		resolveVisitor.visit(stmt);
	}

	private SchemaResolveVisitor createResolveVisitor(SchemaResolveVisitor.Option... options) {
		// 判断访问的参数类型 : 如果没有参数 , 默认为 0 ,代表查询全部字段
		int optionsValue = SchemaResolveVisitor.Option.of(options);

		SchemaResolveVisitor resolveVisitor;
		switch (dbType) {
			case mysql:
			case mariadb:
			case sqlite:
				// 基于解析参数进行解析操作 ,这里只是初始化
				resolveVisitor = new SchemaResolveVisitorFactory.MySqlResolveVisitor(this, optionsValue);
				break;
		}
		return resolveVisitor;
	}
}
```

- 开始进行解析工作

```java
class SchemaResolveVisitorFactory {
	static void resolve(SchemaResolveVisitor visitor, SQLSelect x) {
		SchemaResolveVisitor.Context ctx = visitor.createContext(x);
		// 取出 with 子串
		SQLWithSubqueryClause with = x.getWithSubQuery();
		if (with != null) {
			// 存在 with 子串 , 访问 with 子串
			visitor.visit(with);
		}
		// 取出查询条件
		SQLSelectQuery query = x.getQuery();
		if (query != null) {
			if (query instanceof SQLSelectQueryBlock) {
				// 如果是查询的话,那么解析查询
				visitor.visit((SQLSelectQueryBlock) query);
			} else {
				// 否则继续解析
				query.accept(visitor);
			}
		}
		// 取出第一个查询块
		SQLSelectQueryBlock queryBlock = x.getFirstQueryBlock();
		// 取出 order by 字段
		SQLOrderBy orderBy = x.getOrderBy();
		if (orderBy != null) {
			for (SQLSelectOrderByItem orderByItem : orderBy.getItems()) {
				// 取出 order by 的分隔符
				SQLExpr orderByItemExpr = orderByItem.getExpr();
				// 如果当前的标识符是 sql 的分隔符
				if (orderByItemExpr instanceof SQLIdentifierExpr) {
					SQLIdentifierExpr orderByItemIdentExpr = (SQLIdentifierExpr) orderByItemExpr;
					// 基于分隔符的名字获取哈希码
					long hash = orderByItemIdentExpr.nameHashCode64();

					SQLSelectItem selectItem = null;
					if (queryBlock != null) {
						// 找到查询的列
						selectItem = queryBlock.findSelectItem(hash);
					}
					// 如果找到了需要查询的列
					if (selectItem != null) {
						// 记录 按照该列进行 order by,
						orderByItem.setResolvedSelectItem(selectItem);

						SQLExpr selectItemExpr = selectItem.getExpr();
						if (selectItemExpr instanceof SQLIdentifierExpr) {
							orderByItemIdentExpr.setResolvedTableSource(((SQLIdentifierExpr) selectItemExpr).getResolvedTableSource());
							orderByItemIdentExpr.setResolvedColumn(((SQLIdentifierExpr) selectItemExpr).getResolvedColumn());
						} else if (selectItemExpr instanceof SQLPropertyExpr) {
							orderByItemIdentExpr.setResolvedTableSource(((SQLPropertyExpr) selectItemExpr).getResolvedTableSource());
							orderByItemIdentExpr.setResolvedColumn(((SQLPropertyExpr) selectItemExpr).getResolvedColumn());
						}
						continue;
					}
				}
				// 表示该阶段解析完成 , 进行下一个阶段解析
				orderByItemExpr.accept(visitor);
			}
		}
		// context = context.parent 类似一个出栈操作
		visitor.popContext();
	}

	static void resolve(SchemaResolveVisitor visitor, SQLWithSubqueryClause x) {
		// 取出 with 子串信息
		List<SQLWithSubqueryClause.Entry> entries = x.getEntries();
		final SchemaResolveVisitor.Context context = visitor.getContext();
		// 遍历信息
		for (SQLWithSubqueryClause.Entry entry : entries) {
			// 取出子串
			SQLSelect query = entry.getSubQuery();
			if (query != null) {
				// 这里实际上是递归查询 , 继续查询所有 Select 下的语句
				visitor.visit(query);
				// 这里采用 Fnv 哈希算法
				final long alias_hash = entry.aliasHashCode64();
				if (context != null && alias_hash != 0) {
					// 这里实际上是一个 map,通过 fnv 计算的哈希值作为 key ,存储访问到的查询资源
					context.addTableSource(alias_hash, entry);
				}
			} else {
				// 如果没有子串了 , 说明该部分解析完成 , 获取 返回的 statement 去进行 解析
				entry.getReturningStatement().accept(visitor);
			}
		}
	}
}

```


