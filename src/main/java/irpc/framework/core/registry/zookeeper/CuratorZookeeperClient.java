package irpc.framework.core.registry.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

/**
 * ZookeeperClient的具体实现类设计了
 * 这里的具体实现是专门针对CuratorFramework做的设计，具体代码如下所示
 * CuratorFramework是Netflix公司开发一款连接zookeeper服务的框架
 * CuratorZookeeperClient 感觉可以看做是对CuratorFramework的封装
 */
public class CuratorZookeeperClient extends AbstractZookeeperClient{

    private CuratorFramework client;

    public CuratorZookeeperClient(String zkAddress) {
        this(zkAddress,null,null);
    }

    public CuratorZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        super(zkAddress, baseSleepTimes, maxRetryTimes);

        //重试策略，为指数退避
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(super.getBaseSleepTimes(),super.getMaxRetryTimes());
        if (client==null){
            client = CuratorFrameworkFactory.newClient(zkAddress,retryPolicy);  //client为空，需要new一个CuratorFramework

            //start connection
            client.start();
        }
    }

    @Override
    public void updateNodeData(String address, String data) {
        try {
            client.setData().forPath(address,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object getClient() {
        return client;
    }

    @Override
    public String getNodeData(String path) {
        try {
            byte[] result =client.getData().forPath(path);
            if (result!=null){
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getChildrenData(String path) {
        try {
            return client.getChildren().forPath(path);
        } catch (KeeperException e) {
            return null;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createPersistentData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT)  //PERSISTENT 持久化目录节点，会话结束存储数据不会丢失
                    .forPath(address,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.PERSISTENT_SEQUENTIAL) //顺序自动编号持久化目录节点, 存储数据不会丢失, 会根据当前已存在节点数自动加1, 然后返回给客户端已经创建成功的节点名
                    .forPath(address,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)  //临时自动编号节点, 一旦创建这个节点,当回话结束, 节点会被删除, 并且根据当前已经存在的节点数自动加1, 然后返回给客户端已经成功创建的目录节点名 .
                    .forPath(address,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporaryData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded()
                    .withMode(CreateMode.EPHEMERAL) //临时目录节点, 一旦创建这个节点当会话结束, 这个节点会被自动删除
                    .forPath(address,data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException ex) {
            try {
                client.setData().forPath(address,data.getBytes());
            } catch (Exception e) {
                throw new IllegalArgumentException(ex.getMessage(),ex);
            }
        }catch (Exception ex){
            throw new IllegalArgumentException(ex.getMessage(),ex);
        }
    }

    @Override
    public void setTemporaryData(String address, String data) {
        try {
            client.setData().forPath(address,data.getBytes());
        }catch (Exception ex){
            throw new IllegalArgumentException(ex.getMessage(),ex);
        }
    }

    @Override
    public void destroy() {
        client.close();
    }

    @Override
    public List<String> listNode(String address) {
        try {
            return client.getChildren().forPath(address);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address); //每个zNode都对应着一个Stat
            return stat!=null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void watchNodeData(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void watchChildNodeData(String path, Watcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
