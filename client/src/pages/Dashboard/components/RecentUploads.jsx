import { Link } from "react-router-dom";
import { FiArrowRight, FiFilm } from "react-icons/fi";
import StatusBadge from "../../../utils/StatusBadge";
import { formatBytes, formatDate } from "../../../utils/formatters";

export default function RecentUploads({ videos }) {
  return (
    <section className="sv-card p-5">
      <div className="flex items-center justify-between">
        <h2 className="text-sm font-semibold">Recent uploads</h2>
        <Link to="/videos" className="flex items-center gap-1.5 text-xs font-semibold text-vault-brand hover:underline">
          View all <FiArrowRight size={13} />
        </Link>
      </div>

      {videos.length === 0 ? (
        <p className="py-8 text-center text-sm text-vault-muted">No uploads yet.</p>
      ) : (
        <ul className="mt-4 divide-y divide-vault-border/70">
          {videos.map((video) => (
            <li key={video.videoId} className="flex items-center gap-3 py-3">
              <span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-vault-surface text-vault-brand2">
                <FiFilm size={16} />
              </span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium">{video.fileName}</p>
                <p className="text-xs text-vault-muted">
                  {formatBytes(video.fileSize)} · {formatDate(video.createdAt)}
                </p>
              </div>
              <StatusBadge status={video.status} />
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
