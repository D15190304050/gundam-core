# 162235007 Ni Yingjie - Analysis Report: Research and Implementation of Metadata Management in a Distributed Storage System

## Document Overview
- **Title**: Research and Implementation of Metadata Management Technology in a Distributed Storage System
- **Author**: Ni Yingjie (School of Computer Science and Technology, Nanjing Normal University)
- **Type**: Master of Engineering Thesis (2018)
- **Research Goal**: Address metadata-management performance bottlenecks in PB-scale data storage systems
- **Core Contribution**: Design and implementation of the ZettastorDBS distributed storage system with an improved metadata-management approach

## Structure Summary
1. **Introduction**
   - Research background: limitations of traditional storage systems in the big-data era
   - Related work: comparison of NAS/SAN architecture and analysis of systems such as GFS and HDFS
   - Scope: metadata model, architecture design, implementation, and testing

2. **Metadata Management Technology Overview**
   - Metadata concepts and responsibilities in distributed storage
   - Challenges in scalability, consistency, and availability

3. **System Design and Implementation**
   - Overall architecture of ZettastorDBS
   - Metadata service design and key data structures
   - Read/write workflow and fault tolerance strategy

4. **Testing and Evaluation**
   - Functional validation and performance benchmarks
   - Comparative analysis against baseline approaches

## Key Takeaways
- Separating metadata services from data services improves scalability.
- Metadata indexing and partitioning are critical to throughput under high concurrency.
- Reliability and failover mechanisms strongly affect production readiness.

## References
The original thesis includes more than 40 references, including:
- classic distributed storage papers (such as GFS and HDFS)
- storage architecture comparisons (such as NAS and SAN)
- recent research on metadata management
- industry use cases in healthcare, astronomy, and seismic analysis
