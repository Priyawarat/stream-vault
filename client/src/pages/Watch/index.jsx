import { useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { FiArrowLeft, FiHardDrive, FiHash } from "react-icons/fi";
import DashboardLayout from "../../utils/DashboardLayout";
import StatusBadge from "../../utils/StatusBadge";
import { formatBytes, formatDate } from "../../utils/formatters";
import useVideos from "../../utils/useVideos";
import StreamModeToggle from "./components/StreamModeToggle";
import VideoPlayer from "./components/VideoPlayer";

export default function WatchPage() {
  const { videoId } = useParams();
  const { videos } = useVideos();
  const [mode, setMode] = useState("range");

  const video = useMemo(
    () => videos.find((item) => item.videoId === videoId),
    [videos, videoId],
  );

  return (
    <DashboardLayout title={video?.fileName || "Now playing"} subtitle={`Video ID ${videoId}`}>
      <Link to="/videos" className="sv-btn-ghost mb-5">
        <FiArrowLeft size={15} />
        Back to library
      </Link>

      <div className="grid gap-6 lg:grid-cols-[1.6fr_1fr]">
        <div className="space-y-4">
          <VideoPlayer videoId={videoId} mode={mode} />
          <StreamModeToggle mode={mode} onChange={setMode} />
        </div>

        <aside className="sv-card h-fit p-5">
          <h2 className="text-sm font-semibold">Video details</h2>
          <dl className="mt-4 space-y-3.5 text-sm">
            <div className="flex items-center justify-between gap-3">
              <dt className="text-vault-muted">Status</dt>
              <dd>
                <StatusBadge status={video?.status || "READY"} />
              </dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="flex items-center gap-1.5 text-vault-muted">
                <FiHardDrive size={14} /> Size
              </dt>
              <dd>{video ? formatBytes(video.fileSize) : "—"}</dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-vault-muted">Type</dt>
              <dd>{video?.contentType || "video/mp4"}</dd>
            </div>
            <div className="flex items-center justify-between gap-3">
              <dt className="text-vault-muted">Uploaded</dt>
              <dd>{formatDate(video?.createdAt)}</dd>
            </div>
            <div>
              <dt className="mb-1.5 flex items-center gap-1.5 text-vault-muted">
                <FiHash size={14} /> Video ID
              </dt>
              <dd className="break-all rounded-lg bg-vault-surface px-3 py-2 font-mono text-xs">{videoId}</dd>
            </div>
          </dl>
        </aside>
      </div>
    </DashboardLayout>
  );
}
