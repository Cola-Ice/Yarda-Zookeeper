# Zookeeper

## 一、Zookeeper介绍

### 1.什么是Zookeeper?

Zookeeper是一种分布式协调服务，用于管理大型主机。在分布式环境中协调和管理服务是一个复杂的过程，Zookeeper通过其简单的架构和API解决了分布式环境重服务的管理和协调。开发人员只需要专注核心应用程序，而不必担心程序的分布式特性

### 2.Zookeeper的应用场景

![image-20220323162101846](https://github.com/Cola-Ice/Yarda-Zookeeper/raw/master/doc/image/image-20220323162101846.png)

- 分布式协调组件：在分布式系统重，需要Zookeeper作为分布式协调组件，协调分布式系统中状态
- 分布式锁：Zookeeper在实现分布式锁上，可以做到强一致性
- 无状态化的实现

## 二、搭建Zookeeper

docker run --name zookeeper --net=zookeeper -p 2181:2181 --restart always -d zookeeper:3.7.0

## 三、Zookeeper内部的数据模型

### 1. Zookeeper是如何保存数据的

Zookeeper的数据是保存在节点上的，节点就是znode，多个znode构成树结构(像是文件系统目录)

<img src="https://github.com/Cola-Ice/Yarda-Zookeeper/raw/master/doc/image/image-20220323164941204.png" alt="image-20220323164941204" style="zoom:50%;" />

树由节点所组成，Zookeeper的数据存储同样是基于节点，这种节点叫做 **Znode**，不同于树的节点，Znode的应用方式是路径引用，类似于文件路径，这样的层级结构，让每个系欸但拥有唯一路径，就像命名空间一样对不同信息做出清晰的隔离

### 2. Znode结构

Znode包含四部分：

- data：保存的数据
- acl：权限，定义了用户对当前节点的操作权限
- stat：描述当前Znode的元数据
- child：当前节点的子节点

### 3. Znode的类型

- 持久节点：创建的节点，在会话结束依然存在

- 持久序号节点：创建的节点，会在节点名称上带一个序列号，越后执行序号越大，适用于分布式锁应用场景，单调递增

- 临时节点：临时节点在会话结束时，会被自动删除，通过这个特性，zk可以实现服务注册与发现的效果（通过ping来保持会话连接，续约session时间），注意删除有延迟（sessionId失效，约10s）

- 临时序号节点：跟持久序号节点相同，适用于临时的分布式锁

- Container节点(3.5.3版本新增)：Container容器节点，当容器中没有任何子节点，该容器节点会被zk定期删除(60s)

- TTL节点：可以指定节点的到期时间，到期后被zk定期删除

  > TTL节点需要通过系统配置开启zookeeper.extendedTypesEnabled=true(暂不推荐生产使用)

### 4. Zookeeper的持久化机制

Zookeeper的数据运行在内存中，其提供了两种持久化机制：

**事务日志：**Zookeeper把执行的命令以日志形式保存在指定文件

**数据快照：**Zookeeper会在一定的时间间隔内做一次数据快照，把该时刻的内存数据保存在快照文件中

Zookeeper通过两种形式的持久化，在数据恢复时，先恢复快照文件中的数据到内存中，再用日志文件做增量恢复，这样的恢复速度更快

## 四、Zookeeper客户端使用(zkCli)

1.多节点类型创建

```xml-dtd
# 创建持久节点
create /test1 value
# 创建持久序号节点
create -s /test1
# 创建临时节点
create -e /test2
# 创建临时序号节点
create -e -s /test2
# 创建容器节点
create -c /test3
```

2.查询节点

```xml-dtd
# 普通查询
get /test1
# 查询节点详细信息
get -s /test1
```

节点详细信息包含：

- cZxid：创建节点的事务id
- mZxid：修改节点的事务id
- pZxid：添加和删除子节点的事务id
- ctime：节点创建时的时间戳
- mtime：节点最新一次更新发生时的时间戳
- dataVersion：节点数据的更新次数
- cversion：其子节点的更新次数
- aclVersion：节点ACL(授权信息)的更新次数
- ephemeralOwner：该节点绑定的session id. 若该节点不是ephemeral节点, ephemeralOwner值为0
- dataLength：节点数据的字节数
- numChildren：子节点个数

3.删除节点

```xml-dtd
#删除节点
delete /test1
# 乐观锁删除(满足版本条件才删除)
delete -v 1 /tes1
```

4.权限设置

## 五、Curator客户端

### 1.Curator介绍

Curator是Netflix公司开源的一套zookeeper客户端框架，Curator是对Zookeeper支持最好的客户端框架，Curator封装了大部分Zookeeper功能，比如Leader选举、分布式锁等，减少了技术人员在使用Zookeeper时的底层细节开发工作

### 2.maven依赖：

```markdown
<!-- curator (对应的zookeeper服务端版本3.7)-->
<dependency>
	<groupId>org.apache.curator</groupId>
	<artifactId>curator-framework</artifactId>
	<version>2.12.0</version>
	<exclusions>
		<exclusion>
			<groupId>org.apache.zookeeper</groupId>
			<artifactId>zookeeper</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>org.apache.curator</groupId>
	<artifactId>curator-recipes</artifactId>
	<version>2.12.0</version>
	<exclusions>
		<exclusion>
			<groupId>org.apache.zookeeper</groupId>
			<artifactId>zookeeper</artifactId>
		</exclusion>
		<exclusion>
			<groupId>org.apache.curator</groupId>
			<artifactId>curator-framework</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<!-- zookeeper -->
<dependency>
	<groupId>org.apache.zookeeper</groupId>
	<artifactId>zookeeper</artifactId>
	<version>3.7.0</version>
</dependency>
```

使用方法见项目代码

## 六、Zookeeper实现分布式锁

分布式锁是解决分布式系统在并发场景下，多个线程访问、操作同一资源时出现的并发数据安全问题

**分布式锁应该具备的条件：**

1、在分布式系统环境下，同一方法在同一时间只能被一个机器的一个线程执行；
2、高可用的获取锁与释放锁；
3、高性能的获取锁与释放锁；
4、具备可重入特性；
5、具备锁失效机制，防止死锁；
6、具备非阻塞锁特性，即没有获取到锁将直接返回获取锁失败。

**实现分布式锁的方法：**

基于 MySQL 的悲观锁来实现分布式锁，这种方式使用的最少，性能不好，且容易造成死锁
基于 Memcached 实现分布式锁，可使用 add 方法来实现，如果添加成功了则表示分布式锁创建成功
基于 Redis 实现分布式锁，可以使用 setnx 方法来实现
基于 ZooKeeper 实现分布式锁，利用 ZooKeeper 顺序临时节点来实现

**Zookeeper实现分布式锁的优缺点：**

优点：具备高可用、可重入、阻塞锁特性，可解决失效死锁问题

缺点：需要频繁的创建和删除节点，性能上不如使用Redis

### 1.Zookeeper中锁的种类

- 读锁：可以跟其他的读锁同时存在
- 写锁：不可以跟其他的写锁或读锁同时存在

### 2.Zookeeper如何上读锁

1.创建一个目录mylock，在该目录下创建一个临时序号节点，数据为read

2.获取当前目录下比自己小的所有节点

3.判断最小节点是否是读锁：

​		是，则上锁成功

​		不是，则上锁失败，为最小节点设置监听(watch)，阻塞等待，zk的watch机制会在最小节点发生变化时通知当前节点，于是重复执行步骤2

### 3.Zookeeper如何上写锁

1.在锁目录下创建一个临时序号节点，数据为write

2.获取当前目录下所有的子节点

3.判断自己是否为最小的节点

​		是，上锁成功

​		不是，则上锁失败，监听最小节点，如果最小节点变化，重复执行步骤2

### 4.羊群效应

如果使用上述的上锁方式，只要有节点发生变化，就会触发其他节点的监听事件(仅有一个成功上锁)，这样的话对zk的压力非常大，——羊群效应。可以调整为链式监听，监听前一个节点，来解决这个问题

### 5.curator客户端实现读写锁

**读锁**

```java
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
```

**写锁**

```java
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
```

## 七、Zookeeper的watch机制

watch可以看作是在特定Znode上的监听器，当这个Znode发生改变时(create,delete,setData)，将会触发Znode上注册的监听，请求watch的客户端会收到异步通知

### 1.zkCli客户端使用watch

```xml-dtd
# watch监听节点变化(该监听是一次性的，可以在变化之后再来一次监听)
get -w /lock-node
# 监听目录，创建、删除子结点会收到通知
ls -w /lock-node
# 监听目录中所有孙、子节点变化
ls -w -R /lock-node
```

### 2.curator客户端使用watch

```java
@Test
void watchNode() throws Exception{
    byte[] bytes = curatorFramework.getData().usingWatcher(new CuratorWatcher() {
        @Override
        public void process(WatchedEvent watchedEvent) throws Exception {
            System.out.println("接收到节点变化:" + watchedEvent.getType().name());
        }
    }).forPath("/curator-node1");
    System.out.println(new String(bytes));
    System.in.read();
}
```

## 八、Zookeeper集群

### 1.Zookeeper集群角色

zookeeper集群中的节点有三种角色

- Leader：处理集群的所有事务请求(读和写)，集群中只有一个Leader
- Follower：只能处理读请求，参与Leader选举
- Observer：观察者，只能处理读请求，提升集群读性能，不能参与Leader选举

集群节点端口分为：客户端通信端口(2181)，集群通信端口，集群选举端口

### 2.Zookeeper集群搭建

略，需要注意Zookeeper集群规模尽量为奇数（节省资源，4比3并没有提高可用性）

## 九、ZAB协议

### 1.什么时ZAB协议？

Zookeeper作为分布式协调组件，采用一主多从形式进行集群部署，为了保证数据的一致性，使用了ZAB协议（Zookeeper原子广播协议），该协议解决了Zookeeper集群崩溃恢复和主从数据同步问题

### 2.ZAB协议的四种状态

- Looking：选举状态
- Following：Follower节点所处的状态
- Leading：Leader节点所处的状态
- Observer：观察者节点所处的状态

### 3.集群选举

**基本概念：**

myid：机器id，手动指定，每个zookeeper节点的id全局唯一

zxid：事务id，Zookeeper会给每个增删改请求分配一个事务ID，全局唯一，不断递增

**选举场景：**

1.Zookeeper集群启动初始化时选举

2.Zookeeper集群leader节点失联时选举

**选举前提条件：**

1.Zookeeper服务器处于Looking竞选状态

2.Zookeeper集群规模至少要3台（注意是集群规模，不是可用服务数）

> 为什么至少3台？因为选举leader需要获得至少一半以上的选票，2个节点的集群就不能挂任何1个节点(无法选举leader)，无法实现集群高可用目的

3.Zookeeper集群要有两台以上机器可以通信

**选举规则：**

获得半数以上的投票才能被选举为Leader

**选举流程：**

1.第一轮投票

每个Server都会投票给自己，投票包含所投服务器的机器id和事务id，如：Server(myid, zxid)

2.同步投票结果

集群中服务器投票后，会将各自投票结果同步给集群中其他服务器

3.检查投票有效性

是否本轮投票、是否来自Looking服务器

4.处理投票

服务器之间会进行投票比对，如果发现有比自己更大的选票，则变更自己的投票，重新发起投票

```java
# 比对规则
优先检查zxid，较大的服务器优先作为Leader；如果zxid相同，则myid较大的服务器作为Leader
```

5.统计投票结果

每轮投票结束都会统计投票结果，确认是否有机器得到半数以上的选票，如果是则选出Leader，否则继续投票（半数是指集群规模，并非可用服务器的半数）

6.更新服务器状态

一旦选举出Leader，每个服务器就会各自更新自己的状态

```
# 因此，集群依次初始化时：
1）集群有 3 台机器，第 2 大的 myid 所在服务器就是 Leader；
2）集群有 4 台机器，第 3 大的 myid 所在服务器就是 Leader；
3）集群有 5 台机器，第 3 大的 myid 所在服务器就是 Leader；
3）集群有 6 台机器，第 4 大的 myid 所在服务器就是 Leader；
```

### 4.崩溃恢复时的Leader选举

Leader选举完成后，Leader会周期性的向Follower发送心跳(ping)，当Leader崩溃后，Follower发现接收不到心跳，于是Follower开始进入到Looking状态，重复上边的选举过程，此时集群不对外提供服务

### 5.主从服务器数据同步

![image-20220325170327839](C:\Users\11382\AppData\Roaming\Typora\typora-user-images\image-20220325170327839.png)

客户端在向Zookeeper集群写数据时，可能访问到任意节点，该节点会将请求转发给Leader：

1.向主节点写数据

2.主节点先把数据写到自己的数据文件中，并给自己返回一个ACK

3.Leader把数据发送给Follower

4.从节点将数据写到本地数据文件

5.从节点返回ACK给Leader

6.Leader收到半数以上的ACK后向Follower发送Commit

7.从节点收到Commit后把数据文件中数据写到内存中

也就是说，Zookeeper写数据时，要有半数以上的节点写入成功，才算是写入成功

## 十、CAP理论

### 1.CAP定理

任何分布式系统，最多只能同时满足一致性、可用性和分区容错性这三项中的两项

- 一致性（Consistency）：所有节点在同一时刻数据完全一致
- 可用性（Availability）：服务一致可用，且在正常响应时间
- 分区容错性（Partition tolerance）：分布式系统在遇到某个节点故障或网络分区故障时，仍然能够对外提供满足一致性或可用性的服务。避免单点故障，就要进行冗余部署，冗余部署相当于服务的分区，这样的分区就具备了容错性

### 2.CAP权衡

对于多数大型互联网应用场景，强调可用性，即保证P和A，舍弃C（退而求其次，保证最终一致性）

对于涉及钱财的场景，一致性必须保证

### 3.BASE理论

BASE理论是对CAP理论的延申，核心思想是即使无法做到强一致性，但应用程序可以采用适合的方式达到最终一致性

- 基本可用

  基本可用是指分布式系统在出现故障时，允许损失部分可用性，保证核心可用

- 软状态

  软状态是指允许系统存在中间状态，而该状态不会影响系统整体可用性

- 最终一致性

  最终一致性是指系统中的所有数据副本经过一段时间后，最终都能达到一致性的状态。弱一致性和强一致性相反，最终一致性是弱一致性的一种特殊情况

### 4.Zookeeper追求的一致性

Zookeeper在数据同步时，追求的并不是强一致性，而是顺序一致性（事务ID的单调递增）