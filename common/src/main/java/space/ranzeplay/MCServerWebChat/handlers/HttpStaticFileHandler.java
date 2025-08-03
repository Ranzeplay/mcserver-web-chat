package space.ranzeplay.MCServerWebChat.handlers;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

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
        
        // Simple static content for testing
        if ("/".equals(uri) || "/index.html".equals(uri)) {
            sendHTMLResponse(ctx, getIndexHTML());
        } else if ("/chat.js".equals(uri)) {
            sendJSResponse(ctx, getChatJS());
        } else {
            sendError(ctx, NOT_FOUND);
        }
    }

    private void sendHTMLResponse(ChannelHandlerContext ctx, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendJSResponse(ChannelHandlerContext ctx, String content) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, OK, Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/javascript; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Error: " + status, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private String getIndexHTML() {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>MC Web Chat</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        #chatContainer { border: 1px solid #ccc; height: 400px; overflow-y: scroll; padding: 10px; margin-bottom: 10px; }
        #messageInput { width: 70%; padding: 5px; }
        #sendButton { padding: 5px 10px; }
        .message { margin: 5px 0; }
        .web-message { color: blue; }
        .game-message { color: green; }
        #loginForm { margin-bottom: 20px; }
        #otpForm { display: none; margin-bottom: 20px; }
    </style>
</head>
<body>
    <h1>Minecraft Web Chat</h1>
    
    <div id="loginForm">
        <input type="text" id="username" placeholder="Username" />
        <input type="password" id="password" placeholder="Password (optional for new users)" />
        <button onclick="login()">Login</button>
    </div>
    
    <div id="otpForm">
        <input type="text" id="otpCode" placeholder="OTP Code" />
        <input type="password" id="newPassword" placeholder="New Password" />
        <button onclick="verifyOTP()">Verify OTP</button>
    </div>
    
    <div id="chatSection" style="display: none;">
        <div id="chatContainer"></div>
        <input type="text" id="messageInput" placeholder="Type a message..." onkeypress="if(event.key==='Enter') sendMessage()" />
        <button id="sendButton" onclick="sendMessage()">Send</button>
    </div>

    <script src="chat.js"></script>
</body>
</html>
""";
    }

    private String getChatJS() {
        return """
let ws;
let currentUsername;

function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (!username) {
        alert('Please enter a username');
        return;
    }
    
    currentUsername = username;
    connect();
    
    const authData = {
        type: 'auth',
        username: username
    };
    
    if (password) {
        authData.password = password;
    }
    
    ws.send(JSON.stringify(authData));
}

function verifyOTP() {
    const otp = document.getElementById('otpCode').value;
    const password = document.getElementById('newPassword').value;
    
    if (!otp || !password) {
        alert('Please enter both OTP and password');
        return;
    }
    
    const otpData = {
        type: 'otp_verify',
        username: currentUsername,
        otp: otp,
        password: password
    };
    
    ws.send(JSON.stringify(otpData));
}

function connect() {
    ws = new WebSocket('ws://localhost:8080/ws');
    
    ws.onopen = function() {
        console.log('Connected to WebSocket');
    };
    
    ws.onmessage = function(event) {
        const data = JSON.parse(event.data);
        
        switch(data.type) {
            case 'auth_success':
                document.getElementById('loginForm').style.display = 'none';
                document.getElementById('otpForm').style.display = 'none';
                document.getElementById('chatSection').style.display = 'block';
                break;
                
            case 'auth_failure':
                alert('Authentication failed: ' + data.reason);
                break;
                
            case 'otp_required':
                document.getElementById('loginForm').style.display = 'none';
                document.getElementById('otpForm').style.display = 'block';
                alert(data.message);
                break;
                
            case 'chat':
                addMessage(data.message);
                break;
                
            case 'history':
                data.messages.forEach(msg => addMessage(msg));
                break;
                
            case 'error':
                alert('Error: ' + data.message);
                break;
        }
    };
    
    ws.onclose = function() {
        console.log('WebSocket connection closed');
    };
    
    ws.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
}

function sendMessage() {
    const input = document.getElementById('messageInput');
    const message = input.value.trim();
    
    if (!message) return;
    
    const chatData = {
        type: 'chat',
        message: message
    };
    
    ws.send(JSON.stringify(chatData));
    input.value = '';
}

function addMessage(messageText) {
    const container = document.getElementById('chatContainer');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    messageDiv.textContent = messageText;
    container.appendChild(messageDiv);
    container.scrollTop = container.scrollHeight;
}
""";
    }
}