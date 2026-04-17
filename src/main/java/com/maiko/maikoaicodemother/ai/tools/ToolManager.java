package com.maiko.maikoaicodemother.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 工具管理器
 * <p>
 * 统一管理所有工具，提供根据名称获取工具的功能。
 * 利用 Spring 的依赖注入机制自动收集所有继承自 BaseTool 的组件。
 */
@Slf4j
@Component
public class ToolManager {

/**
 * 它这里它能初始化？
 * ⚙️ 核心原理：Spring 的自动装配与生命周期
 * 这个类之所以能“自动”工作，是因为它完全遵循了 Spring Bean 的标准生命周期。具体流程如下：
 * 1. 扫描与注册 (@Component)
 * 当你的 Spring Boot 应用启动时，Spring 会进行包扫描。
 * 一旦发现了 @Component 注解（或者 @Service, @Controller 等），Spring 就会知道：“哦，这是一个需要我管理的 Bean”。
 * 于是，Spring 会在内存中创建 ToolManager 的一个实例（Singleton 单例）。
 * 2. 依赖注入 (@Resource)
 * 在 ToolManager 实例化之后，Spring 发现里面有一个 @Resource private BaseTool[] tools;。
 * Spring 会去容器里找所有继承自 BaseTool 的类（比如你之前写的 FileReadTool, FileWriteTool 等）。
 * 然后，Spring 会自动把这些工具实例打包成一个数组，塞进 tools 字段里。此时，tools 就不再是 null 了。
 * 3. 触发初始化 (@PostConstruct)
 * 这是最关键的一步。@PostConstruct 是 Java 标准（JSR-250）的注解。
 * 它的意思是：“在这个类实例化完成、并且所有依赖注入（@Resource）都做完之后，立刻执行这个方法。”
 * 所以，当 Spring 启动完毕，initTools() 方法会自动被调用。它遍历刚才注入进来的 tools 数组，把它们一个个放进 toolMap 里。
 */

    /**
     * 工具名称到工具实例的映射表
     * Key: 工具的英文名称 (如 "readFile")
     * Value: 工具实例对象
     */
    private final Map<String, BaseTool> toolMap = new HashMap<>();

    /**
     * 自动注入所有实现了 BaseTool 的工具类实例
     * Spring 会自动将所有 @Component 标记的 BaseTool 子类注入到此数组中
     */
    @Resource
    private BaseTool[] tools;

    /**
     * 初始化工具映射
     * <p>
     * 在 Bean 创建后执行，遍历所有注入的工具，将其注册到 Map 中以便快速查找。
     */
    @PostConstruct
    public void initTools() {
        for (BaseTool tool : tools) {
            toolMap.put(tool.getToolName(), tool);
            log.info("注册工具: {} -> {}", tool.getToolName(), tool.getDisplayName());
        }
        log.info("工具管理器初始化完成，共注册 {} 个工具", toolMap.size());
    }

    /**
     * 根据工具名称获取工具实例
     *
     * @param toolName 工具英文名称（例如：readFile, writeFile）
     * @return 对应的工具实例，如果不存在则返回 null
     */
    public BaseTool getTool(String toolName) {
        return toolMap.get(toolName);
    }

    /**
     * 获取所有已注册的工具数组
     *
     * @return 包含所有工具实例的数组
     */
    public BaseTool[] getAllTools() {
        return tools;
    }
}