# Legal Procedure Viewer

React frontend for the legal procedure API in the Spring Boot application.

## Run

Start the Spring Boot server on `http://localhost:8080`, then run:

```powershell
npm install
npm run dev
```

Open the Vite URL shown in the terminal. The API base URL can be changed in the page when the backend uses another port.

## API mapping

| React workflow | Spring API |
| --- | --- |
| Procedure list and domain filter | `GET /api/legal/procedures` |
| Procedure detail | `GET /api/legal/procedures/{code}` |
| Criminal supplementary checklist | `GET /api/legal/criminal/checklist/supplementary-investigation` |

The API functions are separated under `src/api`, while list, detail, checklist, and status states use shared React components and the same design tokens in `src/styles.css`.

The UI includes loading, error, empty, search, filter, reload, detail, and checklist states so the Spring Boot to React integration can be tested without relying on a successful request only.
