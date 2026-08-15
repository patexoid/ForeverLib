import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const dirname = path.dirname(fileURLToPath(import.meta.url));

// Proxies API calls to a local Spring Boot instance during `npm run dev` so the
// browser sees same-origin requests and CORS never comes into play.
const backend = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/author': backend,
      '/book': backend,
      '/sequence': backend,
      '/user': backend,
    },
  },
  build: {
    // Prod build lands directly in core's static resources so it's served
    // same-origin by the Spring Boot app (no separate deploy/CORS story).
    outDir: path.resolve(dirname, '../core/src/main/resources/static/admin'),
    emptyOutDir: true,
  },
});
