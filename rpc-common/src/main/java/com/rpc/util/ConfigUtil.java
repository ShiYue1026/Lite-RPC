package com.rpc.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

@Slf4j
public class ConfigUtil {

    // 加载配置文件，使用默认环境
    public static <T> T loadConfig(Class<T> targerClass, String prefix) {
        return loadConfig(targerClass, prefix, "");
    }

    // 加载配置文件，使用默认环境
    public static <T> T loadConfig(Class<T> targetClass, String prefix, String environment) {
        StringBuilder configFileNameBuilder = new StringBuilder("application");

        // 根据环境拼接文件名
        if (environment != null && !environment.isEmpty()) {
            configFileNameBuilder.append("-").append(environment);
        }
        configFileNameBuilder.append(".yml");

        String configFileName = configFileNameBuilder.toString();
        Yaml yaml = new Yaml();

        try(InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream(configFileName)) {
            if(in == null) {
                throw new RuntimeException("配置文件: " + configFileName + "未找到");
            }
            Map<String, Object> yamlMap = yaml.load(in);
            if(yamlMap == null || yamlMap.isEmpty()) {
                throw new RuntimeException("配置文件: " + configFileName + "内容为空");
            }
            Map<String, Object> subConfig = getSubConfig(yamlMap, prefix);
            if(subConfig == null || subConfig.isEmpty()) {
                throw new RuntimeException("配置前缀: " + configFileName + "未找到对应配置");
            }

            // 将子配置转换为目标对象
            return convertMapToBean(subConfig, targetClass);

        } catch (Exception e){
            throw new RuntimeException("加载配置文件失败: " + configFileName, e);
        }

    }


    // 获取前缀对应的配置
    private static Map<String, Object> getSubConfig(Map<String, Object> yamlMap, String prefix) {
        String[] keys = prefix.split("\\.");
        Map<String, Object> currentMap = yamlMap;
        for (String key : keys) {
            Object value = currentMap.get(key);
            if(value instanceof Map) {
                currentMap = (Map<String, Object>) value;
            } else{
                return null;
            }
        }
        return currentMap;
    }


    // 将Map转换为目标对象
    private static <T> T convertMapToBean(Map<String, Object> map, Class<T> targetClass) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(map, targetClass);
        } catch (Exception e) {
            log.error("配置转换失败，目标类: {}", targetClass.getName(), e);
            throw new RuntimeException("配置加载失败", e);
        }
    }
}
