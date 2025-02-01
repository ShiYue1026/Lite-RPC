package com.rpc.client.netty.handler;

import com.rpc.message.RpcHeartBeat;
import com.rpc.message.RpcResponse;
import com.rpc.util.PendingProcessedMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        PendingProcessedMap.receiveResponse(rpcResponse);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.WRITER_IDLE)){  // 15秒未发送数据就向服务端发送心跳
                log.info("客户端发送心跳...");
                RpcHeartBeat heartBeat = RpcHeartBeat.builder().msg("PING").build();
                ctx.writeAndFlush(heartBeat);
            }
        }
    }
}
