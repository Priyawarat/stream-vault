const MODES = [
  { key: "range", label: "Range stream", hint: "/stream" },
  { key: "full", label: "Full stream", hint: "/stream" },
];

export default function StreamModeToggle({ mode, onChange }) {
  return (
    <div className="flex flex-wrap gap-2">
      {MODES.map(({ key, label, hint }) => (
        <button
          key={key}
          type="button"
          onClick={() => onChange(key)}
          className={`rounded-xl border px-3.5 py-2 text-left text-sm transition ${
            mode === key
              ? "border-vault-brand/60 bg-vault-brand/12 text-vault-brand"
              : "border-vault-border bg-vault-surface text-vault-muted hover:text-vault-text"
          }`}
        >
          <span className="block font-semibold">{label}</span>
          <span className="block text-xs opacity-80">{hint}</span>
        </button>
      ))}
    </div>
  );
}
