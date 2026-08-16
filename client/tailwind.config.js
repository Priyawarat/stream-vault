/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        vault: {
          bg: "#0b0f14",
          surface: "#121821",
          card: "#161d28",
          border: "#232c3b",
          text: "#e6edf6",
          muted: "#8a99ad",
          brand: "#00d3a7",
          brand2: "#1e9df1",
          danger: "#ff5d5d",
        },
      },
      fontFamily: { sans: ["Inter", "system-ui", "sans-serif"] },
      boxShadow: { glow: "0 0 0 1px rgba(0,211,167,.25), 0 12px 40px -12px rgba(0,211,167,.35)" },
    },
  },
  plugins: [],
};
