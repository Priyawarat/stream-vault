export default function StatCard({ label, value, hint, icon: Icon, tone = "brand" }) {
  const tones = {
    brand: "bg-vault-brand/15 text-vault-brand",
    brand2: "bg-vault-brand2/15 text-vault-brand2",
    warn: "bg-amber-400/15 text-amber-300",
    muted: "bg-vault-border text-vault-muted",
  };
  return (
    <div className="sv-card p-5">
      <div className="flex items-start justify-between gap-3">
        <div>
          <p className="text-xs font-semibold uppercase tracking-wider text-vault-muted">{label}</p>
          <p className="mt-2 text-2xl font-extrabold tracking-tight">{value}</p>
          {hint && <p className="mt-1 text-xs text-vault-muted">{hint}</p>}
        </div>
        {Icon && (
          <span className={`grid h-10 w-10 place-items-center rounded-xl ${tones[tone] || tones.brand}`}>
            <Icon size={18} />
          </span>
        )}
      </div>
    </div>
  );
}
