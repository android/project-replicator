Capture:
- java / kotlin language level
- Build Type and associated dependencies, and maybe some properties (debuggable flag?)
- Product Flavor and associated dependencies, and maybe some properties (minSdkVersion? other values?)
- dynamic-feature list from app plugin
- lint checks dependencies
- variant filtering results (to skip disabled variants)
- java platform plugin
- target from test plugin
- missingDimensionStrategy/fallbacks
- kotlin source folder detection for kotlin file count
- Resource count
- composite builds?

Flags:
- ways to disable class count and resource count (--no-class-count on task)

Generation
- Fetch resource artifact from AGP to reference resources from other modules
- Two-pass resource generation so menu resources can be generated with code references
- Implement random raw resource generation. Also implement random java resource generation and asset generation.