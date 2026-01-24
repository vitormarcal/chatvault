<template>
  <div>
    <index />
  </div>
</template>
<script setup lang="ts">
import Index from "~/pages/index.vue";
</script>

<style>
:root {
  --color-bg: #020617;
  --color-bg-alt: #0f172a;
  --color-bg-elevated: #0b1120;
  --color-surface: #fbfbfd;
  --color-surface-muted: #f8f9fb;
  --color-text: #f8fafc;
  --color-text-muted: rgba(226, 232, 240, 0.7);
  --color-text-subtle: rgba(226, 232, 240, 0.6);
  --color-text-dark: #111827;
  --color-text-muted-dark: #6b7280;
  --color-border: rgba(148, 163, 184, 0.2);
  --color-border-strong: rgba(148, 163, 184, 0.35);
  --color-border-soft: rgba(15, 23, 42, 0.08);
  --color-accent: #3b82f6;
  --color-accent-soft: rgba(59, 130, 246, 0.12);
  --color-accent-strong: rgba(59, 130, 246, 0.45);
  --shadow-sm: 0 10px 20px rgba(2, 6, 23, 0.2);
  --shadow-md: 0 20px 60px rgba(15, 23, 42, 0.2);
  --shadow-lg: 0 30px 70px rgba(2, 6, 23, 0.6);
  --radius-sm: 10px;
  --radius-md: 12px;
  --radius-lg: 16px;
  --radius-pill: 999px;
  --focus-ring: rgba(59, 130, 246, 0.7);
}

body {
  background: var(--color-bg);
  color: var(--color-text);
}

a {
  color: inherit;
}

button:focus-visible,
a:focus-visible,
.btn:focus-visible,
input:focus-visible,
select:focus-visible,
textarea:focus-visible,
[role="button"]:focus-visible {
  outline: 2px solid var(--focus-ring);
  outline-offset: 2px;
}
</style>
