import { formatBytes } from "../../../utils/formatters";

export default function StorageBar({ used, quota }) {
  const percent = quota > 0 ? Math.min(100, Math.round((used / quota) * 100)) : 0;
  return (
    <div className="sv-card p-5">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold">Storage usage</h2>
        <span className="text-xs text-vault-muted">
          {formatBytes(used)} of {formatBytes(quota)}
        </span>
      </div>
      <div className="mt-4 h-2.5 overflow-hidden rounded-full bg-vault-border">
        <div
          className="h-full rounded-full bg-gradient-to-r from-vault-brand to-vault-brand2 transition-all"
          style={{ width: `${percent}%` }}
        />
      </div>
      <p className="mt-2 text-xs text-vault-muted">{percent}% of your vault allocation is in use.</p>
    </div>
  );
}
