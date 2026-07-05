# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

React Native Expo monorepo (NX) containing mobile loyalty apps for Ecobonus/Sparklo/SIRC brands. Users earn bonuses by depositing recyclables into reverse vending machines and redeem rewards in-app.

**Tech Stack:** React Native 0.79.6, Expo SDK 53, React 19, TypeScript 5.8, NX 21.4, Node 22.16.0

## Common Commands

```bash
# Install dependencies (also runs postinstall: patch-package, builds test-ids, bootstraps libs)
yarn

# Start development server (Ecobonus app)
yarn start

# Run all tests
yarn test

# Run tests for specific project
yarn nx test ecobonus
yarn nx test ui-kit

# Run single test file
yarn nx test ecobonus --testFile=path/to/file.spec.ts

# Lint all projects
yarn check:all

# Lint affected files (compared to development branch)
yarn check:affected

# Type check specific project
yarn nx lint:ts ecobonus

# Start Storybook
yarn storybook:start

# GraphQL codegen (runs automatically on install, manual if needed)
yarn nx run graphql-service:generate
```

## Architecture

### Monorepo Structure

- **apps/** - Application entry points
  - `ecobonus/` - Main mobile app (default)
  - `content-preview/` - Content preview app
  - `storybook/` - Component documentation
  - `ecobonus-e2e/` - E2E tests (Detox)
- **libs/** - Shared libraries with strict module boundaries
- **services/sanity/** - Sanity CMS service
- **tools/executors/** - Custom NX executors

### Key Libraries (Path Aliases)

| Alias | Purpose |
|-------|---------|
| `@ui-kit` | Design system, themed components, tokens |
| `@graphql-service` | Apollo Client, generated types, queries/mutations |
| `@state-managment` |  a custom state management layer built on top of Apollo Client's reactive variables |
| `@i18n` | Internationalization (i18next) |
| `@feature-service` | Feature flags (Unleash) |
| `@config-service` | App configuration |
| `@test-ids` | Test ID constants |
| `@analytics` | A custom analytics SDK for tracking user events in the React Native app |
| `@scanner` | QR/barcode scanning |

### App Structure (Feature Sliced Design)

The app follows **Feature Sliced Design** architectural methodology. FSD organizes code by business domain with strict layer hierarchy.

#### Layer Hierarchy (top to bottom)

| Layer | Purpose | Can Import From |
|-------|---------|-----------------|
| `app/` | App initialization, routing, providers | All layers below |
| `screens/` | Page-level components, compose features | features, entities, shared |
| `features/` | Business features with user interactions | entities, shared |
| `entities/` | Business entities, domain models | shared only |
| `shared/` | Reusable utilities, UI kit, configs | Nothing (base layer) |

#### Key Principles

- **Layers can only import from layers below** - never upward (e.g., `entities` cannot import from `features`)
- **No cross-imports within same layer** - features cannot import from other features directly
- **Public API via index files** - each slice exposes only what's needed through `index.ts`
- **Slices are isolated** - each feature/entity is self-contained with its own UI, model, API

#### Slice Structure

Each slice (feature/entity) typically contains:
```
feature-name/
  ui/        - React components
  model/     - State, hooks, business logic
```

## Code Conventions

### Imports
- **Always use path aliases** (`@ui-kit`, `@ecobonus/*`, etc.) - never relative imports like `../../../`
- Import from library index, not internal paths

### Styling
- Use `createThemedStyles` + `useThemedStyles` for styles (not raw `StyleSheet.create`)
- Use semantic tokens from theme: `theme.colors.textPrimary`, `theme.spacings.md`
- Use `Surface` component for layout with spacing props (`ph`, `pv`, `gap`, etc.)
- **Prefer `@ui-kit` component props over `style`** — if a component from `@ui-kit` exposes a prop for a visual/layout property (e.g., `Surface` props like `flexDirection`, `gap`, `position`, `bg`, `br`; `Text` props like `color`, `type`; etc.), use the prop directly instead of creating a themed style. Resort to `style` / `createThemedStyles` only for properties that the component does not expose as props.
- WebP for raster images, SVG for vector (imported as React components)

### Internationalization (i18n)
- **Never use hardcoded user-facing strings** (labels, titles, placeholders, messages, button text, tab names, etc.) directly in components
- All display text must use the `t()` function from `@i18n`: `import { t } from '@i18n'`
- Generate translation keys based on the file location following the pattern: `screens.<screenName>.<section>.<key>` (e.g., `screens.rating.tabs.leagues`)
- Translations are managed via **locize** — just use the generated key with `t()`, no need to add values to local JSON files
- **Never modify locale JSON files** (`libs/i18n/src/__locales__/**/*.json`). Translations are added manually by the developer. Only use i18n keys via the `t()` function in code.

### TypeScript
- Strict mode enabled
- Avoid `any` - use `unknown` if type is truly unknown

### Components
- Functional components with hooks only
- Use `expo-image` over `Image`
- Use `@gorhom/bottom-sheet` for bottom sheets
- Use `@shopify/flash-list` for performant lists
- **No redundant default props**: Do not pass props to `@ui-kit` components when the value matches the component's built-in default. Known defaults: `Text` — `type="bodyRegularS"`, `color="textPrimary"`, `align="left"`; `Icon` — `color="iconAction"`, `size="m"`; `Button` — `type="primary"`, `size="l"`, `iconSize="s"`. Omit these props unless you need a non-default value.

**Bad:**
```tsx
<Text type="bodyRegularS" color="textPrimary">...</Text>
<Icon name="Copy" color="iconAction" />
<Button type="primary" size="l" />
```

**Good:**
```tsx
<Text>...</Text>
<Icon name="Copy" />
<Button />
```

### GraphQL
- Queries/mutations in `@graphql-service`
- Types auto-generated via `@graphql-codegen`


### Multi-Brand Support
- Controlled via `APP_CONTEXT` env variable
- Three themes: default (Ecoplatform), sparklo, sirc
- Brand-specific logic in `@app-switcher`

## Commit Convention

Uses Conventional Commits (`@commitlint/config-conventional`). Format:
```
type(scope): description
```
Types: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `style`, `perf`

## Important Notes

- Patches applied via `patch-package` on install (see `patches/` directory)
- Metro config includes SVG transformer (`react-native-svg-transformer`)
- Translation keys required - no hardcoded user-facing strings
- Error tracking via `@sentry/react-native`

## Skills

### figma-to-rn-screen
Use the `/figma-to-rn-screen` skill when the user says any of the following (or similar):
- "отверстай экран"
- "отверстай компонент"
- "figma to rn"

### explain-term
Use the `/explain-term` skill when the user says any of the following (or similar):
- "объясни термин"
- "что такое"
- "explain term"

### explain-junior
Use the `/explain-junior` skill when the user says any of the following (or similar):
- "объясни код"
- "объясни для новичка"
- "explain for junior"

### debug
Use the `/debug` skill when the user says any of the following (or similar):
- "задебагай"
- "помоги с ошибкой"
- "debug this"
- "разбери стектрейс"

### refactoring
Use the `/refactoring` skill when the user says any of the following (or similar):
- "отрефактори"
- "refactor this"
- "улучши код"

### png-to-webp
Use the `/png-to-webp` skill when the user says any of the following (or similar):
- "конвертируй png"
- "convert png to webp"
- "добавил изображения"

### analytics
Use the `/analytics` skill when the user says any of the following (or similar):
- "добавь аналитику"
- "добавь событие аналитики"
- "add analytics event"
- "track event"
