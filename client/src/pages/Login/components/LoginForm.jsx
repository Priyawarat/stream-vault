import { useState } from "react";
import { FiLock, FiLogIn, FiMail } from "react-icons/fi";
import Alert from "../../../utils/Alert";
import Button from "../../../utils/Button";
import Input from "../../../utils/Input";
import { extractErrorMessage } from "../../../utils/apiClient";
import { useAuth } from "../../../utils/AuthContext";

export default function LoginForm({ onSuccess }) {
  const { login } = useAuth();
  const [form, setForm] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const update = (key) => (event) => setForm((prev) => ({ ...prev, [key]: event.target.value }));

  const validate = () => {
    const next = {};
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) next.email = "Enter a valid email address.";
    if (form.password.length < 6) next.password = "Password must be at least 6 characters.";
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    if (!validate()) return;
    setLoading(true);
    try {
      await login(form);
      onSuccess?.();
    } catch (error) {
      setMessage(extractErrorMessage(error, "Invalid email or password."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit} noValidate>
      <Alert tone="error">{message}</Alert>
      <Input
        label="Email"
        type="email"
        icon={FiMail}
        autoComplete="email"
        placeholder="you@example.com"
        value={form.email}
        onChange={update("email")}
        error={errors.email}
      />
      <Input
        label="Password"
        type="password"
        icon={FiLock}
        autoComplete="current-password"
        placeholder="••••••••"
        value={form.password}
        onChange={update("password")}
        error={errors.password}
      />
      <Button type="submit" className="w-full" loading={loading} icon={FiLogIn}>
        Sign in
      </Button>
    </form>
  );
}
