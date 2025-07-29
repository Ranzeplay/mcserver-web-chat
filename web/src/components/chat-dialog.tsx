import { useState, useRef, useEffect } from "react"
import { ChatBubble, type ChatMessage } from "./chat-bubble"
import { Button } from "./ui/button"
import { Input } from "./ui/input"
import { Send } from "lucide-react"
import { cn } from "@/lib/utils"

interface ChatDialogProps {
  className?: string
}

export function ChatDialog({ className }: ChatDialogProps) {
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "1",
      content: "Welcome to the Minecraft server chat!",
      timestamp: new Date(Date.now() - 300000), // 5 minutes ago
      isOwnMessage: false,
      author: "Server",
    },
    {
      id: "2", 
      content: "Hey everyone! How's the new build going?",
      timestamp: new Date(Date.now() - 120000), // 2 minutes ago
      isOwnMessage: false,
      author: "Steve",
    },
    {
      id: "3",
      content: "Looking great! Just finished the castle walls.",
      timestamp: new Date(Date.now() - 60000), // 1 minute ago
      isOwnMessage: true,
    },
  ])
  
  const [newMessage, setNewMessage] = useState("")
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const inputRef = useRef<HTMLInputElement>(null)

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" })
  }

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const handleSendMessage = () => {
    if (newMessage.trim()) {
      const message: ChatMessage = {
        id: Date.now().toString(),
        content: newMessage.trim(),
        timestamp: new Date(),
        isOwnMessage: true,
      }
      setMessages((prev) => [...prev, message])
      setNewMessage("")
      inputRef.current?.focus()
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault()
      handleSendMessage()
    }
  }

  return (
    <div
      className={cn(
        "flex flex-col h-screen max-h-screen bg-background",
        className
      )}
    >
      {/* Header */}
      <div className="flex-shrink-0 border-b bg-card p-4">
        <h1 className="text-xl font-semibold text-foreground">
          Minecraft Server Chat
        </h1>
        <p className="text-sm text-muted-foreground">
          Connected to server • 12 players online
        </p>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-4 space-y-2">
        {messages.map((message) => (
          <ChatBubble key={message.id} message={message} />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="flex-shrink-0 border-t bg-card p-4">
        <div className="flex gap-2">
          <Input
            ref={inputRef}
            value={newMessage}
            onChange={(e) => setNewMessage(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type your message..."
            className="flex-1"
            maxLength={256}
          />
          <Button
            onClick={handleSendMessage}
            disabled={!newMessage.trim()}
            size="icon"
            className="flex-shrink-0"
          >
            <Send className="h-4 w-4" />
          </Button>
        </div>
        <div className="text-xs text-muted-foreground mt-2">
          Press Enter to send • {newMessage.length}/256 characters
        </div>
      </div>
    </div>
  )
}