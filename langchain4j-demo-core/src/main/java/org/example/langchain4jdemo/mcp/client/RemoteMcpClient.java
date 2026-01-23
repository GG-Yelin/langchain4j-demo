package org.example.langchain4jdemo.mcp.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 远程 MCP 客户端
 * 通过 HTTP REST API 调用远程 MCP Server
 */
@Slf4j
@Component
public class RemoteMcpClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String mcpServerUrl;

    public RemoteMcpClient(
            @Value("${mcp.server.url:http://localhost:8081}") String mcpServerUrl,
            RestTemplate restTemplate,
            ObjectMapper objectMapper) {
        this.mcpServerUrl = mcpServerUrl;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        log.info("RemoteMcpClient initialized with server URL: {}", mcpServerUrl);
    }

    /**
     * 健康检查
     */
    public boolean checkHealth() {
        try {
            String url = mcpServerUrl + "/mcp/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("Health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 列出所有可用工具
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listTools() {
        try {
            String url = mcpServerUrl + "/mcp/tools";
            log.info("Listing tools from: {}", url);

            ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> tools = (List<Map<String, Object>>) (List<?>) response.getBody();
                log.info("Found {} tools from remote server", tools.size());
                return tools;
            }

            log.warn("Failed to list tools, status: {}", response.getStatusCode());
            return List.of();

        } catch (Exception e) {
            log.error("Failed to list tools from remote MCP server", e);
            throw new RuntimeException("Failed to list tools: " + e.getMessage(), e);
        }
    }

    /**
     * 执行工具
     */
    public String executeTool(String toolName, Map<String, Object> arguments) {
        try {
            String url = mcpServerUrl + "/mcp/execute";
            log.info("Executing tool: {} at {}", toolName, url);

            // 构建请求
            Map<String, Object> request = new HashMap<>();
            request.put("toolName", toolName);
            request.put("arguments", arguments);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            // 发送请求
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                Boolean success = (Boolean) body.get("success");

                if (Boolean.TRUE.equals(success)) {
                    String result = (String) body.get("result");
                    log.info("Tool executed successfully: {}", result);
                    return result;
                } else {
                    String error = (String) body.get("error");
                    log.error("Tool execution failed: {}", error);
                    throw new RuntimeException("Tool execution failed: " + error);
                }
            }

            log.error("Unexpected response status: {}", response.getStatusCode());
            throw new RuntimeException("Unexpected response from MCP server");

        } catch (Exception e) {
            log.error("Failed to execute tool: {}", toolName, e);
            throw new RuntimeException("Failed to execute tool: " + e.getMessage(), e);
        }
    }
}
