package irpc.framework.core.proxy.jdk;

import irpc.framework.core.common.RpcInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static irpc.framework.core.common.cache.CommonClientCache.RESP_MAP;
import static irpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;

/**
 * 各种代理工厂统一使用这个InvocationHandler
 * 该代理处理器的核心任务就是：将需要调用的方法名称，服务名称，参数统统都封装好到RocInvocation当中
 * 然后塞入到一个队列里，并且等待服务端的数据返回
 */

public class JDKClientInvocationHandler implements InvocationHandler {

    private final static Object OBJECT = new Object();

    private Class<?> clazz;

    public JDKClientInvocationHandler(Class<?> clazz){
        this.clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcInvocation rpcInvocation = new RpcInvocation();
        rpcInvocation.setArgs(args);
        rpcInvocation.setTargetMethod(method.getName());
        rpcInvocation.setUuid(UUID.randomUUID().toString());
        rpcInvocation.setTargetServiceName(clazz.getName());
        RESP_MAP.put(rpcInvocation.getUuid(), OBJECT);  //在这里已经将结果RESP_MAP，等到远程调用的结果回来，可以根据UUID更新结果
        SEND_QUEUE.add(rpcInvocation);
        long beginTime = System.currentTimeMillis();
        while (System.currentTimeMillis()-beginTime<3*1000){
            Object object = RESP_MAP.get(rpcInvocation.getUuid());
            if (object instanceof RpcInvocation){
                return ((RpcInvocation) object).getResponse();
            }
        }
        throw new TimeoutException("client wait server's response timeout!");
    }
}
