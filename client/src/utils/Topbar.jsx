import { FiLogOut, FiMenu } from "react-icons/fi";
import { useAuth } from "./AuthContext";
import { initialsOf } from "./formatters";

export default function Topbar({ title, subtitle, onMenuClick }) {
  const { user, logout } = useAuth();

  return (
    <header className="sticky top-0 z-20 flex items-center gap-3 border-b border-vault-border bg-vault-bg/85 px-4 py-4 backdrop-blur lg:px-8">
      <button
        type="button"
        onClick={onMenuClick}
        aria-label="Open navigation"
        className="rounded-lg border border-vault-border p-2 text-vault-muted lg:hidden"
      >
        <FiMenu size={18} />
      </button>

      <div className="min-w-0 flex-1">
        <h1 className="truncate text-lg font-bold tracking-tight">{title}</h1>
        {subtitle && <p className="truncate text-xs text-vault-muted">{subtitle}</p>}
      </div>

      <div className="flex items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-semibold leading-tight">{user?.fullName || "StreamVault User"}</p>
          <p className="text-xs text-vault-muted">{user?.email}</p>
        </div>
        <span className="grid h-9 w-9 place-items-center rounded-full bg-vault-brand/15 text-sm font-bold text-vault-brand">
          {initialsOf(user?.fullName)}
        </span>
        <button
          type="button"
          onClick={logout}
          aria-label="Log out"
          className="rounded-lg border border-vault-border p-2 text-vault-muted transition hover:text-vault-danger"
        >
          <FiLogOut size={17} />
        </button>
      </div>
    </header>
  );
}
