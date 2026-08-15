export const formatBytes = (bytes) => {
  const value = Number(bytes);
  if (!Number.isFinite(value) || value <= 0) return "0 B";
  const units = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1);
  return `${(value / 1024 ** i).toFixed(i === 0 ? 0 : 1)} ${units[i]}`;
};

export const formatDate = (input) => {
  if (!input) return "—";
  const date = new Date(input);
  if (Number.isNaN(date.getTime())) return "—";
  return date.toLocaleString(undefined, {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
};

export const shortId = (id) => (id ? `${String(id).slice(0, 8)}…` : "—");

export const initialsOf = (name) =>
  (name || "SV")
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join("");
