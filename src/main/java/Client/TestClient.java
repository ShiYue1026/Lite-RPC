package Client;

import Client.proxy.ClientProxy;
import common.pojo.User;
import common.service.UserService;

public class TestClient {
    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy=new ClientProxy();
        //ClientProxy clientProxy=new part2.Client.proxy.ClientProxy("127.0.0.1",9999,0);  // 不需要再写入指定的ip和port了
        UserService proxy=clientProxy.getProxy(UserService.class);

        for(int i=0; i<150; i++){
            Integer i1 = i;
            Integer i2 = i1 + 100;
            if(i % 50 == 0){
                Thread.sleep(10000);
            }
            new Thread(() -> {
                try{
                    User user1 = proxy.getUserByUserId(i1);
                    System.out.println("从服务端得到的user: " + user1.toString());

                    Integer id1 = proxy.insertUserId(User.builder().id(i1).userName("User" + i1).sex(true).build());
                    System.out.println("向服务端插入user的id: " + id1);

                    User user2 = proxy.getUserByUserId(i2);
                    System.out.println("从服务端得到的user: " + user2.toString());

                    Integer id2 = proxy.insertUserId(User.builder().id(i2).userName("User" + i2).sex(true).build());
                    System.out.println("向服务端插入user的id: " + id2);
                } catch (NullPointerException e){
                    System.out.println("user为空");
                    e.printStackTrace();
                }
            }).start();
        }

//        User user1 = proxy.getUserByUserId(1);
//        System.out.println("从服务端得到的user: " + user1.toString());
//        System.out.println();
//
//        User user2 = proxy.getUserByUserId(2);
//        System.out.println("从服务端得到的user: " + user2.toString());
//        System.out.println();
//
//        User u1=User.builder().id(100).userName("abc").sex(true).build();
//        Integer id1 = proxy.insertUserId(u1);
//        System.out.println("向服务端插入user的id: " + id1);
//        System.out.println();
//
//        User u2=User.builder().id(200).userName("def").sex(true).build();
//        Integer id2 = proxy.insertUserId(u2);
//        System.out.println("向服务端插入user的id: " + id2);
//        System.out.println();

        // clientProxy.rpcClient.stop();
    }
}
