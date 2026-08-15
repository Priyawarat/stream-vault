import { useMemo, useState } from "react";
import { FiCheckCircle, FiClock, FiFilm, FiHardDrive, FiRefreshCw, FiUploadCloud } from "react-icons/fi";
import Alert from "../../utils/Alert";
import Button from "../../utils/Button";
import DashboardLayout from "../../utils/DashboardLayout";
import Spinner from "../../utils/Spinner";
import { formatBytes } from "../../utils/formatters";
import { useAuth } from "../../utils/AuthContext";
import useVideos from "../../utils/useVideos";
import UploadModal from "../Videos/components/UploadModal";
import RecentUploads from "./components/RecentUploads";
import StatCard from "./components/StatCard";
import StorageBar from "./components/StorageBar";

const STORAGE_QUOTA = 5 * 1024 * 1024 * 1024;

export default function DashboardPage() {
  const { user } = useAuth();
  const { videos, loading, error, reload, setVideos } = useVideos();
  const [uploadOpen, setUploadOpen] = useState(false);

  const stats = useMemo(() => {
    const totalBytes = videos.reduce((sum, video) => sum + (Number(video.fileSize) || 0), 0);
    return {
      total: videos.length,
      ready: videos.filter((video) => video.status === "READY").length,
      pending: videos.filter((video) => video.status !== "READY").length,
      totalBytes,
    };
  }, [videos]);

  const recent = useMemo(
    () =>
      [...videos]
        .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0))
        .slice(0, 5),
    [videos],
  );

  const handleUploaded = (uploaded) => {
    if (!uploaded?.videoId) return;
    setVideos((prev) => [{ ...uploaded, createdAt: new Date().toISOString() }, ...prev]);
  };

  return (
    <DashboardLayout
      title={`Welcome back, ${user?.fullName?.split(" ")[0] || "there"}`}
      subtitle="Here is how your StreamVault library is doing today."
      onUploadClick={() => setUploadOpen(true)}
      actions={
        <>
          <Button onClick={() => setUploadOpen(true)} icon={FiUploadCloud}>
            Upload video
          </Button>
          <Button variant="ghost" onClick={reload} icon={FiRefreshCw}>
            Refresh data
          </Button>
        </>
      }
    >
      {error && (
        <Alert tone="error" className="mb-5">
          {error}
        </Alert>
      )}

      {loading ? (
        <div className="grid place-items-center py-24 text-vault-brand">
          <Spinner size={28} />
        </div>
      ) : (
        <div className="space-y-6">
          <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
            <StatCard label="Total videos" value={stats.total} icon={FiFilm} hint="All uploads in your vault" />
            <StatCard label="Ready to stream" value={stats.ready} icon={FiCheckCircle} tone="brand" />
            <StatCard label="Processing" value={stats.pending} icon={FiClock} tone="warn" />
            <StatCard
              label="Storage used"
              value={formatBytes(stats.totalBytes)}
              icon={FiHardDrive}
              tone="brand2"
            />
          </div>

          <StorageBar used={stats.totalBytes} quota={STORAGE_QUOTA} />
          <RecentUploads videos={recent} />
        </div>
      )}

      <UploadModal open={uploadOpen} onClose={() => setUploadOpen(false)} onUploaded={handleUploaded} />
    </DashboardLayout>
  );
}
