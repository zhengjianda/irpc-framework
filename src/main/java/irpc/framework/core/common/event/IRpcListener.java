package irpc.framework.core.common.event;

// 监听器接口
// 当zookeeper的某个节点发生数据变动的时候,就会发送一个变更事件,然后由对应的监听器去捕获这些数据并处理
public interface IRpcListener<T> {

    // 绑定一个回调函数
    void callBack(Object t);
}
