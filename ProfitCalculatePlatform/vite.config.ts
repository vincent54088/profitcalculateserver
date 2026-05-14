import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

const DEV_SERVER_CONFIG = resolve(process.cwd(), 'dev-server.config.json')

type DevServerFileConfig = {
  devServerPort?: number
  previewServerPort?: number
}

function readDevServerFileConfig(): DevServerFileConfig {
  if (!existsSync(DEV_SERVER_CONFIG)) return {}
  try {
    return JSON.parse(readFileSync(DEV_SERVER_CONFIG, 'utf-8')) as DevServerFileConfig
  } catch {
    return {}
  }
}

function portFromConfig(value: unknown, fallback: number): number {
  if (typeof value === 'number' && Number.isFinite(value) && value > 0) {
    return Math.trunc(value)
  }
  return fallback
}

function parsePort(value: string | undefined, fallback: number): number {
  const n = Number.parseInt(value ?? '', 10)
  return Number.isFinite(n) && n > 0 ? n : fallback
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const fileCfg = readDevServerFileConfig()
  const defaultDev = portFromConfig(fileCfg.devServerPort, 5173)
  const defaultPreview = portFromConfig(fileCfg.previewServerPort, defaultDev)
  const devPort = parsePort(env.DEV_SERVER_PORT, defaultDev)
  const previewPort = parsePort(env.PREVIEW_SERVER_PORT, defaultPreview)

  return {
    plugins: [vue()],
    server: {
      port: devPort,
      proxy: {
        '/api': {
          target: 'http://127.0.0.1:8080',
          changeOrigin: true,
        },
      },
    },
    preview: {
      port: previewPort,
    },
  }
})
