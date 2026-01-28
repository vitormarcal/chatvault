# Docker image build and release workflow

This project uses a Makefile-based workflow to build and publish Docker images in a safe and explicit way.

The goal is to clearly separate dev/test images from stable release images, and to avoid accidentally publishing unstable versions as latest.

## Dev / test images

Dev and test images must always use an explicit version tag, such as:

```
1.2.3-beta.1
```

To build and publish a dev/test image:

```bash
make dev VERSION=1.2.3-beta.1
```

What this does:
- Builds `ghcr.io/vitormarcal/chatvault:1.2.3-beta.1`
- Pushes only that tag
- Does not update `latest`

Dev images are never published as `latest`.

---

## Release images

Stable releases must use a clean semantic version, such as:

```
1.2.3
```

To build and publish a release image:

```bash
make release VERSION=1.2.3
```

What this does:
- Validates the version string
- Builds `ghcr.io/vitormarcal/chatvault:1.2.3`
- Tags the same image as `latest`
- Pushes both `1.2.3` and `latest`

This is the only supported way to update the `latest` tag.

---

## Safety checks

The release process includes built-in safety checks and will fail if:

- `VERSION=latest`
- `VERSION=dev`
- The version contains pre-release identifiers:
  - `-alpha`
  - `-beta`
  - `-rc`
  - `-snapshot`

This prevents unstable versions from being accidentally released as `latest`.

---

## Help and usage

Running `make` without arguments shows a built-in help message describing all available commands:

```bash
make
```

If you are unsure which command to use:
- Testing or experimentation: use `make dev`
- Publishing a stable version: use `make release`
