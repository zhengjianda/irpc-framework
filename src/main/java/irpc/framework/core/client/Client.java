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
import irpc.framework.core.proxy.jdk.JDKProxyFactory;
import irpc.framework.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

import static irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private ClientConfig clientConfig;

    public ClientConfig getClientConfig(){
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public RpcReference startClientApplication() throws InterruptedException {
        EventLoopGroup clientGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientGroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                //管道中初始化一些逻辑，包括编解码器和客户端响应类
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });

        //常规的链接netty服务端，ChannelFuture对象返回异步调用的结果
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(),clientConfig.getPort()).sync();
        logger.info("=============服务启动============");

        this.startClient(channelFuture);

        //这里注入了一个代理工厂
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }

    public static void main(String[] args) throws Throwable {
        Client client = new Client();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);
        RpcReference rpcReference = client.startClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        for(int i=0;i<100;i++){
            String result = dataService.sendData("test");
            System.out.println(result);
        }
    }

    /**
     * 开启发送线程，专门从事将数据报发送给服务端，起到一个解耦的效果
     * @param channelFuture
     */
    private void startClient(ChannelFuture channelFuture){
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }

    /**
     * 异步发送消息任务
     */
    static class AsyncSendJob implements Runnable{

        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
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

                    //netty的通道负责发送数据给服务器端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
