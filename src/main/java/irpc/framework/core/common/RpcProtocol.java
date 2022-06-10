package irpc.framework.core.common;

import java.io.Serializable;
import java.util.Arrays;

import static irpc.framework.core.constants.RpcConstants.MAGIC_NUMBER;

public class RpcProtocol implements Serializable {

    private static final long serialVersionUID = 5359096060555795690L;

    //魔法数，主要是在做服务通讯的时候定义的一个安全检测，确认当前请求的协议是否合法。
    private short magicNumber = MAGIC_NUMBER;

    // 协议传输核心数据的长度
    private int contentLength;

    /*
     * 核心的传输数据，这里核心的传输数据主要是请求的 服务名称，请求的方法名称，参数内容
     * 为了方便后期扩展，这些核心的请求数据我们都统一封装到RpcInvocation对象当中
     * 所以这个字段其实是RpcInvocation类的字节数组，在RpcInvocation中包含了更多的调用信息
     * */
    private byte[] content;

    public RpcProtocol(byte[] data) {
        this.contentLength = data.length;
        this.content = data;
    }

    public short getMagicNumber() {
        return magicNumber;
    }

    public int getContentLength() {
        return contentLength;
    }

    public byte[] getContent() {
        return content;
    }

    public void setMagicNumber(short magicNumber) {
        this.magicNumber = magicNumber;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "RpcProtocol{" +
                "magicNumber=" + magicNumber +
                ", contentLength=" + contentLength +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
