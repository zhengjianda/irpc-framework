package irpc.framework.core.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import irpc.framework.core.common.RpcInvocation;
import irpc.framework.core.common.RpcProtocol;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static irpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

// 服务端接收数据之后的处理器ServerHandler
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //服务端接收数据的时候统一以RpcProtocol协议的格式接收
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        String json = new String(rpcProtocol.getContent(),0,rpcProtocol.getContentLength());

        // 将RpcProtocol中的content数组转换为rpcInvocation对象
        RpcInvocation rpcInvocation = JSON.parseObject(json,RpcInvocation.class);

        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;
        for(Method method:methods){
            if (method.getName().equals(rpcInvocation.getTargetMethod())){
                //通过反射找到目标对象，然后执行目标方法并返回对应值
                if (method.getReturnType().equals(Void.TYPE)){
                    method.invoke(aimObject,rpcInvocation.getArgs()); //无返回值，直接调用方法
                }
                else{
                    result = method.invoke(aimObject,rpcInvocation.getArgs());
                }
            }
            break;
        }
        rpcInvocation.setResponse(result);
        RpcProtocol respRpcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes(StandardCharsets.UTF_8));
        ctx.writeAndFlush(respRpcProtocol);
    }
}
