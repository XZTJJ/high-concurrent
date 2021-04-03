package org.example.Serialization.json;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;

/**
 * 简单的pojo类
 *  序列化使用 Google 的Gson
 *  反序列化使用 阿里 的 fastjson
 */
public class JsonMsg {

    //版本号
    private int version;
    //副版本号
    private int fversion;
    //魔数
    private int magic;
    //内容
    private String content;


    public JsonMsg() {

    }

    public JsonMsg(int version, int fversion, int magic, String content) {
        this.version = version;
        this.fversion = fversion;
        this.magic = magic;
        this.content = content;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getFversion() {
        return fversion;
    }

    public void setFversion(int fversion) {
        this.fversion = fversion;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    //序列化
    public String converToJson() {
        return new Gson().toJson(this);
    }

    //反序列化
    public static JsonMsg jsonConverPOJO(String json) {
        return JSONObject.parseObject(json, JsonMsg.class);

    }

    @Override
    public String toString() {
        return "version ：" + version + " , fversion : " + fversion +
                " , magic : " + magic + " , content : " + content;
    }
}
