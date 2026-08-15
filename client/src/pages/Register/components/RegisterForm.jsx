import { useState } from "react";
import { FiLock, FiMail, FiPhone, FiUser, FiUserPlus } from "react-icons/fi";
import Alert from "../../../utils/Alert";
import Button from "../../../utils/Button";
import Input from "../../../utils/Input";
import { extractErrorMessage } from "../../../utils/apiClient";
import { useAuth } from "../../../utils/AuthContext";

const EMPTY = { fullName: "", email: "", mobile: "", password: "" };

export default function RegisterForm({ onSuccess }) {
  const { register } = useAuth();
  const [form, setForm] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const update = (key) => (event) => setForm((prev) => ({ ...prev, [key]: event.target.value }));

  const validate = () => {
    const next = {};
    if (form.fullName.trim().length < 3) next.fullName = "Enter your full name.";
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) next.email = "Enter a valid email address.";
    if (!/^\d{10,15}$/.test(form.mobile)) next.mobile = "Mobile must be 10 to 15 digits.";
    if (form.password.length < 6) next.password = "Use at least 6 characters.";
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage("");
    if (!validate()) return;
    setLoading(true);
    try {
      await register(form);
      onSuccess?.();
    } catch (error) {
      setMessage(extractErrorMessage(error, "Could not create your account."));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="space-y-4" onSubmit={handleSubmit} noValidate>
      <Alert tone="error">{message}</Alert>
      <Input
        label="Full name"
        icon={FiUser}
        autoComplete="name"
        placeholder="Sourav Kumar"
        value={form.fullName}
        onChange={update("fullName")}
        error={errors.fullName}
      />
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
        label="Mobile"
        inputMode="numeric"
        icon={FiPhone}
        autoComplete="tel"
        placeholder="7454354334"
        value={form.mobile}
        onChange={update("mobile")}
        error={errors.mobile}
      />
      <Input
        label="Password"
        type="password"
        icon={FiLock}
        autoComplete="new-password"
        placeholder="••••••••"
        value={form.password}
        onChange={update("password")}
        error={errors.password}
      />
      <Button type="submit" className="w-full" loading={loading} icon={FiUserPlus}>
        Create account
      </Button>
    </form>
  );
}
