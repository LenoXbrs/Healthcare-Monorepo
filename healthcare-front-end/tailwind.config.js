/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        obsidian: "#0A0E1A",
        "navy-deep": "#0D1B3E",
        "midnight-blue": "#112244",
        "glass-dark": "rgba(26, 37, 80, 0.75)",
        "glass-light": "rgba(30, 45, 92, 0.85)",
        "slate-600": "#3D5280",
        "slate-400": "#7A90B8",
        "slate-200": "#C8D4F0",
        "slate-100": "#F0F4FF",
        "cyber-teal": "#00D4CC",
        "neon-aqua": "#00F5E4",
        "electric-blue": "#0099FF",
        // Triage levels (Manchester Protocol)
        "triage-critical": "#FF2D55",
        "triage-urgent": "#FF6B00",
        "triage-semi": "#FFD600",
        "triage-low": "#00C853",
        "triage-none": "#2196F3",
      },
      fontFamily: {
        mono: ["'DM Mono'", "monospace"],
        sans: ["'Plus Jakarta Sans'", "sans-serif"],
      },
      backgroundImage: {
        "gradient-surface": "linear-gradient(135deg, #0A0E1A 0%, #0D1B3E 50%, #112244 100%)",
        "gradient-card": "linear-gradient(145deg, rgba(26, 37, 80, 0.9) 0%, rgba(13, 27, 62, 0.7) 100%)",
        "gradient-teal": "linear-gradient(90deg, #00D4CC 0%, #0099FF 100%)",
        "gradient-critical": "linear-gradient(90deg, #FF2D55 0%, #FF6B00 100%)",
        "gradient-sidebar": "linear-gradient(180deg, #0D1B3E 0%, #0A0E1A 100%)",
      },
      boxShadow: {
        "glow-teal": "0 0 24px rgba(0, 212, 204, 0.35)",
        "glow-critical": "0 0 32px rgba(255, 45, 85, 0.5)",
        "glow-urgent": "0 0 24px rgba(255, 107, 0, 0.4)",
      },
    },
  },
  plugins: [],
}
