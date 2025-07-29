/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_MAX_MESSAGE_LENGTH: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
