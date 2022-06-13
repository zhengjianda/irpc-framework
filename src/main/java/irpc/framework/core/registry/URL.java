package irpc.framework.core.registry;

import irpc.framework.core.registry.zookeeper.ProviderNodeInfo;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URL是一个配置类
 * 将IRPC的主要配置都封装在URL中
 * 这个类可以说是整个框架的一个核心部分，所有重要的配置后期都是基于URL这个类来进行存储
 */
public class URL {

    /**
     * 服务应用名称 比如提供的是订单查询服务，则applicationName可以是Order
     */
    private String applicationName;

    /**
     * 注册到节点的 服务名称 如 com.luo.OrderService
     */
    private String serviceName;

    /**
     * 参数Map
     * 通过Map可以自定义不限地进行扩展
     * 分组
     * 权重
     * 服务提供者地址
     * 服务提供者的端口
     */
    private Map<String,String> parameters = new HashMap<>();

    public void addParameter(String key,String value){
        this.parameters.putIfAbsent(key,value);
    }

    public String getApplicationName(){
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * 将URL转换为写入zookeeper的provider节点下的 一段字符串: 该字符串的结构为 applicationName;serviceName;host;port;System.currentTimeMillis
     *
     * @param url
     * @return
     */
    public static String buildProviderUrlStr(URL url){
        String host = url.getParameters().get("host");
        String port = url.getParameters().get("port");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ":" + port + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }


    /**
     * 将某个节点下的信息转换为一个Provider节点对象
     *
     * @param providerNodeStr
     * @return
     */
    public static ProviderNodeInfo buildURLFromUrlStr(String providerNodeStr){
        String[] items = providerNodeStr.split("/");
        ProviderNodeInfo providerNodeInfo = new ProviderNodeInfo();
        providerNodeInfo.setServiceName(items[2]);
        providerNodeInfo.setAddress(items[4]);
        return providerNodeInfo;
    }


    /**
     * 将URL转换为写入zookeeper的consumer节点下的 一段字符串: 该字符串的结构为 applicationName;serviceName;host;System.currentTimeMillis
     *
     * @param url
     * @return
     */
    public static String buildConsumerUrlStr(URL url){
        String host = url.getParameters().get("host");
        return new String((url.getApplicationName() + ";" + url.getServiceName() + ";" + host + ";" + System.currentTimeMillis()).getBytes(), StandardCharsets.UTF_8);
    }


    public static void main(String[] args) {
        buildURLFromUrlStr("/irpc/org.idea.irpc.framework.interfaces.DataService/provider/192.168.43.227:9092");  //生成一个ProviderInfo对象，也就是一个服务提供者节点
    }
}
