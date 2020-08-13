package com.example.demo.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/wobsocket/{username}")
public  class WebSocket {
    private Logger logger= LoggerFactory.getLogger(this.getClass());
    public  static  int onlienNumber=0;
    private  static Map<String , WebSocket> clients=new ConcurrentHashMap<String, WebSocket>();
    private Session session ;
    private String username;

   @OnOpen
   public  void onOpen(@PathParam("username") String username, Session session )
   {
       onlienNumber ++;
       logger .info("现在来连接的客户id:"+session .getId() +"用户名："+username);
       this.username =username ;
       this .session =session ;

       logger .info("有新连接加入！当前在线人数"+onlienNumber );
       try{
           Map<String,Object>map1= Maps.newHashMap();
           map1.put("massageType",1);
           map1.put("username",username);
           sendMessageAll(JSON.toJSONString(map1),username);
           clients.put(username,this);
           Map<String,Object> map2 = Maps.newHashMap();
           map2.put("messageType",3);

           Set<String> set = clients.keySet();
           map2.put("onlineUsers",set);
           sendMessageTo(JSON.toJSONString(map2),username);

       }
       catch (IOException e) {
           logger.info(username + "上线的时候通知所有人发生错误");
           }
       }
       @OnError
       public void onError(Session session, Throwable error) {
       logger.info("服务端发生了错误"+error.getMessage());
       //error.printStackTrace();
   }
       @OnClose
               public  void onClose()
       {
           onlienNumber --;
           clients .remove(username);
           try{
               Map<String,Object>map1= Maps.newHashMap();
               map1.put("mseeageType",2);
               map1.put("onlineUsers",clients .keySet() );
               map1.put("username",username);
               sendMessageAll(JSON.toJSONString(map1),username );

           }
           catch (IOException e){
               logger.info(username +"下线的时候通知所有人发生了错误");

           }
           logger .info("有连接关闭！当前在线人数"+onlienNumber );

   }
       @OnMessage
       public  void onMessage( String message,Session session)

       {
           try{
               logger.info("来自客户端消息；"+message+"客户端id是："+session.getId() );
               JSONObject jsonObject =JSON.parseObject(message);
               String textMessage=jsonObject.getString("message");
               String  fromusername=jsonObject .getString("username");
               String tousername=jsonObject .getString("to");

               Map<String,Object> map1 = Maps.newHashMap();
               map1.put("messageType",4);
               map1.put("textMessage",textMessage);
               map1.put("fromusername",fromusername);
               if(tousername.equals("All")){
                   map1.put("tousername","所有人");
                   sendMessageAll(JSON.toJSONString(map1),fromusername);
               }
               else{
                   map1.put("tousername",tousername);
                   sendMessageTo(JSON.toJSONString(map1),tousername);
               }
           }
           catch (Exception e){
               logger.info("发生了错误了");
           }

       }


    public void sendMessageTo(String message,String ToUserName)throws IOException{
           for (WebSocket item:clients .values() ){
               if (item.username.equals(ToUserName)){
                   item.session.getAsyncRemote().sendText(message);
               break; }

           }
   }
       private void sendMessageAll(String message, String FromUserName) throws IOException{
       for (WebSocket item : clients.values()) {
           item.session.getAsyncRemote().sendText(message);
       }

     
   }

       public static synchronized int getOnlineCount()
   { return onlienNumber;}


   }




