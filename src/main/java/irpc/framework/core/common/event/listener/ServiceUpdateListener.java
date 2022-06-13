package irpc.framework.core.common.event.listener;

import io.netty.channel.ChannelFuture;
import irpc.framework.core.client.ConnectionHandler;
import irpc.framework.core.common.ChannelFutureWrapper;
import irpc.framework.core.common.event.IRpcListener;
import irpc.framework.core.common.event.data.URLChangeWrapper;
import irpc.framework.core.common.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;

//zk的服务提供者节点发生了变更，发送事件通知操作告知客户端
//这个时候客户端需要更新本地的一个目标服务列表，避免向无用的服务发送请求。
public class ServiceUpdateListener implements IRpcListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceUpdateListener.class);

    @Override
    public void callBack(Object t) {
        //获取到字节点的数据信息
        URLChangeWrapper urlChangeWrapper = (URLChangeWrapper) t;
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(urlChangeWrapper.getServiceName());
        if (CommonUtils.isEmptyList(channelFutureWrappers)) {
            LOGGER.error("[ServiceUpdateListener] channelFutureWrappers is empty");
            return ;
        } else {
            List<String> matchProviderUrl = urlChangeWrapper.getProviderUrl();
            Set<String> finalUrl = new HashSet<>();
            List<ChannelFutureWrapper> finalChannelFutureWrappers = new ArrayList<>();
            for (ChannelFutureWrapper channelFutureWrapper : channelFutureWrappers) {
                String oldServerAddress = channelFutureWrapper.getHost() + ":" + channelFutureWrapper.getPort();
                //如果老的url没有，说明已经被移除了
                if (!matchProviderUrl.contains(oldServerAddress)) {
                    continue;
                } else {
                    finalChannelFutureWrappers.add(channelFutureWrapper);
                    finalUrl.add(oldServerAddress);
                }
            }
            //此时老的url已经被移除了，开始检查是否有新的url
            //ChannelFutureWrapper其实是一个自定义的包装类，将netty建立好的ChannelFuture做了一些封装
            List<ChannelFutureWrapper> newChannelFutureWrapper = new ArrayList<>();
            for (String newProviderUrl : matchProviderUrl) {
                if (!finalUrl.contains(newProviderUrl)) {
                    ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();
                    String host = newProviderUrl.split(":")[0];
                    Integer port = Integer.valueOf(newProviderUrl.split(":")[1]);
                    channelFutureWrapper.setPort(port);
                    channelFutureWrapper.setHost(host);
                    ChannelFuture channelFuture = null;
                    try {
                        channelFuture = ConnectionHandler.createChannelFuture(host,port);
                        channelFutureWrapper.setChannelFuture(channelFuture);
                        newChannelFutureWrapper.add(channelFutureWrapper);
                        finalUrl.add(newProviderUrl);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            finalChannelFutureWrappers.addAll(newChannelFutureWrapper);
            //最终更新服务在这里
            CONNECT_MAP.put(urlChangeWrapper.getServiceName(),finalChannelFutureWrappers);
        }
    }
}
