package irpc.framework.core.registry;

public interface RegistryService {

    /**
     * 注册url 当某个服务要启动的时候，需要将接口注册到注册中心，之后服务调用方才可以获取到新服务的数据
     *
     * 将irpc服务注入注册中心节点
     * 当出现网络抖动的时候需要进行适当的重试做法
     * 注册服务url的时候需要写入持久化文件中
     * @param url
     */
    void register(URL url);

    /**
     * 服务下线，当某个服务提供者要下线了，则需要主动将注册过的服务信息从zk的指定节点上摘除
     *
     * 持久化节点是无法进行服务下线操作的
     * 下线的服务必须保证url是完整匹配的
     * 移除持久化文件中的一些内容信息
     * @param url
     */
    void unRegister(URL url);

    /**
     * 消息方订阅服务
     * 订阅某个服务，通常是客户端在启动阶段需要调用的接口，客户端在启动过程中需要调用该函数，从注册中心中提取现有的服务提供者地址，从而实现服务订阅功能。
     * @param url
     */
    void subscribe(URL url);


    /**
     * 执行取消订阅内部的逻辑
     *
     * 取消订阅服务，当服务调用方不打算再继续订阅某些服务的时候，就需要调用该函数去取消服务的订阅功能，将注册中心的订阅记录进行移除操作。
     * @param url
     */
    void doUnSubscribe(URL url);
}
