package org.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 数据库查询工具
 * 使用 Spring AI 的 @Tool 注解定义 MCP 工具
 *
 * 注意: 只允许SELECT查询，防止数据被修改
 */
@Service
public class DatabaseTool {

    // 可在 application.yml 中配置数据库连接信息
    @Value("${mcp.tools.database.jdbc-url:}")
    private String jdbcUrl;

    @Value("${mcp.tools.database.username:}")
    private String username;

    @Value("${mcp.tools.database.password:}")
    private String password;

    // TODO: 实现数据库查询工具
    //
    // @Tool(description = "执行SQL查询（只支持SELECT语句）")
    // public String query(
    //     @ToolParam(description = "SQL查询语句，只支持SELECT") String sql) {
    //     // 安全检查
    //     String trimmedSql = sql.trim().toUpperCase();
    //     if (!trimmedSql.startsWith("SELECT")) {
    //         throw new SecurityException("只允许执行SELECT查询");
    //     }
    //
    //     try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
    //          Statement stmt = conn.createStatement();
    //          ResultSet rs = stmt.executeQuery(sql)) {
    //
    //         ResultSetMetaData meta = rs.getMetaData();
    //         int columnCount = meta.getColumnCount();
    //
    //         StringBuilder result = new StringBuilder();
    //
    //         // 表头
    //         for (int i = 1; i <= columnCount; i++) {
    //             result.append(meta.getColumnName(i));
    //             if (i < columnCount) result.append("\t");
    //         }
    //         result.append("\n");
    //         result.append("-".repeat(50)).append("\n");
    //
    //         // 数据行 (限制100行)
    //         int rowCount = 0;
    //         while (rs.next() && rowCount < 100) {
    //             for (int i = 1; i <= columnCount; i++) {
    //                 result.append(rs.getString(i));
    //                 if (i < columnCount) result.append("\t");
    //             }
    //             result.append("\n");
    //             rowCount++;
    //         }
    //
    //         result.append("\n共 ").append(rowCount).append(" 行");
    //         return result.toString();
    //     } catch (SQLException e) {
    //         throw new RuntimeException("数据库查询失败: " + e.getMessage(), e);
    //     }
    // }
    //
    // @Tool(description = "列出数据库中的所有表")
    // public String listTables() {
    //     try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
    //         DatabaseMetaData meta = conn.getMetaData();
    //         ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
    //
    //         StringBuilder result = new StringBuilder("数据库表列表:\n\n");
    //         while (rs.next()) {
    //             result.append("- ").append(rs.getString("TABLE_NAME")).append("\n");
    //         }
    //         return result.toString();
    //     } catch (SQLException e) {
    //         throw new RuntimeException("获取表列表失败: " + e.getMessage(), e);
    //     }
    // }
}
