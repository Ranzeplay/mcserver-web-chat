package space.ranzeplay.MCServerWebChat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import space.ranzeplay.MCServerWebChat.handlers.WebSocketHandler;
import space.ranzeplay.MCServerWebChat.handlers.HttpStaticFileHandler;
import space.ranzeplay.MCServerWebChat.services.ConfigService;

@Slf4j
public class WebServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    @Getter
    private boolean isRunning = false;
    private final ConfigService configService;

    public WebServer() {
        this.configService = ConfigService.getInstance();
    }

    public void start() {
        if (isRunning) {
            return;
        }

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new HttpObjectAggregator(65536));
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler(configService.getWebsocketPath()));
                        pipeline.addLast(new WebSocketHandler());
                        pipeline.addLast(new HttpStaticFileHandler());
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            int port = configService.getWebPort();
            ChannelFuture f = b.bind(port).sync();
            isRunning = true;
            serverChannel = f.channel();
            log.info("MC Web Chat server started on port {} with WebSocket path {}", port, configService.getWebsocketPath());
            
        } catch (Exception e) {
            log.error("Failed to start web server: {}", e.getMessage(), e);
            stop();
        }
    }

    public void stop() {
        if (!isRunning) {
            return;
        }

        try {
            if (serverChannel != null) {
                serverChannel.close().sync();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while stopping web server", e);
        } finally {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
            }
            isRunning = false;
            log.info("MC Web Chat server stopped");
        }
    }

}