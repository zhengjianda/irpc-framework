package irpc.framework.core.proxy.jdk;

import irpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

//辅助客户端发起调用的代理对象
public class JDKProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new JDKClientInvocationHandler(clazz));
    }
}
