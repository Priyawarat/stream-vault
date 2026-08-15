import { NavLink } from "react-router-dom";
import { FiFilm, FiGrid, FiUploadCloud, FiX } from "react-icons/fi";
import Logo from "./Logo";

const NAV_ITEMS = [
  { to: "/dashboard", label: "Overview", icon: FiGrid },
  { to: "/videos", label: "Video library", icon: FiFilm },
];

export default function Sidebar({ open, onClose, onUploadClick }) {
  return (
    <>
      {open && <div className="fixed inset-0 z-30 bg-black/60 lg:hidden" onClick={onClose} aria-hidden />}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col border-r border-vault-border bg-vault-surface
          px-4 py-5 transition-transform lg:translate-x-0 ${open ? "translate-x-0" : "-translate-x-full"}`}
      >
        <div className="flex items-center justify-between">
          <Logo />
          <button
            type="button"
            onClick={onClose}
            aria-label="Close navigation"
            className="rounded-lg p-1.5 text-vault-muted lg:hidden"
          >
            <FiX size={18} />
          </button>
        </div>

        <nav className="mt-8 flex flex-col gap-1">
          {NAV_ITEMS.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium transition ${
                  isActive
                    ? "bg-vault-brand/12 text-vault-brand"
                    : "text-vault-muted hover:bg-vault-card hover:text-vault-text"
                }`
              }
            >
              <Icon size={17} />
              {label}
            </NavLink>
          ))}
        </nav>

        <button type="button" onClick={onUploadClick} className="sv-btn-primary mt-6 w-full">
          <FiUploadCloud size={16} />
          Upload video
        </button>

        <div className="mt-auto rounded-xl border border-vault-border bg-vault-card p-3.5 text-xs text-vault-muted">
          Streams are delivered in byte ranges over a JWT protected channel.
        </div>
      </aside>
    </>
  );
}
