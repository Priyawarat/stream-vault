import Logo from "../../../utils/Logo";

export default function AuthShell({ title, subtitle, children, footer }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="relative hidden overflow-hidden border-r border-vault-border lg:block">
        <img
          src="/images/auth-cover.svg"
          alt="Abstract StreamVault streaming artwork"
          className="h-full w-full object-cover"
        />
        <div className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-vault-bg to-transparent p-10">
          <h2 className="max-w-sm text-2xl font-extrabold leading-snug">
            Your video vault, streamed byte by byte.
          </h2>
          <p className="mt-2 max-w-sm text-sm text-vault-muted">
            Range based delivery, JWT protected endpoints and instant upload feedback.
          </p>
        </div>
      </div>

      <div className="flex items-center justify-center px-5 py-12">
        <div className="w-full max-w-sm">
          <Logo />
          <h1 className="mt-8 text-2xl font-extrabold tracking-tight">{title}</h1>
          <p className="mt-1.5 text-sm text-vault-muted">{subtitle}</p>
          <div className="mt-7">{children}</div>
          {footer && <div className="mt-6 text-center text-sm text-vault-muted">{footer}</div>}
        </div>
      </div>
    </div>
  );
}
