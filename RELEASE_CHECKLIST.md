# Release checklist

- [ ] Build from a clean checkout with JDK 21.
- [ ] Run every applicable item in `TESTING.md`.
- [ ] Update the version in `gradle.properties` and the expected JAR name in `build.ps1` if version changes.
- [ ] Update `CHANGELOG.md`.
- [ ] Verify `LICENSE`, `NOTICE.md`, and source attribution are present.
- [ ] Create a Git tag matching the version (for example `v1.0.5`).
- [ ] Attach the tested JAR to the GitHub Release.
- [ ] Attach or link the exact corresponding source for that release.
- [ ] Publish the same tested JAR on CurseForge and link the source repository.
- [ ] Mark as Beta until compatibility and gameplay testing are complete.

- [ ] Verify the v1.0.5 split draw order (panel → third-party layer above HOTBAR → item icons).
- [ ] Verify third-party layers between JUMP_METER and EFFECTS (including SPECTATOR_TOOLTIP and EXPERIENCE_LEVEL) inherit the vertical shift.
- [ ] Verify no matrix/pose-stack leak when GUI layer rendering is cancelled or disabled.
- [ ] State known limitations for direct mixins into vanilla Gui.renderHotbar.
