package irpc.framework.core.registry.zookeeper;

import irpc.framework.core.common.event.IRpcEvent;
import irpc.framework.core.common.event.IRpcListenerLoader;
import irpc.framework.core.common.event.IRpcUpdateEvent;
import irpc.framework.core.common.event.data.URLChangeWrapper;
import irpc.framework.core.registry.RegistryService;
import irpc.framework.core.registry.URL;
import irpc.framework.interfaces.DataService;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import javax.xml.bind.SchemaOutputResolver;
import java.util.List;


/**
 * 注册层的具体实现类ZookeeperRegister，真正实现注册中心的类
 */
public class ZookeeperRegister extends AbstractRegister implements RegistryService {

    //封装了AbstractZookeeperClient 借助zkClient去操作Zookeeper中的zNode节点
    private AbstractZookeeperClient zkClient;

    private final String ROOT = "/irpc";

    //返回服务提供者的路径
    private String getProviderPath(URL url){
        return ROOT +"/"+url.getServiceName()+"/provider/"+url.getParameters().get("host")+ ":" + url.getParameters().get("port");
    }

    private String getConsumerPath(URL url) {
        return ROOT + "/" + url.getServiceName() + "/consumer/" + url.getApplicationName() + ":" + url.getParameters().get("host")+":";
    }

    public ZookeeperRegister(String address){
        this.zkClient = new CuratorZookeeperClient(address);
    }


    /**
     * 订阅之后要执行的业务
     * @param url
     */
    @Override
    public void doAfterSubscribe(URL url) {
        //订阅之后，监听是否有新的服务注册
        String newServerNodePath =ROOT + "/" + url.getServiceName() + "/provider";
        watchChildNodeData(newServerNodePath);
    }

    private void watchChildNodeData(String newServerNodePath) {
        zkClient.watchChildNodeData(newServerNodePath, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(watchedEvent);
                String path = watchedEvent.getPath();  //事件发生对应的path
                List<String> childrenDataList = zkClient.getChildrenData(path);
                URLChangeWrapper urlChangeWrapper = new URLChangeWrapper();
                urlChangeWrapper.setProviderUrl(childrenDataList);
                urlChangeWrapper.setServiceName(path.split("/")[2]);

                //自定义的一套事件监听组件
                IRpcEvent iRpcEvent = new IRpcUpdateEvent(urlChangeWrapper);
                IRpcListenerLoader.sendEvent(iRpcEvent); //发送变化事件

                //收到回调之后再注册一次监听，这样能保证一直都收到消息
                watchChildNodeData(path);
            }
        });
    }

    @Override
    public void doBeforeSubscribe(URL url) {

    }


    @Override
    public List<String> getProviderIps(String serviceName) {
        List<String> nodeDataList = this.zkClient.getChildrenData(ROOT+"/"+serviceName+"/provider");
        return nodeDataList;
    }

    /**
     * 服务注册
     * @param url
     */
    @Override
    public void register(URL url) {
        if (!this.zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT,"");
        }
        String urlStr = URL.buildProviderUrlStr(url);
        if (!zkClient.existNode(getProviderPath(url))){
            zkClient.createTemporaryData(getProviderPath(url),urlStr);
        }
        else{
            zkClient.deleteNode(getProviderPath(url));
            zkClient.createTemporaryData(getProviderPath(url),urlStr);
        }
        super.register(url);
    }

    @Override
    public void unRegister(URL url) {
        zkClient.deleteNode(getProviderPath(url));
        super.unRegister(url);
    }

    @Override
    public void subscribe(URL url) {
        if(!this.zkClient.existNode(ROOT)){
            zkClient.createPersistentData(ROOT,"");
        }
        String urlStr =URL.buildConsumerUrlStr(url);
        if (!zkClient.existNode(getConsumerPath(url))){
            zkClient.createTemporarySeqData(getConsumerPath(url),urlStr);
        }
        else{
            zkClient.deleteNode(getConsumerPath(url));
            zkClient.createTemporarySeqData(getConsumerPath(url),urlStr);
        }
        super.subscribe(url);
    }

    @Override
    public void doUnSubscribe(URL url) {
        this.zkClient.deleteNode(getConsumerPath(url));
        super.doUnSubscribe(url);
    }


    public static void main(String[] args) throws InterruptedException {
        ZookeeperRegister zookeeperRegister = new ZookeeperRegister("localhost:2181");
        List<String> urls = zookeeperRegister.getProviderIps(DataService.class.getName());
        System.out.println(urls);
        Thread.sleep(2000000);
    }


}
