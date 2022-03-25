package com.yarda.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ZookeeperApplicationTests {
    @Resource
    private CuratorFramework curatorFramework;

    @Test
    void createNode() throws Exception {
        // 创建持久节点
        String path1 = curatorFramework.create().forPath("/curator-node1");
        // 创建临时序号节点
        String path2 = curatorFramework.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/curator-node2", "linshi".getBytes());
        // 创建节点当父节点不存在时顺带创建父级节点
        curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/curator-node3/child", "auto".getBytes());
        System.out.println("创建的持久节点路径：" + path1);
        System.out.println("创建的临时序号节点路径：" + path2);
        Thread.sleep(100000);
    }

    @Test
    void updateNode() throws Exception{
        curatorFramework.setData().forPath("/curator-node1", "changed".getBytes());
    }

    @Test
    void getNodeData() throws Exception{
        byte[] bytes = curatorFramework.getData().forPath("/curator-node1");
        System.out.println(new String(bytes));
    }

    @Test
    void deleteNode() throws Exception{
        curatorFramework.delete().deletingChildrenIfNeeded().forPath("/curator-node1");
    }

    @Test
    void watchNode() throws Exception{
        // watch监听节点数据变化
        byte[] bytes = curatorFramework.getData().usingWatcher(new CuratorWatcher() {
            @Override
            public void process(WatchedEvent watchedEvent) throws Exception {
                System.out.println("接收到节点变化:" + watchedEvent.getType().name());
            }
        }).forPath("/curator-node1");
        System.out.println(new String(bytes));
        System.in.read();
    }

    @Test
    void readLock() throws Exception{
        // 创建读写锁对象
        InterProcessReadWriteLock lock = new InterProcessReadWriteLock(curatorFramework, "/lock-node");
        // 获取读锁对象
        InterProcessMutex mutex = lock.readLock();
        System.out.println("等待获取读锁");
        // 获取锁
        mutex.acquire();
        System.out.println("获取到锁对象，程序执行开始");
        for (int i = 0; i < 100; i++){
            Thread.sleep(1000);
            System.out.println(i);
        }
        mutex.release();
        System.out.println("程序执行结束，释放锁");
    }

    @Test
    void writeLock() throws Exception{
        // 创建读写锁对象
        InterProcessReadWriteLock lock = new InterProcessReadWriteLock(curatorFramework, "/lock-node");
        // 获取读锁对象
        InterProcessMutex mutex = lock.writeLock();
        System.out.println("等待获取写锁");
        // 获取锁
        mutex.acquire();
        System.out.println("获取到锁对象，程序执行开始");
        for (int i = 0; i < 100; i++){
            Thread.sleep(1000);
            System.out.println(i);
        }
        mutex.release();
        System.out.println("程序执行结束，释放锁");
    }
}
