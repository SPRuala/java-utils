package cn.itcast.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZooKeeperDemo {
    public static void main(String[] args)throws IOException, InterruptedException,KeeperException{
        //获取连接
        //getConnect();

        //创建节点
        createNode();

        //判断节点是否存在
        /*Stat stat = existsNode();
        if (stat != null){
            System.out.println(" 已存在 ");
        }else {
            System.out.println(" 不存在 ");
        }*/

        //查看持久节点数据内容
        /*byte[] nodeData = getNode();
        System.out.println(" 持久节点 zkapi 的数据内容为： "+new String(nodeData));*/

        //修改节点数据内容
            /*//第 1 步: 查看持久节点 zkapi 修改数据前的数据版本号、长度和数据内容。
            Stat beforeStat=existsNode();
            System.out.println("持久节点zkapi修改数据内容前的数据版本号: "+beforeStat.getVersion());
            System.out.println("持久节点zkapi修改数据内容前的数据内容长度: "+beforeStat.getDataLength());
            byte[] beforeNode=getNode();
            System.out.println("持久节点zkapi修改数据内容前的数据内容: "+new String(beforeNode));
            //第 2 步: 进行更新
            updateNode();
            Stat afterStat=existsNode();
            //第 3 步: 查看持久节点 zkapi 修改数据内容后的数据版本号、长度和数据内容。
            System.out.println("持久节点 zkapi 修改数据内容后的数据版本号： " +afterStat.getVersion());
            System.out.println("持久节点 zkapi 修改数据内容后的数据内容长度： " +afterStat.getDataLength());
            byte[] afterNode = getNode();
            System.out.println("持久节点 zkapi 修改数据内容后的数据内容： " +new String(afterNode));*/
        //查看子节点列表
        /*List<String> childNodes = getChildNode();
        for (String childNode : childNodes) {
            System.out.println(" 持久节点 zkapi 的子节点 :"+childNode);
        }*/
        //删除(子)节点
        //deleteNode();
        /*List<String> childNodes = getChildNode();
        for (String childNode : childNodes) {
            System.out.println(" 持久节点 zkapi 的子节点 :"+childNode);
        }*/
    }
    //创建 getConnect() 方法，实现连接指定 ZooKeeper 服务创建会话，并指定ZooKeeper服务的地址为 hadoop1:2181
    public static ZooKeeper getConnect() throws IOException,InterruptedException {
        String zkServer = "hadoop1:2181";
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ZooKeeper zooKeeper = new ZooKeeper(zkServer, 3000, new Watcher() {
            //重写 process() 方法，在 process() 方法指定处理事件的逻辑。
            @Override
            public void process(WatchedEvent watchedEvent) {
                System.out.println(" 通知状态： " + watchedEvent.getState() + "\t"
                        + " 事件类型： " + watchedEvent.getType() + "\t"
                        + " 节点路径： " + watchedEvent.getPath());
                if (Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    countDownLatch.countDown();
                }
            }
        });
        countDownLatch.await();
        return zooKeeper;
    }
    //ZooKeeper 类提供的方法实现:
        /*create(path,data,acl,createMode):
        用于创建 ZNode ，并且指定 ZNode 的数据内容，
        参数 path :表示 ZNode 的节点路径;
        参数 data :表示 ZNode 的数据内容;
        参数 acl :表示 ZNode 的权限策略;
        参数 createMode :表示 ZNode的类型。
        */
        /*exists (path,watch):
        判断指定 ZNode 是否存在，
        参数 path :表示 ZNode 的节点路径；
        参数 watch :表示向指定 ZNode 注册 Watcher 监听 ZNode 的变化，包括 ZNode 被创建、删除或者数据内容发生变化，
        可选值为 true 或 false ，其中 true 表示注册； false 表示不注册。
        */
        /*getData (path,watch,stat):
        获取指定 ZNode 的数据内容，
        参数 path :表示 ZNode 的节点路径；
        参数 watch 表示向指定 ZNode 注册 Watcher 监听 ZNode数据内容的变化；
        参数 stat 表示 Sata 类的对象，即 ZNode 的属性，参数值可以为 null 。
        */
        /*setData(path,data,version):
        修改指定 ZNode 的数据内容，
        参数 path :表示 ZNode 的节点路径；
        参数 data :表示修改后的数据内容；
        参数 version :表示ZNode 中数据内容的版本号，用于修改指定版本的数据内容，若该参数值为 -1 ，则跳过版本号匹配。
        */
        /*getChildren(path,watch):
        查看指定 ZNode 的子节点列表，
        参数 path :表示 ZNode 的节点路径；
        参数 watch :表示向指定 ZNode 注册 Watcher 监听ZNode 子节点发生变化。
        */
        /*delete (path,version):
        删除指定 ZNode ，若 ZNode 存在子节点则无法删除，
        参数path 表示 ZNode 的节点路径；
        参数 version 表示 ZNode 中数据内容的版本号，用于修改指定版本的数据内容，若该参数值为 -1 ，则跳过版本号匹配。
        */

    //createNode(): 用于创建持久节点zkapi 并挂载子节点zkChild(子节点类型为持久节点)
    public static void createNode()throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        connect.create(
                "/zkapi",
                "fruit".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
        connect.create(
                "/zkapi/zkChild",
                "apple".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.PERSISTENT);
    }
    //existsNode(): 用于判断持久节点zkapi是否存在
    public static Stat existsNode() throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        Stat exists = connect.exists("/zkapi", true);
        return exists;
    }
    //getNode(): 用于获取持久节点zkapi的数据内容
    public static byte[] getNode() throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        byte[] data = connect.getData("/zkapi", true, null);
        return data;
    }
    //updateNode():用于将持久节点 zkap的数据内容修改为 fruit_new
    public static Stat updateNode()throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        Stat stat = connect.setData(
                "/zkapi", "fruit_new".getBytes(), -1);
        return stat;
    }
    //getChildNode():用于查看持久节点 zkapi 的子节点列表
    public static List<String> getChildNode() throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        List<String> nodeList = connect.getChildren("/zkapi", false);
        return nodeList;
    }
    //用于删除持久节点 zkapi 的子节点 zkChild
    public static void deleteNode()
            throws IOException, InterruptedException, KeeperException {
        ZooKeeper connect = getConnect();
        connect.delete("/zkapi", -1);
    }


    }
