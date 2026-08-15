import { VIDEO_STATUS } from "./constants";

export default function StatusBadge({ status }) {
  const config = VIDEO_STATUS[status] || {
    label: status || "Unknown",
    className: "bg-vault-border text-vault-muted",
  };
  return (
    <span className={`sv-badge ${config.className}`}>
      <span className="h-1.5 w-1.5 rounded-full bg-current" />
      {config.label}
    </span>
  );
}
