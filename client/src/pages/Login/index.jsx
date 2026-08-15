import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../utils/AuthContext";
import AuthShell from "./components/AuthShell";
import LoginForm from "./components/LoginForm";

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const redirectTo = location.state?.from || "/dashboard";

  if (isAuthenticated) return <Navigate to={redirectTo} replace />;

  return (
    <AuthShell
      title="Welcome back"
      subtitle="Sign in to manage and stream your StreamVault library."
      footer={
        <>
          New to StreamVault?{" "}
          <Link to="/register" className="font-semibold text-vault-brand hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <LoginForm onSuccess={() => navigate(redirectTo, { replace: true })} />
    </AuthShell>
  );
}
