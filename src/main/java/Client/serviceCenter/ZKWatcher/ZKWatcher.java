package Client.serviceCenter.ZKWatcher;

import Client.cache.ServiceCache;
import lombok.AllArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;

@AllArgsConstructor
public class ZKWatcher {

    private CuratorFramework client;

    private ServiceCache serviceCache;


    public void watchToUpdate() {
        CuratorCache curatorCache = CuratorCache.build(client, "/");
        curatorCache.listenable().addListener(new CuratorCacheListener() {
            @Override
            public void event(Type type, ChildData oldChild, ChildData newChild) {
                switch(type.name()) {
                    case "NODE_CREATED":
                        String[] addPathList = parsePath(newChild);
                        if(addPathList.length <= 2){
                            break;
                        }
                        String serviceName = addPathList[1];
                        String address = addPathList[2];
                        serviceCache.addServiceToCache(serviceName, address);
                        break;
                    case "NODE_CHANGED":
                        String[] oldPathList = parsePath(oldChild);
                        String[] newPathList = parsePath(newChild);
                        if(oldPathList.length <= 2 || newPathList.length <= 2){
                            throw new RuntimeException("缓存更新节点失败，路径错误");
                        }
                        serviceCache.updateServiceAddress(oldPathList[1], oldPathList[2], newPathList[2]);
                        break;
                    case "NODE_DELETED":
                        String[] deletePathList = parsePath(oldChild);
                        if(deletePathList.length <= 2){
                            throw new RuntimeException("缓存删除节点失败，路径错误");
                        }
                        serviceCache.deleteServiceAddress(deletePathList[1], deletePathList[2]);
                        break;
                    default:
                        break;
                }
            }
        });

        // 开始监听
        curatorCache.start();
    }

    private String[] parsePath(ChildData childData) {
        String path = new String(childData.getPath());
        return path.split("/");
    }
}
