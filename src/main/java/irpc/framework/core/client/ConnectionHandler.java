package irpc.framework.core.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import irpc.framework.core.common.ChannelFutureWrapper;
import irpc.framework.core.common.utils.CommonUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static irpc.framework.core.common.cache.CommonClientCache.CONNECT_MAP;
import static irpc.framework.core.common.cache.CommonClientCache.SERVER_ADDRESS;

/**
 * 将连接的建立，断开，按照服务名筛选等功能都封装到ConnectionHandler中,按照单一职责的设计原则，将与连接有关的功能都统一封装在了一起。
 * 职责：当注册中心的节点新增或者移除或者权重变化的时候，这个类主要负责对内存中的url做变更
 */
public class ConnectionHandler {

    /**
     * 核心的连接处理器
     * 专门用于负责和服务端构建连接通信
     */
    private static Bootstrap bootstrap;

    public static Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static void setBootstrap(Bootstrap bootstrap){
        ConnectionHandler.bootstrap = bootstrap;
    }


    /**
     * 构建单个连接通道，元操作，既要处理连接，还要统一将连接进行内存存储管理
     * @param providerServiceName
     * @param providerIp
     * @throws InterruptedException
     */
    public static void connect(String providerServiceName,String providerIp) throws InterruptedException {
        if (bootstrap==null){
            throw new RuntimeException("bootstrap can not be null");
        }

        //格式错误类型的信息
        if (!providerIp.contains(":")){
            return ;
        }
        String[] providerAddress = providerIp.split(":");
        String ip = providerAddress[0];  //提取ip
        Integer port = Integer.parseInt(providerAddress[1]); //提取端口

        //到底这个channelFuture里面是什么？
        ChannelFuture channelFuture = bootstrap.connect(ip,port).sync();
        ChannelFutureWrapper channelFutureWrapper = new ChannelFutureWrapper();

        //ChannelFutureWrapper封装channelFuture，增加了ip和port
        channelFutureWrapper.setChannelFuture(channelFuture);
        channelFutureWrapper.setHost(ip);
        channelFutureWrapper.setPort(port);
        SERVER_ADDRESS.add(providerIp);
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)){
            channelFutureWrappers = new ArrayList<>();
        }
        channelFutureWrappers.add(channelFutureWrapper);
        CONNECT_MAP.put(providerServiceName,channelFutureWrappers);
    }

    /**
     * 构建ChannelFuture
     * @param ip
     * @param port
     * @return
     * @throws InterruptedException
     */
    public static ChannelFuture createChannelFuture(String ip,Integer port) throws InterruptedException {
        return bootstrap.connect(ip,port).sync();
    }

    /**
     * 断开连接
     * @param providerServiceName
     * @param providerIp
     */
    public static void disConnect(String providerServiceName,String providerIp){
        SERVER_ADDRESS.remove(providerIp);
        //从连接MAP中找出连接
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isNotEmptyList(channelFutureWrappers)){
            Iterator<ChannelFutureWrapper> iterator = channelFutureWrappers.iterator();
            while (iterator.hasNext()){
                ChannelFutureWrapper channelFutureWrapper = iterator.next();
                if (providerIp.equals(channelFutureWrapper.getHost()+":"+channelFutureWrapper.getPort())){
                    iterator.remove();
                }
            }
        }
    }

    /**
     * 默认走随机策略获取ChannelFuture
     *
     * @param providerServiceName
     * @return
     */
    public static ChannelFuture getChannelFuture(String providerServiceName){
        List<ChannelFutureWrapper> channelFutureWrappers = CONNECT_MAP.get(providerServiceName);
        if (CommonUtils.isEmptyList(channelFutureWrappers)){
            throw new RuntimeException("no provider exist for " + providerServiceName);
        }
        ChannelFuture channelFuture = channelFutureWrappers.get(new Random().nextInt(channelFutureWrappers.size())).getChannelFuture();
        return channelFuture;
    }
}
