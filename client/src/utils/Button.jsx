import Spinner from "./Spinner";

const VARIANTS = {
  primary: "sv-btn-primary",
  ghost: "sv-btn-ghost",
  danger: "sv-btn-danger",
};

export default function Button({
  variant = "primary",
  loading = false,
  icon: Icon,
  children,
  className = "",
  disabled,
  ...rest
}) {
  return (
    <button
      className={`${VARIANTS[variant] || VARIANTS.primary} ${className}`}
      disabled={disabled || loading}
      {...rest}
    >
      {loading ? <Spinner size={16} /> : Icon ? <Icon size={16} /> : null}
      {children}
    </button>
  );
}
