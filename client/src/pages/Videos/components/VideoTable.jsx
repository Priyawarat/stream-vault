import { Link } from "react-router-dom";
import { FiPlay } from "react-icons/fi";
import StatusBadge from "../../../utils/StatusBadge";
import { formatBytes, formatDate, shortId } from "../../../utils/formatters";

export default function VideoTable({ videos }) {
  return (
    <div className="sv-card overflow-x-auto">
      <table className="w-full min-w-[640px] text-sm">
        <thead>
          <tr className="border-b border-vault-border text-left text-xs uppercase tracking-wider text-vault-muted">
            <th className="px-5 py-3.5 font-semibold">File</th>
            <th className="px-5 py-3.5 font-semibold">Status</th>
            <th className="px-5 py-3.5 font-semibold">Size</th>
            <th className="px-5 py-3.5 font-semibold">Created</th>
            <th className="px-5 py-3.5 text-right font-semibold">Action</th>
          </tr>
        </thead>
        <tbody>
          {videos.map((video) => (
            <tr key={video.videoId} className="border-b border-vault-border/60 last:border-0">
              <td className="px-5 py-3.5">
                <p className="font-medium">{video.fileName}</p>
                <p className="text-xs text-vault-muted">{shortId(video.videoId)}</p>
              </td>
              <td className="px-5 py-3.5">
                <StatusBadge status={video.status} />
              </td>
              <td className="px-5 py-3.5 text-vault-muted">{formatBytes(video.fileSize)}</td>
              <td className="px-5 py-3.5 text-vault-muted">{formatDate(video.createdAt)}</td>
              <td className="px-5 py-3.5 text-right">
                <Link to={`/watch/${video.videoId}`} className="sv-btn-ghost">
                  <FiPlay size={14} />
                  Watch
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
