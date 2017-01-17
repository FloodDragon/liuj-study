package com.stereo.via.ipc.server;

import com.stereo.via.event.Dispatcher;
import com.stereo.via.ipc.Constants;
import com.stereo.via.ipc.Heartbeat;
import com.stereo.via.ipc.Packet;
import com.stereo.via.ipc.server.api.IpcEngine;
import com.stereo.via.ipc.server.event.HeartbeatEvent;
import com.stereo.via.ipc.server.event.RequestEvent;
import com.stereo.via.ipc.server.event.ResponseEvent;
import com.stereo.via.ipc.server.event.enums.HeartbeatEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by stereo on 16-8-9.
 */
public class IpcHandler extends ChannelInboundHandlerAdapter implements IpcEngine {

    private static Logger LOG = LoggerFactory.getLogger(IpcHandler.class);

    private Dispatcher dispatcher;
    public IpcHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try
        {
            if(msg != null  && msg instanceof Packet)
            {
                final Packet packet = (Packet) msg;
                byte type = packet.getType();
                switch (type)
                {
                    case Constants.TYPE_REQUEST:
                        dispatcher.getEventHandler().handle(new RequestEvent(packet,ctx));
                        break;
                    case Constants.TYPE_RESPONSE:
                        dispatcher.getEventHandler().handle(new ResponseEvent(packet,ctx));
                        break;
                    case Constants.TYPE_HEARTBEAT_REQUEST_REGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.REGISTER,ctx,packet.getHeartbeat()));
                        break;
                    case Constants.TYPE_HEARTBEAT:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.HEARTBEAT,ctx,packet.getHeartbeat()));
                        break;
                    case Constants.TYPE_HEARTBEAT_REQUEST_UNREGISTER:
                        dispatcher.getEventHandler().handle(new HeartbeatEvent(HeartbeatEnum.UNREGISTER,ctx,packet.getHeartbeat()));
                        break;
                    default:
                        LOG.error("IpcHandler.channelRead error msg is " + msg);
                }
            } else
                LOG.error("IpcHandler.channelRead error msg is " + msg);
        } catch (Exception e)
        {
            LOG.error("IpcHandler.handle packet is " + msg + " error",e);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOG.error("IpcHandler.exceptionCaught",cause);
        ctx.close();
    }
}
