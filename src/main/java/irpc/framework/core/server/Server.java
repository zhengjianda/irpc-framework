package irpc.framework.core.server;


import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import irpc.framework.core.common.RpcDecoder;
import irpc.framework.core.common.RpcEncoder;
import irpc.framework.core.common.config.PropertiesBootstrap;
import irpc.framework.core.common.config.ServerConfig;
import irpc.framework.core.common.utils.CommonUtils;
import irpc.framework.core.registry.RegistryService;
import irpc.framework.core.registry.URL;
import irpc.framework.core.registry.zookeeper.ZookeeperRegister;
import javafx.scene.layout.Region;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.net.Socket;
import java.util.concurrent.ThreadLocalRandom;

import static irpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;
import static irpc.framework.core.common.cache.CommonServerCache.PROVIDER_URL_SET;

public class Server {
    private static EventLoopGroup bossGroup = null;
    private static EventLoopGroup workerGroup = null;

    private ServerConfig serverConfig;  //服务端配置

    private RegistryService registryService;

    public ServerConfig getServerConfig(){
        return serverConfig;
    }

    public static EventLoopGroup getBossGroup() {
        return bossGroup;
    }


    public static EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public static void setBossGroup(EventLoopGroup bossGroup) {
        Server.bossGroup = bossGroup;
    }

    public static void setWorkerGroup(EventLoopGroup workerGroup) {
        Server.workerGroup = workerGroup;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);  //指明channel的类型
        bootstrap.option(ChannelOption.TCP_NODELAY,true); //设置选项
        bootstrap.option(ChannelOption.SO_BACKLOG,1024); //设置线程队列得到连接个数
        bootstrap.option(ChannelOption.SO_RCVBUF,16*1024) //TCP数据接收缓冲区大小
                .option(ChannelOption.SO_KEEPALIVE,true) //设置保持活动连接状态，启用该功能时，TCP会主动探测空闲连接的有效性。
                .option(ChannelOption.SO_SNDBUF,16*1024);

        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() { //使用匿名内部类的形式初始化通道对象
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                System.out.println("初始化provider过程");

                //设置流水线，ChannelPipeline是Netty处理请求的责任链，ChannelHandler则是具体
                //处理请求的处理器 实际上每一个channel都有一个处理器的流水线。
                ch.pipeline().addLast(new RpcEncoder());  //编码处理器
                ch.pipeline().addLast(new RpcDecoder());  //解码处理器
                ch.pipeline().addLast(new ServerHandler()); //服务器处理器
            }
        });
        this.batchExportUrl();
        bootstrap.bind(serverConfig.getServerPort()).sync();  //绑定端口
    }

    public void initServerConfig(){
        ServerConfig serverConfig = PropertiesBootstrap.loadServerConfigFromLocal();
        this.setServerConfig(serverConfig);
    }


    /**
     * 暴露服务信息
     *
     * @param serviceBean
     */
    public void exportService(Object serviceBean){
        if (serviceBean.getClass().getInterfaces().length==0){
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length>1){
            throw new RuntimeException("service must only had one interfaces!");
        }
        if (registryService==null){
            registryService = new ZookeeperRegister(serverConfig.getRegisterAddr());
        }

        //默认选择该对象的第一个实现接口
        Class interfaceClass = classes[0];
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
        URL url = new URL();
        url.setServiceName(interfaceClass.getName());
        url.setApplicationName(serverConfig.getApplicationName());
        url.addParameter("host", CommonUtils.getIpAddress());
        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
        PROVIDER_URL_SET.add(url);
    }

    /**
     * 这个函数的内部设计就是为了将服务端的具体服务都暴露到注册中心，方便客户端进行调用
     */
    public void batchExportUrl(){
        Thread task = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (URL url:PROVIDER_URL_SET){
                    registryService.register(url);
                }
            }
        });
        task.start();
    }




    /**
     * 注册服务端的服务，相当于是注册了服务端中存在的本地方法，供给客户端远程调用
     * @param serviceBean
     */
    public void registryService(Object serviceBean){
        if (serviceBean.getClass().getInterfaces().length==0){
             throw new RuntimeException("service must had intserfaces");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length>1){
            throw new RuntimeException("service must only had one interfaces!");
        }

        Class interfaceClass = classes[0];

        //需要注册的对象 统一放在一个MAP集合中进行管理
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.initServerConfig();
        server.exportService(new DataServiceImpl());
        server.startApplication();
    }
}
