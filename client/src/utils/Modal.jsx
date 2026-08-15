import { useEffect } from "react";
import { FiX } from "react-icons/fi";

export default function Modal({ open, onClose, title, children, footer }) {
  useEffect(() => {
    if (!open) return undefined;
    const onKeyDown = (event) => event.key === "Escape" && onClose?.();
    document.addEventListener("keydown", onKeyDown);
    document.body.style.overflow = "hidden";
    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    };
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/70 p-4 backdrop-blur-sm">
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="sv-card w-full max-w-lg overflow-hidden bg-vault-surface"
      >
        <header className="flex items-center justify-between border-b border-vault-border px-5 py-4">
          <h2 className="text-base font-semibold">{title}</h2>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close dialog"
            className="rounded-lg p-1.5 text-vault-muted transition hover:bg-vault-card hover:text-vault-text"
          >
            <FiX size={18} />
          </button>
        </header>
        <div className="px-5 py-5">{children}</div>
        {footer && <footer className="flex justify-end gap-2 border-t border-vault-border px-5 py-4">{footer}</footer>}
      </div>
    </div>
  );
}
