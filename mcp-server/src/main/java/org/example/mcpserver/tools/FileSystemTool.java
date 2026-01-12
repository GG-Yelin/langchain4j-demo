package org.example.mcpserver.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 文件系统工具
 * 使用 Spring AI 的 @Tool 注解定义 MCP 工具
 *
 * 注意: 出于安全考虑，应该限制可访问的目录范围
 */
@Service
public class FileSystemTool {

    // 可在 application.yml 中配置: mcp.tools.filesystem.base-path=/your/path
    @Value("${mcp.tools.filesystem.base-path:#{systemProperties['user.home']}}")
    private String basePath;

    // TODO: 实现文件系统工具
    //
    // @Tool(description = "列出指定目录下的文件和文件夹")
    // public String listFiles(
    //     @ToolParam(description = "相对路径，如: . 或 documents") String relativePath) {
    //     Path dir = resolveSafePath(relativePath);
    //     StringBuilder result = new StringBuilder();
    //     result.append("目录: ").append(dir).append("\n\n");
    //
    //     try (Stream<Path> stream = Files.list(dir)) {
    //         stream.forEach(path -> {
    //             String type = Files.isDirectory(path) ? "[目录] " : "[文件] ";
    //             result.append(type).append(path.getFileName()).append("\n");
    //         });
    //     } catch (IOException e) {
    //         throw new RuntimeException("读取目录失败: " + e.getMessage(), e);
    //     }
    //     return result.toString();
    // }
    //
    // @Tool(description = "读取文件内容")
    // public String readFile(
    //     @ToolParam(description = "文件的相对路径") String relativePath) {
    //     Path file = resolveSafePath(relativePath);
    //     if (!Files.isRegularFile(file)) {
    //         throw new IllegalArgumentException("不是有效的文件: " + relativePath);
    //     }
    //     try {
    //         return Files.readString(file);
    //     } catch (IOException e) {
    //         throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
    //     }
    // }
    //
    // @Tool(description = "写入内容到文件")
    // public String writeFile(
    //     @ToolParam(description = "文件的相对路径") String relativePath,
    //     @ToolParam(description = "要写入的内容") String content) {
    //     Path file = resolveSafePath(relativePath);
    //     try {
    //         Files.writeString(file, content);
    //         return "文件写入成功: " + file;
    //     } catch (IOException e) {
    //         throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
    //     }
    // }
    //
    // /**
    //  * 安全地解析路径，防止路径遍历攻击
    //  */
    // private Path resolveSafePath(String relativePath) {
    //     Path base = Path.of(basePath).toAbsolutePath().normalize();
    //     Path resolved = base.resolve(relativePath).normalize();
    //     if (!resolved.startsWith(base)) {
    //         throw new SecurityException("不允许访问该路径: " + relativePath);
    //     }
    //     return resolved;
    // }
}
