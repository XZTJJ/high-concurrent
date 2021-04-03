package org.example.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *  工具包
 */
public class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    //声明数据,地址
    private final static Map<String, String> config = new HashMap<String, String>();

    //静态初始化函数
    static {
        init();
    }

    //初始化函数
    private static void init() {
        InputStream in = null;
        InputStreamReader ireader = null;
        Properties properties = null;
        try {
            properties = new Properties();
            in = ConfigUtils.class.getClassLoader().getResourceAsStream("config.properties");
            //解决读非UTF-8编码的配置文件时，出现的中文乱码问题
            ireader = new InputStreamReader(in, "utf-8");
            properties.load(ireader);
            //开始加载数据
            for (String key : properties.stringPropertyNames())
                config.put(StringUtils.substringAfterLast(key, "."), properties.getProperty(key));
        } catch (IOException e) {
            LOGGER.error("加载配置文件出现错误", e);
        } finally {
            IOUtils.closeQuietly(ireader);
            IOUtils.closeQuietly(in);
        }
    }

    //获取对应的地址
    public static String getAddr() {
        return config.get("addr");
    }

    //获取端口
    public static int getPort() {
        return Integer.valueOf(config.get("port"));
    }

    //获取书名
    public static String getBook() {
        return config.get("book");
    }

    //获取限定长度
    public static int getStrLength() {
        return Integer.valueOf(config.get("strlength"));
    }

    //获取前缀长度
    public static int getPreLength() {
        return Integer.valueOf(config.get("preLength"));
    }

    //编码格式
    public static String getCodeTyep() {
        return config.get("codeTyep");
    }


}
