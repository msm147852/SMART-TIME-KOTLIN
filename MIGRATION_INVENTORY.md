# SMART TIME React → Kotlin Native Migration Inventory

## Goal
Rebuild the Android client as a native Kotlin + Jetpack Compose app while preserving the approved Smart Time experience (Arabic RTL, navy/black surfaces, gold accents, “وقتك من ذهب”), user-visible behavior, module structure, and existing backend contracts. The original React repository remains unchanged.

## Important boundary
The source repository contains 398 tracked files, including a React client, Express/Node backend, AI providers, model-training assets, datasets, CI checks, and documentation. These are not all Android source files:
- Client UI, navigation, state, local persistence, and Android capabilities must be reimplemented natively in Kotlin.
- The existing server, database, AI runtime, provider integrations, and training pipelines should remain server-side unless a separate backend migration is explicitly needed. Kotlin Android must call their documented HTTP/WebSocket contracts rather than embedding server secrets or training infrastructure in the app.
- Existing assets and specifications should be reused where licensing and technical format permit.

## Source client inventory → native destination

| React source area | Kotlin destination / migration target |
|---|---|
| `src/App.tsx`, `src/main.tsx` | `MainActivity.kt`, app navigation and root state |
| `src/components/DashboardView.tsx`, `Organic3DDashboard.tsx`, `LiveHeaderWidgets.tsx`, `DhakirniReminderBar.tsx` | Dashboard feature: approved home layout, clock/date, weather/market widgets, reminders |
| `AuthView.tsx`, `ChatPhoneGate.tsx`, auth service | Native sign-in, guest flow, session handling |
| `ExpensesView.tsx`, `WalletView.tsx`, `AdminWalletPanel.tsx`, `components/expenses/*` | Finance feature: expenses, income, cars, education, home/work/personal costs, reports |
| `NotesAndAccountingView.tsx` | Notes and daily-work feature; editing, save/export and reminder linkage |
| `CalendarView.tsx` | Calendar and reminder feature |
| `TripsView.tsx`, `components/trips/*`, `VehiclesView.tsx` | Trips, maps/location selection, vehicles and ride estimates |
| `EducationView.tsx`, `ReligiousView.tsx`, `SportsView.tsx` | Education, religious, and sports features |
| `HotChatView.tsx`, `ChatMembersSidebar.tsx`, chat modals/services | Private/group chat, members, attachments, voice and history |
| `MediaCenterView.tsx`, camera modals, media repository | Media library, camera capture, Android media permissions |
| `SecureVaultView.tsx` | Secure vault with Android-appropriate secure storage |
| `AiCenterView.tsx`, `SmartAiVoiceConversationModal.tsx`, voice DNA components | AI/chat/voice client screens and API adapters |
| `SettingsAndBackupModal.tsx`, customizer modals | Settings, themes, dashboard customization, backup/restore |
| `src/services/*`, `src/repositories/*`, `src/context/*` | Kotlin repositories, Retrofit/OkHttp data sources, DataStore/Room, ViewModels |
| `src/types.ts`, `src/types/finance.ts`, `src/contracts/*` | Kotlin domain models and API DTOs |
| `src/index.css`, `BrandLogo.tsx`, `public/*`, `src/assets/images/*` | Compose theme, typography, logo and image resources |
| `server.ts`, `backend/*`, `app/api/*` | Keep server-side; document and verify API/WebSocket contracts consumed by Android |
| `infra/*`, `backend/ai/training/*`, `artifacts/*`, `scripts/*` | Keep server/operations-side; not copied into Android app package |

## Migration completion criteria
- Every user-facing React section is represented by a native screen and reachable through navigation.
- The approved dashboard layout and RTL behavior are preserved; a visible Home destination returns to the dashboard from every section.
- Guest entry, authentication, data loading, error states, persistence, notifications, permissions, maps, media, voice, and exports are implemented where supported by the original feature and Android APIs.
- API paths, request/response shapes, WebSocket events, and session behavior are checked against the existing server before wiring each client repository.
- No API keys or server secrets are shipped in the APK.
- The complete app assembles in CI; then run emulator/device smoke tests and resolve compile/runtime errors.
- Any feature that cannot be carried over literally is recorded with its native equivalent and tested rather than silently omitted.

## Current status
The repository currently has the Gradle/Compose scaffold and a placeholder shell only. It is not yet a full migration, and the first CI run failed during Android SDK setup before compilation. A corrected workflow has been committed and must finish before the build result can be assessed.
