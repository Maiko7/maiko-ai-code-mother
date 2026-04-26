//package com.maiko.maikoaicodemother.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// * Redis配置类
// * 配置RedisTemplate的序列化策略，确保key使用String序列化，value使用JSON序列化
// */
//@Configuration
//public class RedisConfig {
//
//    /**
//     * 配置RedisTemplate实例
//     * 自定义序列化处理方式：key采用String序列化，value采用JSON序列化
//     *
//     * @param connectionFactory Redis连接工厂，由Spring容器自动注入
//     * @return 配置好序列化策略的RedisTemplate实例
//     */
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//        // 1. 实例化 RedisTemplate
//        // RedisTemplate 是 Spring Data Redis 提供的核心类，用于在代码中操作 Redis 数据库。
//        // <String, Object> 表示 Key 的类型是 String，Value 的类型是 Object。
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//
//        // 2. 设置连接工厂
//        // 将之前配置好的连接工厂（包含 Redis 服务器地址、端口、密码等信息）注入到模板中，
//        // 这样 template 才知道要连接哪一个 Redis 服务器。
//        template.setConnectionFactory(connectionFactory);
//
//        // 3. 配置 Key 的序列化方式
//        // 创建 StringRedisSerializer，用于将 Java 的 String 转换为 Redis 的字节数组。
//        StringRedisSerializer stringSerializer = new StringRedisSerializer();
//
//        // 设置 Key 的序列化器：
//        // 这保证了存入 Redis 的 Key 是普通的可读字符串（例如 "user:1001"），而不是乱码或包含特殊字符的字节流。
//        template.setKeySerializer(stringSerializer);
//
//        // 设置 Hash 结构中 Key（即 field）的序列化器：
//        // 同样保证 Hash 里的字段名也是可读的字符串。
//        template.setHashKeySerializer(stringSerializer);
//
//        // 4. 配置 Value 的序列化方式
//        // 创建 GenericJackson2JsonRedisSerializer，它利用 Jackson 库将 Java 对象转换为 JSON 字符串存储。
//        // 这种序列化方式有两个优点：
//        // 1. 存入 Redis 的数据是标准的 JSON 格式，人类可读，且方便其他语言解析。
//        // 2. 它在 JSON 中写入了类的全限定名（@class 属性），反序列化时能自动还原为原来的 Java 对象类型。
//        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
//
//        // 设置 Value 的序列化器：
//        // 决定了普通 Value（例如 set 命令）如何存储。这里使用 JSON 格式。
//        template.setValueSerializer(jsonSerializer);
//
//        // 设置 Hash 结构中 Value 的序列化器：
//        // 决定了 Hash 里的字段值（例如 hset 命令）如何存储。这里也使用 JSON 格式。
//        template.setHashValueSerializer(jsonSerializer);
//
//        // 5. 初始化配置
//        // 这是一个关键步骤。在 Spring 管理的 Bean 中，设置完属性后必须调用此方法。
//        // 它会检查必要的属性（如 connectionFactory）是否已设置，并应用上面的序列化配置。
//        // 如果不调用，template 可能会使用默认的 JDK 序列化方式，导致数据乱码。
//        template.afterPropertiesSet();
//
//        // 6. 返回配置好的 Bean
//        return template;
//    }
//}
