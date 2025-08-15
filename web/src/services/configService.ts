import buildConfig from '../config/build-config';

export interface RuntimeConfig {
  backend: {
    port: number;
    baseUrl: string;
    websocketPath: string;
    staticPath: string;
  };
  ui: {
    theme: 'light' | 'dark' | 'auto';
    language: string;
  };
}

class ConfigurationService {
  private config: RuntimeConfig;

  constructor() {
    // Initialize with build-time defaults
    this.config = {
      backend: {
        port: buildConfig.backend.defaultPort,
        baseUrl: buildConfig.backend.defaultBaseUrl,
        websocketPath: buildConfig.backend.defaultWebsocketPath,
        staticPath: buildConfig.backend.defaultStaticPath,
      },
      ui: {
        theme: 'auto',
        language: 'en-us',
      },
    };

    // Load any runtime overrides from localStorage
    this.loadFromStorage();
  }

  private loadFromStorage(): void {
    try {
      const stored = localStorage.getItem('mcserver-web-chat-config');
      if (stored) {
        const parsedConfig = JSON.parse(stored);
        this.config = { ...this.config, ...parsedConfig };
      }
    } catch (error) {
      console.warn('Failed to load configuration from storage:', error);
    }
  }

  private saveToStorage(): void {
    try {
      localStorage.setItem('mcserver-web-chat-config', JSON.stringify(this.config));
    } catch (error) {
      console.warn('Failed to save configuration to storage:', error);
    }
  }

  public getConfig(): RuntimeConfig {
    return { ...this.config };
  }

  public updateBackendConfig(config: Partial<RuntimeConfig['backend']>): void {
    this.config.backend = { ...this.config.backend, ...config };
    this.saveToStorage();
  }

  public updateUIConfig(config: Partial<RuntimeConfig['ui']>): void {
    this.config.ui = { ...this.config.ui, ...config };
    this.saveToStorage();
  }

  public getBackendUrl(): string {
    return this.config.backend.baseUrl;
  }

  public getWebSocketUrl(): string {
    const baseUrl = this.config.backend.baseUrl;
    const wsPath = this.config.backend.websocketPath;
    
    // Convert HTTP URL to WebSocket URL
    const wsUrl = baseUrl.replace(/^http/, 'ws') + wsPath;
    return wsUrl;
  }

  public getBuildInfo() {
    return {
      buildTime: buildConfig.buildTime,
      version: buildConfig.version,
      environment: buildConfig.buildEnvironment,
      nodeVersion: buildConfig.build.nodeVersion,
      platform: buildConfig.build.platform,
    };
  }

  public resetToDefaults(): void {
    this.config = {
      backend: {
        port: buildConfig.backend.defaultPort,
        baseUrl: buildConfig.backend.defaultBaseUrl,
        websocketPath: buildConfig.backend.defaultWebsocketPath,
        staticPath: buildConfig.backend.defaultStaticPath,
      },
      ui: {
        theme: 'auto',
        language: 'en-us',
      },
    };
    this.saveToStorage();
  }
}

export const configService = new ConfigurationService();
export default configService;