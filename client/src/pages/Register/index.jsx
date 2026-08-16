import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import Alert from "../../utils/Alert";
import { useAuth } from "../../utils/AuthContext";
import AuthShell from "../Login/components/AuthShell";
import RegisterForm from "./components/RegisterForm";

export default function RegisterPage() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [done, setDone] = useState(false);

  if (isAuthenticated) return <Navigate to="/dashboard" replace />;

  const handleSuccess = () => {
    setDone(true);
    setTimeout(() => navigate("/login", { replace: true }), 1200);
  };

  return (
    <AuthShell
      title="Create your vault"
      subtitle="Upload, organise and stream your videos in one place."
      footer={
        <>
          Already registered?{" "}
          <Link to="/login" className="font-semibold text-vault-brand hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      {done && (
        <Alert tone="success" className="mb-4">
          Account created. Redirecting you to sign in…
        </Alert>
      )}
      <RegisterForm onSuccess={handleSuccess} />
    </AuthShell>
  );
}
