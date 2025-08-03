# GitHub Actions Workflows

This repository includes two GitHub Actions workflows for automated builds:

## Gradle Build (`gradle-build.yml`)

Builds the multi-platform Minecraft mod using Gradle and uploads build artifacts.

**Triggers:**
- Push to `master` branch
- Pull requests to `master` branch

**What it does:**
1. Sets up JDK 21 (required for the mod)
2. Caches Gradle dependencies for faster builds
3. Runs `./gradlew build` to build all subprojects
4. Uploads build artifacts from each subproject's `build/libs/` directory:
   - `common-build-artifacts` - Common module artifacts
   - `fabric-build-artifacts` - Fabric mod artifacts
   - `neoforge-build-artifacts` - NeoForge mod artifacts

## Web Build (`web-build.yml`)

Builds the React web application using pnpm and uploads the built static files.

**Triggers:**
- Push to `master` branch
- Pull requests to `master` branch

**What it does:**
1. Sets up Node.js 20
2. Installs and configures pnpm
3. Caches pnpm dependencies for faster builds
4. Runs `pnpm install --frozen-lockfile` to install dependencies
5. Runs `pnpm build` to build the web application
6. Uploads build artifacts from `web/dist/` directory as `web-build-artifacts`

## Artifacts

All uploaded artifacts are retained for 30 days and can be downloaded from the GitHub Actions runs page.