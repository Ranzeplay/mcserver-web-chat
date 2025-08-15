import React, { useState } from 'react';
import configService, { type RuntimeConfig } from '../services/configService';
import BuildInfo from './BuildInfo';

export const ConfigDemo: React.FC = () => {
  const [config, setConfig] = useState<RuntimeConfig>(configService.getConfig());
  const [showBuildInfo, setShowBuildInfo] = useState(false);

  const handleBackendConfigChange = (field: keyof RuntimeConfig['backend'], value: string | number) => {
    const updatedConfig = { [field]: value };
    configService.updateBackendConfig(updatedConfig);
    setConfig(configService.getConfig());
  };

  const handleUIConfigChange = (field: keyof RuntimeConfig['ui'], value: string) => {
    const updatedConfig = { [field]: value };
    configService.updateUIConfig(updatedConfig);
    setConfig(configService.getConfig());
  };

  const handleReset = () => {
    configService.resetToDefaults();
    setConfig(configService.getConfig());
  };

  const buildInfo = configService.getBuildInfo();

  return (
    <div className="config-demo p-4 bg-gray-100 rounded-lg">
      <h2 className="text-lg font-bold mb-4">Configuration Demo</h2>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Backend Configuration */}
        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2">Backend Configuration</h3>
          
          <div className="space-y-2">
            <div>
              <label className="block text-sm font-medium">Port:</label>
              <input
                type="number"
                value={config.backend.port}
                onChange={(e) => handleBackendConfigChange('port', parseInt(e.target.value))}
                className="w-full p-1 border rounded"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium">Base URL:</label>
              <input
                type="text"
                value={config.backend.baseUrl}
                onChange={(e) => handleBackendConfigChange('baseUrl', e.target.value)}
                className="w-full p-1 border rounded"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium">WebSocket Path:</label>
              <input
                type="text"
                value={config.backend.websocketPath}
                onChange={(e) => handleBackendConfigChange('websocketPath', e.target.value)}
                className="w-full p-1 border rounded"
              />
            </div>
          </div>
        </div>

        {/* UI Configuration */}
        <div className="bg-white p-4 rounded shadow">
          <h3 className="font-semibold mb-2">UI Configuration</h3>
          
          <div className="space-y-2">
            <div>
              <label className="block text-sm font-medium">Theme:</label>
              <select
                value={config.ui.theme}
                onChange={(e) => handleUIConfigChange('theme', e.target.value)}
                className="w-full p-1 border rounded"
              >
                <option value="light">Light</option>
                <option value="dark">Dark</option>
                <option value="auto">Auto</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium">Language:</label>
              <input
                type="text"
                value={config.ui.language}
                onChange={(e) => handleUIConfigChange('language', e.target.value)}
                className="w-full p-1 border rounded"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Current Configuration Display */}
      <div className="mt-4 bg-white p-4 rounded shadow">
        <h3 className="font-semibold mb-2">Current Configuration</h3>
        <div className="text-sm">
          <div><strong>WebSocket URL:</strong> {configService.getWebSocketUrl()}</div>
          <div><strong>Backend URL:</strong> {configService.getBackendUrl()}</div>
        </div>
      </div>

      {/* Build Information */}
      <div className="mt-4 bg-white p-4 rounded shadow">
        <div className="flex justify-between items-center mb-2">
          <h3 className="font-semibold">Build Information</h3>
          <button
            onClick={() => setShowBuildInfo(!showBuildInfo)}
            className="text-blue-500 hover:text-blue-700 text-sm"
          >
            {showBuildInfo ? 'Hide' : 'Show'} Details
          </button>
        </div>
        
        {showBuildInfo && <BuildInfo />}
        
        <div className="text-sm text-gray-600">
          <div>Version: {buildInfo.version}</div>
          <div>Built: {new Date(buildInfo.buildTime).toLocaleString()}</div>
          <div>Environment: {buildInfo.environment}</div>
        </div>
      </div>

      {/* Actions */}
      <div className="mt-4 flex gap-2">
        <button
          onClick={handleReset}
          className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Reset to Defaults
        </button>
      </div>
    </div>
  );
};

export default ConfigDemo;