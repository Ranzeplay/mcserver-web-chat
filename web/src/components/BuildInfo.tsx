import React from 'react';
import buildConfig from '../config/build-config';

export const BuildInfo: React.FC = () => {
  return (
    <div className="build-info text-xs text-gray-500 p-2">
      <div className="font-semibold">Build Information</div>
      <div>Version: {buildConfig.version}</div>
      <div>Built: {new Date(buildConfig.buildTime).toLocaleString()}</div>
      <div>Environment: {buildConfig.buildEnvironment}</div>
      <div>Backend URL: {buildConfig.backend.defaultBaseUrl}</div>
      <div>WebSocket Path: {buildConfig.backend.defaultWebsocketPath}</div>
    </div>
  );
};

export default BuildInfo;