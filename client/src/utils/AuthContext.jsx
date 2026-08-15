import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { registerUnauthorizedHandler } from "./apiClient";
import { loginRequest, registerRequest } from "./authService";
import { clearAccessToken, decodeJwt, getAccessToken, setAccessToken } from "./storage";

const AuthContext = createContext(null);

const userFromToken = (token) => {
  const claims = decodeJwt(token);
  if (!claims) return null;
  if (claims.exp && claims.exp * 1000 < Date.now()) return null;
  return {
    id: claims.sub,
    fullName: claims.name || "StreamVault User",
    email: claims.email || "",
    mobile: claims.mobile || "",
    active: claims.active !== false,
  };
};

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [initializing, setInitializing] = useState(true);

  useEffect(() => {
    const token = getAccessToken();
    setUser(token ? userFromToken(token) : null);
    setInitializing(false);
  }, []);

  const logout = useCallback(() => {
    clearAccessToken();
    setUser(null);
  }, []);

  useEffect(() => registerUnauthorizedHandler(logout), [logout]);

  const login = useCallback(async (credentials) => {
    const result = await loginRequest(credentials);
    const token = result?.accessToken;
    if (!token) throw new Error("Login response did not contain an access token.");
    setAccessToken(token);
    setUser(userFromToken(token) || { fullName: "StreamVault User", email: credentials.email });
    return token;
  }, []);

  const register = useCallback((payload) => registerRequest(payload), []);

  const value = useMemo(
    () => ({ user, isAuthenticated: Boolean(user), initializing, login, register, logout }),
    [user, initializing, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside an AuthProvider");
  return context;
};
