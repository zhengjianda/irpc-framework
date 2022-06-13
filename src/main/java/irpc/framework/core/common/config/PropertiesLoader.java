package irpc.framework.core.common.config;

import irpc.framework.core.common.utils.CommonUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 配置加载层
 */
public class PropertiesLoader {
    private static Properties properties;

    // 这里感觉是类似缓存的作用，先查看propertiesMap找不到再去properties中查询
    private static Map<String,String> propertiesMap = new HashMap<>();

    // file路径还需要再看看具体怎么写
    private static String DEFAULT_PROPERTIES_FILE ="D:\\irpc-framework\\src\\main\\resources\\irpc.properties";

    //todo 如果这里直接使用static修饰是否可以？
    // 从本地文件中加载配置
    public static void loadConfiguration() throws IOException {
        if (properties!=null){
            return ;
        }
        properties = new Properties();
        FileInputStream in = null;
        in = new FileInputStream(DEFAULT_PROPERTIES_FILE);
        properties.load(in);
    }

    /**
     * 根据键值获取配置属性
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key){
        if (properties==null){
            return null;
        }
        if (CommonUtils.isEmpty(key)){
            return null;
        }
        if (!propertiesMap.containsKey(key)){
            String value = properties.getProperty(key);
            propertiesMap.put(key,value);
        }
        return String.valueOf(propertiesMap.get(key));
    }

    public static Integer getPropertiesInteger(String key){
        if (properties==null){
            return null;
        }
        if (CommonUtils.isEmpty(key)){
            return null;
        }
        if (!propertiesMap.containsKey(key)){
            String value = properties.getProperty(key);
            propertiesMap.put(key,value);
        }
        return Integer.valueOf(propertiesMap.get(key));
    }


}
