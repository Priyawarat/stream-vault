import { Link } from "react-router-dom";
import { FiArrowLeft } from "react-icons/fi";
import Logo from "../../utils/Logo";

export default function NotFoundPage() {
  return (
    <div className="grid min-h-screen place-items-center px-5 text-center">
      <div>
        <div className="flex justify-center">
          <Logo />
        </div>
        <h1 className="mt-8 text-6xl font-extrabold tracking-tight text-vault-brand">404</h1>
        <p className="mt-3 text-lg font-semibold">This page is not in the vault</p>
        <p className="mt-1.5 text-sm text-vault-muted">The page you are looking for was moved or never existed.</p>
        <Link to="/dashboard" className="sv-btn-primary mt-7">
          <FiArrowLeft size={15} />
          Back to dashboard
        </Link>
      </div>
    </div>
  );
}
