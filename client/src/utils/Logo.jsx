import { FiPlayCircle } from "react-icons/fi";

export default function Logo({ compact = false }) {
  return (
    <span className="flex items-center gap-2.5">
      <span className="grid h-9 w-9 place-items-center rounded-xl bg-vault-brand text-vault-bg">
        <FiPlayCircle size={20} />
      </span>
      {!compact && (
        <span className="text-lg font-extrabold tracking-tight">
          Stream<span className="text-vault-brand">Vault</span>
        </span>
      )}
    </span>
  );
}
