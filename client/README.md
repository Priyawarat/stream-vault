# StreamVault — Video Streaming Dashboard (React + Tailwind CSS)

Production-ready frontend for the StreamVault video API: register/login with JWT,
upload MP4 files, browse your library and stream videos (range or full).

## Quick start

```bash
npm install
cp .env.example .env      # set VITE_API_BASE_URL
npm run dev               # http://localhost:5173
npm run build             # production bundle in dist/
npm run preview
```

## Environment

| Variable | Default | Purpose |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `http://localhost:8080/v1` | Base URL of the StreamVault API |

## API mapping

| Feature | Endpoint |
| --- | --- |
| Register | `POST /users/register` |
| Login | `POST /users/login` (returns `accessToken`, sets `refreshToken` cookie) |
| Silent refresh | `POST /users/refresh-token` (auto-retried on any 401) |
| Upload | `POST /videos/upload` (multipart, `video/mp4` only) |
| List videos | `GET /videos` |
| Range stream | `GET /videos/:videoId/stream` |
| Full stream | `GET /videos/:videoId/stream-full` |

The access token is stored in `localStorage` and attached as `Authorization: Bearer <token>`.
The refresh token stays in the httpOnly cookie; every request uses `withCredentials: true`.
Because the stream endpoints are JWT protected, video bytes are fetched with the bearer
token and handed to `<video>` as an object URL.

If the API is unreachable, `POST /videos/upload` falls back to the documented mock
response so the UI stays demoable offline.

## Project structure

```
public/
  images/            static images (logo, poster, auth cover)
src/
  pages/
    Login/           index.jsx + components/ (AuthShell, LoginForm)
    Register/        index.jsx + components/ (RegisterForm)
    Dashboard/       index.jsx + components/ (StatCard, StorageBar, RecentUploads)
    Videos/          index.jsx + components/ (VideoCard, VideoTable, UploadModal)
    Watch/           index.jsx + components/ (VideoPlayer, StreamModeToggle)
    NotFound/        index.jsx
  utils/             shared/common code: apiClient, authService, videoService,
                     AuthContext, ProtectedRoute, DashboardLayout, Sidebar, Topbar,
                     Button, Input, Modal, Alert, Spinner, StatusBadge, EmptyState,
                     Logo, useVideos, formatters, storage, constants
  App.jsx            routes
  main.jsx           entry
```

## Routes

`/login`, `/register` (public) · `/dashboard`, `/videos`, `/watch/:videoId` (protected) · `*` → 404

Icons come from `react-icons` (Feather set).
