# Understandability Plugins 
This repository contains two IntelliJ plugins:
- *Understandability Metrics* (understandability-plugin): allows to compute some metrics possibly related to code understandability;
- *TIRESIAS* (starting-plugin): a tool for automatically selecting a set of classes from which it is worth starting analyzing a code repository [3].

Please, note that the understandability metrics exported by the first plugin are state-of-the-art metrics that are *not* actually related to understandability (see [1, 2]).

## Build
To build the plugins, you need the IntelliJ IDEA SDK.
- open the project with IntelliJ IDEA;
- build the JAR for the root module (*Build/Build Artifacts* and then `intellij-plugins`);
- build the plugin you need (*Build/Prepare All Plugin Modules For Deployment*)

## References
1) Scalabrino, S., Bavota, G., Vendome, C., Linares-Vásquez, M., Poshyvanyk, D., & Oliveto, R. (2017). Automatically assessing code understandability: How far are we?. Proceedings of the 32nd IEEE/ACM International Conference on Automated Software Engineering (ASE).
2) Scalabrino, S., Bavota, G., Vendome, C., Linares-Vásquez, M., Poshyvanyk, D., & Oliveto, R. (2019). Automatically assessing code understandability. IEEE Transactions on Software Engineering.
3) Scalabrino, Simone (2017). On software odysseys and how to prevent them. Proceedings of the IEEE/ACM 39th International Conference on Software Engineering Companion (ICSE-C).