import { useRef, useState } from "react";
import { FiFilm, FiUploadCloud } from "react-icons/fi";
import Alert from "../../../utils/Alert";
import Button from "../../../utils/Button";
import Modal from "../../../utils/Modal";
import { ACCEPTED_VIDEO_TYPE, MAX_UPLOAD_BYTES } from "../../../utils/constants";
import { extractErrorMessage } from "../../../utils/apiClient";
import { formatBytes } from "../../../utils/formatters";
import { uploadVideo } from "../../../utils/videoService";

export default function UploadModal({ open, onClose, onUploaded }) {
  const inputRef = useRef(null);
  const [file, setFile] = useState(null);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [busy, setBusy] = useState(false);

  const reset = () => {
    setFile(null);
    setProgress(0);
    setError("");
    setSuccess("");
    setBusy(false);
  };

  const pickFile = (selected) => {
    setError("");
    setSuccess("");
    if (!selected) return;
    if (selected.type !== ACCEPTED_VIDEO_TYPE) {
      setError("Only MP4 files (video/mp4) are accepted.");
      return;
    }
    if (selected.size > MAX_UPLOAD_BYTES) {
      setError(`File is larger than the ${formatBytes(MAX_UPLOAD_BYTES)} limit.`);
      return;
    }
    setFile(selected);
  };

  const handleUpload = async () => {
    if (!file) return;
    setBusy(true);
    setError("");
    setProgress(0);
    try {
      const response = await uploadVideo(file, setProgress);
      setSuccess(`${response?.data?.fileName || file.name} uploaded (${response?.data?.status || "UPLOADED"}).`);
      onUploaded?.(response?.data);
      setFile(null);
    } catch (uploadError) {
      setError(extractErrorMessage(uploadError, "Upload failed. Please try again."));
    } finally {
      setBusy(false);
    }
  };

  return (
    <Modal
      open={open}
      title="Upload a video"
      onClose={() => {
        reset();
        onClose?.();
      }}
      footer={
        <>
          <Button
            variant="ghost"
            onClick={() => {
              reset();
              onClose?.();
            }}
          >
            Close
          </Button>
          <Button onClick={handleUpload} loading={busy} disabled={!file} icon={FiUploadCloud}>
            Upload
          </Button>
        </>
      }
    >
      <div className="space-y-4">
        <Alert tone="error">{error}</Alert>
        <Alert tone="success">{success}</Alert>

        <button
          type="button"
          onClick={() => inputRef.current?.click()}
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => {
            event.preventDefault();
            pickFile(event.dataTransfer.files?.[0]);
          }}
          className="flex w-full flex-col items-center gap-2 rounded-2xl border-2 border-dashed border-vault-border bg-vault-card px-6 py-10 text-center transition hover:border-vault-brand/60"
        >
          <FiUploadCloud size={26} className="text-vault-brand" />
          <span className="text-sm font-semibold">Drop an MP4 here or click to browse</span>
          <span className="text-xs text-vault-muted">video/mp4 only · up to {formatBytes(MAX_UPLOAD_BYTES)}</span>
        </button>

        <input
          ref={inputRef}
          type="file"
          accept="video/mp4"
          className="hidden"
          onChange={(event) => pickFile(event.target.files?.[0])}
        />

        {file && (
          <div className="flex items-center gap-3 rounded-xl border border-vault-border bg-vault-card px-3.5 py-3">
            <FiFilm className="text-vault-brand2" size={18} />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-medium">{file.name}</p>
              <p className="text-xs text-vault-muted">{formatBytes(file.size)}</p>
            </div>
          </div>
        )}

        {(busy || progress > 0) && (
          <div>
            <div className="mb-1.5 flex justify-between text-xs text-vault-muted">
              <span>Uploading</span>
              <span>{progress}%</span>
            </div>
            <div className="h-2 overflow-hidden rounded-full bg-vault-border">
              <div className="h-full bg-vault-brand transition-all" style={{ width: `${progress}%` }} />
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}
