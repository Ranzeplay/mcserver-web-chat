package space.ranzeplay.MCServerWebChat.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpStaticFileHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        if (request.method() != HttpMethod.GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        String uri = request.uri();
        
        // Remove query parameters
        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            uri = uri.substring(0, queryIndex);
        }
        
        // Serve files from the static resources
        if ("/".equals(uri) || uri.isEmpty()) {
            uri = "/index.html";
        }
        
        // Try to serve the file from resources
        String resourcePath = "static" + uri;
        // Normalize and validate resourcePath to prevent path traversal
        Path normalizedPath = Paths.get(resourcePath).normalize();
        if (!normalizedPath.startsWith("static")) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(normalizedPath.toString().replace('\\', '/'));
        
        if (inputStream != null) {
            try {
                byte[] content = readAllBytes(inputStream);
                String contentType = getContentType(uri);
                sendResponse(ctx, content, contentType);
            } catch (IOException e) {
                sendError(ctx, INTERNAL_SERVER_ERROR);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        } else {
            // For SPA routing, fallback to index.html for unknown routes
            if (!uri.contains(".")) {
                resourcePath = "static/index.html";
                inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
                if (inputStream != null) {
                    try {
                        byte[] content = readAllBytes(inputStream);
                        sendResponse(ctx, content, "text/html; charset=UTF-8");
                    } catch (IOException e) {
                        sendError(ctx, INTERNAL_SERVER_ERROR);
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                } else {
                    sendError(ctx, NOT_FOUND);
                }
            } else {
                sendError(ctx, NOT_FOUND);
            }
        }
    }

    private void sendResponse(ChannelHandlerContext ctx, byte[] content, String contentType) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.copiedBuffer(content));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".html")) {
            return "text/html; charset=UTF-8";
        } else if (uri.endsWith(".js")) {
            return "application/javascript; charset=UTF-8";
        } else if (uri.endsWith(".css")) {
            return "text/css; charset=UTF-8";
        } else if (uri.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (uri.endsWith(".png")) {
            return "image/png";
        } else if (uri.endsWith(".jpg") || uri.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (uri.endsWith(".ico")) {
            return "image/x-icon";
        } else {
            return "application/octet-stream";
        }
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Error: " + status, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
    
    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}