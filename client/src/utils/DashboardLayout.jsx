import { useState } from "react";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";

export default function DashboardLayout({ title, subtitle, actions, onUploadClick, children }) {
  const [navOpen, setNavOpen] = useState(false);

  return (
    <div className="min-h-screen bg-vault-bg">
      <Sidebar
        open={navOpen}
        onClose={() => setNavOpen(false)}
        onUploadClick={() => {
          setNavOpen(false);
          onUploadClick?.();
        }}
      />
      <div className="lg:pl-64">
        <Topbar title={title} subtitle={subtitle} onMenuClick={() => setNavOpen(true)} />
        <main className="px-4 py-6 lg:px-8">
          {actions && <div className="mb-6 flex flex-wrap gap-2">{actions}</div>}
          {children}
        </main>
      </div>
    </div>
  );
}
