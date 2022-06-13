package irpc.framework.core.common.event;

import irpc.framework.core.common.event.listener.ServiceUpdateListener;
import irpc.framework.core.common.utils.CommonUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//定义好了统一的事件规范，监听接口，就需要有专门的类去发送事件了

public class IRpcListenerLoader {

    // 监听器列表
    private static List<IRpcListener> iRpcListenerList = new ArrayList<>();

    // 线程池
    private static ExecutorService eventThreadPool = Executors.newFixedThreadPool(2);

    // 注册监听器
    public static void registerListener(IRpcListener iRpcListener){
        iRpcListenerList.add(iRpcListener);    //注册监听器
    }

    // 初始函数，注册一个服务更新监听器  ？初始注册的意义何在？
    public void init(){
        registerListener(new ServiceUpdateListener());
    }

    /**
     * 获取接口上的泛型T
     *
     * @param o 接口
     * @return
     */
    public static Class<?> getInterfaceT(Object o){
        Type[] types = o.getClass().getGenericInterfaces();
        ParameterizedType parameterizedType = (ParameterizedType) types[0];

        Type type = parameterizedType.getActualTypeArguments()[0];
        if (type instanceof Class<?>){
            return (Class<?>) type;
        }
        return null;
    }

    // 发送发生变化的消息 到对应的监听器
    public static void sendEvent(IRpcEvent iRpcEvent){
        if (CommonUtils.isEmptyList(iRpcListenerList)){  //监听器列表为空，直接返回
            return ;
        }
        for (IRpcListener<?> iRpcListener:iRpcListenerList){
            Class<?> type = getInterfaceT(iRpcListener);
            if (type.equals(iRpcEvent.getClass())){ //监听器监听的事件与发生的事件是对应的，发送通知
                eventThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            iRpcListener.callBack(iRpcEvent.getData());  //监听器监听到了事件，调用回调函数
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

}
