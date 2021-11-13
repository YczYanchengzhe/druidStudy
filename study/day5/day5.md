## Druid SQL 解析

- 测试case
```java
public class Demo {
    public void test_select_3() throws Exception{
        String sql = "sElect * from t_service left join t_servicefunction on t_service.id = t_servicefunction.sid where servicename like '%frame%' and t_service.type=1 group by sname  having orgid='201509041611044dc486c6' order by clusterName desc  limit 1,20";
        List statementList = SQLUtils.parseStatements(sql, DbType.mysql);
        System.out.println("test");
    }
}
```

- 解析入口

```java
public class SQLUtils {
    public static List<SQLStatement> parseStatements(String sql, DbType dbType, SQLParserFeature... features) {
        // 根据第一个 sql 关键字 创建 sql 解析器
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType, features);
        List<SQLStatement> stmtList = new ArrayList<SQLStatement>();
        // 进行解析
        parser.parseStatementList(stmtList, -1, null);
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("syntax error : " + sql);
        }
        return stmtList;
    }
}
```

- 解析 token 的初始化 : 以聚合函数为例

> 这里对于所有的关键字都采用 fnv 算法进行 hash,构造字符串和哈希值的映射表,并且在初始化映射表时候使用了二分查找,提高初始化速度

```java
public class AntsparkExprParser {
    static {
        String[] strings = { "AVG", "COUNT", "MAX", "MIN", "STDDEV", "SUM", "ROW_NUMBER",
                "ROWNUMBER" };

        AGGREGATE_FUNCTIONS_CODES = FnvHash.fnv1a_64_lower(strings, true);
        AGGREGATE_FUNCTIONS = new String[AGGREGATE_FUNCTIONS_CODES.length];
        /*
            对于字符串使用 fnv 算法进行处理,最终得到一个 hash值
            查询时候根据 hash 值,来进行判断,提高查询速度
         */
        for (String str : strings) {
            long hash = FnvHash.fnv1a_64_lower(str);
            // 使用二分法
            int index = Arrays.binarySearch(AGGREGATE_FUNCTIONS_CODES, hash);
            AGGREGATE_FUNCTIONS[index] = str;
        }
    }
}
```

- 创建 mysql 解析器

```java
public class MySqlExprParser {
    public MySqlExprParser(String sql, SQLParserFeature... features){
        super(new MySqlLexer(sql, features), DbType.mysql);
        this.aggregateFunctions = AGGREGATE_FUNCTIONS;
        this.aggregateFunctionHashCodes = AGGREGATE_FUNCTIONS_CODES;
        if (sql.length() > 6) {
            char c0 = sql.charAt(0);
            char c1 = sql.charAt(1);
            char c2 = sql.charAt(2);
            char c3 = sql.charAt(3);
            char c4 = sql.charAt(4);
            char c5 = sql.charAt(5);
            char c6 = sql.charAt(6);
            // 对于标准的 select insert update 进行此法优化,优化只支持全大写或者全小写 ,所以如果 Select 这种会跳过优化
            if (c0 == 'S' && c1 == 'E' && c2 == 'L' && c3 == 'E' && c4 == 'C' && c5 == 'T' && c6 == ' ') {
                lexer.reset(6, ' ', Token.SELECT);
                return;
            }
            // ....
            // 省略中间部分
            // ....
            if (c0 == '/' && c1 == '*' && (isEnabled(SQLParserFeature.OptimizedForParameterized) && !isEnabled(SQLParserFeature.TDDLHint))) {
                MySqlLexer mySqlLexer = (MySqlLexer) lexer;
                mySqlLexer.skipFirstHintsOrMultiCommentAndNextToken();
                return;
            }
        }
        // 如果没有命中优化,通过 next token 检索第一个查询关键词
        this.lexer.nextToken();
    }
}
```

```java
public class Lexer {
    /**
     * 寻找下一个 token
     */
    public final void nextToken() {
        // 初始化查找位置
        startPos = pos;
        bufPos = 0;
        if (comments != null && comments.size() > 0) {
            comments = null;
        }

        this.lines = 0;
        int startLine = line;

        for (;;) {
            // 跳过空格
            if (isWhitespace(ch)) {
                if (ch == '\n') {
                    line++;

                    lines = line - startLine;
                }

                ch = charAt(++pos);
                startPos = pos;
                continue;
            }

            if (ch == '$' && isVaraintChar(charAt(pos + 1))) {
                scanVariable();
                return;
            }
            // 如果是字符的话,进行字符解析,找到第一个关键字
            if (isFirstIdentifierChar(ch)) {
                if (ch == '（') {
                    scanChar();
                    token = LPAREN;
                    return;
                } else if (ch == '）') {
                    scanChar();
                    token = RPAREN;
                    return;
                }

                if (ch == 'N' || ch == 'n') {
                    if (charAt(pos + 1) == '\'') {
                        ++pos;
                        ch = '\'';
                        scanString();
                        token = Token.LITERAL_NCHARS;
                        return;
                    }
                }

                if (ch == '—' && charAt(pos + 1) == '—' && charAt(pos + 2) == '\n') {
                    pos += 3;
                    ch = charAt(pos);
                    continue;
                }
                // 扫描一个关键字
                scanIdentifier();
                return;
            }

            /*
                解析一个字符
             */
            switch (ch) {
                case '0':
                    if (charAt(pos + 1) == 'x') {
                        // 16 进制解析
                        scanChar();
                        scanChar();
                        scanHexaDecimal();
                    } else {
                        scanNumber();
                    }
                    return;
                // .....
                // 其他条件省略
                // .....
            }
        }
    }
}
```

```java
public class CharTypes {
    /**
     * 判断是不是第一个需要解析的字符
     * @param c
     * @return
     */
    public static boolean isFirstIdentifierChar(char c) {
        // 判断是不是第一个字符 : 借助下标寻址,优化检索速度
        if (c <= firstIdentifierFlags.length) {
            return firstIdentifierFlags[c];
        }
        return c != '　' && c != '，';
    }
}
```

```java
public class MySqlLexer {
    /**
     * 扫描一个关键字 , 决定了将要解析的 下一段 text 是什么类型
     */
    public void scanIdentifier() {
        hash_lower = 0;
        hash = 0;

        final char first = ch;

        if (first == 'U'
                && isEnabled(SQLParserFeature.Presto)
                && charAt(pos + 1) == '&'
                && charAt(pos + 2) == '\'') {
            initBuff(32);
            pos += 3;

            for (;;pos++) {
                ch = charAt(pos);
                if (isEOF()) {
                    lexError("unclosed.str.lit");
                    return;
                }

                if (ch == '\'') {
                    ch = charAt(++pos);
                    break;
                }

                if (ch == '\\') {
                    char c1 = charAt(++pos);
                    char c2 = charAt(++pos);
                    char c3 = charAt(++pos);
                    char c4 = charAt(++pos);

                    String tmp;
                    if (ch == '+') {
                        char c5 = charAt(++pos);
                        char c6 = charAt(++pos);
                        tmp = new String(new char[]{c1, c2, c3, c4, c5, c6});
                    } else {
                        tmp = new String(new char[]{c1, c2, c3, c4});
                    }
                    int intVal = Integer.parseInt(tmp, 16);
                    putChar((char) intVal);
                } else {
                    putChar(ch);
                }
            }
            stringVal = new String(buf, 0, bufPos);
            token = LITERAL_CHARS;
            return;
        }

        if ((ch == 'b' || ch == 'B')
                && charAt(pos + 1) == '\'') {
            int i = 2;
            int mark = pos + 2;
            for (;;++i) {
                char ch = charAt(pos + i);
                if (ch == '0' || ch == '1') {
                    continue;
                } else if (ch == '\'') {
                    bufPos += i;
                    pos += (i + 1);
                    stringVal = subString(mark, i - 2);
                    this.ch = charAt(pos);
                    token = Token.BITS;
                    return;
                } else if (ch == EOI) {
                    throw new ParserException("illegal identifier. " + info());
                } else {
                    break;
                }
            }
        }

        if (ch == '`') {
            mark = pos;
            bufPos = 1;
            char ch;

            int startPos = pos + 1;
            int quoteIndex;

            /*
                进行 fnv hash 算法获取到哈希值
                哈希初始值 : hash_lower
             */
            hash_lower = 0xcbf29ce484222325L;
            hash = 0xcbf29ce484222325L;

            for (int i = startPos;; ++i) {
                if (i >= text.length()) {
                    throw new ParserException("illegal identifier. " + info());
                }

                ch = text.charAt(i);

                if ('`' == ch) {
                    if (i + 1 < text.length() && '`' == text.charAt(i + 1)) {
                        ++i;
                    } else {
                        // End of identifier.
                        quoteIndex = i;
                        break;
                    }
                }

                hash_lower ^= ((ch >= 'A' && ch <= 'Z') ? (ch + 32) : ch);
                hash_lower *= 0x100000001b3L;

                hash ^= ch;
                hash *= 0x100000001b3L;
            }

            stringVal = quoteTable.addSymbol(text, pos, quoteIndex + 1 - pos, hash);
            //stringVal = text.substring(mark, pos);
            pos = quoteIndex + 1;
            this.ch = charAt(pos);
            token = Token.IDENTIFIER;
        } else {
            final boolean firstFlag = isFirstIdentifierChar(first);
            if (!firstFlag) {
                throw new ParserException("illegal identifier. " + info());
            }

            hash_lower = 0xcbf29ce484222325L;
            hash = 0xcbf29ce484222325L;
            // 大小写转换
            hash_lower ^= ((ch >= 'A' && ch <= 'Z') ? (ch + 32) : ch);
            hash_lower *= 0x100000001b3L;

            hash ^= ch;
            hash *= 0x100000001b3L;

            mark = pos;
            bufPos = 1;
            char ch = '\0';
            for (;;) {
                ch = charAt(++pos);
                // 找到分隔符后退出
                if (!isIdentifierChar(ch)) {
                    break;
                }
                /*
                    int64  fnv hash值计算
                 */
                bufPos++;

                hash_lower ^= ((ch >= 'A' && ch <= 'Z') ? (ch + 32) : ch);
                hash_lower *= 0x100000001b3L;

                hash ^= ch;
                hash *= 0x100000001b3L;

                continue;
            }

            this.ch = charAt(pos);

            if (bufPos == 1) {
                token = Token.IDENTIFIER;
                stringVal = CharTypes.valueOf(first);
                if (stringVal == null) {
                    stringVal = Character.toString(first);
                }
                return;
            }

            Token tok = keywords.getKeyword(hash_lower);
            if (tok != null) {
                /*
                    这里指定 token , token 决定了查询解析器需要解析的 sql 关键字
                 */
                token = tok;
                if (token == Token.IDENTIFIER) {
                    stringVal = SymbolTable.global.addSymbol(text, mark, bufPos, hash);
                } else {
                    stringVal = null;
                }
            } else {
                /*
                   不在token 列表中  IDENTIFIER 表示不在关键字列表中的 token
                */
                token = Token.IDENTIFIER;
                stringVal = SymbolTable.global.addSymbol(text, mark, bufPos, hash);
            }

        }
    }
}
```


