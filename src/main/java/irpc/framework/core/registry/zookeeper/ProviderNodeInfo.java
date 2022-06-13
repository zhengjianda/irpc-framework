package irpc.framework.core.registry.zookeeper;


/**
 * ZooKeeper中的一个节点
 * 在ZooKeeper中先定义一个rpc的根节点，接着是不同的服务名称例如com.luo.data.UserService作为二级节点
 * 在二级节点下划分了provider节点和consumer节点
 * provider节点下存放的数据以ip+端口的格式存储 如192.12.3.23：9090
 * consumer下边存放具体的 **服务调用名与地址** &{applicationName}:196.12.3.21:00001
 */
public class ProviderNodeInfo {

    // 服务名
    private String serviceName;

    //服务的地址  ip+地址
    private String address;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "ProviderNodeInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
