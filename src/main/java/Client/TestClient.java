package Client;

import Client.proxy.ClientProxy;
import common.pojo.User;
import common.service.UserService;

public class TestClient {
    public static void main(String[] args) {
        ClientProxy clientProxy=new ClientProxy();
        //ClientProxy clientProxy=new part2.Client.proxy.ClientProxy("127.0.0.1",9999,0);  // 不需要再写入指定的ip和port了
        UserService proxy=clientProxy.getProxy(UserService.class);

        User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user: "+user.toString());
        System.out.println();

        User u=User.builder().id(100).userName("abc").sex(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id: "+id);
        System.out.println();

        clientProxy.rpcClient.stop();
    }
}
