/**
 * BlueMap Web Chat Integration
 * This script integrates the MCServer Web Chat into BlueMap's web interface
 */
(function() {
    'use strict';
    
    console.log('[MCServer Web Chat] BlueMap integration loaded');
    
    // Configuration
    const CHAT_CONFIG = {
        // Port where web chat server is running
        port: 8080,
        // Whether to auto-connect on page load
        autoConnect: true,
        // Position of chat widget (bottom-right, bottom-left, etc.)
        position: 'bottom-right'
    };
    
    // Check if we're running in BlueMap context
    if (typeof bluemap === 'undefined') {
        console.warn('[MCServer Web Chat] BlueMap API not found - standalone mode');
        return;
    }
    
    /**
     * Initialize chat integration with BlueMap
     */
    function initializeChatIntegration() {
        console.log('[MCServer Web Chat] Initializing BlueMap integration');
        
        // Create chat iframe container
        const chatContainer = createChatContainer();
        document.body.appendChild(chatContainer);
        
        // Add toggle button to BlueMap UI
        addToggleButton(chatContainer);
        
        console.log('[MCServer Web Chat] BlueMap integration initialized successfully');
    }
    
    /**
     * Create the chat container with iframe
     */
    function createChatContainer() {
        const container = document.createElement('div');
        container.id = 'mcserver-webchat-container';
        container.style.cssText = `
            position: fixed;
            ${CHAT_CONFIG.position.includes('bottom') ? 'bottom: 20px;' : 'top: 20px;'}
            ${CHAT_CONFIG.position.includes('right') ? 'right: 20px;' : 'left: 20px;'}
            width: 400px;
            height: 600px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.3);
            z-index: 10000;
            display: none;
            overflow: hidden;
        `;
        
        const iframe = document.createElement('iframe');
        iframe.src = `http://${window.location.hostname}:${CHAT_CONFIG.port}`;
        iframe.style.cssText = `
            width: 100%;
            height: 100%;
            border: none;
        `;
        iframe.setAttribute('sandbox', 'allow-same-origin allow-scripts allow-forms');
        
        container.appendChild(iframe);
        
        return container;
    }
    
    /**
     * Add toggle button for chat widget
     */
    function addToggleButton(chatContainer) {
        const button = document.createElement('button');
        button.id = 'mcserver-webchat-toggle';
        button.innerHTML = '💬 Chat';
        button.style.cssText = `
            position: fixed;
            ${CHAT_CONFIG.position.includes('bottom') ? 'bottom: 20px;' : 'top: 20px;'}
            ${CHAT_CONFIG.position.includes('right') ? 'right: 20px;' : 'left: 20px;'}
            padding: 12px 20px;
            background: #2196F3;
            color: white;
            border: none;
            border-radius: 24px;
            cursor: pointer;
            font-size: 14px;
            font-weight: bold;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            z-index: 9999;
            transition: all 0.3s ease;
        `;
        
        button.addEventListener('mouseenter', () => {
            button.style.background = '#1976D2';
            button.style.transform = 'scale(1.05)';
        });
        
        button.addEventListener('mouseleave', () => {
            button.style.background = '#2196F3';
            button.style.transform = 'scale(1)';
        });
        
        button.addEventListener('click', () => {
            const isVisible = chatContainer.style.display !== 'none';
            chatContainer.style.display = isVisible ? 'none' : 'block';
            button.style.display = isVisible ? 'block' : 'none';
        });
        
        document.body.appendChild(button);
    }
    
    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initializeChatIntegration);
    } else {
        initializeChatIntegration();
    }
    
})();
