package irpc.framework.core.proxy.javassist;

import irpc.framework.core.proxy.ProxyFactory;

public class JavassistProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class clazz) throws Throwable {
        return (T) ProxyGenerator.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                clazz, new JavassistInvocationHandler(clazz));
    }
}
