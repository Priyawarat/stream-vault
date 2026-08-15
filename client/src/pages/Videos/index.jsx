import { useMemo, useState } from "react";
import { FiFilm, FiGrid, FiList, FiRefreshCw, FiSearch, FiUploadCloud } from "react-icons/fi";
import Alert from "../../utils/Alert";
import Button from "../../utils/Button";
import DashboardLayout from "../../utils/DashboardLayout";
import EmptyState from "../../utils/EmptyState";
import Input from "../../utils/Input";
import Spinner from "../../utils/Spinner";
import useVideos from "../../utils/useVideos";
import UploadModal from "./components/UploadModal";
import VideoCard from "./components/VideoCard";
import VideoTable from "./components/VideoTable";

export default function VideosPage() {
  const { videos, loading, error, reload, setVideos } = useVideos();
  const [query, setQuery] = useState("");
  const [view, setView] = useState("grid");
  const [uploadOpen, setUploadOpen] = useState(false);

  const filtered = useMemo(() => {
    const term = query.trim().toLowerCase();
    if (!term) return videos;
    return videos.filter(
      (video) =>
        video.fileName?.toLowerCase().includes(term) || video.videoId?.toLowerCase().includes(term),
    );
  }, [videos, query]);

  const handleUploaded = (uploaded) => {
    if (!uploaded?.videoId) return;
    setVideos((prev) =>
      prev.some((video) => video.videoId === uploaded.videoId)
        ? prev
        : [{ ...uploaded, createdAt: new Date().toISOString() }, ...prev],
    );
  };

  return (
    <DashboardLayout
      title="Video library"
      subtitle={`${videos.length} video${videos.length === 1 ? "" : "s"} in your vault`}
      onUploadClick={() => setUploadOpen(true)}
    >
      <div className="mb-6 flex flex-wrap items-center gap-3">
        <Input
          className="min-w-[220px] flex-1"
          icon={FiSearch}
          placeholder="Search by file name or video ID"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <div className="flex items-center gap-1 rounded-xl border border-vault-border bg-vault-surface p-1">
          {[
            { key: "grid", Icon: FiGrid, label: "Grid view" },
            { key: "list", Icon: FiList, label: "List view" },
          ].map(({ key, Icon, label }) => (
            <button
              key={key}
              type="button"
              aria-label={label}
              onClick={() => setView(key)}
              className={`rounded-lg p-2 transition ${
                view === key ? "bg-vault-brand/15 text-vault-brand" : "text-vault-muted hover:text-vault-text"
              }`}
            >
              <Icon size={16} />
            </button>
          ))}
        </div>
        <Button variant="ghost" onClick={reload} icon={FiRefreshCw}>
          Refresh
        </Button>
        <Button onClick={() => setUploadOpen(true)} icon={FiUploadCloud}>
          Upload
        </Button>
      </div>

      {error && (
        <Alert tone="error" className="mb-5">
          {error}
        </Alert>
      )}

      {loading ? (
        <div className="grid place-items-center py-24 text-vault-brand">
          <Spinner size={28} />
        </div>
      ) : filtered.length === 0 ? (
        <EmptyState
          icon={FiFilm}
          title={videos.length === 0 ? "No videos yet" : "No matching videos"}
          description={
            videos.length === 0
              ? "Upload your first MP4 to start building your StreamVault library."
              : "Try a different file name or video ID."
          }
          action={
            videos.length === 0 ? (
              <Button className="mt-2" onClick={() => setUploadOpen(true)} icon={FiUploadCloud}>
                Upload video
              </Button>
            ) : null
          }
        />
      ) : view === "grid" ? (
        <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
          {filtered.map((video) => (
            <VideoCard key={video.videoId} video={video} />
          ))}
        </div>
      ) : (
        <VideoTable videos={filtered} />
      )}

      <UploadModal open={uploadOpen} onClose={() => setUploadOpen(false)} onUploaded={handleUploaded} />
    </DashboardLayout>
  );
}
