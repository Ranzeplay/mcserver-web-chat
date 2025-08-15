#!/usr/bin/env node

import { writeFileSync, mkdirSync, existsSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Create src/config directory if it doesn't exist
const configDir = join(__dirname, '../src/config');
if (!existsSync(configDir)) {
    mkdirSync(configDir, { recursive: true });
}

// Generate build configuration
const buildConfig = {
    buildTime: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0',
    buildEnvironment: process.env.NODE_ENV || 'development',
    backend: {
        defaultPort: 8080,
        defaultBaseUrl: 'http://localhost:8080',
        defaultWebsocketPath: '/ws',
        defaultStaticPath: '/static'
    },
    build: {
        timestamp: Date.now(),
        nodeVersion: process.version,
        platform: process.platform
    }
};

// Write the configuration file
const configPath = join(configDir, 'build-config.ts');
const configContent = `// This file is auto-generated during build time
// Do not edit manually

export interface BuildConfig {
    buildTime: string;
    version: string;
    buildEnvironment: string;
    backend: {
        defaultPort: number;
        defaultBaseUrl: string;
        defaultWebsocketPath: string;
        defaultStaticPath: string;
    };
    build: {
        timestamp: number;
        nodeVersion: string;
        platform: string;
    };
}

export const buildConfig: BuildConfig = ${JSON.stringify(buildConfig, null, 2)};

export default buildConfig;
`;

writeFileSync(configPath, configContent, 'utf8');

console.log('✅ Build configuration generated successfully');
console.log(`📝 Generated: ${configPath}`);
console.log(`🕒 Build time: ${buildConfig.buildTime}`);
console.log(`📦 Version: ${buildConfig.version}`);
console.log(`🌍 Environment: ${buildConfig.buildEnvironment}`);