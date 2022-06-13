package irpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import irpc.framework.core.common.RpcDecoder;
import irpc.framework.core.common.RpcEncoder;
import irpc.framework.core.common.RpcInvocation;
import irpc.framework.core.common.RpcProtocol;
import irpc.framework.core.common.config.ClientConfig;
import irpc.framework.core.common.config.PropertiesBootstrap;
import irpc.framework.core.common.event.IRpcListener;
import irpc.framework.core.common.event.IRpcListenerLoader;
import irpc.framework.core.common.utils.CommonUtils;
import irpc.framework.core.proxy.javassist.JavassistProxyFactory;
import irpc.framework.core.proxy.jdk.JDKProxyFactory;
import irpc.framework.core.registry.URL;
import irpc.framework.core.registry.zookeeper.AbstractRegister;
import irpc.framework.core.registry.zookeeper.ZookeeperRegister;
import irpc.framework.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;
import static irpc.framework.core.common.cache.CommonClientCache.SUBSCRIBE_SERVICE_LIST;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private static EventLoopGroup clientGroup = new NioEventLoopGroup(); //线程组

    private ClientConfig clientConfig;  //客户端配置

    private AbstractRegister abstractRegister;  //注册的具体设计

    private IRpcListenerLoader iRpcListenerLoader;  //iRpcListenerLoader可以注册监听器和发送事件

    private Bootstrap bootstrap = new Bootstrap();  //Netty启动类，也可以认为是工厂类

    public Bootstrap getBootstrap(){
        return bootstrap;
    }

    public ClientConfig getClientConfig(){
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public RpcReference initClientApplication(){
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        iRpcListenerLoader = new IRpcListenerLoader();
        iRpcListenerLoader.init();
        this.clientConfig = PropertiesBootstrap.loadClientConfigFromLocal();  //加载客户端配置
        RpcReference rpcReference;  //代理对象
        if ("javassist".equals(clientConfig.getProxyType())) {
            rpcReference = new RpcReference(new JavassistProxyFactory());
        } else {
            rpcReference = new RpcReference(new JDKProxyFactory());
        }
        return rpcReference;
    }

    /**
     * 启动服务之前需要订阅对应的dubbo服务
     * 该函数提供客户端订阅服务的功能
     *
     * @param serviceBean
     */
    public void doSubscribeService(Class serviceBean){
        if (abstractRegister==null){ //注册服务需要依赖于abstractRegister抽象类，所以需要检查是否为空
            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddr());
        }
        URL url = new URL();
        url.setApplicationName(clientConfig.getApplicationName());
        url.setServiceName(serviceBean.getName());
        url.addParameter("host", CommonUtils.getIpAddress());
        abstractRegister.subscribe(url); //订阅服务
    }

    /**
     * 开始和各个provider建立连接
     */
    public void doConnectServer() {
        //获取所有的服务和其ip
        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
            for (String providerIp : providerIps) {
                try {
                    ConnectionHandler.connect(providerServiceName, providerIp);
                } catch (InterruptedException e) {
                    logger.error("[doConnectServer] connect fail ", e);
                }
            }
            URL url = new URL();
            url.setServiceName(providerServiceName);
            //客户端在此新增一个订阅的功能
            abstractRegister.doAfterSubscribe(url); //连接后订阅
        }
    }


    /**
     * 开启发送线程，专门从事将数据报发送给服务端，起到一个解耦的效果
     */
    private void startClient(){
        Thread asyncSendJob = new Thread(new AsyncSendJob());
        asyncSendJob.start();
    }

    /**
     * 异步发送消息任务
     */
    static class AsyncSendJob implements Runnable{

        public AsyncSendJob(){

        }

        @Override
        public void run() {
            while (true){
                try{
                    //阻塞模式，取出一条消息，无消息则阻塞等待
                    RpcInvocation data = SEND_QUEUE.take();

                    //将data(RpcInvocation形式的) 封装到RpcProtocol对象中，然后发送给服务器 这里正好对应了上文中的ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes(StandardCharsets.UTF_8));

                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getTargetServiceName());
                    //netty的通道负责发送数据给服务器端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        RpcReference rpcReference = client.initClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        client.doSubscribeService(DataService.class);
        ConnectionHandler.setBootstrap(client.getBootstrap());
        client.doConnectServer();
        client.startClient();
        for (int i = 0; i < 100; i++) {
            try {
                System.out.println("Hello");
                String result = dataService.sendData("test");
                System.out.println(result);
                Thread.sleep(1000);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

