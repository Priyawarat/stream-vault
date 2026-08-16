import { FiAlertTriangle, FiCheckCircle, FiInfo } from "react-icons/fi";

const TONES = {
  error: { className: "border-vault-danger/30 bg-vault-danger/10 text-vault-danger", Icon: FiAlertTriangle },
  success: { className: "border-vault-brand/30 bg-vault-brand/10 text-vault-brand", Icon: FiCheckCircle },
  info: { className: "border-vault-brand2/30 bg-vault-brand2/10 text-vault-brand2", Icon: FiInfo },
};

export default function Alert({ tone = "info", children, className = "" }) {
  if (!children) return null;
  const { className: toneClass, Icon } = TONES[tone] || TONES.info;
  return (
    <div className={`flex items-start gap-2.5 rounded-xl border px-3.5 py-3 text-sm ${toneClass} ${className}`}>
      <Icon size={16} className="mt-0.5 shrink-0" />
      <span>{children}</span>
    </div>
  );
}
