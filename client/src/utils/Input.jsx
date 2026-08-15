import { useId } from "react";

export default function Input({ label, icon: Icon, error, className = "", ...rest }) {
  const id = useId();
  return (
    <div className={className}>
      {label && (
        <label className="sv-label" htmlFor={id}>
          {label}
        </label>
      )}
      <div className="relative">
        {Icon && (
          <Icon
            size={16}
            className="pointer-events-none absolute left-3.5 top-1/2 -translate-y-1/2 text-vault-muted"
          />
        )}
        <input id={id} className={`sv-input ${Icon ? "pl-10" : ""}`} {...rest} />
      </div>
      {error && <p className="mt-1.5 text-xs font-medium text-vault-danger">{error}</p>}
    </div>
  );
}
