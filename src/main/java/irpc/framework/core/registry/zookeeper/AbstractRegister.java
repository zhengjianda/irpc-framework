package irpc.framework.core.registry.zookeeper;

import irpc.framework.core.registry.RegistryService;
import irpc.framework.core.registry.URL;

import java.util.List;

import static irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;
import static irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

/**
 * AbstractRegister抽象类的主要作用是：对一些注册数据做统一的处理
 * 日后我们需要考虑多种类型的注册中心，例如redis，etcd的话
 * 所有基础的记录操作都可以统一放在抽象类实现
 * 依次实现代码的复用
 * 同时为了留给子类更多的拓展行为，我们定义了一些抽象函数(扩展函数)，可以供给子类去拓展实现
 */
public abstract class AbstractRegister implements RegistryService {


    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void unRegister(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.add(url.getServiceName());
    }

    @Override
    public void doUnSubscribe(URL url) {
        SUBSCRIBE_SERVICE_LIST.remove(url.getServiceName());
    }

    /**
     * 留给子类扩展，订阅后要做些什么？
     * @param url
     */
    public abstract void doAfterSubscribe(URL url);

    /**
     * 留给子类拓展，订阅前要做些什么？
     * @param url
     */
    public abstract void doBeforeSubscribe(URL url);

    /**
     *
     * @param serviceName
     * @return
     */
    public abstract List<String> getProviderIps(String serviceName);
}
