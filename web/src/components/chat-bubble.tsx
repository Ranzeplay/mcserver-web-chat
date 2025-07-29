import { cn } from "@/lib/utils"

export interface ChatMessage {
  id: string
  content: string
  timestamp: Date
  isOwnMessage: boolean
  author?: string
}

interface ChatBubbleProps {
  message: ChatMessage
  className?: string
}

export function ChatBubble({ message, className }: ChatBubbleProps) {
  return (
    <div
      className={cn(
        "flex w-full mb-4",
        message.isOwnMessage ? "justify-end" : "justify-start",
        className
      )}
    >
      <div
        className={cn(
          "max-w-[70%] rounded-lg px-3 py-2 text-sm",
          message.isOwnMessage
            ? "bg-primary text-primary-foreground ml-12"
            : "bg-muted text-muted-foreground mr-12"
        )}
      >
        {!message.isOwnMessage && message.author && (
          <div className="text-xs font-medium mb-1 opacity-70">
            {message.author}
          </div>
        )}
        <div className="break-words">{message.content}</div>
        <div
          className={cn(
            "text-xs mt-1 opacity-60",
            message.isOwnMessage ? "text-right" : "text-left"
          )}
        >
          {message.timestamp.toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
          })}
        </div>
      </div>
    </div>
  )
}