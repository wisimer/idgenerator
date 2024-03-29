package com.xp.id.idgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class IDGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IDGeneratorApplication.class, args);
    }

//    /**
//     * 设置 redisTemplate 的序列化设置
//     *
//     * @param redisConnectionFactory
//     * @return
//     */
//    @Bean
//    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        // 1.创建 redisTemplate 模版
//        RedisTemplate<Object, Object> template = new RedisTemplate<>();
//        // 2.关联 redisConnectionFactory
//        template.setConnectionFactory(redisConnectionFactory);
//        // 3.创建 序列化类
//        GenericToStringSerializer genericToStringSerializer = new GenericToStringSerializer(Integer.class);
//        // 6.序列化类，对象映射设置
//        // 7.设置 value 的转化格式和 key 的转化格式
//        template.setValueSerializer(genericToStringSerializer);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.afterPropertiesSet();
//        return template;
//    }

}
