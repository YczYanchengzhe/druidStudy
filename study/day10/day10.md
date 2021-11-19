# SQL 解析

- 对于建表语句的解析

```java
class SchemaResolveVisitorFactory {
	
	static void resolve(SchemaResolveVisitor visitor, SQLCreateTableStatement x) {
		// 解析建表语句
		SchemaResolveVisitor.Context ctx = visitor.createContext(x);
		// 取到表的资源
		SQLExprTableSource table = x.getTableSource();
		ctx.setTableSource(table);
		// 通过 SQLExprTableSource 进行表的解析
		table.accept(visitor);

		List<SQLTableElement> elements = x.getTableElementList();
		for (int i = 0; i < elements.size(); i++) {
			SQLTableElement e = elements.get(i);
			if (e instanceof SQLColumnDefinition) {
				// sql 列
				SQLColumnDefinition columnn = (SQLColumnDefinition) e;
				SQLName columnnName = columnn.getName();
				if (columnnName instanceof SQLIdentifierExpr) {
					// 该列是不是标识符
					SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) columnnName;
					identifierExpr.setResolvedTableSource(table);
					identifierExpr.setResolvedColumn(columnn);
				}
			} else if (e instanceof SQLUniqueConstraint) {
				// 唯一约束
				List<SQLSelectOrderByItem> columns = ((SQLUniqueConstraint) e).getColumns();
				for (SQLSelectOrderByItem orderByItem : columns) {
					SQLExpr orderByItemExpr = orderByItem.getExpr();
					// 找到 order by
					if (orderByItemExpr instanceof SQLIdentifierExpr) {
						SQLIdentifierExpr identifierExpr = (SQLIdentifierExpr) orderByItemExpr;
						identifierExpr.setResolvedTableSource(table);

						SQLColumnDefinition column = x.findColumn(identifierExpr.nameHashCode64());
						if (column != null) {
							identifierExpr.setResolvedColumn(column);
						}
					}
				}
			} else {
				e.accept(visitor);
			}
		}
		// 解析 select
		SQLSelect select = x.getSelect();
		if (select != null) {
			visitor.visit(select);
		}

		SchemaRepository repository = visitor.getRepository();
		if (repository != null) {
			repository.acceptCreateTable(x);
		}

		visitor.popContext();

		SQLExprTableSource like = x.getLike();
		if (like != null) {
			like.accept(visitor);
		}
	}
}

```


# 上面已经分析了很多解析逻辑, 下面我们来简单总结一下 visit 到底是怎么工作的

## 1. 根据 db 类型创建 visit

这里实际上创建的是一个 : SchemaStatVisitor ,他是基于 `SQLASTVisitor`的, `SQLASTVisitor`中定义了AST(抽象语法树)的统一方法 : 
- endVisit : 对于每一种语法都应该有一个结束 , 这里结束就是通过endVisit来标识
- postVisit : 解析的后置处理 , SQLASTOutputVisitor 该类中进行实现,用于在结尾添加一个`;`
- preVisit : 解析的前置处理 , 例如 监控和 sql 注入检测,都可以在这里进行埋点
- visit : 对于每一种语法树的解析逻辑,这里的参数代表了要解析的类型,例如要解析的是`SQLAllColumnExpr`,那么在 SQLASTOutPutVisit 中就会打印`*`,这里 visit 的参数也代表了 sql 中可能存在的语句情况

# 2. 根据 parseStatements 的结果将 visit 传入进行访问
- SchemaStatVisitor : stat 访问器 , 通过该方法初始化 , createSchemaStatVisitor
- SQLASTOutputVisitor : 输出访问器 , 通过该方法初始化 , createFormatOutputVisitor , createOutputVisitor

```java
class Demo{
	/**
     * 测试通过 SQLASTOutputVisitor 打印参数
	 */
	public void testPrintParameter() {
		StringBuilder sb=new StringBuilder();
		SQLASTOutputVisitor statementParser = new SQLASTOutputVisitor(sb);
		LocalDateTime now = LocalDateTime.now();
		statementParser.printParameter(now);
		assertEquals(now.toString(),sb.toString());
	}
}
```

# 3. 将解析出来的 SQLStatement 数据传递给 visit : 
- `statemen.accept(visitor);`
- statemen 表示解析的结果 , 调用 accept 将其传给 visit
- 在 accept 中,会基于SQLStatement进行赋值处理 , 对于每一个关键字都会进行解析.
  

# 4. 对于每一个sql statement 的处理抽象就是 SQLObject
- accept : 核心方法,用于进行解析 , 每一个accept , 都会按照 : preVisit -> accept0 -> postVisit 的流程进行执行
- clone : 深拷贝出来一个和自己一样的 SQLObject
- getParent/setParent : 当前解析端的父亲
- putAttribute/getAttribute/containsAttribute : sql 解析属性相关操作,例如进行了两个字段相加,这里会把相加的结果作为 Attribute存储其中
- output : 入参是一个 buffer , 用于将指定字符输出到 buffer 中
- addBeforeComment/addAfterComment/hasBeforeComment/hasAfterComment : 判断 attributes 中是否有设置一些前置后置的 comment,进行添加,实际上也是设置在attributes中

# 5. SQLReplaceable : 用于进行元素替代
- `replace(SQLExpr expr, SQLExpr target);` : 第一个参数是期望值 ,第二个参数是替代值


# 总结
上面主要是针对 sql 解析的核心抽象接口进行了一个梳理,在结合之前的代码分析,相信大家可以对 sql 解析的处理流程有一个大体的印象