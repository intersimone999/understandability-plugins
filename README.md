# Understandability Plugins 

This repository contains two IntelliJ plugins:
- *Understandability Metrics* (understandability-plugin): allows to compute some metrics that are commonly related to understandability;
- *TIRESIAS* (starting-plugin): a tool for automatically selecting a set of classes from which it is worth starting analyzing a code repository.

## Popularity files
popularity.csv contains the number of occurrences of many classes in GitHub projects. Star imports are distributed proportionally among classes. E.g., java.util.* is distributed among java.util.List, java.util.Map, ...
pure_popularity.csv contains just the number of occurrences, star imports are ignored.



