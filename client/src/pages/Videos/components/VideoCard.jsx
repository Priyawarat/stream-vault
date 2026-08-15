import { Link } from "react-router-dom";
import { FiClock, FiHardDrive, FiPlay } from "react-icons/fi";
import StatusBadge from "../../../utils/StatusBadge";
import { formatBytes, formatDate, shortId } from "../../../utils/formatters";
import  {useVideoThumbnail} from "@/utils/useVideos.js";

export default function VideoCard({ video }) {
  const { thumbnail } = useVideoThumbnail(video.videoId);

  return (
    <article className="sv-card overflow-hidden transition hover:border-vault-brand/50">
      <div className="relative aspect-video bg-vault-surface">
        <img
          src={thumbnail !== null ? thumbnail : "/images/video-poster.svg"}
          alt={`Thumbnail for ${video.fileName}`}
          className="h-full w-full object-cover opacity-80"
          loading="lazy"
        />
        <Link
          to={`/watch/${video.videoId}`}
          className="absolute inset-0 grid place-items-center bg-vault-bg/35 opacity-0 transition hover:opacity-100"
          aria-label={`Play ${video.fileName}`}
        >
          <span className="grid h-12 w-12 place-items-center rounded-full bg-vault-brand text-vault-bg">
            <FiPlay size={20} />
          </span>
        </Link>
        <span className="absolute left-3 top-3">
          <StatusBadge status={video.status} />
        </span>
      </div>

      <div className="space-y-3 p-4">
        <div>
          <h3 className="truncate text-sm font-semibold">{video.fileName}</h3>
          <p className="text-xs text-vault-muted">ID {shortId(video.videoId)}</p>
        </div>
        <dl className="flex flex-wrap gap-x-4 gap-y-1.5 text-xs text-vault-muted">
          <div className="flex items-center gap-1.5">
            <FiHardDrive size={13} />
            <dd>{formatBytes(video.fileSize)}</dd>
          </div>
          <div className="flex items-center gap-1.5">
            <FiClock size={13} />
            <dd>{formatDate(video.createdAt)}</dd>
          </div>
        </dl>
        <Link to={`/watch/${video.videoId}`} className="sv-btn-ghost w-full">
          <FiPlay size={15} />
          Watch
        </Link>
      </div>
    </article>
  );
}
