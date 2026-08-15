export default function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="sv-card flex flex-col items-center gap-3 px-6 py-14 text-center">
      {Icon && (
        <span className="grid h-14 w-14 place-items-center rounded-2xl bg-vault-surface text-vault-brand">
          <Icon size={24} />
        </span>
      )}
      <h3 className="text-base font-semibold">{title}</h3>
      {description && <p className="max-w-sm text-sm text-vault-muted">{description}</p>}
      {action}
    </div>
  );
}
