import { cloudflare } from "@cloudflare/vite-plugin";
import tailwindcss from "@tailwindcss/vite";
import { tanstackStart } from "@tanstack/react-start/plugin/vite";
import react from "@vitejs/plugin-react";
import tsConfigPaths from "vite-tsconfig-paths";
import { defineConfig } from "vite";

const isCloudflare = process.env.CF_PAGES === "1" || process.env.CLOUDFLARE === "true";

export default defineConfig({
  plugins: [
    isCloudflare ? cloudflare({ viteEnvironment: { name: "ssr" } }) : null,
    tsConfigPaths(),
    tailwindcss(),
    tanstackStart({
      server: { entry: "server" },
    }),
    react(),
  ].filter(Boolean),
  build: {
    minify: true,
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("node_modules/react/") || id.includes("node_modules/react-dom/")) {
            return "vendor-react";
          }
          if (id.includes("node_modules/recharts")) {
            return "vendor-recharts";
          }
          if (id.includes("node_modules/@radix-ui/") || id.includes("node_modules/@tanstack/")) {
            return "vendor-framework";
          }
          return undefined;
        },
      },
    },
  },
});
